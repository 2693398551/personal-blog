package com.myo.blog.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myo.blog.dao.pojo.VisitStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VisitStatsMapper extends BaseMapper<VisitStats> {

    /**
     * UPSERT 单天统计数据
     * INSERT ... ON DUPLICATE KEY UPDATE
     * uk_date 唯一索引保证幂等，定时任务重跑不会产生脏数据
     */
    int upsertStats(VisitStats stats);

    /**
     * 查询最近 N 天的统计数据，用于后台仪表盘折线图
     */
    List<VisitStats> listRecentDays(@Param("days") int days);

    /**
     * 查询指定日期范围内的统计数据
     */
    List<VisitStats> listByDateRange(@Param("startDate") String startDate,
                                     @Param("endDate") String endDate);

    /**
     * 查询全站累计 PV/UV 总量，用于前台底部展示
     */
    VisitStats sumTotal();
}
