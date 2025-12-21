package com.konnac.mapper;

import com.konnac.pojo.Task;
import com.konnac.pojo.TaskStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper
public interface TasksMapper {
    //添加任务
    void addTask(Task task);

    //批量删除任务
    void deleteTask(Integer[] ids);

    //根据id查询任务
    @Select("select * from tasks where id = #{id}")
    Task getTaskById(Integer id);

    //修改任务
    void updateTask(Task task);

    //查询未完成的任务数
    int getUncompletedTaskCountByProjectIdAndUserId(Integer projectId, Integer userId);

    //查询用户在项目中的任务统计信息
    TaskStats getUserTaskStatsInProject(Integer projectId, Integer userId);

    //获取任务成员id
    List<Integer> getTaskMembersId(Integer projectId, Integer taskId);
}
