package com.konnac.controller;

import com.konnac.PageBean;
import com.konnac.Result;
import com.konnac.Task;
import com.konnac.service.TasksService;
import com.konnac.utils.AuthUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/my-tasks")
@Tag(name = "我的任务", description = "当前用户任务查询接口")
public class MyTasksController {
    @Autowired
    private TasksService tasksService;

    @Operation(summary = "分页查询我的任务", description = "根据当前登录用户分页查询其负责的任务列表")
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
        Boolean isAdmin = AuthUtils.getCurrentUser().getRole() == com.konnac.User.UserRole.ADMIN;

        log.info("分页查询我的任务, 参数: page={}, pageSize={}, projectId={}, id={}, title={}, assigneeName={}, status={}, begin={}, end={}, currentUserId={}, userRole={}",
                page, pageSize, projectId, id, title, assigneeName, status, begin, end, currentUserId, userRole);

        PageBean pageBean = tasksService.pageMyTasks(page, pageSize, projectId, id, title, assigneeName, status, begin, end, currentUserId, userRole, isAdmin);
        return Result.success(pageBean);
    }
}
