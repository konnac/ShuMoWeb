package com.konnac.service.impl;

import com.github.pagehelper.PageInfo;
import com.konnac.annotation.RequirePermission;
import com.konnac.enums.PermissionType;
import com.konnac.exception.BusinessException;
import com.konnac.mapper.TasksMapper;
import com.konnac.mapper.TasksMemberMapper;
import com.konnac.mapper.UsersMapper;
import com.konnac.pojo.*;
import com.konnac.service.NotificationService;
import com.konnac.service.TaskMemberService;
import com.konnac.utils.PageHelperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Transactional(rollbackFor = Exception.class, timeout = 15)
@Service
public class TaskMemberServiceImpl implements TaskMemberService {
    @Autowired
    private TasksMapper tasksMapper;

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private TasksMemberMapper tasksMemberMapper;

    @Autowired
    private NotificationService notificationService;

    // ======================增删改功能======================
    @RequirePermission(value = PermissionType.TASK_ASSIGN, checkProject = false, checkTask = true)
    @Override
    public void addTaskMember(Integer taskId, Integer userId, String taskRole, Integer operatorId) {
        log.debug("添加任务成员: taskId={}, userId={}, taskRole={}", taskId, userId, taskRole);
        //1.验证任务存在
        Task task = tasksMapper.getTaskById(taskId);
        if (task == null) {
            log.warn("任务id：{}，任务不存在", taskId);
            throw new BusinessException("要添加任务的任务不存在");
        }
        //2.验证用户存在
        User user = usersMapper.getUserById(userId);
        if (user == null) {
            log.warn("用户id：{}，用户不存在", userId);
            throw new BusinessException("要添加任务的用户不存在");
        }

        TaskMember taskMember = tasksMemberMapper.getMemberByTaskIdAndUserId(taskId, userId);
        if (taskMember != null) {
            if (taskMember.getStatus() == TaskMember.MemberStatus.INACTIVE) {
                taskMember.setStatus(TaskMember.MemberStatus.ACTIVE);
                taskMember.setTaskRole(taskRole);
                taskMember.setJoinBy(operatorId);
                taskMember.setUpdateTime(LocalDateTime.now());
                tasksMemberMapper.updateTaskMember(taskMember);
                log.info("重新激活任务成员: taskId={}, userId={}", taskId, userId);
            } else {
                throw new BusinessException("用户已经是任务成员");
            }
        } else {
            TaskMember newTaskMember = new TaskMember();
            newTaskMember.setTaskId(taskId);
            newTaskMember.setUserId(userId);
            newTaskMember.setTaskRole(taskRole);
            newTaskMember.setJoinBy(operatorId);
            newTaskMember.setStatus(TaskMember.MemberStatus.ACTIVE);
            newTaskMember.setJoinDate(LocalDateTime.now());
            tasksMemberMapper.addTaskMember(newTaskMember);
        }

        notificationService.sendTaskAssignNotification(taskId, userId, operatorId);

        log.debug("添加任务成员成功: taskId={}, userId={}, taskRole={}", taskId, userId, taskRole);
    }

