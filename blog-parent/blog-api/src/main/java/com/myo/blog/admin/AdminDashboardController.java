package com.myo.blog.admin;

import com.myo.blog.entity.Result;
import com.myo.blog.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仪表盘数据接口
 * GET /admin/dashboard — 返回所有仪表盘展示数据（聚合接口，前端只需一次请求）
 */
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public Result getDashboard() {
        return dashboardService.getDashboardData();
    }
}