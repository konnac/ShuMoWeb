package com.konnac.context;

import com.konnac.pojo.User;
import lombok.Data;

/**
 * 用户上下文类
 * 使用ThreadLocal存储当前线程的用户信息
 * 核心作用：
 * 1. 请求链中共享用户信息
 * 2. 避免在每个方法参数中传递id 可以直接获取当前操作人的id
 * 3. 线程之间互不影响
 * 使用场景：
 *  LoginCheckFilter：设置用户上下文
 *  PermissionAspect：获取用户信息进行权限验证
 *  Controller/Service：获取当前用户信息
 *  UserContextCleanupInterceptor：清理用户上下文
 */

public class UserContext {
    //

    // 当前用户id
    private static ThreadLocal<Integer> currentUserId = new ThreadLocal<>();
    // 当前用户
    private static ThreadLocal<User> currentUser = new ThreadLocal<>();
    // 当前用户角色
    private static ThreadLocal<String> currentUserRole = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     */
    public static void setCurrentUserId(Integer userId) {
        currentUserId.set(userId);
    }

    /**
     *  获取当前用户ID
     */
    public static Integer getCurrentUserId() {
        return currentUserId.get();
    }

    /**
     * 获取当前用户角色
     */
    public static String getCurrentUserRole() {
        return currentUserRole.get();
    }

    /**
     * 设置当前用户角色
     */
    public static void setCurrentUserRole(String role) {
        currentUserRole.set(role);
    }

    /**
     * 设置当前用户完整信息
     */
    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }

    /**
     * 获取当前用户完整信息
     */
    public static User getCurrentUser() {
        return currentUser.get();
    }

    /**
     * 检查是否已登录
     */
    public static boolean isLoggedIn() {
        return getCurrentUserId() != null;
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        User user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 清除用户上下文
     */
    public static void clear() {
        currentUserId.remove();
        currentUser.remove();
        currentUserRole.remove();
    }
}