    /**
     * 批量添加任务成员(允许部分失败)
     */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            rollbackFor = Exception.class,
            timeout = 30)
    @RequirePermission(value = PermissionType.TASK_ASSIGN)
    @Override
    public BatchResult addTaskMembers(Integer taskId, List<Integer> userIds, Integer operatorId) {
        log.debug("批量添加任务成员: taskId={}, userIds={}", taskId, userIds);
        if (userIds == null || userIds.isEmpty()) {
            log.warn("用户列表不能为空");
            throw new BusinessException("用户列表不能为空");
        }

        //1.包装批量结果
        BatchResult batchResult = new BatchResult();
        batchResult.setTotal(userIds.size());

        //2.批量添加项目成员,失败跳过且添加失败的成员记录
        for (Integer userId : userIds) {
            try {
                addTaskMember(taskId, userId, "COLLABORATOR", null);
                notificationService.sendAddNotification(taskId, userId, operatorId);
                batchResult.addSuccess(userId);
            } catch (BusinessException e) {
                batchResult.addFailure(userId, e.getMessage());
                log.warn("成员添加失败跳过: userId={}, error={}", userId, e.getMessage());
            }
        }

        if(batchResult.isAllFailure()){
            throw new BusinessException("批量添加任务成员失败" + batchResult.getFailureDetails());
        }

        log.info("批量添加任务成员结果: total={}, successCount={}, failureCount={}", batchResult.getTotal(), batchResult.getSuccessCount(), batchResult.getFailureCount());
        return batchResult;
    }

    /**
     * 批量删除任务成员
     */
    @RequirePermission(value = PermissionType.TASK_UPDATE, checkTask = true)
    @Override
    public void deleteTaskMembers(Integer taskId, List<Integer> userIds, Integer operatorId) {
        log.info("批量删除任务成员: taskId={}, userIds={}, operatorId={}", taskId, userIds, operatorId);
        if (userIds == null || userIds.isEmpty()){
            log.warn("用户列表不能为空");
            throw new BusinessException("用户列表不能为空");
        }

        //包装批量结果
        BatchResult batchResult = new BatchResult();
        batchResult.setTotal(userIds.size());


        for(Integer userId : userIds){
            try{
                TaskMember taskMember = tasksMemberMapper.getMemberByTaskIdAndUserId(taskId, userId);
                if (taskMember != null){
                    taskMember.setStatus(TaskMember.MemberStatus.INACTIVE);
                    taskMember.setUpdateTime(LocalDateTime.now());
                    tasksMemberMapper.updateTaskMember(taskMember);
                }
                log.info("删除任务成员成功: taskId={}, userId={}, operatorId={}", taskId, userId, operatorId);

                //发送通知
                notificationService.sendTaskRemovalNotification(taskId, userId, operatorId);
            } catch (Exception e){
                log.warn("移除任务成员失败: taskId={}, userId={}, operatorId={}, error={}", taskId, userId, operatorId, e.getMessage());
                throw new BusinessException("移除任务成员失败" + e.getMessage());
            }
        }

    }

    /**
     * 更新项目成员角色
     */
    @RequirePermission(value = PermissionType.TASK_UPDATE, checkTask = true)
    @Override
    public void updateMemberRole(Integer taskId, Integer userId, String newTaskRole, Integer operatorId) {
        log.debug("更新任务成员角色: taskId={}, userId={}, newTaskRole={}, operatorId={}", taskId, userId, newTaskRole, operatorId);
        TaskMember taskMember = tasksMemberMapper.getMemberByTaskIdAndUserId(taskId, userId);
        if (taskMember == null) {
            log.warn("任务成员不存在: taskId={}, userId={}", taskId, userId);
            throw new BusinessException("任务成员不存在");
        }
        taskMember.setTaskRole(newTaskRole);
        tasksMemberMapper.updateTaskMember(taskMember);

        log.info("更新任务成员角色成功: taskId={}, userId={}, newTaskRole={}, operatorId={}", taskId, userId, newTaskRole, operatorId);

        notificationService.sendUpdateTaskMemberRoleNotification(taskId, userId, operatorId, TaskRole.valueOf(newTaskRole));
    }

    /**
     * 发送任务完成通知给任务成员
     */
    @Override
    public BatchResult sendTaskCompleteNotification(Integer taskId) {
        //获取任务
        Task task = tasksMapper.getTaskById(taskId);
        if (task == null) {
            log.warn("任务不存在: taskId={}", taskId);
            throw new BusinessException("任务不存在");
        }
        //获取任务成员列表
        List<TaskMember> taskMembers = tasksMemberMapper.list(taskId, null, null, null, null);

        if (taskMembers == null || taskMembers.isEmpty()) {
            log.warn("任务成员列表为空: taskId={}", taskId);
            throw new BusinessException("任务成员列表为空");
        }

        BatchResult batchResult = new BatchResult();
        batchResult.setTotal(taskMembers.size());

        //2.发送通知
        for (TaskMember taskMember : taskMembers) {
            try{
                log.info("发送任务完成通知给任务成员: taskId={}, userId={}", taskId, taskMember.getUserId());
                notificationService.sendTaskCompleteNotification(taskId, taskMember.getUserId());
                batchResult.addSuccess(taskMember.getUserId());
            } catch (BusinessException e){
                log.warn("发送任务完成通知给任务成员失败: taskId={}, userId={}, error={}", taskId, taskMember.getUserId(), e.getMessage());
                batchResult.addFailure(taskMember.getUserId(), e.getMessage());
            }

        }
        if (batchResult.isAllFailure()){
            throw new BusinessException("发送任务完成通知给任务成员失败" + batchResult.getFailureDetails());
        }
        log.info("发送任务完成通知给任务成员结果: total={}, successCount={}, failureCount={}", batchResult.getTotal(), batchResult.getSuccessCount(), batchResult.getFailureCount());
        return batchResult;
    }

    /**
     * 分页查询任务成员
     */
    @Override
    public PageBean page(Integer page,
                         Integer pageSize,
                         Integer taskId,
                         String name,
                         String realName,
                         String taskRole,
                         String department) {
        PageInfo<TaskMember> pageInfo = PageHelperUtils.safePageQuery(page, pageSize, () -> tasksMemberMapper.list(taskId, name, realName, taskRole, department));
        return new PageBean(pageInfo.getTotal(), pageInfo.getList());
    }
}
