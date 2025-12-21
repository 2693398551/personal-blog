package com.myo.blog.controller;

import com.alibaba.fastjson.JSON;
import com.myo.blog.entity.Result;
import com.myo.blog.entity.params.CaptchaParam;
import com.wf.captcha.GifCaptcha;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import com.wf.captcha.utils.CaptchaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author : myo
 * @create 2023/8/3 21:39
 */
@RestController
@RequestMapping("yzm")
public class CaptchaController {
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @PostMapping("/captchaClass")
    public Result captchaClass() throws Exception {
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 5);
        /**
         * TYPE_DEFAULT	数字和字母混合
         * TYPE_ONLY_NUMBER	纯数字
         * TYPE_ONLY_CHAR	纯字母
         * TYPE_ONLY_UPPER	纯大写字母
         * TYPE_ONLY_LOWER	纯小写字母
         * TYPE_NUM_AND_UPPER	数字和大写字母
         * **/
        specCaptcha.setCharType(Captcha.TYPE_DEFAULT);
        String verCode = specCaptcha.text().toLowerCase();
        String key = "yzm_"+verCode;

        // 存入redis并设置过期时间为2分钟
        redisTemplate.opsForValue().set(key, verCode,2, TimeUnit.MINUTES);
        Map map =new HashMap();
        map.put("key", key);
        map.put("image", specCaptcha.toBase64());
        return Result.success(map);
    }


    @PostMapping("/login")
    public Result login(@RequestBody CaptchaParam captcha){
        String verKey=captcha.getVerKey();
        String verCode=captcha.getVerCode();
        // 获取redis中的验证码
        String redisCode = redisTemplate.opsForValue().get(verKey);

        if(redisCode==null){
            return Result.success("验证码已过期");
        }

        if (verCode==null||!redisCode.equals(verCode.trim().toLowerCase())) {
            return Result.success("验证码不正确");
        }
        // 判断验证码
        boolean t=redisCode.equals(verCode.trim().toLowerCase());


        if(t){
            //删除成功的验证码
            redisTemplate.delete("yzm_"+redisCode);
        }

        return Result.success("验证码正确");
    }




}

