package com.myo.blog.dao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 登录日志实体，对应 myo_login_log 表
 */
@Data
@TableName("myo_login_log")
public class LoginLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID，登录失败时可能为 null */
    private String userId;

    /** 登录账号 */
    private String account;

    /** 登录 IP */
    private String ip;

    /** IP 归属地，通过 ip2region 查询 */
    private String ipLocation;

    /** 浏览器名称，例如 Chrome 114 */
    private String browser;

    /** 操作系统，例如 Windows 10 */
    private String os;

    /** 登录状态：1=成功 0=失败 */
    private Integer status;

    /** 失败原因，成功时为 null */
    private String msg;

    /** 登录时间戳（毫秒） */
    private Long createDate;
}