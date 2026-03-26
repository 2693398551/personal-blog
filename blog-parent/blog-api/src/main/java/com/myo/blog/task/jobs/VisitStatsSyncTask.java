package com.myo.blog.task.jobs;

import com.myo.blog.service.VisitStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 访客统计每日汇总任务
 *
 * 将 myo_visit_log 访问明细表中的数据按天聚合（PV / UV / 新访客数），
 * 写入（或更新）myo_visit_stats 汇总表，同时清理对应的 Redis 临时计数键。
 *
 * 触发策略（建议在数据库 myo_sys_task 中这样配置）：
 *   cron：0 5 0 * * ?  每天凌晨 00:05 执行，避开跨日边界的并发写入
 *   beanName：visitStatsSyncTask
 *   methodName：run
 *   taskParam（可选）：{"date": "2026-03-25"}  指定回补某天；不传则默认汇总【昨天】
 *
 * 为什么要汇总昨天而不是今天？
 *   凌晨 00:05 执行时，今天的日志刚开始积累，昨天的数据才是完整的。
 *   如果要补今天，等任务结束后再跑一遍（或者手动触发带参数的方式）。
 *
 * 数据安全：
 *   syncStatsByDate 内部使用 INSERT ... ON DUPLICATE KEY UPDATE（uk_date 唯一索引），
 *   可以安全重跑，不会产生重复数据。
 */
@Slf4j
@Component("visitStatsSyncTask")
@RequiredArgsConstructor
public class VisitStatsSyncTask {

    // 引用访客统计 Service，负责聚合 visit_log 数据并写入 visit_stats 表
    private final VisitStatsService visitStatsService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ========================================================================
    //  无参入口：每天凌晨自动汇总【昨天】
    // ========================================================================

    /**
     * 无参方法：供定时任务自动调度使用
     * 汇总昨天一整天的访问数据（凌晨运行时昨天数据已完整）
     */
    public void run() {
        String yesterday = LocalDate.now().minusDays(1).format(DATE_FMT);
        log.info("[访客汇总任务] 自动触发，汇总日期：{}", yesterday);
        executeSync(yesterday);
    }

    // ========================================================================
    //  有参入口：手动补跑 / 指定日期
    // ========================================================================

    /**
     * 有参方法：在数据库配置 taskParam 或手动执行时调用
     *
     * 支持两种参数格式：
     *   1. 纯日期字符串：{"date": "2026-03-20"}  → 汇总指定日期
     *   2. 天数偏移：{"daysAgo": 2}              → 汇总 N 天前（如 daysAgo=0 = 今天）
     *
     * 传入非法参数会抛出 IllegalArgumentException，
     * SchedulingRunnable 识别后阻断重试并静默处理，不发告警邮件。
     *
     * @param param JSON 格式的参数字符串
     */
    public void run(String param) {
        log.info("[访客汇总任务] 接收到动态参数：{}", param);

        // 参数格式校验
        if (StringUtils.hasText(param)) {
            String trimmed = param.trim();
            if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
                throw new IllegalArgumentException("参数必须是 JSON 格式，示例：{\"date\": \"2026-03-20\"}");
            }
        }

        String targetDate = resolveTargetDate(param);
        log.info("[访客汇总任务] 解析出目标日期：{}", targetDate);
        executeSync(targetDate);
    }

    // ========================================================================
    //  核心逻辑
    // ========================================================================

    /**
     * 执行聚合：调用 Service 层的 syncStatsByDate
     *
     * Service 内部会：
     *   1. 从 myo_visit_log 聚合 PV / UV / 新访客数
     *   2. UPSERT 到 myo_visit_stats（重跑安全）
     *   3. 删除 Redis 中对应的临时计数键（visit:pv:yyyy-MM-dd / visit:uv:yyyy-MM-dd）
     *
     * @param date yyyy-MM-dd 格式日期
     */
    private void executeSync(String date) {
        try {
            log.info("[访客汇总任务] 开始汇总，目标日期：{}", date);
            visitStatsService.syncStatsByDate(date);
            log.info("[访客汇总任务] 汇总完成，目标日期：{}", date);
        } catch (Exception e) {
            log.error("[访客汇总任务] 汇总失败，目标日期：{}，原因：{}", date, e.getMessage(), e);
            // 向上抛出，触发 SchedulingRunnable 的重试与告警机制
            throw new RuntimeException("访客汇总失败，目标日期：" + date, e);
        }
    }

    // ========================================================================
    //  参数解析工具
    // ========================================================================

    /**
     * 从 JSON 参数中解析目标日期
     *
     * 支持 "date"（指定日期）和 "daysAgo"（相对偏移）两种字段。
     * 两者都不存在时，默认返回昨天的日期。
     * 日期格式非法时抛出 IllegalArgumentException。
     */
    private String resolveTargetDate(String param) {
        if (!StringUtils.hasText(param)) {
            return LocalDate.now().minusDays(1).format(DATE_FMT);
        }

        // 尝试提取 "date" 字段，格式："date": "2026-03-20"
        java.util.regex.Matcher dateMatcher =
                java.util.regex.Pattern.compile("\"date\"\\s*:\\s*\"([^\"]+)\"").matcher(param);
        if (dateMatcher.find()) {
            String dateStr = dateMatcher.group(1).trim();
            // 格式校验，不合法就直接抛，让 SchedulingRunnable 阻断重试
            try {
                LocalDate.parse(dateStr, DATE_FMT);
            } catch (Exception e) {
                throw new IllegalArgumentException("date 参数格式非法，请使用 yyyy-MM-dd，当前值：" + dateStr);
            }
            return dateStr;
        }

        // 尝试提取 "daysAgo" 字段，格式："daysAgo": 2
        java.util.regex.Matcher daysAgoMatcher =
                java.util.regex.Pattern.compile("\"daysAgo\"\\s*:\\s*(\\d+)").matcher(param);
        if (daysAgoMatcher.find()) {
            int daysAgo = Integer.parseInt(daysAgoMatcher.group(1));
            if (daysAgo < 0) {
                throw new IllegalArgumentException("daysAgo 不能为负数，当前值：" + daysAgo);
            }
            return LocalDate.now().minusDays(daysAgo).format(DATE_FMT);
        }

        // 没有识别到任何字段，用昨天兜底
        log.warn("[访客汇总任务] 参数中未找到 date 或 daysAgo 字段，将使用昨天作为目标日期");
        return LocalDate.now().minusDays(1).format(DATE_FMT);
    }
}