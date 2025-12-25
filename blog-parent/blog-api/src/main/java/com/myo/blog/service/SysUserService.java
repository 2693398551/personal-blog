package com.myo.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.myo.blog.dao.pojo.SysUser;
import com.myo.blog.entity.Result;
import com.myo.blog.entity.UserVo;
import com.myo.blog.entity.params.UserParam;
import org.apache.catalina.User;

public interface SysUserService {

    UserVo findUserVoById(Long id);

    SysUser findUserById(Long id);

    SysUser findUser(String account, String password);

    SysUser findIpaddr(String account, String password);
    /**
     * 根据token查询用户信息
     * @param token
     * @return
     */
    Result findUserByToken(String token);
    /**
     * 修改用户个人信息
     * @param userParam
     * @return
     */
     int  updateUser(UserParam userParam);
    /**
     * 修改用户个人头像
     * @param userParam
     * @return
     */
     int  updateUserAvatar(UserParam userParam);

    /**
     * 根据账户查找用户
     * @param account
     * @return
     */
    SysUser findUserByAccount(String account);
    /**
     * 根据账户跟id查找用户
     * @param user
     * @return
     */
    UserVo findUserByAccount(UserParam user);

    /**
     * 根据用户信息查找用户
     * @param user
     * @return
     */
    SysUser findUserByAccount(SysUser user);


    /**
     * 保存用户
     * @param sysUser
     */
    void save(SysUser sysUser);

    /**
     * 根据ID更新用户信息
     * @param sysUser
     */
    boolean updateById(SysUser sysUser);
}
