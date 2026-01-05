package com.konnac.service;

import com.konnac.pojo.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;


public interface TasksService {
    //添加任务
    void addTask(Task task);

    //批量删除任务
    void deleteTask(Integer[] ids);

    //根据id查询任务
    Task getTaskById(Integer id);

    //修改任务
    void updateTask(Task task);

    //分页查询
    PageBean page(Integer page,
                  Integer pageSize,
                  Integer projectId,
                  Integer Id,
                  String title,
                  String assigneeName,
                  Task.TaskStatus status,
                  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end,
                  Integer currentUserId);

    /**
     * 统计任务总数
     */
    long countTasks();

    /**
     * 获取所有任务状态
     */
    AdminOverview.TaskStatusStats getTaskStatsOptimized();

    /**
     * 获取指定用户的任务总数
     */
    long getUserTaskCount(Integer userId);

    /**
     * 获取指定用户的任务状态统计
     */
    UserOverview.TaskStatusStats getUserTaskStats(Integer userId);

    /**
     * 分页查询指定用户的任务
     */
    PageBean pageMyTasks(Integer page,
                         Integer pageSize,
                         Integer projectId,
                         Integer Id,
                         String title,
                         String assigneeName,
                         Task.TaskStatus status,
                         @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                         @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end,
                         Integer currentUserId,
                         String userRole);
}
