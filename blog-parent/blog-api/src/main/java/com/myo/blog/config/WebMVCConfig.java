package com.myo.blog.config;

import com.myo.blog.handler.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMVCConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;//日志拦截

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        /** 解决跨域问题 **/
        registry.addMapping("/**");

    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        /* 拦截器配置 */
        //拦截test接口，拦截器主要用途：进行用户登录状态的拦截，日志的拦截等。
        registry.addInterceptor(loginInterceptor)
                /*指定拦截器要拦截的请求(支持*通配符)*/
                /*.addPathPatterns("/util/*")*/
                .addPathPatterns("/test")
                .addPathPatterns("/comments/create/change")
                .addPathPatterns("/articles/publish");
    }
}
