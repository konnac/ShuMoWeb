package com.konnac.service;

import com.konnac.pojo.PageBean;
import com.konnac.pojo.User;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public interface UsersService {

    /**
     * 添加用户
     */
    void addUser(User user);

    /**
     * 删除用户
     */
    void deleteUser(Integer[] id);

    /**
     * 修改用户(管理员)
     */
    void updateUserAdmin(User user);

    /**
     * 修改用户
     */
    void updateUser(User user);

    /**
     * 根据id查询用户
     */
    User getUserById(Integer id);

    /**
     * 分页查询
     */
    PageBean page(Integer page, Integer pageSize, Integer id, String username, String realName, User.UserRole role,
                  @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate begin,
                  @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate end);
}
