package com.myo.blog.service;

import com.myo.blog.entity.Result;
import com.myo.blog.dao.pojo.WebInformation;

public interface WebInformationService {

    // 获取网站全局配置信息
    Result getWebInfo();

    // 更新网站全局配置信息
    Result updateWebInfo(WebInformation webInformation);
}