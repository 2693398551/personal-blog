package com.myo.blog.dao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 访客统计按天汇总表
 * 每天一条记录，永久保留，数据量极小
 * 由凌晨定时任务 visitStatsSyncTask 从 myo_visit_log 聚合写入
 * uk_date 唯一索引，支持 INSERT ... ON DUPLICATE KEY UPDATE 安全重跑
 */
@Data
@TableName("myo_visit_stats")
public class VisitStats {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 统计日期，格式 yyyy-MM-dd */
    private String date;

    /** 当日总页面访问量（Page View） */
    private Long pv;

    /** 当日独立访客数（Unique Visitor，以 uuid 去重） */
    private Long uv;

    /** 当日新增访客数（首次出现的 uuid） */
    private Long newVisitor;

    /** 记录写入时间戳（毫秒） */
    private Long createDate;
}
