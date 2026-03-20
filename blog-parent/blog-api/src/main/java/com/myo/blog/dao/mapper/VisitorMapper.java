package com.myo.blog.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myo.blog.dao.pojo.Visitor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface VisitorMapper extends BaseMapper<Visitor> {

    /**
     * 按城市聚合访客数，用于前台地图展示
     * SELECT city, COUNT(DISTINCT uuid) AS uv FROM myo_visitor GROUP BY city
     */
    List<Map<String, Object>> countUvByCity();

    /**
     * 分页查询访客列表（后台管理）
     * 支持按 IP、城市、UUID 模糊搜索
     */
    Page<Visitor> pageVisitors(Page<Visitor> page,
                               @Param("keyword") String keyword);

    /**
     * UPSERT：uuid 已存在则更新 pv/last_visit/user_id，不存在则插入
     * 使用 INSERT ... ON DUPLICATE KEY UPDATE 实现
     */
    int upsert(Visitor visitor);
}
