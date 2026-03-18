package com.myo.blog.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myo.blog.dao.pojo.VisitLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VisitLogMapper extends BaseMapper<VisitLog> {
        // 继承 MyBatis Plus 的基础映射接口，直接获得强大的增删改查能力
}