package com.konnac.service.impl;

import com.konnac.User;
import com.konnac.mapper.LoginMapper;

import com.konnac.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
@Slf4j
public class LoginServiceImpl implements LoginService {
    @Autowired
    private LoginMapper LoginMapper;
    /**
     * 登录
     */
    @Override
    public User login(User user) {
        log.info("用户登录1，用户名：{}", user.getUsername());

        String username = user.getUsername();
        String password = user.getPassword();

        log.info("用户登录2，用户名：{}", username);
        User newUser = LoginMapper.getByUsername(username);

        log.info("用户登录3：{}", newUser);
        if (newUser == null){
            // 用户不存在
            log.warn("用户登录失败不存在");
            return null;
        }

        password = DigestUtils.md5DigestAsHex(password.getBytes());

        if (!password.equals(newUser.getPassword())){
            // 密码错误
            log.warn("用户登录失败，密码错误");
            return null;
        }

        log.info("用户登录成功4：{}", newUser);
        return newUser;
    }
}
