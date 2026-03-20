package com.myo.blog.entity.params;

import lombok.Data;
// 文章内容参数
@Data
public class ArticleBodyParam {

    private String content;

    private String contentHtml;

}
