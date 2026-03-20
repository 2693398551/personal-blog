package com.myo.blog.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myo.blog.common.aop.RequirePermission;
import com.myo.blog.dao.pojo.Visitor;
import com.myo.blog.dao.pojo.VisitLog;
import com.myo.blog.dao.pojo.VisitStats;
import com.myo.blog.entity.Result;
import com.myo.blog.service.VisitStatsService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台访客统计接口
 * AdminInterceptor 已拦截 /admin/**，无需额外鉴权
 */
@RestController
@RequestMapping("/admin/visit")
@RequiredArgsConstructor
public class AdminVisitController {

    private final VisitStatsService visitStatsService;

    // ----------------------------------------------------------------
    //  仪表盘
    //visit:stats — 统计概览（today/recent/range）
    //visit:map — 访客地图
    //visit:list — 访客列表
    //visit:log — 行为明细（只有站长和超管能看）
    // ----------------------------------------------------------------

    /**
     * 今日 PV / UV 概览
     * GET /admin/visit/today
     */
    @GetMapping("/today")
    @RequirePermission("visit:stats")
    public Result todayStats() {
        Map<String, Long> data = Map.of(
                "pv", visitStatsService.getTodayPv(),
                "uv", visitStatsService.getTodayUv()
        );
        return Result.success(data);
    }

    /**
     * 最近 N 天折线图数据（默认 30 天）
     * GET /admin/visit/recent?days=30
     */
    @GetMapping("/recent")
    @RequirePermission("visit:stats")
    public Result recentDays(@RequestParam(defaultValue = "30") int days) {
        List<VisitStats> list = visitStatsService.getRecentDays(days);
        return Result.success(list);
    }

    /**
     * 自定义日期范围
     * GET /admin/visit/range?startDate=2025-01-01&endDate=2025-01-31
     */
    @GetMapping("/range")
    @RequirePermission("visit:stats")
    public Result dateRange(@RequestParam String startDate,
                            @RequestParam String endDate) {
        List<VisitStats> list = visitStatsService.getByDateRange(startDate, endDate);
        return Result.success(list);
    }

    // ----------------------------------------------------------------
    //  地图
    // ----------------------------------------------------------------

    /**
     * 按城市聚合 UV，ECharts 地图打点
     * GET /admin/visit/map
     */
    @GetMapping("/map")
    @RequirePermission("visit:map")
    public Result cityMap() {
        List<Map<String, Object>> data = visitStatsService.getUvByCity();
        return Result.success(data);
    }

    // ----------------------------------------------------------------
    //  访客列表
    // ----------------------------------------------------------------

    /**
     * 分页查询访客列表
     * GET /admin/visit/visitors?page=1&pageSize=20&keyword=
     */
    @GetMapping("/visitors")
    @RequirePermission("visit:list")
    public Result visitors(@RequestParam(defaultValue = "1")  int page,
                           @RequestParam(defaultValue = "20") int pageSize,
                           @RequestParam(defaultValue = "")   String keyword) {
        Page<Visitor> result = visitStatsService.pageVisitors(page, pageSize, keyword);
        return Result.success(result);
    }

    /**
     * 查看某访客的行为明细
     * GET /admin/visit/logs?visitorUuid=xxx&page=1&pageSize=20
     */
    @GetMapping("/logs")
    @RequirePermission("visit:log")
    public Result visitorLogs(@RequestParam                      String visitorUuid,
                              @RequestParam(defaultValue = "1")  int page,
                              @RequestParam(defaultValue = "20") int pageSize) {
        Page<VisitLog> result = visitStatsService.pageVisitLogs(page, pageSize, visitorUuid);
        return Result.success(result);
    }
}
