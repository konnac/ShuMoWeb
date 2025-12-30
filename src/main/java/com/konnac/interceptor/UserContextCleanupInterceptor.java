package com.konnac.interceptor;

import com.konnac.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class UserContextCleanupInterceptor implements HandlerInterceptor {

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            // 在请求处理完成后清理UserContext，防止ThreadLocal内存泄漏
            UserContext.clear();
            log.debug("请求处理完成，清理用户上下文");
        } catch (Exception e) {
            log.error("清理用户上下文时发生异常", e);
        }
    }
}