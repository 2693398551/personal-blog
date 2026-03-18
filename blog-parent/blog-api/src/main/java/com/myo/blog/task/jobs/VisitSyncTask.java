package com.myo.blog.task.jobs;

import com.myo.blog.dao.mapper.DailyVisitMapper;
import com.myo.blog.dao.pojo.DailyVisit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 每日流量数据(PV/UV)同步任务
 *
 * 网站被访问时，PV/UV 实时写入 Redis，
 * 由本任务定时将 Redis 中的昨日流量批量结转到 MySQL，用于后台大屏展示。
 */
@Slf4j
@Component("visitSyncTask")
@RequiredArgsConstructor
public class VisitSyncTask {

    private final RedisTemplate<String, String> redisTemplate;
    private final DailyVisitMapper dailyVisitMapper;
    @Autowired
    @Lazy
    private  VisitSyncTask proxySelf;
    /**
     * 无参方法：供定时任务自动调度使用
     * 默认结算昨天的数据
     */
    public void run() {
        proxySelf.executeSync(null);
    }

    /**
     * 核心同步逻辑
     * 开启事务保证一致性：落库失败则回滚，绝不误删 Redis 缓存
     */
    @Transactional(rollbackFor = Exception.class)
    public void executeSync(String targetDateStr) {
        log.info("开始执行每日流量数据(PV/UV)结转任务...");

        LocalDate targetDate;
        if (StringUtils.hasText(targetDateStr)) {
            // 如果传了参数，就结算参数指定的日期
            targetDate = LocalDate.parse(targetDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } else {
            // 默认结算昨天的数据
            targetDate = LocalDate.now().minusDays(1);
        }

        String dateStr = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String pvKey = "site:pv:" + dateStr;
        String uvKey = "site:uv:" + dateStr;

        // 从 Redis 中读取数据
        String pvStr = redisTemplate.opsForValue().get(pvKey);
        Integer pv = pvStr != null ? Integer.parseInt(pvStr) : 0;

        Long uvLong = redisTemplate.opsForHyperLogLog().size(uvKey);
        Integer uv = uvLong != null ? uvLong.intValue() : 0;

        // 如果 PV 和 UV 都是 0，直接跳过不存库，减少垃圾数据
        if (pv == 0 && uv == 0) {
            log.info("日期: {} 的 Redis 中无流量数据，跳过同步。", dateStr);
            return;
        }

        try {
            // 存入 MySQL
            DailyVisit dailyVisit = new DailyVisit();
            dailyVisit.setDate(Date.from(targetDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            dailyVisit.setPv(pv);
            dailyVisit.setUv(uv);
            dailyVisit.setCreateTime(new Date());

            dailyVisitMapper.insert(dailyVisit);
            log.info("日期: {} 的流量结转成功，PV: {}, UV: {}", dateStr, pv, uv);

            // 存库成功后清理 Redis 缓存
            redisTemplate.delete(pvKey);
            redisTemplate.delete(uvKey);
            log.info("日期: {} 的 Redis 流量缓存已清理完毕", dateStr);

        } catch (Exception e) {
            log.error("[每日流量同步任务] 结转失败，发生异常: {}", e.getMessage(), e);
            throw new RuntimeException("流量同步异常，已触发事务回滚！", e);
        }
    }

}