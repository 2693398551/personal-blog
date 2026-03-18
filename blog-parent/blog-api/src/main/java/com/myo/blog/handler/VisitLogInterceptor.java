package com.myo.blog.handler;

import com.alibaba.fastjson.JSON;
import com.myo.blog.config.RabbitConfig;
import com.myo.blog.dao.pojo.VisitLog;
import com.myo.blog.utils.IpUtils;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

@Component
public class VisitLogInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. 获取今天的日期格式字符串（例如：2026-03-25），用于拼接Redis键名
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String pvKey = "site:pv:" + today;
        String uvKey = "site:uv:" + today;

        // 2. 利用项目中现有的工具类获取访客真实的IP地址
        String ip = IpUtils.getIpAddr(request);

        // 如果是本地开发测试 IP 是直接放行，不记录流量
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return true;
        }

        // 3. Redis 实时统计区（极速模式）
        try {
            // 3.1 增加当天的全站 PV (页面浏览量)，简单的字符串自增
            redisTemplate.opsForValue().increment(pvKey);

            // 3.2 增加当天的全站 UV (独立访客数)，使用极致压缩的 HyperLogLog 算法自动去重
            redisTemplate.opsForHyperLogLog().add(uvKey, ip);
        } catch (Exception e) {
            // 捕获Redis可能出现的异常，防止缓存宕机导致主业务瘫痪
            e.printStackTrace();
        }

        // 4. 提取访客上下文详细信息
        UserAgent userAgent = IpUtils.getUserAgent(request);
        String os = userAgent.getOperatingSystem().getName();
        String browser = userAgent.getBrowser().getName();
        // 利用 ip2region 解析出实际城市地理位置
        String ipSource = IpUtils.getCityInfo(ip);

        // 5. 组装流量明细日志实体对象
        VisitLog visitLog = new VisitLog();
        // 生成临时的追踪UUID
        visitLog.setUuid(UUID.randomUUID().toString());
        visitLog.setIp(ip);
        visitLog.setIpSource(ipSource);
        visitLog.setOs(os);
        visitLog.setBrowser(browser);
        visitLog.setPageUrl(request.getRequestURI());
        // 可以根据具体的前端路径来区分模块，这里暂定为统一标识
        visitLog.setModule("前台请求");
        visitLog.setCreateTime(new Date());

        // 6. 将明细日志对象转化为 JSON 字符串，异步投递到 RabbitMQ
        // 这一步瞬间完成，保护了数据库，也让用户的网页加载不受任何延迟影响
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.VISIT_LOG_EXCHANGE,
                    RabbitConfig.VISIT_LOG_ROUTING_KEY,
                    JSON.toJSONString(visitLog)
            );
        } catch (Exception e) {
            // 同样增加容错处理
            e.printStackTrace();
        }

        // 7. 无论日志记录成功与否，全部放行，确保用户正常浏览网站
        return true;
    }
}