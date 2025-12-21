package com.myo.blog.dao.pojo;

import lombok.Data;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author : myo
 * @create 2023/7/29 14:23
 */
@Data
public class Link {
    /***
     * id
     * */
    private Long id;
    /***
     * 连接名字
     * */
    private String name;
    /***
     * 连接描述
     * */
    private String content;
    /***
     * 连接路径
     * */
    private String url;
    /***
     * 连接图片
     * */
    private String imgicon;


}
