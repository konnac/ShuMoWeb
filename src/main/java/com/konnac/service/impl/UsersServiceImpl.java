package com.konnac.service.impl;

import com.github.pagehelper.PageInfo;
import com.konnac.annotation.RequirePermission;
import com.konnac.enums.PermissionType;
import com.konnac.exception.BusinessException;
import com.konnac.mapper.UsersMapper;
import com.konnac.pojo.PageBean;
import com.konnac.pojo.User;
import com.konnac.service.UsersService;
import com.konnac.utils.PageHelperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Transactional(rollbackFor = Exception.class, timeout = 15)
@Service
public class UsersServiceImpl implements UsersService {
    @Autowired
    private UsersMapper UsersMapper;

//==================增删改方法=======================
    /**
     * 添加用户
     */
    @RequirePermission(value = PermissionType.USER_ADD, checkProject = false)
    @Override
    public void addUser(User user) {
        user.setCreatedTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        UsersMapper.addUser(user);
    }

    /**
     * 删除用户
     */
    @RequirePermission(value = PermissionType.USER_DELETE, checkProject = false)
    @Override
    public void deleteUser(Integer[] ids) {
        UsersMapper.deleteUser(ids);
    }

    /**
     * 修改用户(管理员)
     */
    @RequirePermission(value = PermissionType.USER_UPDATE_ADMIN, checkProject = false)
    @Override
    public void updateUserAdmin(User user) {
        user.setUpdateTime(LocalDateTime.now());
        UsersMapper.updateUserAdmin(user);
    }

    /**
     * 修改用户(普通用户)
     */
    @RequirePermission(value = PermissionType.USER_UPDATE, checkProject = false)
    @Override
    public void updateUser(User user) {
        user.setUpdateTime(LocalDateTime.now());
        UsersMapper.updateUser(user);
    }

//==================查询=====================


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
    @RequirePermission(value = PermissionType.USER_VIEW_SIMPLE, checkProject = false)
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

    /**
     * 检查用户名是否存在
     */
    @Override
    public boolean existsByUsername(String username, Integer excludeId) {
        return UsersMapper.existsByUsername(username, excludeId);
    }

    /**
     * 统计用户总数
     */
    @Override
    public long countUsers() {
        return UsersMapper.countUsers();
    }


}
