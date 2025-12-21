package com.myo.blog.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myo.blog.dao.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface CommentMapper extends BaseMapper<Comment> {
    /*查询评论数量*/
    public Integer queryCommentCount(int articleId);
}
