package com.myo.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myo.blog.dao.mapper.WebInformationMapper;
import com.myo.blog.dao.pojo.VisitStats;
import com.myo.blog.dao.pojo.WebInformation;
import com.myo.blog.entity.Result;
import com.myo.blog.entity.params.VisitParam;
import com.myo.blog.service.LoginService;
import com.myo.blog.service.VisitStatsService;
import com.myo.blog.utils.IpUtils;
import com.myo.blog.utils.UserAgentUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/visit")
@RequiredArgsConstructor
public class VisitStatsController {

    private final VisitStatsService    visitStatsService;
    private final WebInformationMapper webInformationMapper;
    private final LoginService         loginService;
    private final UserAgentUtils       userAgentUtils;

    /**
     * 前端路由埋点接口
     * POST /visit/record
     */
    @PostMapping("/record")
    public Result record(@RequestBody Map<String, String> body,
                         HttpServletRequest request,
                         HttpServletResponse response) {

        VisitParam param = buildParam(body.get("uri"), request, response);
        visitStatsService.record(param);
        return Result.success(null);
    }

    /**
     * 获取网站累计访问量（前台底部展示）
     * GET /visit/total
     */
    @GetMapping("/total")
    public Result totalStats() {
        WebInformation webInfo = webInformationMapper.selectOne(
                new LambdaQueryWrapper<WebInformation>().last("LIMIT 1"));

        if (webInfo == null || webInfo.getShowVisitorCount() == null
                || webInfo.getShowVisitorCount() == 0) {
            return Result.success(null);
        }

        VisitStats total = visitStatsService.getTotalStats();
        if (total == null) {
            return Result.success(Map.of("pv", 0L, "uv", 0L));
        }
        return Result.success(Map.of(
                "pv", total.getPv()  != null ? total.getPv()  : 0L,
                "uv", total.getUv()  != null ? total.getUv()  : 0L
        ));
    }

    // ================================================================
    //  私有：提取 HTTP 数据，组装 VisitParam
    //  所有 HTTP 相关操作都在 Controller 层完成
    //  Service 和 Listener 完全不感知 HTTP
    // ================================================================

    private VisitParam buildParam(String uri, HttpServletRequest request,
                                  HttpServletResponse response) {
        VisitParam param = new VisitParam();

        // ---- URI ----
        param.setUri(uri);

        // ---- behavior & content（根据 URI 推断，在 Controller 层就确定好）----
        param.setBehavior(resolveBehavior(uri));
        param.setContent(resolveContent(uri));

        // ---- UUID ----
        String uuid = request.getHeader("Visitor-UUID");
        if (!hasText(uuid)) {
            uuid = UUID.randomUUID().toString().replace("-", "");
            param.setNewVisitor(true);
            response.setHeader("Set-Visitor-UUID", uuid);
            response.addHeader("Access-Control-Expose-Headers", "Set-Visitor-UUID");
        }
        param.setVisitorUuid(uuid);

        // ---- 登录用户 ----
        try {
            String token = request.getHeader("Authorization");
            if (hasText(token)) {
                var user = loginService.checkToken(token);
                if (user != null) param.setUserId(user.getId());
            }
        } catch (Exception ignored) {}

        // ---- IP 解析 ----
        String ip             = IpUtils.getIpAddr(request);
        IpUtils.Region region = IpUtils.search(ip);
        param.setIp(ip);
        param.setIpLocation(IpUtils.getIp2region(ip));
        param.setProvince(region != null ? region.getProvince() : null);
        param.setCity(region != null ? region.getCity() : null);

        // ---- UA 解析 ----
        String uaStr = request.getHeader("User-Agent");
        param.setOs(userAgentUtils.getOs(uaStr));
        param.setBrowser(userAgentUtils.getBrowser(uaStr));

        // ---- 来源 & 时间 ----
        param.setReferer(request.getHeader("Referer"));
        param.setCreateDate(System.currentTimeMillis());

        return param;
    }

    private String resolveBehavior(String uri) {
        if (uri == null) return "PAGE_VIEW";
        if (uri.matches("/view/.*"))           return "VIEW_ARTICLE";
        if (uri.matches("/(category|tag)/.*")) return "VIEW_CATEGORY";
        return "PAGE_VIEW";
    }

    private String resolveContent(String uri) {
        if (uri == null) return "未知页面";
        if (uri.equals("/"))             return "首页";
        if (uri.matches("/view/.*"))     return "文章详情页";
        if (uri.matches("/articles.*"))  return "文章列表页";
        if (uri.matches("/category/.*")) return "分类页";
        if (uri.matches("/tag/.*"))      return "标签页";
        if (uri.equals("/messageBoard")) return "留言板";
        if (uri.equals("/nav"))          return "导航页";
        if (uri.equals("/Resume"))       return "关于页";
        if (uri.matches("/space/.*"))    return "用户空间";
        if (uri.equals("/login"))        return "登录页";
        if (uri.equals("/register"))     return "注册页";
        return uri;
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}