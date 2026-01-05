package com.konnac.controller;

import com.konnac.annotation.RequirePermission;
import com.konnac.enums.PermissionType;
import com.konnac.pojo.PageBean;
import com.konnac.pojo.Result;
import com.konnac.pojo.Task;
import com.konnac.service.TasksService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/*
* 任务管理 - 隶属于项目
*/
@Slf4j
@RestController
@RequestMapping("/projects/{projectId}/tasks")
public class TasksController {
    @Autowired
    private TasksService tasksService;

    //添加任务
    @RequirePermission(value = PermissionType.TASK_ADD, checkProject = true, projectIdParam = "projectId")
    @PostMapping
    public Result addTask(@PathVariable Integer projectId, @RequestBody Task task) {
        log.info("添加任务，项目id：{}，任务信息：{}", projectId, task);
        task.setProjectId(projectId);
        tasksService.addTask(task);
        return Result.success();
    }

    //批量删除任务
    @DeleteMapping("/{ids}")
    public Result deleteTask(@PathVariable Integer projectId, @PathVariable Integer[] ids) {
        log.info("删除任务，项目id：{}，任务id：{}", projectId, ids);
        tasksService.deleteTask(ids);
        return Result.success();
    }

    //根据id查询任务
    @RequirePermission(value = PermissionType.MEMBER_VIEW, checkProject = true, projectIdParam = "projectId")
    @GetMapping("/{id}")
    public Result getTask(@PathVariable Integer projectId, @PathVariable Integer id) {
        log.info("查询任务，项目id：{}，任务id：{}", projectId, id);
        Task task = tasksService.getTaskById(id);
        return Result.success(task);
    }

    //修改任务
    @PutMapping("/{id}")
    public Result updateTask(@PathVariable("projectId") Integer projectId, @PathVariable("id") Integer id, @RequestBody Task task) {
        log.info("修改任务，项目id：{}，任务id：{}，任务信息：{}", projectId, id, task);
        task.setId(id);
        task.setProjectId(projectId);
        tasksService.updateTask(task);
        return Result.success();
    }

    //分页查询项目下的任务
    @RequestMapping
    public Result page(@PathVariable Integer projectId,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       Integer id,
                       String title,
                       String assigneeName,
                       Task.TaskStatus status,
                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
                   ){
                log.info("分页查询项目任务, 项目id：{}，参数: page={}, pageSize={}, id={}, title={}, assigneeName={}, status={}, begin={}, end={}",
                        projectId, page, pageSize, id, title, assigneeName, status, begin, end);
        PageBean pageBean = tasksService.page(page, pageSize, projectId, id, title, assigneeName, status, begin, end, com.konnac.utils.AuthUtils.getCurrentUserId());
        return Result.success(pageBean);
    }
}
