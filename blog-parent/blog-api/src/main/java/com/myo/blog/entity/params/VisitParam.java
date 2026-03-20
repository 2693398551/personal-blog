package com.myo.blog.entity.params;

import lombok.Data;

/**
 * 访客访问参数封装
 * 职责：
 *  1. Controller 层从 HTTP 请求提取数据后组装此对象
 *  2. 作为 MQ 消息体在 Service → Listener 之间传递
 *  3. Listener 收到后直接用于写库，不需要再转换
 */
@Data
public class VisitParam {

    /** 访客 UUID */
    private String visitorUuid;

    /** 是否新访客（后端生成的新 UUID） */
    private boolean newVisitor;

    /** 登录用户 ID，未登录为 null */
    private String userId;

    /** 前端路由路径，如 /、/view/123 */
    private String uri;

    /** 行为类型：PAGE_VIEW / VIEW_ARTICLE / VIEW_CATEGORY / COMMENT / OTHER */
    private String behavior;

    /** 页面名称描述，如：首页、文章详情页 */
    private String content;

    /** IP 地址 */
    private String ip;

    /** IP 归属地原始字符串 */
    private String ipLocation;

    /** 省份 */
    private String province;

    /** 城市 */
    private String city;

    /** 操作系统 */
    private String os;

    /** 浏览器 */
    private String browser;

    /** 来源页面 URL */
    private String referer;

    /** 访问时间戳（毫秒） */
    private Long createDate;
}