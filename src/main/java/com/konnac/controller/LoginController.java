package com.konnac.controller;

import com.konnac.pojo.Result;
import com.konnac.pojo.User;
import com.konnac.service.LoginService;
import com.konnac.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class LoginController {
    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public Result login(@RequestBody User user){
        log.info("用户登录，用户名：{}", user.getUsername());
        User u = loginService.login(user);
        //登陆成功,生成令牌并下发令牌
        if(u != null){
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", u.getId());
            claims.put("username", u.getUsername());
            claims.put("role", u.getRole().toString());

            //jwt包含了用户信息
            String jwt = JwtUtils.generateJwt(claims);
            return Result.success(jwt);
        }
        log.warn("用户登录失败，用户名：{}", user.getUsername());
        return Result.error("用户名或密码错误");
    }
}
