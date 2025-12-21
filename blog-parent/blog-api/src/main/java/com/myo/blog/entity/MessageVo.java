package com.myo.blog.entity;

import lombok.Data;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author : myo
 * @create 2023/8/8 14:40
 */
@Data
public class MessageVo {
    private String id;
    private UserVo author;
    private String content;
    private String createDate;
    private List<MessageVo> childrens;
    private Integer level;
    private Integer state;//1表示用户表2表示游客表
    private UserVo toUser;



}
