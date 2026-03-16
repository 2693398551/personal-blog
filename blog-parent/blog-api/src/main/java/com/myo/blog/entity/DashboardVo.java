package com.myo.blog.entity;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘聚合数据 VO
 * 一次返回所有展示所需的数据，减少前端请求次数
 */
@Data
public class DashboardVo {

    // ===== 顶部 4 张数据卡片 =====

    /** 文章总数 */
    private Long articleCount;

    /** 总浏览量（从 Redis Hash 中读取累计值，Redis 无数据时读数据库） */
    private Long totalViewCount;

    /** 评论总数（status=1 正常评论） */
    private Long commentCount;

    /** 注册用户总数 */
    private Long userCount;

    /** 本月新增文章数 */
    private Long monthArticleCount;

    /** 本月新增评论数 */
    private Long monthCommentCount;

    /** 本月新增用户数 */
    private Long monthUserCount;

    // ===== 近 30 天浏览量趋势折线图 =====

    /**
     * 近 30 天每日数据
     * key: date（格式 MM/dd）
     * value: { views: Long, comments: Long }
     */
    private List<DayStats> trendData;

    // ===== 热门文章 Top5 =====
    private List<HotArticle> hotArticles;

    // ===== 分类文章占比 =====
    private List<CategoryStat> categoryStats;

    // ===== 最新评论 =====
    private List<RecentComment> recentComments;

    // ===== 内部 DTO =====

    @Data
    public static class DayStats {
        private String date;    // 日期标签，例如 03/01
        private Long views;     // 当天浏览量（文章创建日期维度）
        private Long comments;  // 当天评论数
    }

    @Data
    public static class HotArticle {
        private String id;
        private String title;
        private Integer viewCounts;
    }

    @Data
    public static class CategoryStat {
        private String categoryName;
        private Long count;
    }

    @Data
    public static class RecentComment {
        private String id;
        private String content;
        private String authorNickname;
        private String authorAvatar;
        private Long createDate;
    }
}