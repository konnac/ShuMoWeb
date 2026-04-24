package com.konnac.controller;


import com.konnac.Result;
import com.konnac.User;
import com.konnac.service.LoginService;
import com.konnac.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@Tag(name = "登录管理", description = "用户登录相关接口")
public class LoginController {
    @Autowired
    private LoginService loginService;

    @Operation(summary = "用户登录", description = "根据用户名和密码进行用户登录认证")
    @PostMapping("/login")
    public Result login(@RequestBody User user){
        log.info("用户登录，用户名：{}", user.getUsername());
        User u = loginService.login(user);
        if(u != null){
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", u.getId());
            claims.put("username", u.getUsername());
            claims.put("role", u.getRole().toString());

            String jwt = JwtUtils.generateJwt(claims);
            
            Map<String, Object> result = new HashMap<>();
            result.put("token", jwt);
            result.put("userInfo", u);
            return Result.success(result);
        }
        log.warn("用户登录失败，用户名：{}", user.getUsername());
        return Result.error("用户名或密码错误");
    }
}
