package com.myo.blog.controller;

import com.myo.blog.common.aop.LogAnnotation;
import com.myo.blog.common.aop.RequirePermission;
import com.myo.blog.common.cache.Cache;
import com.myo.blog.dao.pojo.WebInformation;
import com.myo.blog.entity.Result;
import com.myo.blog.service.WebInformationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webInfo")
@RequiredArgsConstructor
public class WebInformationController {


    private final WebInformationService webInformationService;

    // 前台获取网站信息调用的接口,redis缓存1天
    @GetMapping
    @Cache(expire = 24 * 60 * 60 * 1000, name = "webInfo")
    public Result getWebInfo() {
        return webInformationService.getWebInfo();
    }


}