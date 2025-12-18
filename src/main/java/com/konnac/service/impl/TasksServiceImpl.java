package com.konnac.service.impl;

import com.konnac.mapper.TasksMapper;
import com.konnac.pojo.Task;
import com.konnac.service.TasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TasksServiceImpl implements TasksService {
    @Autowired
    private TasksMapper tasksMapper;

    //添加任务
    @Override
    public void addTask(Task task) {
        task.setCreatedTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        tasksMapper.addTask(task);
    }

    //批量删除任务
    @Override
    public void deleteTask(Integer[] ids) {
        tasksMapper.deleteTask(ids);
    }

    //修改任务
    @Override
    public void updateTask(Task task) {
        task.setUpdateTime(LocalDateTime.now());
        tasksMapper.updateTask(task);
    }


    //根据id查询任务
    @Override
    public Task getTaskById(Integer id) {
        return tasksMapper.getTaskById(id);
    }
}
