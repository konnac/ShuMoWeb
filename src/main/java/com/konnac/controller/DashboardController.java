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

    @RequestMapping
    public Result index() {
        log.debug("DashboardController.index()");
        AdminOverview adminOverview = new AdminOverview();
        adminOverview.setTotalUsers(usersService.countUsers());
        adminOverview.setTotalProjects(projectsService.countProjects());
        adminOverview.setTotalTasks(tasksService.countTasks());
        adminOverview.setTaskStats(tasksService.getTaskStatsOptimized());
        return Result.success(adminOverview);
    }

    @RequestMapping("/user")
    public Result userOverview() {
        log.debug("DashboardController.userOverview()");
        
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            log.error("用户未登录");
            return Result.error("用户未登录");
        }
        
        User.UserRole userRole = UserContext.getCurrentUser().getRole();
        
        UserOverview userOverview = new UserOverview();
        userOverview.setTotalProjects(projectsService.getUserProjectCount(userId));
        userOverview.setActiveProjects(projectsService.getUserActiveProjectCount(userId));
        
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
