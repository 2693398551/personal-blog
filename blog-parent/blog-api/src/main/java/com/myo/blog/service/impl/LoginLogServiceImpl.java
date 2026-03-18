package com.myo.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myo.blog.dao.mapper.LoginLogMapper;
import com.myo.blog.dao.mapper.SysUserMapper;
import com.myo.blog.dao.pojo.LoginLog;
import com.myo.blog.entity.LoginLogVo;
import com.myo.blog.entity.Result;
import com.myo.blog.entity.params.PageParams;
import com.myo.blog.service.LoginLogService;
import com.myo.blog.utils.IpUtils;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {

    private final LoginLogMapper loginLogMapper;
    private final SysUserMapper sysUserMapper;

    /**
     * 异步记录登录日志，@Async 依赖项目已有的 taskExecutor 线程池
     */
    @Override
    @Async("taskExecutor")
    public void record(String ip, String userAgent, String userId,
                       String account, int status, String msg) {
        try {
            LoginLog loginLog = new LoginLog();
            loginLog.setUserId(userId);
            loginLog.setAccount(account);
            loginLog.setStatus(status);
            loginLog.setMsg(msg);
            loginLog.setCreateDate(System.currentTimeMillis());

            // 直接使用传进来的 ip 字符串，不再使用 request
            loginLog.setIp(ip);

            // 查询 IP 归属地（失败时静默处理）
            try {
                loginLog.setIpLocation(IpUtils.getCityInfo(ip));
            } catch (Exception ignored) {}

            // 直接使用传进来的 userAgent 字符串解析
            try {
                if (StringUtils.isNotBlank(userAgent)) {
                    UserAgent ua = UserAgent.parseUserAgentString(userAgent);
                    Browser browser = ua.getBrowser();
                    OperatingSystem os = ua.getOperatingSystem();
                    loginLog.setBrowser(browser != null ? browser.getName() : null);
                    loginLog.setOs(os != null ? os.getName() : null);
                }
            } catch (Exception ignored) {}

            loginLogMapper.insert(loginLog);
        } catch (Exception e) {
            log.error("[登录日志] 记录失败: {}", e.getMessage());
        }
    }

    @Override
    public Result listLog(PageParams pageParams) {
        Page<LoginLog> page = new Page<>(pageParams.getPage(), pageParams.getPageSize());
        LambdaQueryWrapper<LoginLog> qw = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(pageParams.getKeyword())) {
            qw.like(LoginLog::getAccount, pageParams.getKeyword());
        }
        if (pageParams.getStatus() != null) {
            qw.eq(LoginLog::getStatus, pageParams.getStatus());
        }
        qw.orderByDesc(LoginLog::getCreateDate);
        loginLogMapper.selectPage(page, qw);

        List<LoginLog> records = page.getRecords();
        if (records.isEmpty()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("records", Collections.emptyList());
            empty.put("total", 0);
            return Result.success(empty);
        }

        // 批量查角色名：收集有 userId 的记录
        Set<String> userIds = records.stream()
                .map(LoginLog::getUserId)
                .filter(id -> id != null && !id.isEmpty())
                .collect(Collectors.toSet());

        // userId → 角色名称（取第一个角色）
        Map<String, String> roleMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (String uid : userIds) {
                List<String> roles = sysUserMapper.findRoleNamesByUserId(uid);
                roleMap.put(uid, (roles != null && !roles.isEmpty()) ? roles.get(0) : "普通用户");
            }
        }

        // 组装 VO
        List<LoginLogVo> voList = records.stream().map(r -> {
            LoginLogVo vo = new LoginLogVo();
            BeanUtils.copyProperties(r, vo);
            // 角色名
            if (r.getUserId() != null && roleMap.containsKey(r.getUserId())) {
                vo.setRoleName(roleMap.get(r.getUserId()));
            } else {
                vo.setRoleName("未知账号");
            }
            // 归属地格式化
            vo.setIpLocation(formatLocation(r.getIpLocation(), r.getIp()));
            return vo;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", voList);
        result.put("total", page.getTotal());
        return Result.success(result);
    }

    /**
     * 归属地格式化
     * ip2region 原始格式：国家|区域|省份|城市|ISP，例如：中国|0|广东省|深圳市|电信
     * 内网/本地 IP 直接返回「内网」
     * 公网 IP 返回：「中国 广东 深圳 · 电信」
     */
    private String formatLocation(String location, String ip) {
        // 本地回环 & 内网 IP
        if (ip == null || ip.equals("127.0.0.1")
                || ip.startsWith("192.168.")
                || ip.startsWith("10.")
                || ip.startsWith("172.")) {
            return "内网";
        }
        if (location == null || location.isEmpty()
                || location.contains("内网IP")
                || location.startsWith("0|")) {
            return "内网";
        }
        // 解析 ip2region 格式
        String[] parts = location.split("\\|");
        // parts: [国家, 区域, 省份, 城市, ISP]
        List<String> addr = new ArrayList<>();
        List<String> isp  = new ArrayList<>();
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i].trim();
            if (p.isEmpty() || p.equals("0")) continue;
            if (i == 4) {
                isp.add(p); // 最后一个是运营商
            } else {
                // 去掉「省」「市」「自治区」等后缀，保持简洁
                p = p.replaceAll("(省|市|自治区|特别行政区|壮族|维吾尔|回族)$", "");
                addr.add(p);
            }
        }
        String addrStr = String.join(" ", addr);
        String ispStr  = isp.isEmpty() ? "" : " · " + String.join("", isp);
        return addrStr + ispStr;
    }
}