package com.konnac.filter;

import com.alibaba.fastjson.JSONObject;
import com.konnac.mapper.UsersMapper;
import com.konnac.pojo.Result;
import com.konnac.pojo.User;
import com.konnac.context.UserContext;
import com.konnac.utils.JwtUtils;
import com.konnac.utils.SpringContextUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Slf4j
public class LoginCheckFilter implements Filter {

    private UsersMapper usersMapper;

    public void setUsersMapper(UsersMapper usersMapper) {
        this.usersMapper = usersMapper;
    }

    // 白名单列表 其实也就只用上了登录
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
        HttpServletRequest req = (HttpServletRequest) servletRequest; //强转请求,转换后可以访问HTTP请求的特定功能
        HttpServletResponse resp = (HttpServletResponse) servletResponse; //强转响应,转换后可以访问HTTP响应的特定功能

        // 1. 获取请求url
        String url = req.getRequestURL().toString(); //获取请求的URL转换成字符串
        log.info("拦截的url：{}", url);

        // 2. 检查是否是白名单
        if (checkWhiteList(url)) {
            log.info("白名单请求，放行：{}", url);
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // 3. 判断请求url中是否包含login，如果包含，说明是登录操作，放行。(白名单里有了,但是没改)
        if (url.contains("login")){
            log.info("登录操作放行");
            filterChain.doFilter(servletRequest, servletResponse); //放行
            return; //跳过后面的过滤器，直接返回结果
        }

        //前端发送的氢气示例

        //headers: {
        //    'Authorization': `Bearer ${token}`  // 必须使用 "Authorization"
        //  }

        // 4. 获取请求头中的令牌（token）
        String jwt = req.getHeader("Authorization"); //用于身份认证的请求头
        
        // 处理Bearer前缀
        if (StringUtils.hasLength(jwt) && jwt.startsWith("Bearer ")) {
            //截取第七个字符后的内容作为真正的JWT令牌
            jwt = jwt.substring(7);
        }

        // 5. 判断令牌是否存在，如果不存在，返回错误结果（未登录）
        if (!StringUtils.hasLength(jwt)){
            log.info("请求头token为空, 返回未登录的信息");
            // Filter 的 doFilter() 方法返回类型是 void
            // 无法直接返回对象，必须手动处理响应
            Result error = Result.error("NOT_LOGIN"); //创建错误结果
            // 手动转换成 JSON
            String notLogin = JSONObject.toJSONString(error); //转换成JSON字符串
            // 手动写入响应体
            resp.getWriter().write(notLogin);
            return;
        }

        // 6. 解析token，如果解析失败，返回错误结果（未登录）
        try {
            // 解析token
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


            // 查询用户信息 保证令牌确实有效
            User user = usersMapper.getUserById(userId);
            if (user == null) {
                log.warn("用户不存在: userId={}", userId);
                throw new Exception("用户不存在");
            }

            // 设置用户上下文
            UserContext.setCurrentUserId(userId); //设置用户ID
            UserContext.setCurrentUserRole(userRole); //设置用户角色
            UserContext.setCurrentUser(user); //设置用户信息
            log.debug("设置用户上下文: userId={}, username={}, userRole={}", userId, user.getUsername(), userRole);

        } catch (Exception e) {
            log.info("令牌验证失败: {}", e.getMessage());
            // 返回错误结果（未登录）
            Result error = Result.error("NOT_LOGIN");
            // 手动转换成 JSON
            String notLogin = JSONObject.toJSONString(error);
            // 手动写入响应体
            resp.getWriter().write(notLogin);
            return;
        }

        // 7. 放行
        log.info("令牌合法，放行");
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * 检查是否是白名单
     * @param url 请求url
     * @return true:是白名单 false:不是
     */
    private boolean checkWhiteList(String url) {
        for (String whiteUrl : WHITE_LIST){
            if (url.contains(whiteUrl)){
                return true;
            }
        }
        return false;
    }
}