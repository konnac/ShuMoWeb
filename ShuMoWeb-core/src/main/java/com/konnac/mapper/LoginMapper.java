package com.konnac.mapper;

import com.konnac.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginMapper {
    //验证用户名密码
    User getByUserNameAndPassword(User user);

    //验证用户名
    User getByUsername(String username);
}
