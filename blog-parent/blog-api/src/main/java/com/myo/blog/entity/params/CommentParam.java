package com.myo.blog.entity.params;

import lombok.Data;

@Data
public class CommentParam {

    private Long articleId;

    private String content;

    private Long parent;

    private Long toUserId;
}
