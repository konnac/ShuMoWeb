package com.konnac.service;

import com.konnac.pojo.BatchResult;
import com.konnac.pojo.PageBean;

import java.util.List;

public interface TaskMemberService {
    // ======================增删改功能======================

    /**
     *  添加任务成员
     */
    void addTaskMember(Integer taskId, Integer userId, String taskRole, Integer operatorId);

    /**
     * 批量添加任务成员
     */
    BatchResult addTaskMembers(Integer taskId, List<Integer> userIds, Integer operatorId);

    /**
     * 删除任务成员
     */
    void deleteTaskMembers(Integer taskId, List<Integer> userIds, Integer operatorId);

    /**
     * 更新任务成员角色
     */
    void updateMemberRole(Integer taskId, Integer userId, String newTaskRole, Integer operatorId);

    /**
     * 激活任务成员
     */
    void activateMember(Integer taskId, Integer userId, Integer operatorId);

    //  ======================查询功能======================
    /**
     * 分页查询任务成员
     */
    PageBean page(Integer page,
                  Integer pageSize,
                  Integer taskId,
                  String name,
                  String realName,
                  String taskRole,
                  String department,
                  Boolean isAdmin);

    //  ======================其他功能======================

    /**
     *  发送任务完成通知给任务成员
     */
    BatchResult sendTaskCompleteNotification(Integer taskId);

    /**
     * 获取任务成员列表
     */

    /**
     * 获取任务成员id
     */

    /**
     * 获取任务成员角色详细 (待完成)
     */
}
