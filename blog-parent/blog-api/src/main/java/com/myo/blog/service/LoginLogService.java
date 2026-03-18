package com.myo.blog.service;

import com.myo.blog.entity.Result;
import com.myo.blog.entity.params.PageParams;
import jakarta.servlet.http.HttpServletRequest;

public interface LoginLogService {

    /**
     * 异步记录登录行为（不阻塞登录主流程）
     */
    void record(String ip, String userAgent, String userId,
                String account, int status, String msg);

    /**
     * 分页查询登录日志，返回带角色名的 VO 列表
     */
    Result listLog(PageParams pageParams);
}