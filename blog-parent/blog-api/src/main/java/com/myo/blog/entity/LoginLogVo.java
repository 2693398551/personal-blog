package com.myo.blog.entity;

import lombok.Data;

/**
 * 登录日志展示 VO
 * 在 LoginLog 基础上扩展了角色名，用于前端展示
 */
@Data
public class LoginLogVo {
    private Long id;
    private String userId;
    private String account;
    private String roleName;    // 角色名称，例如「管理员」「站长」，未知账号填「未知账号」
    private String ip;
    private String ipLocation;  // 已格式化的归属地，例如「中国 广东 深圳 · 电信」
    private String browser;
    private String os;
    private Integer status;
    private String msg;
    private Long createDate;
}