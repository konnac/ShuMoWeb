package com.konnac.filter;

import com.alibaba.fastjson.JSONObject;
import com.konnac.mapper.UsersMapper;
import com.konnac.pojo.Result;
import com.konnac.pojo.User;
import com.konnac.context.UserContext;
import com.konnac.utils.JwtUtils;
import com.konnac.utils.SpringContextUtils;
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

    // 这里需要注入UsersMapper，但由于Filter不是Spring管理的Bean，我们需要特殊处理
    // 方法1：使用Spring的AutowiredAnnotationBeanPostProcessor
    // 方法2：通过ApplicationContext获取
    // 方法3：在init方法中获取

    private UsersMapper usersMapper;

    @Override
    public void init(FilterConfig filterConfig){
        // 通过Spring工具类获取Bean
        usersMapper = SpringContextUtils.getBean(UsersMapper.class);
    }

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
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        // 1. 获取请求url
        String url = req.getRequestURL().toString();
        log.info("拦截的url：{}", url);

        // 2. 检查是否是白名单
        if (checkWhiteList(url)) {
            log.info("白名单请求，放行：{}", url);
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // 3. 判断请求url中是否包含login，如果包含，说明是登录操作，放行。
        if (url.contains("login")){
            log.info("登录操作放行");
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // 4. 获取请求头中的令牌（token）
        String jwt = req.getHeader("Authorization");
        
        // 处理Bearer前缀
        if (StringUtils.hasLength(jwt) && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }

        // 5. 判断令牌是否存在，如果不存在，返回错误结果（未登录）
        if (!StringUtils.hasLength(jwt)){
            log.info("请求头token为空, 返回未登录的信息");
            Result error = Result.error("NOT_LOGIN");
            String notLogin = JSONObject.toJSONString(error);
            resp.getWriter().write(notLogin);
            return;
        }

        // 6. 解析token，如果解析失败，返回错误结果（未登录）
        try {
            // 解析token验证有效性
            JwtUtils.parseJWT(jwt);

            // 从token中获取用户ID
            Integer userId = JwtUtils.getUserIdFromToken(jwt);
            if (userId == null) {
                log.warn("Token中未找到用户ID");
                throw new Exception("无效的token");
            }

            // 从token中获取用户角色
            String userRole = JwtUtils.getUserRoleFromToken(jwt);
            if (userRole == null){
                log.warn("Token中未找到用户角色");
                throw new Exception("无效的token");
            }


            // 查询用户信息（根据需要决定是否查询完整信息）
            User user = usersMapper.getUserById(userId);
            if (user == null) {
                log.warn("用户不存在: userId={}", userId);
                throw new Exception("用户不存在");
            }

            // 设置用户上下文
            UserContext.setCurrentUser(user);
            log.debug("设置用户上下文: userId={}, username={}, userRole={}", userId, user.getUsername(), userRole);

        } catch (Exception e) {
            log.info("令牌验证失败: {}", e.getMessage());
            Result error = Result.error("NOT_LOGIN");
            String notLogin = JSONObject.toJSONString(error);
            resp.getWriter().write(notLogin);
            return;
        }

        // 7. 放行
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