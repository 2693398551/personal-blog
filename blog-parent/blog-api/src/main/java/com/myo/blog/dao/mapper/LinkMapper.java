package com.myo.blog.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.myo.blog.dao.pojo.Link;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author : myo
 * @create 2023/7/29 14:35
 */
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface LinkMapper extends BaseMapper<Link> {

    List<Link> listLink();
}
