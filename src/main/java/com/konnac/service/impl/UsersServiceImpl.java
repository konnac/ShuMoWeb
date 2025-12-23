package com.konnac.service.impl;

import com.github.pagehelper.PageInfo;
import com.konnac.exception.BusinessException;
import com.konnac.mapper.UsersMapper;
import com.konnac.pojo.PageBean;
import com.konnac.pojo.User;
import com.konnac.service.UsersService;
import com.konnac.utils.PageHelperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    public PageBean page(Integer page,
                         Integer pageSize,
                         Integer id,
                         String username,
                         String realName,
                         User.UserRole role,
                         LocalDate begin,
                         LocalDate end) throws BusinessException {
        PageInfo<User> pageBean = PageHelperUtils.safePageQuery(page, pageSize, () -> UsersMapper.list(id, username, realName, role, begin, end));
        return new PageBean(pageBean.getTotal(), pageBean.getList());
    }
}
