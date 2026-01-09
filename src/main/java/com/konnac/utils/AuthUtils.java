package com.konnac.utils;

import com.konnac.exception.BusinessException;
import com.konnac.pojo.User;
import com.konnac.context.UserContext;

/**
 * 权限工具类
 * 获取和验证当前登录用户的信息,写着有时忘了就直接就去UserContext拿了其实..
 */

public class AuthUtils {

    /**
     * 获取当前登录用户ID
     */
    public static Integer getCurrentUserId(){
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null){
            throw new BusinessException("用户未登录");
        }
        return userId;
    }

    /**
     * 获取当前登录用户
     */
    public static User getCurrentUser(){
        User user = UserContext.getCurrentUser();
        if (user == null){
            throw new BusinessException("用户未登录");
        }
        return user;
    }

    /**
     * 检查当前用户是否具有指定角色
     */
    public static boolean hasRole(String role){
        User user = getCurrentUser();
        if(user.getRole() == null){
            return false;
        }
        return role.equals(user.getRole().name());
    }

    /**
     * 检查当前用户是否是管理员
     */
    public static boolean isAdmin(){
        return hasRole("ADMIN");
    }
}
