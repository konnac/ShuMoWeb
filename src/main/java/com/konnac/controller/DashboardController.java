package com.konnac.controller;

import com.konnac.context.UserContext;
import com.konnac.pojo.AdminOverview;
import com.konnac.pojo.Result;
import com.konnac.pojo.User;
import com.konnac.pojo.UserOverview;
import com.konnac.service.ProjectsService;
import com.konnac.service.TasksService;
import com.konnac.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提供总览的功能接口
 */
@Slf4j
@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    @Autowired
    private ProjectsService projectsService;

    @Autowired
    private UsersService usersService;

    @Autowired
    private TasksService tasksService;

    /**
     * 获取管理员总览数据
     * 返回系统总体的用户、项目、任务统计数据
     * @return Result 包含AdminOverview数据的成功响应结果
     */
    @RequestMapping
    public Result index() {
        log.debug("DashboardController.index()");
        // 构建管理员概览数据对象
        AdminOverview adminOverview = new AdminOverview();
        adminOverview.setTotalUsers(usersService.countUsers());
        adminOverview.setTotalProjects(projectsService.countProjects());
        adminOverview.setTotalTasks(tasksService.countTasks());
        adminOverview.setTaskStats(tasksService.getTaskStatsOptimized());
        return Result.success(adminOverview);
    }

    /**
     * 获取用户总览
     * 根据当前登录用户的角色返回个人的项目和任务统计数据
     * @return Result 包含UserOverview数据的成功响应结果，或错误响应
     */
    @RequestMapping("/user")
    public Result userOverview() {
        log.debug("DashboardController.userOverview()");
        // 获取当前用户ID，验证登录状态
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            log.error("用户未登录");
            return Result.error("用户未登录");
        }
        // 获取当前用户角色
        User.UserRole userRole = UserContext.getCurrentUser().getRole();
        // 构建用户概览数据对象
        UserOverview userOverview = new UserOverview();
        userOverview.setTotalProjects(projectsService.getUserProjectCount(userId));
        userOverview.setActiveProjects(projectsService.getUserActiveProjectCount(userId));
        // 根据用户角色返回不同的任务统计数据
        if (userRole == User.UserRole.PROJECT_MANAGER) {
            userOverview.setTotalTasks(tasksService.getManagerTaskCount(userId));
            userOverview.setTaskStats(tasksService.getManagerTaskStats(userId));
        } else {
            userOverview.setTotalTasks(tasksService.getUserTaskCount(userId));
            userOverview.setTaskStats(tasksService.getUserTaskStats(userId));
        }
        
        return Result.success(userOverview);
    }
}
