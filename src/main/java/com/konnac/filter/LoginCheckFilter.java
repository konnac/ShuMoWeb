package com.konnac.filter;

import com.alibaba.fastjson.JSONObject;
import com.konnac.pojo.Result;
import com.konnac.utils.JwtUtils;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;


import java.io.IOException;

@Slf4j
@WebFilter("/*")
public class LoginCheckFilter implements Filter {
    // 白名单列表
    private static final String[] WHITE_LIST = {
            "/login",
            "/register",
            "/captcha",
            "/doc.html",
            "/webjars/.*",
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-ui.html",
            "/favicon.ico"
    };
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        //1. 获取请求url。 request可以获取请求参数 response可以响应结果
        String url = req.getRequestURL().toString();
        log.info("拦截的url：{}", url);

        // 2. 检查是否是白名单
        if (checkWhiteList(url)) {
            log.info("白名单请求，放行：{}", url);
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        //2. 判断请求url中是否包含login，如果包含，说明是登录操作，放行。
        if (url.contains("login")){
            log.info("登录操作放行");
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        //3. 获取请求头中的令牌（token）。
        String jwt = req.getHeader("token");

        //4. 判断令牌是否存在，如果不存在，返回错误结果（未登录）。
        if (!StringUtils.hasLength(jwt)){
            log.info("请求头token为空, 返回未登录的信息");
            Result error = Result.error("NOT_LOGIN");
            //手动转换 对象--> json-->阿里巴巴 fastjson
            String notLogin = JSONObject.toJSONString(error);
            resp.getWriter().write(notLogin);
            return;
        }

        //5. 解析token，如果解析失败，返回错误结果（未登录）。
        try {
            JwtUtils.parseJWT(jwt);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("解析令牌失败，返回未登录信息");
            Result error = Result.error("NOT_LOGIN");
            //手动转换 对象--> json-->阿里巴巴 fastjson
            String notLogin = JSONObject.toJSONString(error);
            resp.getWriter().write(notLogin);
            return;
        }
        //6. 放行。
        log.info("令牌合法，放行");
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean checkWhiteList(String url) {
        for (String whiteUrl : WHITE_LIST){
            if (url.contains(whiteUrl)){
                return true;
            }
        }
        return false;
    }
}
