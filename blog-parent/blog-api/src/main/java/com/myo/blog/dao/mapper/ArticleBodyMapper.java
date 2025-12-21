package com.myo.blog.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myo.blog.dao.pojo.ArticleBody;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface ArticleBodyMapper extends BaseMapper<ArticleBody> {
}
