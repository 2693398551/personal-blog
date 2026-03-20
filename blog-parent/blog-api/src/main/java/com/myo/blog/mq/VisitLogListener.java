package com.myo.blog.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myo.blog.config.RabbitConfig;
import com.myo.blog.dao.mapper.VisitLogMapper;
import com.myo.blog.dao.mapper.VisitorMapper;
import com.myo.blog.dao.pojo.VisitLog;
import com.myo.blog.dao.pojo.Visitor;
import com.myo.blog.entity.params.VisitParam;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 访客行为 MQ 消费者
 *
 * 消费流程：
 *  ① 反序列化 JSON 字符串为 VisitParam
 *  ② INSERT myo_visit_log（访问明细，源头真相）
 *  ③ UPSERT myo_visitor（访客主表，uuid 已存在则更新 pv/last_visit）
 *  ④ ACK 确认消息消费完成，MQ 删除该消息
 *  ⑤ 更新 Redis PV/UV 计数（失败只记日志，降级到 MySQL 兜底）
 *
 * 可靠性保障：
 *  - 写库失败 → basicNack(requeue=false) → 消息进死信队列，不无限重试
 *  - Redis 失败 → 降级，查询时从 MySQL 聚合回写，数据不丢失
 *  - 反序列化失败 → 直接 ACK 丢弃，格式错误重试也没意义
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VisitLogListener {

    private final VisitLogMapper      visitLogMapper;
    private final VisitorMapper       visitorMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper        objectMapper;

    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String            PV_PREFIX = "visit:pv:";        // Redis PV Key 前缀
    private static final String            UV_PREFIX = "visit:uv:";        // Redis UV Key 前缀
    private static final Duration          KEY_TTL   = Duration.ofDays(2); // Redis Key 过期时间

    /**
     * 监听访客行为队列，消费消息
     *
     * @param msgBody  消息体，JSON 字符串，对应 VisitParam 对象
     *                 由 VisitStatsServiceImpl.record() 序列化后发送
     * @param message  原始消息对象，用于获取 deliveryTag 等元数据
     * @param channel  RabbitMQ 信道，用于手动 ACK / NACK
     */
    @RabbitListener(queues = RabbitConfig.VISIT_LOG_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(String msgBody, Message message, Channel channel) throws Exception {
        // 获取消息唯一标识，ACK/NACK 时需要带上，告诉 MQ 操作的是哪条消息
        // deliveryTag 在同一个 Channel 内从 1 开始递增，服务重启后新建 Channel 重新从 1 计数
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        // 消息体为空，直接 ACK 丢弃，不做任何处理
        if (msgBody == null || msgBody.isBlank()) {
            channel.basicAck(deliveryTag, false);
            return;
        }

        // ---- 反序列化：JSON 字符串 → VisitParam ----
        VisitParam param;
        try {
            param = objectMapper.readValue(msgBody, VisitParam.class);
        } catch (Exception e) {
            // 格式错误的消息重试也没用，直接 ACK 丢弃
            log.error("[VisitLogListener] 反序列化失败，ACK 丢弃: err={}", e.getMessage());
            channel.basicAck(deliveryTag, false);
            return;
        }

        try {
            // ---- ① 写访问明细 visit_log ----
            // 每次页面访问一条记录，是整个统计系统的"源头真相"
            // Redis 宕机后可从此表重新聚合恢复
            visitLogMapper.insert(buildVisitLog(param));

            // ---- ② UPSERT 访客主表 visitor ----
            // uuid 已存在则更新 pv+1 / last_visit / user_id
            // uuid 不存在则插入新记录
            visitorMapper.upsert(buildVisitor(param));

            // ---- ACK：写库成功，通知 MQ 删除这条消息 ----
            // 第一个参数 deliveryTag：告诉 MQ 确认的是哪条消息
            // 第二个参数 false：只确认当前这一条，不批量确认
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("[VisitLogListener] 写库失败，消息进入死信: uuid={}, err={}",
                    param.getVisitorUuid(), e.getMessage(), e);
            // NACK：写库失败，拒绝消息
            // 第二个参数 false：只处理当前这一条
            // 第三个参数 false：不重新入队（requeue=false），进入死信队列，避免无限重试
            channel.basicNack(deliveryTag, false, false);
            return;
        }

        // ---- ③④ 更新 Redis PV / UV（失败降级，不影响消息状态）----
        // 注意：此时消息已经 ACK，Redis 失败不会影响已写库的数据
        // 查询接口 miss Redis 时会从 MySQL 聚合回写，数据不丢失
        String today = LocalDate.now().format(DATE_FMT);
        try {
            // PV：每次访问 +1，String 类型，INCR 原子操作
            String pvKey = PV_PREFIX + today;
            redisTemplate.opsForValue().increment(pvKey);
            redisTemplate.expire(pvKey, KEY_TTL);

            // UV：HyperLogLog 按 UUID 去重，相同 UUID 多次访问只计一次
            String uvKey = UV_PREFIX + today;
            redisTemplate.opsForHyperLogLog().add(uvKey, param.getVisitorUuid());
            redisTemplate.expire(uvKey, KEY_TTL);

        } catch (Exception e) {
            log.warn("[VisitLogListener] Redis 更新失败（降级到 MySQL 兜底）: {}", e.getMessage());
        }
    }

    /**
     * 构建访问明细记录
     * 对应 myo_visit_log 表，每次页面访问插入一条
     *
     * @param param 访客访问参数（从 MQ 消息反序列化而来）
     * @return VisitLog 实体
     */
    private VisitLog buildVisitLog(VisitParam param) {
        VisitLog v = new VisitLog();
        v.setVisitorUuid(param.getVisitorUuid());   // 访客唯一标识
        v.setUserId(param.getUserId());             // 登录用户 ID，未登录为 null
        v.setBehavior(param.getBehavior());         // 行为类型：PAGE_VIEW / VIEW_ARTICLE 等
        v.setContent(param.getContent());           // 页面描述：首页 / 文章详情页 等
        v.setUri(param.getUri());                   // 前端路由路径：/ / /view/123 等
        v.setMethod("GET");                         // 页面访问统一为 GET
        v.setIp(param.getIp());
        v.setIpLocation(param.getIpLocation());     // 原始归属地字符串：中国|广东|深圳|电信
        v.setProvince(param.getProvince());
        v.setCity(param.getCity());
        v.setOs(param.getOs());
        v.setBrowser(param.getBrowser());
        v.setReferer(param.getReferer());           // 来源页面 URL，直接访问为 null
        v.setCreateDate(param.getCreateDate());
        return v;
    }

    /**
     * 构建访客主表记录
     * 对应 myo_visitor 表，每个 UUID 一条记录，长期保留
     * 使用 UPSERT（ON DUPLICATE KEY UPDATE）：
     *   - uuid 已存在：更新 pv+1 / last_visit / user_id
     *   - uuid 不存在：插入新记录
     *
     * @param param 访客访问参数（从 MQ 消息反序列化而来）
     * @return Visitor 实体
     */
    private Visitor buildVisitor(VisitParam param) {
        long now = param.getCreateDate() != null ? param.getCreateDate() : System.currentTimeMillis();
        Visitor v = new Visitor();
        v.setUuid(param.getVisitorUuid());
        v.setUserId(param.getUserId());
        v.setIp(param.getIp());
        v.setIpLocation(param.getIpLocation());
        v.setProvince(param.getProvince());
        v.setCity(param.getCity());
        v.setOs(param.getOs());
        v.setBrowser(param.getBrowser());
        // firstVisit 仅首次插入时生效，ON DUPLICATE KEY UPDATE 里不会覆盖它
        // 所以这个字段永远记录的是该 UUID 第一次出现的时间
        v.setFirstVisit(now);
        v.setLastVisit(now);
        v.setCreateDate(now);
        return v;
    }
}