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
        // 作用：在请求处理完成后清理 UserContext，防止 ThreadLocal 内存泄漏
        registry.addInterceptor(new UserContextCleanupInterceptor())
                .addPathPatterns("/**"); // 对所有请求生效
    }

    /**
     * 流程:
     *     ↓
     * 携带 Authorization: Bearer token
     *     ↓
     * LoginCheckFilter.doFilter()
     *     ↓
     * 解析token，获取userId=1  ← 还是同一个用户
     *     ↓
     * UserContext.setCurrentUserId(1)  ← 重新设置
     *     ↓
     * PermissionAspect.checkPermission()
     */
}