package com.konnac.config;

import com.konnac.filter.LoginCheckFilter;
import com.konnac.mapper.UsersMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<LoginCheckFilter> loginCheckFilterRegistration(UsersMapper usersMapper) {
        LoginCheckFilter filter = new LoginCheckFilter();
        filter.setUsersMapper(usersMapper);
        
        FilterRegistrationBean<LoginCheckFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setName("LoginCheckFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
