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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Transactional // 2. 类级别开启事务，确保数据一致性
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
    /*
      @Transactional：
     *
     * 加在了类名上方。

     * 作用：如果 sysUserMapper.update 成功了，但假设后面的代码抛出了异常，数据库的操作会自动回滚，保证数据不会脏读。

     * updateRedisCache 方法 (解决缓存不同步)：

     * 这是一个关键的辅助方法。

     * 逻辑：它利用了我们之前建立的 反向索引 (USER_TOKEN_userId)。

     * 它拿到 Token 后，重新把数据库里最新的 SysUser 对象序列化成 JSON，覆盖掉 Redis 里旧的 JSON。

     * 这样，用户一下次刷新页面，checkToken 从 Redis 拿到的就是新名字、新头像了！
      */

    /**
     * 修改用户信息 (资料更新)
     * 注意：不包含密码修改，密码修改建议单独走 changePassword 接口
     */
    @Override
    public int updateUser(UserParam userParam) {
        String id = userParam.getId();

        // 1. 参数校验 (防止 ID 为空)
        if (StringUtils.isBlank(id)) {
            // 可以在这里抛出自定义业务异常
            return 0;
        }

        // 2. 组装动态 SQL
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getId, id);

        boolean hasUpdate = false;

        if (StringUtils.isNotBlank(userParam.getNickname())) {
            updateWrapper.set(SysUser::getNickname, userParam.getNickname());
            hasUpdate = true;
        }
        if (StringUtils.isNotBlank(userParam.getAvatar())) {
            updateWrapper.set(SysUser::getAvatar, userParam.getAvatar());
            hasUpdate = true;
        }
        if (StringUtils.isNotBlank(userParam.getEmail())) {
            // 优化：可以在这里先查一下邮箱是否已被其他人占用
            // checkEmailExist(userParam.getEmail());
            updateWrapper.set(SysUser::getEmail, userParam.getEmail());
            hasUpdate = true;
        }
        if (StringUtils.isNotBlank(userParam.getMobilePhoneNumber())) {
            updateWrapper.set(SysUser::getMobilePhoneNumber, userParam.getMobilePhoneNumber());
            hasUpdate = true;
        }

        // 3. 执行数据库更新
        if (hasUpdate) {
            int rows = this.sysUserMapper.update(null, updateWrapper);

            // 4. 【核心修复】Redis 缓存同步
            if (rows > 0) {
                updateRedisCache(Long.parseLong(id));
            }
            return rows;
        }

        return 0;
    }

    /**
     * 辅助方法：更新 Redis 中的用户信息
     */
    private void updateRedisCache(Long userId) {
        // A. 通过反向索引找到 Token
        String token = redisTemplate.opsForValue().get("USER_TOKEN_" + userId);

        if (StringUtils.isNotBlank(token)) {
            // B. 从数据库查询最新的用户信息 (确保是全量最新数据)
            SysUser newestUser = sysUserMapper.selectById(userId);

            if (newestUser != null) {
                // C. 覆盖 Redis 中的旧数据 (保持过期时间一致，或重置)
                // 这里我们简单起见，重置为 3 天，或者读取旧 Key 的 TTL
                redisTemplate.opsForValue().set("TOKEN_" + token, JSON.toJSONString(newestUser), 3, TimeUnit.DAYS);
            }
        }
    }


    @Override
    public int updateUserAvatar(UserParam userParam){


        return updateUser(userParam);
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
    // 引入 StringUtils 工具类 (org.apache.commons.lang3.StringUtils)





    @Override
    public boolean updateById(SysUser sysUser) {
        // 根据用户ID更新用户信息
        // 使用MyBatis-Plus的updateById方法，根据实体类主键进行更新
        // 注意：sysUser对象必须包含有效的ID字段值
        return this.sysUserMapper.updateById(sysUser) > 0;
    }




}
