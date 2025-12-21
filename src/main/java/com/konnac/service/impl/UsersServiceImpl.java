package com.konnac.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.konnac.mapper.UsersMapper;
import com.konnac.pojo.PageBean;
import com.konnac.pojo.User;
import com.konnac.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsersServiceImpl implements UsersService {
    @Autowired
    private UsersMapper UsersMapper;

    /**
     * 添加用户
     */
    @Override
    public void addUser(User user) {
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        UsersMapper.addUser(user);
    }

    /**
     * 删除用户
     */
    @Override
    public void deleteUser(Integer[] ids) {
        UsersMapper.deleteUser(ids);
    }

    /**
     * 修改用户
     */
    @Override
    public void updateUser(User user) {
        user.setUpdateTime(LocalDateTime.now());
        UsersMapper.updateUser(user);
    }

    /**
     * 根据id查询用户
     */
    @Override
    public User getUserById(Integer id) {
        return UsersMapper.getUserById(id);
    }

    /**
     * 分页查询
     */
    @Override
    public PageBean page(Integer page, Integer pageSize, Integer id, String username, String realName, User.UserRole role, LocalDate begin, LocalDate end) {
        //1.设置分页参数
        PageHelper.startPage(page, pageSize);
        //2.执行查询
        List<User> usersList = UsersMapper.list(id, username, realName, role, begin, end);
        Page<User> pageBean = (Page<User>) usersList;

        //3.获取分页结果
        return new PageBean(pageBean.getTotal(), pageBean.getResult());
    }
}
