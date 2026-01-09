package com.konnac.config;

import com.konnac.filter.LoginCheckFilter;
import com.konnac.mapper.UsersMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class FilterConfig {

    /**
     * 登录检查过滤器
     * LoginCheckFilter 不是 Spring Bean,但是需要使用Spring 管理UsersMapper查询数据库
     * 为过滤器注入依赖（UsersMapper）
     * 配置过滤器的拦截规则和执行顺序
     */
    @Bean
    public FilterRegistrationBean<LoginCheckFilter> loginCheckFilterRegistration(UsersMapper usersMapper) {
        // 1. 创建 LoginCheckFilter 实例
        LoginCheckFilter filter = new LoginCheckFilter();
        // 2. 注入 UsersMapper 依赖
        filter.setUsersMapper(usersMapper);

        // 3. 创建 FilterRegistrationBean 对象
        // FilterRegistrationBean 是 Spring 提供的工具类，用于配置和注册过滤器
        FilterRegistrationBean<LoginCheckFilter> registration = new FilterRegistrationBean<>();

        //设置过滤器实例
        registration.setFilter(filter);
        //设置拦截规则: 拦截所有请求
        registration.addUrlPatterns("/*");
        //设置过滤器的名称: 用于标识和管理过滤器，在日志和监控中可以看到这个名称
        registration.setName("LoginCheckFilter");
        //设置过滤器执行顺序最高: 确保在业务逻辑处理之前先验证用户身份
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        // 返回配置好的注册Bean: Spring 会自动将这个过滤器注册到 Servlet 容器
        return registration;
    }
}
