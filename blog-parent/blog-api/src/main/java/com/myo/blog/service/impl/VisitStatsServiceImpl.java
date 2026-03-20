package com.myo.blog.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myo.blog.config.RabbitConfig;
import com.myo.blog.dao.mapper.VisitLogMapper;
import com.myo.blog.dao.mapper.VisitorMapper;
import com.myo.blog.dao.mapper.VisitStatsMapper;
import com.myo.blog.dao.pojo.Visitor;
import com.myo.blog.dao.pojo.VisitLog;
import com.myo.blog.dao.pojo.VisitStats;
import com.myo.blog.entity.params.VisitParam;
import com.myo.blog.service.VisitStatsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitStatsServiceImpl implements VisitStatsService {

    private final VisitStatsMapper    visitStatsMapper;
    private final VisitLogMapper      visitLogMapper;
    private final VisitorMapper       visitorMapper;
    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate      rabbitTemplate;
    private final ObjectMapper        objectMapper;

    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String            PV_PREFIX = "visit:pv:";
    private static final String            UV_PREFIX = "visit:uv:";
    private static final Duration          KEY_TTL   = Duration.ofDays(2);

    // ================================================================
    //  前端路由埋点入口
    //  直接把 VisitParam 序列化成 JSON 发到 MQ
    //  不再需要 LoginService / UserAgentUtils / IpUtils
    //  这些工作已经在 Controller.buildParam() 里做完了
    // ================================================================

    @Override
    public void record(VisitParam param) {
        try {
            String msgJson = objectMapper.writeValueAsString(param);
            System.out.println("msgJson: " + msgJson);
            rabbitTemplate.convertAndSend(
                    RabbitConfig.VISIT_LOG_EXCHANGE,
                    RabbitConfig.VISIT_LOG_ROUTING_KEY,
                    msgJson);
        } catch (Exception e) {
            log.error("[VisitStatsService] record 失败: {}", e.getMessage(), e);
        }
    }

    // ================================================================
    //  今日 PV / UV
    // ================================================================

    @Override
    public long getTodayPv() {
        String today = LocalDate.now().format(DATE_FMT);
        String key   = PV_PREFIX + today;
        try {
            String val = redisTemplate.opsForValue().get(key);
            if (val != null) return Long.parseLong(val);
        } catch (Exception e) {
            log.warn("[VisitStatsService] Redis 读取 PV 失败，降级到 MySQL: {}", e.getMessage());
        }
        long pv = visitLogMapper.countPvByDate(today);
        trySetRedis(key, String.valueOf(pv), KEY_TTL);
        return pv;
    }

    @Override
    public long getTodayUv() {
        String today = LocalDate.now().format(DATE_FMT);
        String key   = UV_PREFIX + today;
        try {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                Long uv = redisTemplate.opsForHyperLogLog().size(key);
                return uv != null ? uv : 0L;
            }
        } catch (Exception e) {
            log.warn("[VisitStatsService] Redis 读取 UV 失败，降级到 MySQL: {}", e.getMessage());
        }
        long uv = visitLogMapper.countUvByDate(today);
        trySetRedis(UV_PREFIX + "fallback:" + today, String.valueOf(uv), KEY_TTL);
        return uv;
    }

    // ================================================================
    //  全站累计
    // ================================================================

    @Override
    public VisitStats getTotalStats() {
        return visitStatsMapper.sumTotal();
    }

    // ================================================================
    //  后台仪表盘
    // ================================================================

    @Override
    public List<VisitStats> getRecentDays(int days) {
        return visitStatsMapper.listRecentDays(days);
    }

    @Override
    public List<VisitStats> getByDateRange(String startDate, String endDate) {
        return visitStatsMapper.listByDateRange(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getUvByCity() {
        return visitorMapper.countUvByCity();
    }

    // ================================================================
    //  访客列表
    // ================================================================

    @Override
    public Page<Visitor> pageVisitors(int page, int pageSize, String keyword) {
        Page<Visitor> p = new Page<>(page, pageSize);
        return visitorMapper.pageVisitors(p, keyword);
    }

    @Override
    public Page<VisitLog> pageVisitLogs(int page, int pageSize, String visitorUuid) {
        Page<VisitLog> p = new Page<>(page, pageSize);
        return visitLogMapper.pageByVisitorUuid(p, visitorUuid);
    }

    // ================================================================
    //  定时任务：聚合落库 + 清 Redis
    // ================================================================

    @Override
    public void syncStatsByDate(String date) {
        long pv         = visitLogMapper.countPvByDate(date);
        long uv         = visitLogMapper.countUvByDate(date);
        long newVisitor = visitLogMapper.countNewVisitorByDate(date);

        VisitStats stats = new VisitStats();
        stats.setDate(date);
        stats.setPv(pv);
        stats.setUv(uv);
        stats.setNewVisitor(newVisitor);
        stats.setCreateDate(System.currentTimeMillis());

        visitStatsMapper.upsertStats(stats);
        log.info("[VisitStatsService] 日期 {} 落库完成: PV={}, UV={}, 新访客={}", date, pv, uv, newVisitor);

        clearRedisKey(PV_PREFIX + date);
        clearRedisKey(UV_PREFIX + date);
        clearRedisKey(UV_PREFIX + "fallback:" + date);
        log.info("[VisitStatsService] 日期 {} Redis Key 已清理", date);
    }

    // ================================================================
    //  私有工具
    // ================================================================

    private void trySetRedis(String key, String value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            log.warn("[VisitStatsService] Redis 回写失败: key={}, err={}", key, e.getMessage());
        }
    }

    private void clearRedisKey(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("[VisitStatsService] Redis Key 清理失败: key={}, err={}", key, e.getMessage());
        }
    }
}