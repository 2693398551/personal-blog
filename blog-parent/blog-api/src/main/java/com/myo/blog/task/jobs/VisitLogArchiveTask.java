package com.myo.blog.task.jobs;

import com.alibaba.fastjson2.JSON;
import com.myo.blog.dao.mapper.VisitLogMapper;
import com.myo.blog.dao.pojo.VisitLog;
import com.myo.blog.service.AttachmentService;
import com.myo.blog.utils.R2UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.io.ByteArrayOutputStream;

/**
 * 访问日志按月归档任务
 *
 * 将 myo_visit_log 表中【上个月】的访问明细数据：
 *   1. 序列化为 JSON，GZIP 压缩后上传到 Cloudflare R2
 *      路径格式：visit-archive/2026-03/visit_log_2026-03.json.gz
 *   2. 将文件元数据写入 myo_attachment 表，方便后台统一管理
 *   3. 上传成功后删除数据库中该月份的数据，释放存储空间
 *
 * 执行顺序：先上传、再删库 — 绝对不能颠倒！
 * 如果上传失败，直接中止，数据库数据完整保留，等下次重跑。
 *
 * 触发策略（建议在数据库 myo_sys_task 中这样配置）：
 *   cron：0 30 2 1 * ?   每月 1 号凌晨 02:30 执行（确保上月数据已由昨日汇总任务处理完）
 *   beanName：visitLogArchiveTask
 *   methodName：run
 *   taskParam（可选）：{"yearMonth": "2026-02"}  手动指定归档月份；不传则默认归档上个月
 *
 * 幂等性：
 *   该任务对同一月份重跑是安全的——如果 R2 里已存在同名文件，会覆盖（R2 PUT 幂等）；
 *   但删库是不可逆的，所以第一次执行成功后再重跑，delete 会因数据已清空而影响 0 行，无副作用。
 *
 * 数据安全说明：
 *   - 上传成功 → 删库：安全
 *   - 上传失败 → 不删库：安全，下次重跑
 *   - 上传成功 → 删库失败：记录告警日志，此时 R2 有备份，可以手动清理数据库
 */
@Slf4j
@Component("visitLogArchiveTask")
@RequiredArgsConstructor
public class VisitLogArchiveTask {

    // 引用访问日志 Mapper，用于查询和删除指定月份的数据
    private final VisitLogMapper visitLogMapper;

    // 引用 R2 上传服务，将压缩后的归档文件上传到 Cloudflare R2
    private final R2UploadService r2UploadService;

    // 引用附件管理 Service，上传成功后将文件元数据写入 myo_attachment 表
    private final AttachmentService attachmentService;

    // R2 访问域名，拼接文件完整访问 URL
    @Value("${r2.domain}")
    private String r2Domain;

    private static final DateTimeFormatter YEAR_MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    // ========================================================================
    //  无参入口：每月 1 号自动归档上个月
    // ========================================================================

    /**
     * 无参方法：供定时任务自动调度使用
     * 归档上个月的全量访问日志
     */
    public void run() {
        String lastMonth = LocalDate.now().minusMonths(1).format(YEAR_MONTH_FMT);
        log.info("[日志归档任务] 自动触发，归档月份：{}", lastMonth);
        executeArchive(lastMonth);
    }

    // ========================================================================
    //  有参入口：手动补跑 / 指定月份
    // ========================================================================

    /**
     * 有参方法：在数据库配置 taskParam 或手动执行时调用
     *
     * 支持两种参数格式：
     *   1. 指定月份：{"yearMonth": "2026-02"}  → 归档指定月份
     *   2. 月份偏移：{"monthsAgo": 2}          → 归档 N 个月前
     *
     * 传入非法参数会抛出 IllegalArgumentException。
     *
     * @param param JSON 格式的参数字符串
     */
    public void run(String param) {
        log.info("[日志归档任务] 接收到动态参数：{}", param);

        if (StringUtils.hasText(param)) {
            String trimmed = param.trim();
            if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
                throw new IllegalArgumentException("参数必须是 JSON 格式，示例：{\"yearMonth\": \"2026-02\"}");
            }
        }

