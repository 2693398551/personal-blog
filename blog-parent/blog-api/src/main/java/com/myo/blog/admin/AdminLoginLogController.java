package com.myo.blog.admin;

import com.myo.blog.common.aop.RequirePermission;
import com.myo.blog.entity.Result;
import com.myo.blog.entity.params.PageParams;
import com.myo.blog.service.LoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录日志后台接口
 * POST /admin/loginLog/list — 分页查询登录日志
 */
@RestController
@RequestMapping("/admin/loginLog")
@RequiredArgsConstructor
public class AdminLoginLogController {

    private final LoginLogService loginLogService;

    @PostMapping("/list")
    @RequirePermission("sys:log:list")
    public Result list(@RequestBody PageParams pageParams) {
        return loginLogService.listLog(pageParams);
    }
}