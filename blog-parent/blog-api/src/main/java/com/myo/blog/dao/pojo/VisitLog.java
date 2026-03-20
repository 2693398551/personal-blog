package com.myo.blog.dao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 访问行为明细表
 * 每次请求一条记录，是统计系统的"源头真相"
 * Redis 宕机可从此表重新聚合恢复
 * 每月由定时任务归档到 R2 后删除历史记录
 */
@Data
@TableName("myo_visit_log")
public class VisitLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 访客唯一标识 */
    private String visitorUuid;

    /** 关联登录用户ID，未登录为 null */
    private String userId;

    /**
     * 行为类型：
     * PAGE_VIEW / VIEW_ARTICLE / VIEW_CATEGORY / COMMENT / OTHER
     */
    private String behavior;

    /** 行为内容描述，如文章标题、分类名 */
    private String content;

    /** 请求路径，如 /articles/view/ART123 */
    private String uri;

    /** 请求方法：GET / POST */
    private String method;

    private String ip;

    /** IP 归属地原始字符串 */
    private String ipLocation;

    private String province;

    private String city;

    private String os;

    private String browser;

    /** 来源页面 URL，直接访问为 null */
    private String referer;

    /** 访问时间戳（毫秒） */
    private Long createDate;
}
