package com.myo.blog.dao.dos;

import lombok.Data;
// 文章统计
@Data
public class articles {

    private Integer year;

    private Integer month;

    private Long count;
}
