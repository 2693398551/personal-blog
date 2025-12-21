package com.myo.blog.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myo.blog.dao.pojo.Tag;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface TagMapper extends BaseMapper<Tag> {
    /**
     * 根据文章id查询标签列表
     * @param articleId
     * @return
     */
    List<Tag> findTagsByArticleId(Long articleId);


    /**
     * 查询最热的标签 前n条
     * @param limit
     * @return
     */
    List<Long> findHotsTagIds(int limit);

    List<Tag> findTagsByTagIds(List<Long> tagIds);
}
