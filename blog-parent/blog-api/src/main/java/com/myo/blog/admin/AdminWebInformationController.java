package com.myo.blog.admin;

import com.myo.blog.common.aop.LogAnnotation;
import com.myo.blog.common.aop.RequirePermission;
import com.myo.blog.dao.pojo.WebInformation;
import com.myo.blog.entity.Result;
import com.myo.blog.service.WebInformationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/webInfo")
@RequiredArgsConstructor
public class AdminWebInformationController {

    private final WebInformationService webInformationService;
    @GetMapping
    @LogAnnotation(module = "网站信息", operator = "更新")
    @RequirePermission("webInfo:view")
    public Result getWebInfo() {
        return webInformationService.getWebInfo();
    }

    // 后台管理端更新网站信息的接口 (建议在你的 AdminInterceptor 中对此路径做管理员权限校验)
    @PostMapping("/update")
    @LogAnnotation(module = "网站信息", operator = "更新")
    @RequirePermission("webInfo:edit")
    public Result updateWebInfo(@RequestBody WebInformation webInformation) {
        return webInformationService.updateWebInfo(webInformation);
    }

}
