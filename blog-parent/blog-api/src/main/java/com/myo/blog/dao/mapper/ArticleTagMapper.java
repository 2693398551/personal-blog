package com.myo.blog.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myo.blog.dao.pojo.ArticleTag;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface ArticleTagMapper  extends BaseMapper<ArticleTag> {
}
