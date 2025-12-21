package com.myo.blog.controller;

import com.myo.blog.dao.pojo.SysUser;
import com.myo.blog.utils.UserThreadLocal;
import com.myo.blog.entity.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestController {

    @GetMapping
    public Result test(){
        SysUser sysUser = UserThreadLocal.get();

        return Result.success(null);
    }
}
