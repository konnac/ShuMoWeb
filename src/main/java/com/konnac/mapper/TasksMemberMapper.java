package com.konnac.mapper;

import com.konnac.pojo.TaskMember;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TasksMemberMapper {
    //验证任务成员是否存在
    boolean isTaskMemberExist(Integer taskId, Integer userId);

    //根据任务id和用户id获取任务成员
    TaskMember getTaskMember(Integer taskId, Integer userId);

    //添加项目成员
    void addTaskMember(TaskMember taskMember);

    //根据任务id和用户id获取任务成员
    TaskMember getMemberByTaskIdAndUserId(Integer taskId, Integer userId);

    //软删除任务成员
    void updateTaskMember(TaskMember taskMember);

    //分页获取任务成员
    List<TaskMember> list(Integer taskId, String name, String realName, String taskRole, String department, Boolean isAdmin);

    //根据任务id获取任务成员id列表
    List<Integer> getTaskMembersIds(Integer taskId);

    //批量禁用项目下所有任务的成员
    void disableTaskMembersByProjectId(Integer projectId);

    //根据任务id获取活跃的任务成员列表
    List<TaskMember> findActiveByTaskId(Integer taskId);
}
