package com.myo.blog.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myo.blog.dao.dos.Archives;
import com.myo.blog.dao.pojo.Article;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
@Mapper
@Repository
public interface ArticleMapper extends BaseMapper<Article> {


    List<Archives> listArchives();


    IPage<Article> listArticle(Page<Article> page,
                               Long categoryId,
                               Long tagId,
                               String year,
                               String month,
                               boolean isToken

    );
    Integer listArticleCount(boolean isToken);
}
