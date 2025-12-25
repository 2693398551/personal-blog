package com.myo.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.myo.blog.dao.pojo.SysUser;
import com.myo.blog.service.LoginService;
import com.myo.blog.service.SysUserService;
import com.myo.blog.utils.HttpContextUtils;
import com.myo.blog.utils.IpUtils;
import com.myo.blog.utils.JWTUtils;
import com.myo.blog.entity.ErrorCode;
import com.myo.blog.entity.Result;
import com.myo.blog.entity.params.LoginParam;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class LoginServiceImpl implements LoginService {

    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    private static final String slat = "myo!@#";

    @Override
    public Result login(LoginParam loginParam) {
        /**
         * 1. 检查参数是否合法
         * 3. 如果不存在 登录失败
         * 4. 如果存在 ，使用jwt 生成token 返回给前端
         * 5. token放入redis当中，redis  token：user信息 设置过期时间
         *  (登录认证的时候 先认证token字符串是否合法，去redis认证是否存在)

         * 2. 根据用户名和密码去user表中查询 是否存在         */
        String account = loginParam.getAccount();
        String password = loginParam.getPassword();
        if (StringUtils.isBlank(account) || StringUtils.isBlank(password)){
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(),ErrorCode.PARAMS_ERROR.getMsg());
        }
        password = DigestUtils.md5Hex(password + slat);

        SysUser sysUser = sysUserService.findUser(account,password);
        if (sysUser == null){
            return Result.fail(ErrorCode.ACCOUNT_PWD_NOT_EXIST.getCode(),ErrorCode.ACCOUNT_PWD_NOT_EXIST.getMsg());
        }

        String token = JWTUtils.createToken(sysUser.getId());

        //更新最后登录IP,并设置登录时间
        updateLoginInfo(sysUser.getId());
        //直接调用通用方法存储双向索引
        saveTokenToRedis(token, sysUser);

          return Result.success(token);
    }

    // 建议直接传 id 进来更新
    public void updateLoginInfo(Long userId){
        // 获取当前请求的 IP 地址
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        String ip= IpUtils.getIpAddr(request);
        // 创建一个只包含 ID 和需要更新字段的用户对象
        SysUser user = new SysUser();
        user.setId(userId);
        user.setLastIpaddr(ip);// 设置最后登录 IP
        user.setLastLogin(System.currentTimeMillis()); // 通常也会更新登录时间
        // 调用 Service 层去更新数据库
        this.sysUserService.updateById(user);
    }

    @Override
    public SysUser checkToken(String token) {
        if (StringUtils.isBlank(token)){
            return null;// Redis 中没有，token 无效
        }
        //校验 JWT 格式和签名 (JWTUtils.checkToken 即使过期时间很长，也会校验签名是否被篡改)
        Map<String, Object> stringObjectMap = JWTUtils.checkToken(token);
        if (stringObjectMap == null){
            return null;
        }
        // 【核心控制】：去 Redis 查。如果 Redis 里没了，就代表过期/退出了。
        String userJson = redisTemplate.opsForValue().get("TOKEN_" + token);
        if (StringUtils.isBlank(userJson)){
            return null;// Redis 没数据 = Token 失效
        }
        SysUser sysUser = JSON.parseObject(userJson, SysUser.class);
        // 续期 Redis（活跃用户保持登录）
        redisTemplate.expire("TOKEN_" + token, 3, TimeUnit.DAYS);
        redisTemplate.expire("USER_TOKEN_" + sysUser.getId(), 3, TimeUnit.DAYS);
        return sysUser;
    }

    @Override
    public Result logout(String token) {
        // 1. 先查一下这个 Token 是谁的 (为了删反向索引)
        // 如果你的 checkToken 逻辑里已经做了判空，这里可以直接用
        SysUser sysUser = checkToken(token);

        // 2. 删除 Token Key
        redisTemplate.delete("TOKEN_" + token);

        // 3. 如果能找到用户，把他的反向索引也删了
        if (sysUser != null) {
            redisTemplate.delete("USER_TOKEN_" + sysUser.getId());
        }

        return Result.success(null);
    }


    @Override
    public Result register(LoginParam loginParam) {
        /**
         * 1. 判断参数 是否合法
         * 2. 判断账户是否存在，存在 返回账户已经被注册
         * 3. 不存在，注册用户
         * 4. 生成token
         * 5. 存入redis 并返回
         * 6. 注意 加上事务，一旦中间的任何过程出现问题，注册的用户 需要回滚
         */
        String account = loginParam.getAccount();
        String password = loginParam.getPassword();
        String nickname = loginParam.getNickname();
        String email=loginParam.getEmail();
        if (StringUtils.isBlank(account)
                || StringUtils.isBlank(password)
                || StringUtils.isBlank(nickname)
        ){
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(),ErrorCode.PARAMS_ERROR.getMsg());
        }
        SysUser sysUser =  sysUserService.findUserByAccount(account);
        if (sysUser != null){
            return Result.fail(ErrorCode.ACCOUNT_EXIST.getCode(),"账户已经被注册了");
        }
        sysUser = new SysUser();
        sysUser.setNickname(nickname);
        sysUser.setAccount(account);
        sysUser.setPassword(DigestUtils.md5Hex(password+slat));
        sysUser.setCreateDate(System.currentTimeMillis());
        sysUser.setLastLogin(System.currentTimeMillis());
        sysUser.setAvatar("/static/img/tx.gif");
        sysUser.setAdmin(1); //1 为true
        sysUser.setDeleted(0); // 0 为false
        sysUser.setSalt("");
        sysUser.setStatus("");
        sysUser.setEmail(email);

        //获取request 设置IP地址
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        String ip= IpUtils.getIpAddr(request);
        sysUser.setIpaddr(ip);
        sysUser.setLastIpaddr("");
        this.sysUserService.save(sysUser);
        // 生成 Token
        String token = JWTUtils.createToken(sysUser.getId());
        // 调用通用方法存储双向索引
        saveTokenToRedis(token, sysUser);

        return Result.success(token);
    }

    @Override
    public Result kick(Long userId) {
        // 1. 先根据 用户ID 找到 Token
        String token = redisTemplate.opsForValue().get("USER_TOKEN_" + userId);

        if (StringUtils.isBlank(token)) {
            return Result.fail(ErrorCode.NO_LOGIN.getCode(), "该用户未登录或已下线");
        }

        // 2. 删除 正向索引 (TOKEN_xxxxx) -> 这一步真正让 Token 失效
        redisTemplate.delete("TOKEN_" + token);

        // 3. 删除 反向索引 (USER_TOKEN_xxxxx) -> 清理痕迹
        redisTemplate.delete("USER_TOKEN_" + userId);

        return Result.success("用户已强制下线");
    }


    /*
    正向索引 (验证身份用)
    这是系统每时每刻都在用的索引，没有它用户就没法登录。
    它的作用：拿着 令牌(Token) 找 人。
    使用场景：用户每次刷新页面、点赞、发文章时。后端需要知道“这个 Token 到底是谁？”
    Redis 结构：
    Key: TOKEN_ + eyJhbGciOiJIUz... (那串很长的 Token 字符串)
    Value: {"id":1001, "nickname":"myo", "avatar":"..."} (用户信息的 JSON)
    通俗比喻： 这就像酒店的门禁系统。 客人拿着 房卡 (Token) 刷门，系统读取房卡信息，确认这是 张三 (User Info)，然后开门。

    反向索引 (管理用户用)
    这是为了后台管理和安全控制专门增加的索引。
    它的作用：拿着 人(User ID) 找 令牌(Token)。
    使用场景：管理员踢人、修改密码强制下线、单点登录（挤掉旧设备）。
    Redis 结构：
    Key: USER_TOKEN_ + 1001 (用户的 ID)
    Value: eyJhbGciOiJIUz... (那串很长的 Token 字符串)
    通俗比喻： 这就像酒店前台的入住登记薄。 老板想把 张三 (User ID) 赶出去，但他手里没有张三的房卡。于是他查登记薄，找到张三拿的是 101号房卡 (Token)，然后通知门禁系统把 101号房卡 作废。*/
    /**
     * 辅助方法：将 Token 和用户信息存入 Redis (双向绑定)
     */
    private void saveTokenToRedis(String token, SysUser sysUser) {
        // 1. 正向索引：通过 Token 找 用户信息 (用于登录验证)，正向索引：我有房卡，我要进门 (平时用)
        redisTemplate.opsForValue().set("TOKEN_" + token, JSON.toJSONString(sysUser), 3, TimeUnit.DAYS);

        // 2. 反向索引：通过 用户ID 找 Token (用于踢人下线、单点登录)，反向索引：我有名字，我要查房卡 (踢人用)
        redisTemplate.opsForValue().set("USER_TOKEN_" + sysUser.getId(), token, 3, TimeUnit.DAYS);
    }

}
