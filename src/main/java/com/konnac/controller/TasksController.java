package com.konnac.controller;

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
* 任务管理(tasks)
*/
@Slf4j
@RestController
@RequestMapping("/tasks")
public class TasksController {
    @Autowired
    private TasksService tasksService;

    //添加任务
    @PostMapping
    public Result addTask(Task task) {
        log.info("添加任务，任务信息：{}", task);
        tasksService.addTask(task);
        return Result.success();
    }

    //批量删除任务
    @DeleteMapping("/{ids}")
    public Result deleteTask(@PathVariable Integer[] ids) {
        log.info("删除任务，任务id：{}", ids);
        tasksService.deleteTask(ids);
        return Result.success();
    }

//    //根据id查询任务
//    @GetMapping("/{id}")
//    public Result getTask(@PathVariable Integer id) {
//        log.info("查询任务，任务id：{}", id);
//        Task task = tasksService.getTaskById(id);
//        return Result.success(task);
//    }

    //修改任务
    @PutMapping
    public Result updateTask(@RequestBody Task task) {
        log.info("修改任务，任务信息：{}", task);
        tasksService.updateTask(task);
        return Result.success();
    }

    //分页查询
    @RequestMapping
    public Result page(@RequestParam(defaultValue = "1") Integer page,
                   @RequestParam(defaultValue = "10") Integer pageSize,
                   Integer projectId,
                   Integer id,
                   String title,
                   String assigneeName,
                   Task.TaskStatus status,
                   @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                   @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
                   ){
                log.info("分页查询, 参数: page={}, pageSize={}, projectId={}, id={}, title={}, assigneeName={}, status={}, begin={}, end={}",
                        page, pageSize, projectId, id, title, assigneeName, status, begin, end);
        PageBean pageBean = tasksService.page(page, pageSize, projectId, id, title, assigneeName, status, begin, end);
        return Result.success(pageBean);
    }
}
