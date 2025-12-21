package com.myo.blog.utils;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author : myo
 * @create 2023/8/2 10:38
 */
@Configuration
public class CommonConfig {
        @Bean
        public MultipartConfigElement multipartConfigElement() {
            MultipartConfigFactory factory = new MultipartConfigFactory();
            factory.setMaxFileSize(DataSize.ofBytes(200 * 1024 * 1024)); // 限制上传文件大小
            return factory.createMultipartConfig();
        }
}


