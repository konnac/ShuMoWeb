package com.konnac.service;

import com.konnac.pojo.PageBean;
import com.konnac.pojo.Task;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

public interface TasksService {
    //添加任务
    void addTask(Task task);

    //批量删除任务
    void deleteTask(Integer[] ids);

    //根据id查询任务
    void updateTask(Task task);

    //修改任务
    Task getTaskById(Integer id);

    //分页查询

    //获取未完成的任务数

    //获取任务中的成员id
    List<Integer> getTaskMembersId(Integer projectId, Integer taskId);

}
