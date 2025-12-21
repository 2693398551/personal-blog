package com.myo.blog.controller;

import com.myo.blog.dao.pojo.SysUser;
import com.myo.blog.entity.UserVo;
import com.myo.blog.entity.params.UserParam;
import com.myo.blog.service.SysUserService;
import com.myo.blog.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("users")
public class UsersController {

    @Autowired
    private SysUserService sysUserService;

    ///users/currentUser
    @GetMapping("currentUser")
    public Result currentUser(@RequestHeader("Authorization") String token){
        return sysUserService.findUserByToken(token);
    }
    //根据用户名查询账号信息
    @PostMapping("queryUserByAccount")
    public Result findUserByAccount(@RequestBody UserParam user){

        return Result.success(sysUserService.findUserByAccount(user));
    }
    //修改个人资料
    @PostMapping("updateUser")
    public Result updateUse(@RequestBody UserParam userParam){

        int i=sysUserService.updateUser(userParam);
        return Result.success(i);
    }
    //修改个人头像
    @PostMapping("updateUserAvatar")
    public Result updateUserAvatar(@RequestBody UserParam userParam){

        int i=sysUserService.updateUserAvatar(userParam);
        return Result.success(i);
    }
}
