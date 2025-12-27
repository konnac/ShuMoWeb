package com.konnac.service;

import com.konnac.pojo.PageBean;
import com.konnac.pojo.Task;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;


public interface TasksService {
    //添加任务
    void addTask(Task task);

    //批量删除任务
    void deleteTask(Integer[] ids);

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
                  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end);
}
