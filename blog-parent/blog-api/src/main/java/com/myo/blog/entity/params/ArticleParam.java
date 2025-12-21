package com.myo.blog.entity.params;

import com.myo.blog.entity.CategoryVo;
import com.myo.blog.entity.TagVo;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Data
public class ArticleParam {

    private Long id;

    private ArticleBodyParam body;

    private CategoryVo category;

    private String summary;

    private List<TagVo> tags;

    private String title;

    private String cover;
}
