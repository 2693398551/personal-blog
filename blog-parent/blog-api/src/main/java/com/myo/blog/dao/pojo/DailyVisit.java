package com.myo.blog.dao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("myo_daily_visit")
public class DailyVisit {

    @TableId(type = IdType.AUTO)
    private Integer id;

    // 统计的日期（例如：2023-10-25）
    private Date date;

    // 当日的页面浏览量 PV
    private Integer pv;

    // 当日的独立访客数 UV
    private Integer uv;

    // 记录生成的时间
    private Date createTime;
}