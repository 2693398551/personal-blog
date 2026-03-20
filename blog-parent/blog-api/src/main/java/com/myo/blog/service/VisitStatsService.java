package com.myo.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myo.blog.dao.pojo.Visitor;
import com.myo.blog.dao.pojo.VisitLog;
import com.myo.blog.dao.pojo.VisitStats;
import com.myo.blog.entity.params.VisitParam;


import java.util.List;
import java.util.Map;

public interface VisitStatsService {

    /** 前端路由埋点，记录一次页面访问 */
    void record(VisitParam param);

    /** 获取今日 PV */
    long getTodayPv();

    /** 获取今日 UV */
    long getTodayUv();

    /** 全站累计 PV / UV */
    VisitStats getTotalStats();

    /** 最近 N 天统计数据 */
    List<VisitStats> getRecentDays(int days);

    /** 指定日期范围统计数据 */
    List<VisitStats> getByDateRange(String startDate, String endDate);

    /** 按城市聚合 UV */
    List<Map<String, Object>> getUvByCity();

    /** 分页查询访客列表 */
    Page<Visitor> pageVisitors(int page, int pageSize, String keyword);

    /** 查询某访客的行为明细 */
    Page<VisitLog> pageVisitLogs(int page, int pageSize, String visitorUuid);

    /** 定时任务调用：聚合落库 + 清 Redis */
    void syncStatsByDate(String date);
}