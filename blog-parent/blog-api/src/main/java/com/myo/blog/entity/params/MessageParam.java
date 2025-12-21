package com.myo.blog.entity.params;

import lombok.Data;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author : myo
 * @create 2023/8/8 15:09
 */
@Data
public class MessageParam {
    private Long articleId;

    private String content;

    private Long parent;

    private Long state;

    private Long toUserId;

}
