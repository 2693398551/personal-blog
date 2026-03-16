package com.myo.blog.service;

import com.myo.blog.entity.Result;

public interface DashboardService {
    /**
     * 获取仪表盘聚合数据
     * 一次返回：顶部卡片、趋势折线图、热门文章、分类占比、最新评论
     */
    Result getDashboardData();

    void refreshCache();

}