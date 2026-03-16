package com.myo.blog.dao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class SysUser {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String account;

    private String nickname;

    /** 个人简介 */
    private String bio;

    /** 生日 */
    private java.time.LocalDate birthday;

    /** 个人主页 */
    private String website;

    private Integer sex;          // 0未知 1男 2女

    private String avatar;

    private String email;

    private String mobilePhoneNumber;

    // ===== 安全相关 =====

    private String password;

    private String salt;

    /** 最后修改密码时间戳（毫秒） */
    private Long pwdUpdateDate;

    // ===== 状态 & 管理 =====

    /** 账号状态：0=正常 1=警告 99=封禁 */
    private Integer status;

    /** 封禁到期时间戳（毫秒），NULL=永久封禁，有值=临时封禁 */
    private Long banExpireTime;

    private Integer deleted;

    /** 状态备注/封禁理由 */
    private String remark;

    // ===== 时间 & 登录 =====

    private Long createDate;

    /** 注册来源：1=账号注册 2=QQ登录 3=微信登录 */
    private Integer source;

    /** 最后更新时间戳（毫秒） */
    private Long updateDate;

    private Long lastLogin;

    private String lastIpaddr;

    /** 连续登录失败次数，登录成功后清零 */
    private Integer loginFailCount;

    /** 账号锁定到期时间戳（毫秒），NULL=未锁定 */
    private Long lockTime;

    // ===== 三方登录 =====

    /** QQ登录 open_id */
    private String qqOpenId;

    /** 微信登录 open_id */
    private String wxOpenId;

    // ===== 非数据库字段 =====

    @TableField(exist = false)
    private Boolean online;
}