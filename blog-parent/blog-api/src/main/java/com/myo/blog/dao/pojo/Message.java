package com.myo.blog.dao.pojo;

import lombok.Data;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author : myo
 * @create 2023/8/8 15:35
 */
@Data
public class Message {
    private Long id;

    private String content;

    private Long parentId;

    private Long authorId;

    private Long toUid;

    private Long createDate;

    private Integer level;

    private Integer state;
}
