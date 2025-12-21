package com.myo.blog.entity;

import lombok.Data;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author : myo
 * @create 2023/8/8 14:37
 */
@Data
public class VisitorVo {
    private String id;
    private String nickname;
    private String avatar;
    private String email;
    private String website;
    private String ipaddr;
    private String createDate;
}
