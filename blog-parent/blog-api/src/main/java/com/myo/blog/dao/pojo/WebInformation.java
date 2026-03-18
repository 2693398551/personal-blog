package com.myo.blog.dao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("myo_web_information")
public class WebInformation {

    // 主键ID，自动递增
    @TableId(type = IdType.AUTO)
    private Integer id;

    // 网站名称，例如：Myo Nexus
    private String siteName;

    // 网站描述，主要用于SEO优化和meta标签
    private String siteDesc;

    // 网站关键字，主要用于SEO优化，多个关键字可以用逗号隔开
    private String keywords;

    // 网站Logo图片的URL地址
    private String logoUrl;

    // 网站浏览器标签页图标(Favicon)的URL地址
    private String faviconUrl;

    // 站长名称或网站作者的名字
    private String author;

    // 站长头像的URL地址
    private String authorAvatar;

    // 关于站长的详细介绍，通常支持Markdown或富文本格式
    private String aboutMe;

    // 网站运行状态：1代表正常运行，0代表维护中(前端可根据此状态拦截跳转到维护页面)
    private Integer siteStatus;

    // 是否开放注册功能：1代表开放，0代表关闭(关闭时隐藏前端注册入口并拦截后端注册接口)
    private Integer allowRegister;

    // 全局评论开关：1代表允许发表新评论，0代表全站禁言(仅展示已有评论)
    private Integer allowComment;

    // 是否开启文章打赏功能：1代表开启，0代表关闭
    private Integer showReward;

    // 是否在网站底部显示累计访问量和访客统计信息：1代表显示，0代表隐藏
    private Integer showVisitorCount;

    // 系统全局公告，通常悬浮显示在首页顶部
    private String notice;

    // 站长个人的GitHub主页链接
    private String githubUrl;

    // 站长个人的Gitee主页链接
    private String giteeUrl;

    // 站长联系QQ号码
    private String qqNumber;

    // 站长微信二维码或公众号二维码的URL地址
    private String wechatQrUrl;

    // 站长联系邮箱
    private String email;

    // 百度统计的ID，用于前端动态注入埋点脚本进行流量分析
    private String baiduAnalyticsId;

    // 谷歌统计(Google Analytics)的ID，用于流量分析
    private String googleAnalyticsId;

    // 网站的ICP备案号，例如：湘ICP备2022004529号，展示在网站底部
    private String icpRecord;

    // 网站的公安联网备案号，展示在网站底部
    private String policeRecord;

    // 网站版权年份，例如：2023-2026，用于底部Copyright展示
    private String copyrightYear;

    // 该条配置记录的创建时间
    private Date createTime;

    // 该条配置记录的最后更新时间
    private Date updateTime;
}