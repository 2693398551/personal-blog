package com.myo.blog.dao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 访客主表
 * 每个 UUID 一条记录，长期保留
 */
@Data
@TableName("myo_visitor")
public class Visitor {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 访客唯一标识，前台 localStorage 存储 */
    private String uuid;

    /** 关联登录用户ID，未登录为 null */
    private String userId;

    private String ip;

    /** IP 归属地原始字符串，格式：中国|广东|深圳|电信 */
    private String ipLocation;

    /** 省份，用于地图统计 */
    private String province;

    /** 城市，用于地图统计 */
    private String city;

    /** 操作系统，如 Windows 10 / iOS 18 */
    private String os;

    /** 浏览器，如 Chrome 146 */
    private String browser;

    /** 该访客累计访问次数（PV） */
    private Integer pv;

    /** 首次访问时间戳（毫秒） */
    private Long firstVisit;

    /** 最后访问时间戳（毫秒） */
    private Long lastVisit;

    private Long createDate;

    /** 关联用户昵称（非数据库字段，JOIN 查询带出） */
    @TableField(exist = false)
    private String userNickname;

    /** 关联用户头像（非数据库字段，JOIN 查询带出） */
    @TableField(exist = false)
    private String userAvatar;
}
