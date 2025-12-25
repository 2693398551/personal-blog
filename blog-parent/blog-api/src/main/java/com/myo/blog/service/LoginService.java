package com.myo.blog.service;

import com.myo.blog.dao.pojo.SysUser;
import com.myo.blog.entity.Result;
import com.myo.blog.entity.params.LoginParam;

public interface LoginService {
    /**
     * 登录功能
     * @param loginParam
     * @return
     */
    Result login(LoginParam loginParam);

    SysUser checkToken(String token);

    /**
     * 退出登录
     * @param token
     * @return
     */
    Result logout(String token);

    /**
     * 注册
     * @param loginParam
     * @return
     */
    Result register(LoginParam loginParam);
     /**
     * 强制退出登录（踢人下线）
     * @param userId
     * @return
     */
    Result kick(Long userId);
     /**
     * 更新登录信息（最后登录IP、最后登录时间）
     * @param userId
     */
    void updateLoginInfo(Long userId);
}
