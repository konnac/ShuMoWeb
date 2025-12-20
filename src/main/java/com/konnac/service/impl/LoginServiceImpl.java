package com.konnac.service.impl;

import com.konnac.mapper.LoginMapper;
import com.konnac.pojo.User;
import com.konnac.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private LoginMapper LoginMapper;
    /**
     * 登录
     */
    @Override
    public User login(User user) {
        return LoginMapper.getByUserNameAndPassword(user);
    }
}
