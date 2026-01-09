package com.konnac.controller;

import com.konnac.pojo.PageBean;
import com.konnac.pojo.Result;
import com.konnac.pojo.Task;
import com.konnac.service.TasksService;
import com.konnac.utils.AuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/my-tasks")
public class MyTasksController {
    @Autowired
    private TasksService tasksService;

    /**
     * 分页查询我的任务
     */
    @RequestMapping
    public Result page(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       Integer projectId,
                       Integer id,
                       String title,
                       String assigneeName,
                       Task.TaskStatus status,
                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        Integer currentUserId = AuthUtils.getCurrentUserId();
        String userRole = AuthUtils.getCurrentUser().getRole().name();
        Boolean isAdmin = AuthUtils.getCurrentUser().getRole() == com.konnac.pojo.User.UserRole.ADMIN;

        log.info("分页查询我的任务, 参数: page={}, pageSize={}, projectId={}, id={}, title={}, assigneeName={}, status={}, begin={}, end={}, currentUserId={}, userRole={}",
                page, pageSize, projectId, id, title, assigneeName, status, begin, end, currentUserId, userRole);

        PageBean pageBean = tasksService.pageMyTasks(page, pageSize, projectId, id, title, assigneeName, status, begin, end, currentUserId, userRole, isAdmin);
        return Result.success(pageBean);
    }
}
