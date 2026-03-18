package com.myo.blog.dao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("myo_visit_log")
public class VisitLog {

    // 主键ID，使用数据库自增
    @TableId(type = IdType.AUTO)
    private Long id;

    // 访客唯一标识，可用于更精准的UV统计或追踪特定用户轨迹
    private String uuid;

    // 访客真实的IP地址
    private String ip;

    // 根据IP解析出来的归属地信息，例如：中国|湖南省|长沙市
    private String ipSource;

    // 访客使用的操作系统，例如：Windows 10, macOS
    private String os;

    // 访客使用的浏览器型号，例如：Chrome 114
    private String browser;

    // 当前访问的页面URL或具体的后台接口路径
    private String pageUrl;

    // 访问的模块名称，例如：首页、文章详情等
    private String module;

    // 记录生成的时间，也就是访问发生的时间
    private Date createTime;
}