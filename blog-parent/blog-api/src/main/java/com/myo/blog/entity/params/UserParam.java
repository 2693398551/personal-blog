package com.myo.blog.entity.params;

import lombok.Data;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author : myo
 * @create 2023/8/16 22:17
 */
@Data
public class UserParam {
    private String id;
    private String account;//用户名
    private String password;//密码
    private String nickname;//昵称
    private String avatar;//头像
    private String email;//邮箱
    private String mobilePhoneNumber;//电话
}
