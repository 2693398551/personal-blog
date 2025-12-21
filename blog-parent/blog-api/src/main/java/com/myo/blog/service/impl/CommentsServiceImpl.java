package com.myo.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myo.blog.dao.mapper.CommentMapper;
import com.myo.blog.dao.pojo.Comment;
import com.myo.blog.dao.pojo.SysUser;
import com.myo.blog.dao.pojo.Tag;
import com.myo.blog.service.CommentsService;
import com.myo.blog.service.SysUserService;
import com.myo.blog.utils.UserThreadLocal;
import com.myo.blog.entity.CommentVo;
import com.myo.blog.entity.Result;
import com.myo.blog.entity.UserVo;
import com.myo.blog.entity.params.CommentParam;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CommentsServiceImpl implements CommentsService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private SysUserService sysUserService;

    @Override
    public Result commentsByArticleId(Long id) {
        /**
         * 1. 根据文章id 查询 评论列表 从 comment 表中查询
         * 2. 根据作者的id 查询作者的信息
         * 3. 判断 如果 level = 1 要去查询它有没有子评论
         * 4. 如果有 根据评论id 进行查询 （parent_id）
         */
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getArticleId,id);
        queryWrapper.eq(Comment::getLevel,1);
        queryWrapper.orderByDesc(Comment::getCreateDate);
        List<Comment> comments = commentMapper.selectList(queryWrapper);

        List<CommentVo> commentVoList = copyList(comments);

        return Result.success(commentVoList);
    }

    @Override
    public Result comment(CommentParam commentParam) {
        SysUser sysUser = UserThreadLocal.get();
        System.out.println("用户:"+sysUser);

        Comment comment = new Comment();
        comment.setArticleId(commentParam.getArticleId());
        comment.setAuthorId(sysUser.getId());
        comment.setContent(commentParam.getContent());
        comment.setCreateDate(System.currentTimeMillis());
        Long parent = commentParam.getParent();
        if (parent == null || parent == 0) {
            comment.setLevel(1);
        }else{
            comment.setLevel(2);
        }
        comment.setParentId(parent == null ? 0 : parent);
        Long toUserId = commentParam.getToUserId();
        comment.setToUid(toUserId == null ? 0 : toUserId);
        int t=this.commentMapper.insert(comment);
/*        System.out.println("看评论:"+t);
        System.out.println("看看那个文章:"+comment.getArticleId());*/
        return Result.success(null);
    }



    public Result queryCommentCount(int id) {
        /**
         * 1. 标签所拥有的文章数量最多 最热标签
         * 2. 查询 根据tag_id 分组 计数，从大到小 排列 取前 limit个
         */
        Integer count = commentMapper.queryCommentCount(id);
        return Result.success(count);
    }

    private List<CommentVo> copyList(List<Comment> comments) {
        List<CommentVo> commentVoList = new ArrayList<>();
        for (Comment comment : comments) {
            commentVoList.add(copy(comment));
        }
        return commentVoList;
    }

    private CommentVo copy(Comment comment) {
        CommentVo commentVo = new CommentVo();
        BeanUtils.copyProperties(comment,commentVo);
        commentVo.setId(String.valueOf(comment.getId()));
        //作者信息
        Long authorId = comment.getAuthorId();
        UserVo userVo = this.sysUserService.findUserVoById(authorId);
        commentVo.setAuthor(userVo);
        //子评论
        Integer level = comment.getLevel();
        if (1 == level){
            Long id = comment.getId();
            List<CommentVo> commentVoList = findCommentsByParentId(id);
            commentVo.setChildrens(commentVoList);
        }
        //to User 给谁评论
        if (level > 1){
            Long toUid = comment.getToUid();

            UserVo toUserVo = this.sysUserService.findUserVoById(toUid);
            commentVo.setToUser(toUserVo);
        }
        //评论or回复 时间
        commentVo.setCreateDate(new DateTime(comment.getCreateDate()).toString("yyyy-MM-dd HH:mm"));
        return commentVo;
    }

    private List<CommentVo> findCommentsByParentId(Long id) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getParentId,id);
        queryWrapper.eq(Comment::getLevel,2);
        return copyList(commentMapper.selectList(queryWrapper));
    }
}
