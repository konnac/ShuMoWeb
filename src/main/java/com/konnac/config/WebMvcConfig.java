package com.konnac.config;

import com.konnac.interceptor.UserContextCleanupInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册UserContextCleanupInterceptor，用于在请求处理完成后清理UserContext
        registry.addInterceptor(new UserContextCleanupInterceptor())
                .addPathPatterns("/**"); // 对所有请求生效
    }
}