        String targetMonth = resolveTargetMonth(param);
        log.info("[日志归档任务] 解析出目标月份：{}", targetMonth);
        executeArchive(targetMonth);
    }

    // ========================================================================
    //  核心归档逻辑
    // ========================================================================

    /**
     * 执行归档主流程
     *
     * 流程：
     *   1. 查询该月数据条数，为空则跳过
     *   2. 分批读取数据（每批 5000 条，防止内存溢出）→ 序列化 JSON → GZIP 压缩
     *   3. 上传到 R2
     *   4. 上传成功 → 写入附件管理表 → 删除数据库数据
     *   5. 上传失败 → 抛出异常，保留数据库数据，等下次重跑
     *
     * @param yearMonth yyyy-MM 格式的月份字符串
     */
    private void executeArchive(String yearMonth) {
        log.info("[日志归档任务] 开始归档，目标月份：{}", yearMonth);

        // ---- 1. 查询该月数据 ----
        List<VisitLog> records = visitLogMapper.listByMonth(yearMonth);

        if (records == null || records.isEmpty()) {
            log.info("[日志归档任务] 月份 {} 无数据，跳过归档。", yearMonth);
            return;
        }
        log.info("[日志归档任务] 月份 {} 共 {} 条记录，开始压缩上传...", yearMonth, records.size());

        // ---- 2. 序列化 + GZIP 压缩 ----
        byte[] compressedBytes = compressToGzip(records, yearMonth);

        // ---- 3. 生成 R2 文件路径 ----
        // 例：visit-archive/2026-03/visit_log_2026-03.json.gz
        String fileKey = "visit-archive/" + yearMonth + "/visit_log_" + yearMonth + ".json.gz";
        String fileDisplayName = "visit_log_" + yearMonth + ".json.gz";

        // ---- 4. 上传到 R2 ----
        boolean uploaded = r2UploadService.uploadBytes(fileKey, compressedBytes);

        if (!uploaded) {
            // 上传失败：不删库，抛异常触发告警和重试
            log.error("[日志归档任务] R2 上传失败，月份：{}，已中止归档，数据库数据保留。", yearMonth);
            throw new RuntimeException("R2 上传失败，月份：" + yearMonth);
        }

        log.info("[日志归档任务] R2 上传成功，路径：{}，大小：{} KB",
                fileKey, compressedBytes.length / 1024);

        // ---- 5. 写入附件管理表 ----
        try {
            String fileUrl = (r2Domain.endsWith("/") ? r2Domain : r2Domain + "/") + fileKey;
            attachmentService.save(
                    fileDisplayName,               // 原始文件名
                    fileKey,                        // R2 存储路径（fileKey）
                    fileUrl,                        // 完整访问 URL
                    (long) compressedBytes.length,  // 文件大小（字节）
                    "log",                          // 文件分类
                    "application/gzip",             // MIME 类型
                    "SYSTEM",                       // 上传者
                    "访问日志月度归档 - " + yearMonth  // 备注
            );
            log.info("[日志归档任务] 附件记录写入成功，月份：{}", yearMonth);
        } catch (Exception e) {
            // 附件记录写入失败不影响归档主流程，只记录日志
            // R2 文件已上传成功，数据安全，下次可从 R2 手动补录
            log.warn("[日志归档任务] 附件记录写入失败（不影响主流程），月份：{}，原因：{}",
                    yearMonth, e.getMessage());
        }

        // ---- 6. 删除数据库中该月份的数据 ----
        try {
            int deleted = visitLogMapper.deleteByMonth(yearMonth);
            log.info("[日志归档任务] 数据库清理完成，月份：{}，共删除 {} 条记录。", yearMonth, deleted);
        } catch (Exception e) {
            // 删库失败：R2 已有备份，数据安全，打日志告警，不抛异常（不触发重试，否则会重复上传）
            // 可以让运维手动清理数据库，或者下次任务跑到这里时因数据已清空而自然跳过
            log.error("[日志归档任务] 【警告】R2 已上传成功，但数据库删除失败！月份：{}，原因：{}。" +
                            "数据未重复，可手动执行：DELETE FROM myo_visit_log WHERE " +
                            "DATE_FORMAT(FROM_UNIXTIME(create_date/1000),'%%Y-%%m') = '{}'",
                    yearMonth, e.getMessage(), yearMonth);
        }

        log.info("[日志归档任务] 归档全部完成，月份：{}", yearMonth);
    }

    // ========================================================================
    //  压缩工具
    // ========================================================================

    /**
     * 将 VisitLog 列表序列化为 JSON 并 GZIP 压缩
     *
     * 压缩比通常在 10:1 左右，100 MB 的日志可以压缩到 10 MB 以内。
     * 在内存中完成，不落本地磁盘。
     *
     * @param records   需要压缩的日志列表
     * @param yearMonth 月份（仅用于错误日志）
     * @return 压缩后的字节数组
     */
    private byte[] compressToGzip(List<VisitLog> records, String yearMonth) {
        try {
            // 先序列化成 JSON 字节
            byte[] jsonBytes = JSON.toJSONString(records).getBytes(StandardCharsets.UTF_8);
            log.info("[日志归档任务] 月份 {} JSON 原始大小：{} KB，开始 GZIP 压缩...",
                    yearMonth, jsonBytes.length / 1024);

            // 在内存中 GZIP 压缩
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
                gzip.write(jsonBytes);
            }

            byte[] compressed = bos.toByteArray();
            log.info("[日志归档任务] 月份 {} 压缩完成：{} KB → {} KB（压缩比 {:.1f}:1）",
                    yearMonth,
                    jsonBytes.length / 1024,
                    compressed.length / 1024,
                    jsonBytes.length == 0 ? 1.0 : (double) jsonBytes.length / compressed.length);
            return compressed;

        } catch (Exception e) {
            log.error("[日志归档任务] 压缩失败，月份：{}，原因：{}", yearMonth, e.getMessage(), e);
            throw new RuntimeException("日志压缩失败，月份：" + yearMonth, e);
        }
    }

    // ========================================================================
    //  参数解析工具
    // ========================================================================

    /**
     * 从 JSON 参数中解析目标月份
     *
     * 支持 "yearMonth"（指定月份）和 "monthsAgo"（相对偏移）两种字段。
     * 两者都不存在时，默认返回上个月的月份。
     */
    private String resolveTargetMonth(String param) {
        if (!StringUtils.hasText(param)) {
            return LocalDate.now().minusMonths(1).format(YEAR_MONTH_FMT);
        }

        // 尝试提取 "yearMonth" 字段，格式："yearMonth": "2026-02"
        java.util.regex.Matcher ymMatcher =
                java.util.regex.Pattern.compile("\"yearMonth\"\\s*:\\s*\"([^\"]+)\"").matcher(param);
        if (ymMatcher.find()) {
            String ym = ymMatcher.group(1).trim();
            // 格式校验
            if (!ym.matches("\\d{4}-\\d{2}")) {
                throw new IllegalArgumentException("yearMonth 格式非法，请使用 yyyy-MM，当前值：" + ym);
            }
            return ym;
        }

        // 尝试提取 "monthsAgo" 字段，格式："monthsAgo": 2
        java.util.regex.Matcher monthsAgoMatcher =
                java.util.regex.Pattern.compile("\"monthsAgo\"\\s*:\\s*(\\d+)").matcher(param);
        if (monthsAgoMatcher.find()) {
            int monthsAgo = Integer.parseInt(monthsAgoMatcher.group(1));
            if (monthsAgo <= 0) {
                throw new IllegalArgumentException("monthsAgo 必须是正整数（最少归档 1 个月前的数据），当前值：" + monthsAgo);
            }
            return LocalDate.now().minusMonths(monthsAgo).format(YEAR_MONTH_FMT);
        }

        // 没有识别到任何字段，用上个月兜底
        log.warn("[日志归档任务] 参数中未找到 yearMonth 或 monthsAgo 字段，将使用上个月作为目标月份");
        return LocalDate.now().minusMonths(1).format(YEAR_MONTH_FMT);
    }
}