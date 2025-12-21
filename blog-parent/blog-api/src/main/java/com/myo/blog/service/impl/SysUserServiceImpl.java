package com.myo.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.myo.blog.dao.mapper.SysUserMapper;
import com.myo.blog.dao.pojo.SysUser;
import com.myo.blog.entity.params.UserParam;
import com.myo.blog.service.LoginService;
import com.myo.blog.service.SysUserService;
import com.myo.blog.entity.ErrorCode;
import com.myo.blog.entity.LoginUserVo;
import com.myo.blog.entity.Result;
import com.myo.blog.entity.UserVo;
import com.myo.blog.utils.HttpContextUtils;
import com.myo.blog.utils.IpUtils;
import org.apache.catalina.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private LoginService loginService;

    @Override
    public UserVo findUserVoById(Long id) {

        SysUser sysUser = sysUserMapper.selectById(id);
        if(id==0){//这里表示评论区的回复那个评论的id 0表示在子评论的自己不回复自己评论
            return new UserVo();
        }

        if (sysUser == null){
            sysUser = new SysUser();
            sysUser.setId(1L);
            sysUser.setAvatar("/static/img/tx.gif");
            sysUser.setNickname("baola");
        }
        UserVo userVo  = new UserVo();
        BeanUtils.copyProperties(sysUser,userVo);
        userVo.setId(String.valueOf(sysUser.getId()));
        return userVo;
    }

    @Override
    public SysUser findUserById(Long id) {
        SysUser sysUser = sysUserMapper.selectById(id);
        if (sysUser == null){
            sysUser = new SysUser();
            sysUser.setNickname("myo");
        }
        return sysUser;
    }

    @Override
    public SysUser findUser(String account, String password) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getAccount,account);
        queryWrapper.eq(SysUser::getPassword,password);
        queryWrapper.select(SysUser::getAccount,SysUser::getId,SysUser::getAvatar,SysUser::getNickname,SysUser::getMobilePhoneNumber,SysUser::getEmail);
        queryWrapper.last("limit 1");

        return sysUserMapper.selectOne(queryWrapper);
    }
    @Override
    public SysUser findIpaddr(String account, String password) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getAccount,account);
        queryWrapper.eq(SysUser::getPassword,password);
        queryWrapper.select(SysUser::getIpaddr);
        //queryWrapper.select(SysUser::getAccount,SysUser::getId,SysUser::getAvatar,SysUser::getNickname);

        queryWrapper.last("limit 1");

        return sysUserMapper.selectOne(queryWrapper);
    }

    @Override
    public Result findUserByToken(String token) {
        /**
         * 1. token合法性校验
         *    是否为空，解析是否成功 redis是否存在
         * 2. 如果校验失败 返回错误
         * 3. 如果成功，返回对应的结果 LoginUserVo
         */
        SysUser sysUser = loginService.checkToken(token);
        if (sysUser == null){
            return Result.fail(ErrorCode.TOKEN_ERROR.getCode(),ErrorCode.TOKEN_ERROR.getMsg());
        }
        LoginUserVo loginUserVo = new LoginUserVo();
        loginUserVo.setId(String.valueOf(sysUser.getId()));
        loginUserVo.setNickname(sysUser.getNickname());
        loginUserVo.setAvatar(sysUser.getAvatar());
        loginUserVo.setAccount(sysUser.getAccount());
        loginUserVo.setEmail(sysUser.getEmail());
        loginUserVo.setMobilePhoneNumber(sysUser.getMobilePhoneNumber());
        return Result.success(loginUserVo);
    }
    //查询redis中报存的用户信息
    public SysUser queryUserByToken(String token) {
        SysUser sysUser = loginService.checkToken(token);
        return sysUser;
    }

    @Override
    public SysUser findUserByAccount(String account) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getAccount,account);
        queryWrapper.last("limit 1");
        return this.sysUserMapper.selectOne(queryWrapper);
    }

    @Override
    public UserVo findUserByAccount(UserParam user) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getAccount,user.getAccount())
                .eq(SysUser::getId,user.getId());
        queryWrapper.last("limit 1");
        SysUser sysUser=this.sysUserMapper.selectOne(queryWrapper);
        /*用户信息视图*/
        UserVo userVo  = new UserVo();
        userVo.setId(sysUser.getId().toString());
        userVo.setAccount(sysUser.getAccount());
        userVo.setNickname(sysUser.getNickname());
        userVo.setAvatar(sysUser.getAvatar());
        userVo.setEmail(sysUser.getEmail());
        userVo.setMobilePhoneNumber(sysUser.getMobilePhoneNumber());

        return userVo;
    }
    @Override
    public int updateUser(UserParam userParam){

        return update(userParam);
    }
    @Override
    public int updateUserAvatar(UserParam userParam){


        return update(userParam);
    }

    @Override
    public SysUser findUserByAccount(SysUser user) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getAccount,user.getAccount());
        queryWrapper.eq(SysUser::getId,user.getId());
        queryWrapper.last("limit 1");
        return this.sysUserMapper.selectOne(queryWrapper);
    }

    @Override
    public void save(SysUser sysUser) {
        //保存用户这 id会自动生成
        //这个地方 默认生成的id是 分布式id 雪花算法
        //mybatis-plus
        this.sysUserMapper.insert(sysUser);
    }
    public int update(UserParam userParam){
        String id= userParam.getId();
        String account= userParam.getAccount();
        String nickname= userParam.getNickname();
        String avatar= userParam.getAvatar();
        String email= userParam.getEmail();

        String mobilePhoneNumber= userParam.getMobilePhoneNumber();
        LambdaUpdateWrapper<SysUser> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();

        if((nickname==null||nickname.equals(""))&&(email==null||email.equals(""))){
            lambdaUpdateWrapper
                    .eq(SysUser::getAccount,account )
                    .eq(SysUser::getId,id)
                    .set(SysUser::getAvatar, avatar);

        }else if(nickname!=null&&email!=null){
            lambdaUpdateWrapper
                    .eq(SysUser::getAccount,account )
                    .eq(SysUser::getId,id)
                    .set(SysUser::getNickname, nickname)
                    .set(SysUser::getEmail, email)
                    .set(SysUser::getMobilePhoneNumber, mobilePhoneNumber);
        }

           int i=update(lambdaUpdateWrapper);

           if(i>0){
               //SET key-value 将更新数据
               //redis_db.set('user_name', 'bill');

           }
           return i;
    }

    @Override
    public int update(LambdaUpdateWrapper<SysUser> lambdaUpdateWrapper){

        return this.sysUserMapper.update(null,lambdaUpdateWrapper);
    };


}
