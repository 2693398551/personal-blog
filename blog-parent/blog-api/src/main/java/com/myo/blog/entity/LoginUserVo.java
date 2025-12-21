package com.myo.blog.entity;

import lombok.Data;

@Data
public class LoginUserVo {

    private String id;

    private String account;

    private String nickname;//昵称

    private String avatar;//头像

    private String email;//邮箱

    private String mobilePhoneNumber;//手机号
}
