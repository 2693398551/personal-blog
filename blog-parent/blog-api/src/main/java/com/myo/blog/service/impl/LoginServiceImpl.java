package com.myo.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myo.blog.dao.mapper.SysUserMapper;
import com.myo.blog.dao.mapper.UserTokenMapper;
import com.myo.blog.dao.pojo.SysUser;
import com.myo.blog.dao.pojo.UserToken;
import com.myo.blog.service.LoginService;
import com.myo.blog.service.MailService;
import com.myo.blog.service.SysUserService;
import com.myo.blog.utils.HttpContextUtils;
import com.myo.blog.utils.IpUtils;
import com.myo.blog.utils.JWTUtils;
import com.myo.blog.entity.ErrorCode;
import com.myo.blog.entity.Result;
import com.myo.blog.entity.params.LoginParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final SysUserService sysUserService;
    private final RedisTemplate<String,String> redisTemplate;
    private final SysUserMapper sysUserMapper;

    private static final int MAX_FAIL_COUNT = 10;           // 最大连续失败次数
    private static final long LOCK_DURATION_MS = 10 * 60 * 1000L; // 锁定时长：10分钟

    // 注入邮件发送器
    private final JavaMailSender mailSender;
    // 注入邮件服务类
    private final MailService mailService;

    // === 注入 UserTokenMapper ===

    private final UserTokenMapper userTokenMapper;

    // 从配置文件读取发件人，防止硬编码
    @Value("${spring.mail.username}")
    private String fromEmail;
    // 密码盐值，用于密码加密
    private static final String slat = "myo!@#";
    /**
     * 登录功能
     * @param loginParam
     * @return
     */
    @Override
    public Result login(LoginParam loginParam) {
        String account = loginParam.getAccount();
        String password = loginParam.getPassword();

        if (StringUtils.isBlank(account) || StringUtils.isBlank(password)) {
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), ErrorCode.PARAMS_ERROR.getMsg());
        }

        // 1. 先查账号是否存在（不验证密码，用于判断锁定状态）
        SysUser sysUser = sysUserService.findUserByAccount(account);

        // 2. 账号存在时，检查是否处于锁定期
        if (sysUser != null && sysUser.getLockTime() != null) {
            long now = System.currentTimeMillis();
            if (sysUser.getLockTime() > now) {
                // 还在锁定期，计算剩余分钟数
                long remainMinutes = (sysUser.getLockTime() - now) / 1000 / 60 + 1;
                return Result.fail(ErrorCode.PARAMS_ERROR.getCode(),
                        "账号已被临时锁定，请 " + remainMinutes + " 分钟后再试");
            } else {
                // 锁定已过期，自动解锁（清零失败次数和锁定时间）
                SysUser unlock = new SysUser();
                unlock.setId(sysUser.getId());
                unlock.setLockTime(null);
                unlock.setLoginFailCount(0);
                unlock.setUpdateDate(System.currentTimeMillis());
                sysUserService.updateById(unlock);
                sysUser.setLockTime(null);
                sysUser.setLoginFailCount(0);
            }
        }

        // 3. 验证账号 + 密码
        password = DigestUtils.md5Hex(password + slat);
        SysUser validUser = sysUserService.findUser(account, password);

        // 4. 账号或密码错误
        if (validUser == null) {
            // 账号存在才累计失败次数，账号不存在直接返回（防止用户枚举）
            if (sysUser != null) {
                int failCount = (sysUser.getLoginFailCount() == null ? 0 : sysUser.getLoginFailCount()) + 1;
                SysUser failUpdate = new SysUser();
                failUpdate.setId(sysUser.getId());
                failUpdate.setLoginFailCount(failCount);
                failUpdate.setUpdateDate(System.currentTimeMillis());

                // 达到最大失败次数，写入锁定时间
                if (failCount >= MAX_FAIL_COUNT) {
                    failUpdate.setLockTime(System.currentTimeMillis() + LOCK_DURATION_MS);
                    sysUserService.updateById(failUpdate);
                    return Result.fail(ErrorCode.PARAMS_ERROR.getCode(),
                            "密码错误次数过多，账号已被锁定 10 分钟");
                }

                sysUserService.updateById(failUpdate);
                int remain = MAX_FAIL_COUNT - failCount;
                return Result.fail(ErrorCode.ACCOUNT_PWD_NOT_EXIST.getCode(),
                        "用户名或密码不存在，还可尝试 " + remain + " 次");
            }
            return Result.fail(ErrorCode.ACCOUNT_PWD_NOT_EXIST.getCode(),
                    ErrorCode.ACCOUNT_PWD_NOT_EXIST.getMsg());
        }

        // 5. 账号封禁检查
        if (validUser.getStatus() != null && validUser.getStatus() == 99) {
            // 检查是否临时封禁已到期，自动解封
            if (validUser.getBanExpireTime() != null
                    && validUser.getBanExpireTime() < System.currentTimeMillis()) {
                SysUser unban = new SysUser();
                unban.setId(validUser.getId());
                unban.setStatus(0);
                unban.setBanExpireTime(null);
                unban.setUpdateDate(System.currentTimeMillis());
                sysUserService.updateById(unban);
            } else {
                String banMsg = "账号已被封禁，请联系管理员";
                if (validUser.getBanExpireTime() != null) {
                    long remainDays = (validUser.getBanExpireTime() - System.currentTimeMillis()) / 1000 / 60 / 60 / 24 + 1;
                    banMsg = "账号已被封禁，还有 " + remainDays + " 天解封";
                }
                return Result.fail(ErrorCode.ACCOUNT_DISABLED.getCode(), banMsg);
            }
        }

        // 6. 登录成功：清零失败次数，更新登录信息
        String token = JWTUtils.createToken(validUser.getId());
        updateLoginInfo(validUser.getId());
        saveToken(token, validUser);

        return Result.success(token);
    }

    /**
     * 登录成功后更新登录信息
     * 同时清零失败次数、锁定时间，更新最后登录 IP、时间、update_date
     */
    public void updateLoginInfo(String userId) {
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        String ip = IpUtils.getIpAddr(request);

        SysUser user = new SysUser();
        user.setId(userId);
        user.setLastIpaddr(ip);
        user.setLastLogin(System.currentTimeMillis());
        user.setUpdateDate(System.currentTimeMillis());
        // 登录成功，清零失败计数和锁定时间
        user.setLoginFailCount(0);
        user.setLockTime(null);

        this.sysUserService.updateById(user);
    }
    /**
     * 校验 Token (包含灾难恢复逻辑)
     */
    @Override
    public SysUser checkToken(String token) {
        if (StringUtils.isBlank(token)){
            return null;
        }
        Map<String, Object> stringObjectMap = JWTUtils.checkToken(token);
        if (stringObjectMap == null){
            return null;
        }

        // 1. 先查 Redis
        String userJson = redisTemplate.opsForValue().get("TOKEN:" + token);
        if (StringUtils.isNotBlank(userJson)){
            // Redis 命中，正常返回并续期
            SysUser sysUser = JSON.parseObject(userJson, SysUser.class);
            redisTemplate.expire("TOKEN:" + token, 3, TimeUnit.DAYS);
            redisTemplate.expire("USER_TOKEN:" + sysUser.getId(), 3, TimeUnit.DAYS);

            return sysUser;
        }

        // 2. Redis 未命中，尝试从数据库恢复 (灾难恢复)
        return checkTokenFromDb(token);
    }

    /**
     * 辅助方法：从数据库恢复 Token
     */
    private SysUser checkTokenFromDb(String token) {
        LambdaQueryWrapper<UserToken> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserToken::getToken, token);
        UserToken userToken = userTokenMapper.selectOne(wrapper);

        // 如果数据库也没有，或者已过期
        if (userToken == null || userToken.getExpireTime() < System.currentTimeMillis()) {
            return null;
        }

        // 数据库有记录 -> 复活 Session
        String userId = userToken.getUserId();
        SysUser sysUser = sysUserService.findUserById(userId);

        // 重新写回 Redis
        saveTokenToRedis(token, sysUser);

        log.info("触发灾难恢复机制：从数据库恢复了用户会话 - 用户ID: {}", userId);
        return sysUser;
    }

    @Override
    public Result logout(String token) {
        // 先获取用户，方便后续删数据库
        SysUser sysUser = checkToken(token);

        // 1. 删 Redis
        redisTemplate.delete("TOKEN:" + token);
        if (sysUser != null) {
            redisTemplate.delete("USER_TOKEN:" + sysUser.getId());

            // 2. 删 MySQL
            userTokenMapper.deleteById(sysUser.getId());
        }
        return Result.success(null);
    }

    // ================== 【发送验证码】 ==================
    /**
     * 发送验证码接口（优化版）
     */
    public Result sendEmailCode(String email) {
        log.debug("开始发送邮箱验证码 - 邮箱: {}", email);

        if (StringUtils.isBlank(email)) {
            log.warn("邮箱验证码发送失败 - 邮箱为空");
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), "邮箱不能为空");
        }

        String code = String.valueOf(new Random().nextInt(899999) + 100000);
        log.debug("验证码生成完成 - 邮箱: {}, 验证码: {}", email, code);

        redisTemplate.opsForValue().set("REGISTER_CODE_" + email, code, 5, TimeUnit.MINUTES);
        log.debug("验证码存入Redis完成 - 邮箱: {}, 有效期: 5分钟", email);

        mailService.sendMailAsync(
                email,
                "【月之别邸】注册验证码",
                "欢迎来到月之别邸，您的注册验证码是：" + code + "。有效期 5 分钟，请勿泄露。"
        );

        log.info("邮箱验证码发送成功 - 邮箱: {}", email);
        return Result.success("验证码已发送");
    }


    // ================== 【注册逻辑】 ==================
    @Override
    public Result register(LoginParam loginParam) {

        String account  = loginParam.getAccount();
        String password = loginParam.getPassword();
        String nickname = loginParam.getNickname();
        Integer sex     = loginParam.getSex();
        String email    = loginParam.getEmail();
        String code     = loginParam.getCode();
        String avatar   = loginParam.getAvatar();

        if (StringUtils.isBlank(account)
                || StringUtils.isBlank(password)
                || StringUtils.isBlank(nickname)
                || StringUtils.isBlank(email)
                || StringUtils.isBlank(code)
                || (sex != 0 && sex != 1 && sex != 2)
        ) {
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), ErrorCode.PARAMS_ERROR.getMsg());
        }

        // 校验邮箱验证码
        String redisCode = redisTemplate.opsForValue().get("REGISTER_CODE_" + email);
        if (StringUtils.isBlank(redisCode)) {
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), "验证码已过期或未获取");
        }
        if (!redisCode.equals(code)) {
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), "验证码错误");
        }

        // 账号唯一性检查
        SysUser exist = sysUserService.findUserByAccount(account);
        if (exist != null) {
            return Result.fail(ErrorCode.ACCOUNT_EXIST.getCode(), "账户已经被注册了");
        }

        long now = System.currentTimeMillis();

        SysUser sysUser = new SysUser();
        sysUser.setNickname(nickname);
        sysUser.setAccount(account);
        sysUser.setSex(sex);
        sysUser.setPassword(DigestUtils.md5Hex(password + slat));
        sysUser.setAvatar(avatar);
        sysUser.setEmail(email);
        sysUser.setDeleted(0);
        sysUser.setSalt("");
        sysUser.setStatus(0);
        sysUser.setLastIpaddr("");

        // ===== 新增字段 =====
        sysUser.setSource(1);           // 注册来源：1=账号密码注册
        sysUser.setCreateDate(now);
        sysUser.setLastLogin(now);
        sysUser.setUpdateDate(now);     // 最后更新时间
        sysUser.setPwdUpdateDate(now);  // 密码设置时间（首次注册即为设密时间）
        sysUser.setLoginFailCount(0);   // 初始化失败次数为 0
        // ====================

        this.sysUserService.save(sysUser);

        // 分配普通用户角色（id=4）
        sysUserMapper.insertUserRole(sysUser.getId(), 4L);

        // 删除验证码
        redisTemplate.delete("REGISTER_CODE_" + email);

        // 自动登录，返回 token
        String token = JWTUtils.createToken(sysUser.getId());
        saveToken(token, sysUser);

        return Result.success(token);
    }

    @Override
    public Result kick(Long userId) {
        // 1. 尝试从 Redis 获取 Token
        String token = redisTemplate.opsForValue().get("USER_TOKEN:" + userId);

        // 2. 如果 Redis 挂了或空的，尝试从 DB 捞一下 Token (为了能删掉它)
        if (StringUtils.isBlank(token)) {
            UserToken userToken = userTokenMapper.selectById(userId);
            if (userToken != null) {
                token = userToken.getToken();
            }
        }

        if (StringUtils.isBlank(token)) {
            return Result.fail(ErrorCode.NO_LOGIN.getCode(), "该用户未登录或已下线");
        }

        // 3. 删 Redis
        redisTemplate.delete("TOKEN:" + token);
        redisTemplate.delete("USER_TOKEN:" + userId);

        // 4. 删 MySQL
        userTokenMapper.deleteById(userId);

        return Result.success("用户已强制下线");
    }

    /**
     * 统一保存 Token 到 Redis 和 MySQL
     */
    private void saveToken(String token, SysUser sysUser) {
        // 1. 存 Redis (保持原有的 3 天过期)
        saveTokenToRedis(token, sysUser);

        // 2. 存 MySQL (作为持久化备份)
        UserToken userToken = new UserToken();
        userToken.setUserId(sysUser.getId());
        userToken.setToken(token);
        // 过期时间设为 3 天 (毫秒)
        userToken.setExpireTime(System.currentTimeMillis() + 3L * 24 * 60 * 60 * 1000);

        // 如果存在则更新，不存在则插入
        UserToken exist = userTokenMapper.selectById(sysUser.getId());
        if (exist != null) {
            userTokenMapper.updateById(userToken);
        } else {
            userTokenMapper.insert(userToken);
        }
    }

    private void saveTokenToRedis(String token, SysUser sysUser) {
        redisTemplate.opsForValue().set("TOKEN:" + token, JSON.toJSONString(sysUser), 3, TimeUnit.DAYS);
        redisTemplate.opsForValue().set("USER_TOKEN:" + sysUser.getId(), token, 3, TimeUnit.DAYS);

    }
}