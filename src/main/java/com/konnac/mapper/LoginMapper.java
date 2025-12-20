package com.konnac.mapper;

import com.konnac.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginMapper {
    //验证用户名密码
    User getByUserNameAndPassword(User user);
}
