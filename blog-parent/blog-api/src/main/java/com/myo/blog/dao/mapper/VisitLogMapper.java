package com.myo.blog.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myo.blog.dao.pojo.VisitLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VisitLogMapper extends BaseMapper<VisitLog> {

    /**
     * 统计指定日期的 PV
     * SELECT COUNT(*) FROM myo_visit_log WHERE DATE(FROM_UNIXTIME(create_date/1000)) = #{date}
     */
    long countPvByDate(@Param("date") String date);

    /**
     * 统计指定日期的 UV（uuid 去重）
     */
    long countUvByDate(@Param("date") String date);

    /**
     * 统计指定日期的新增访客数（当天 first_visit = create_date 的 uuid 数）
     * 即：uuid 在该日期前从未在 visit_log 中出现过
     */
    long countNewVisitorByDate(@Param("date") String date);

    /**
     * 查询某个访客的行为明细（后台点击"查看记录"时调用）
     */
    Page<VisitLog> pageByVisitorUuid(Page<VisitLog> page,
                                     @Param("visitorUuid") String visitorUuid);

    /**
     * 归档用：查询指定月份的全量数据
     * 如：date = "2025-02" 则查 create_date 在 2025-02 整月的记录
     */
    List<VisitLog> listByMonth(@Param("yearMonth") String yearMonth);

    /**
     * 归档后删除指定月份的数据
     */
    int deleteByMonth(@Param("yearMonth") String yearMonth);
}
