package com.konnac.service;

import com.konnac.pojo.PageBean;
import com.konnac.pojo.User;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public interface UsersService {

    void addUser(User user);

    void deleteUser(Integer[] id);

    void updateUser(User user);

    User getUserById(Integer id);

    User login(User user);

    PageBean page(Integer page, Integer pageSize, Integer id, String username, String realName, User.UserRole role,
                  @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate begin,
                  @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate end);
}
