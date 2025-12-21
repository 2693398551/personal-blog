package com.myo.blog.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myo.blog.dao.pojo.Message;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author : myo
 * @create 2023/8/8 15:33
 */
@Mapper
@Repository
public interface MessageMapper extends BaseMapper<Message> {
}
