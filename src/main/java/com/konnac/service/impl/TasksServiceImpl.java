package com.konnac.service.impl;

import com.github.pagehelper.PageInfo;
import com.konnac.annotation.RequirePermission;
import com.konnac.context.UserContext;
import com.konnac.enums.PermissionType;
import com.konnac.exception.BusinessException;
import com.konnac.mapper.TasksMapper;
import com.konnac.mapper.TasksMemberMapper;
import com.konnac.pojo.*;
import com.konnac.service.TaskMemberService;
import com.konnac.service.TasksService;
import com.konnac.utils.AuthUtils;
import com.konnac.utils.PageHelperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Transactional(rollbackFor = Exception.class, timeout = 15)
@Service
public class TasksServiceImpl implements TasksService {
    @Autowired
    private TasksMapper tasksMapper;

    @Autowired
    private TaskMemberService taskMemberService;

    @Autowired
    private TasksMemberMapper tasksMemberMapper;

    //添加任务
    @RequirePermission(value = PermissionType.TASK_ADD, projectIdParam = "projectId")
    @Override
    public void addTask(Task task) {
        log.debug("添加任务，任务信息：{}", task);

        task.setCreatedTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        tasksMapper.addTask(task);
        log.info("添加任务成功，任务id：{}", task.getId());

        if (task.getAssigneeId() != null) {
            try {
                taskMemberService.addTaskMember(task.getId(), task.getAssigneeId(), "ASSIGNEE", UserContext.getCurrentUserId());
                log.info("自动将任务负责人添加到任务成员中: taskId={}, assigneeId={}", task.getId(), task.getAssigneeId());
            } catch (BusinessException e) {
                log.warn("自动添加任务负责人到任务成员失败: taskId={}, assigneeId={}, error={}", task.getId(), task.getAssigneeId(), e.getMessage());
            }
        }
    }

    //批量删除任务
    @RequirePermission(value = PermissionType.TASK_DELETE, checkTask = true)
    @Override
    public void deleteTask(Integer[] ids) {
        log.debug("删除任务，任务id：{}", ids);
        // 验证任务列表是否为空
        if (ids == null || ids.length == 0) {
            throw new BusinessException("要删除的任务列表不能为空");
        }
        for (Integer id : ids) {
            try {
                Task task = tasksMapper.getTaskById(id);
                //2.验证任务是否存在
                if (task == null) {
                    log.warn("任务id：{}，任务不存在", id);
                    throw new BusinessException("要删除的任务不存在");
                }
                
                // 将任务状态设置为已取消
                task.setStatus(Task.TaskStatus.CANCELLED);
                tasksMapper.updateTask(task);
                
                // 将任务下的所有成员状态设置为 INACTIVE
                List<TaskMember> taskMembers = tasksMemberMapper.findActiveByTaskId(id);
                for (TaskMember member : taskMembers) {
                    member.setStatus(TaskMember.MemberStatus.INACTIVE);
                    member.setUpdateTime(LocalDateTime.now());
                    tasksMemberMapper.updateTaskMember(member);
                }

                log.info("任务id：{}，任务删除成功，状态已设置为已取消，成员已设置为inactive", id);
            } catch (Exception e) {
                log.warn("任务id：{}，任务删除失败", id, e);
                throw new RuntimeException(e);
            }
        }
    }

    //根据id查询任务
    @Override
    public Task getTaskById(Integer id) {
        log.debug("查询任务，任务id：{}", id);
        Task task = tasksMapper.getTaskById(id);
        if (task == null) {
            log.warn("任务id：{}，任务不存在", id);
            throw new BusinessException("任务不存在");
        }
        return task;
    }

    //修改任务
    /**
     * 更新任务信息
     * 该方法用于更新任务的基本信息，同时处理任务负责人变更时的相关逻辑
     * 包括权限验证、任务信息校验、负责人角色调整等操作
     *
     * @param task 需要更新的任务对象，包含任务的完整信息
     * @throws BusinessException 当任务信息为空或更新过程中出现业务异常时抛出
     */
    @RequirePermission(value = PermissionType.TASK_UPDATE, checkTask = true, projectIdParam = "projectId")
    @Override
    public void updateTask(Task task) {
        log.debug("修改任务，任务信息：{}", task);
        if (task == null) {
            log.warn("修改任务失败，任务信息为空");
            throw new BusinessException("修改任务失败，任务信息为空");
        }

        // 获取任务变更前的负责人ID，用于后续负责人变更判断
        Integer oldAssigneeId = null;
        if (task.getId() != null) {
            Task existingTask = tasksMapper.getTaskById(task.getId());
            if (existingTask != null) {
                oldAssigneeId = existingTask.getAssigneeId();
            }
        }

        task.setUpdateTime(LocalDateTime.now());
        tasksMapper.updateTask(task);

        // 检查任务负责人是否发生变更，并处理相关的成员角色调整
        if (task.getAssigneeId() != null && !task.getAssigneeId().equals(oldAssigneeId)) {
            try {
                Integer operatorId = AuthUtils.getCurrentUser().getId();

                // 如果原负责人存在，将其角色调整为协作者
                if (oldAssigneeId != null) {
                    TaskMember oldAssigneeMember = tasksMemberMapper.getMemberByTaskIdAndUserId(task.getId(), oldAssigneeId);
                    if (oldAssigneeMember != null && oldAssigneeMember.getStatus() == TaskMember.MemberStatus.ACTIVE) {
                        oldAssigneeMember.setTaskRole("COLLABORATOR");
                        oldAssigneeMember.setUpdateTime(LocalDateTime.now());
                        tasksMemberMapper.updateTaskMember(oldAssigneeMember);
                    }
                }

                // 设置新负责人的角色为负责人，或创建新的负责人记录
                TaskMember newAssignee = tasksMemberMapper.getMemberByTaskIdAndUserId(task.getId(), task.getAssigneeId());
                if (newAssignee != null) {
                    if (newAssignee.getStatus() == TaskMember.MemberStatus.INACTIVE) {
                        newAssignee.setStatus(TaskMember.MemberStatus.ACTIVE);
                        newAssignee.setJoinBy(operatorId);
                        newAssignee.setUpdateTime(LocalDateTime.now());
                    }
                    newAssignee.setTaskRole("ASSIGNEE");
                    tasksMemberMapper.updateTaskMember(newAssignee);
                } else {
                    TaskMember newTaskMember = new TaskMember();
                    newTaskMember.setTaskId(task.getId());
                    newTaskMember.setUserId(task.getAssigneeId());
                    newTaskMember.setTaskRole("ASSIGNEE");
                    newTaskMember.setJoinBy(operatorId);
                    newTaskMember.setStatus(TaskMember.MemberStatus.ACTIVE);
                    newTaskMember.setJoinDate(LocalDateTime.now());
                    tasksMemberMapper.addTaskMember(newTaskMember);
                }
            } catch (BusinessException e) {
                log.warn("更新任务负责人失败: taskId={}, assigneeId={}, error={}", task.getId(), task.getAssigneeId(), e.getMessage());
            }
        }

    }


    /**
     * 分页查询
     */
    @Override
    public PageBean page(Integer page,
                         Integer pageSize,
                         Integer projectId,
                         Integer Id,
                         String title,
                         String assigneeName,
                         Task.TaskStatus status,
                         LocalDate begin,
                         LocalDate end,
                         Integer currentUserId,
                         Boolean isAdmin) throws BusinessException {
        log.debug("分页查询任务，参数：page={},pageSize={},projectId={},id={},title={},assigneeName={},status={},begin={},end={}, currentUserId={}",
                page, pageSize, projectId, Id, title, assigneeName, status, begin, end, currentUserId);

        com.konnac.pojo.User currentUser = com.konnac.utils.AuthUtils.getCurrentUser();

        if (com.konnac.pojo.User.UserRole.ADMIN == currentUser.getRole()) {
            PageInfo<Task> pageBean = PageHelperUtils.safePageQuery(page, pageSize,
                    () -> tasksMapper.listAll(
                            projectId,
                            Id,
                            title,
                            assigneeName,
                            status,
                            begin,
                            end,
                            isAdmin
                    )
            );
            log.info("分页查询任务成功，结果：{}", pageBean);
            return new PageBean(pageBean.getTotal(), pageBean.getList());
        }

        PageInfo<Task> pageBean = PageHelperUtils.safePageQuery(page, pageSize,
                () -> tasksMapper.list(
                        projectId,
                        Id,
                        title,
                        assigneeName,
                        status,
                        begin,
                        end,
                        currentUserId,
                        isAdmin
                )
        );
        log.info("分页查询任务成功，结果：{}", pageBean);
        return new PageBean(pageBean.getTotal(), pageBean.getList());
    }

    @Override
    public long countTasks() {
        return tasksMapper.getTaskCount();
    }

    /**
     * 获取任务状态统计
     */
    @Override
    public AdminOverview.TaskStatusStats getTaskStatsOptimized() {
        log.debug("获取任务状态统计（优化版）");

        AdminOverview adminOverview = new AdminOverview();
        AdminOverview.TaskStatusStats stats = adminOverview.new TaskStatusStats();

        try {
            // 方法2：使用分组查询一次性获取所有状态
            List<Map<String, Object>> statusList = tasksMapper.countAllStatus();

            // 初始化所有状态为0
            stats.setNotStarted(0);
            stats.setInProgress(0);
            stats.setPending(0);
            stats.setCompleted(0);
            stats.setCancelled(0);

            // 遍历查询结果，设置对应的状态数量
            for (Map<String, Object> map : statusList) {
                String status = (String) map.get("status");
                Long count = (Long) map.get("count");

                if (status != null && count != null) {
                    // 根据状态值设置对应的字段
                    switch (status) {
                        case "NOT_STARTED":
                            stats.setNotStarted(count);
                            break;
                        case "IN_PROGRESS":
                            stats.setInProgress(count);
                            break;
                        case "DELAY":
                            stats.setPending(count);
                            break;
                        case "COMPLETED":
                            stats.setCompleted(count);
                            break;
                        case "CANCELLED":
                            stats.setCancelled(count);
                            break;
                        default:
                            log.warn("未知的任务状态: {}", status);
                            break;
                    }
                }
            }

        } catch (Exception e) {
            log.error("获取任务状态统计失败", e);
            // 返回空统计
            stats.setNotStarted(0);
            stats.setInProgress(0);
            stats.setPending(0);
            stats.setCompleted(0);
            stats.setCancelled(0);
        }

        return stats;
    }

    @Override
    public long getUserTaskCount(Integer userId) {
        return tasksMapper.getUserTaskCount(userId);
    }

    @Override
    public UserOverview.TaskStatusStats getUserTaskStats(Integer userId) {
        log.debug("获取用户任务状态统计，userId={}", userId);

        UserOverview.TaskStatusStats stats = new UserOverview.TaskStatusStats();

        try {
            List<Map<String, Object>> statusList = tasksMapper.getUserTaskStats(userId);

            stats.setNotStarted(0);
            stats.setInProgress(0);
            stats.setDelay(0);
            stats.setCompleted(0);
            stats.setCancelled(0);

            long totalTasks = 0;

            for (Map<String, Object> map : statusList) {
                String status = (String) map.get("status");
                Long count = (Long) map.get("count");

                if (status != null && count != null) {
                    totalTasks += count;
                    switch (status) {
                        case "NOT_STARTED":
                            stats.setNotStarted(count);
                            break;
                        case "IN_PROGRESS":
                            stats.setInProgress(count);
                            break;
                        case "DELAY":
                            stats.setDelay(count);
                            break;
                        case "COMPLETED":
                            stats.setCompleted(count);
                            break;
                        case "CANCELLED":
                            stats.setCancelled(count);
                            break;
                        default:
                            log.warn("未知的任务状态: {}", status);
                            break;
                    }
                }
            }

            double totalHour = tasksMapper.getUserTotalHours(userId);
            stats.setTotalHour(totalHour);

            double delayRate = 0;
            if (totalTasks > 0) {
                delayRate = (double) stats.getDelay() / totalTasks * 100;
            }
            stats.setDelayRate(delayRate);

        } catch (Exception e) {
            log.error("获取用户任务状态统计失败", e);
            stats.setNotStarted(0);
            stats.setInProgress(0);
            stats.setDelay(0);
            stats.setCompleted(0);
            stats.setCancelled(0);
            stats.setDelayRate(0);
            stats.setTotalHour(0);
        }

        return stats;
    }

    @Override
    public long getManagerTaskCount(Integer userId) {
        return tasksMapper.getManagerTaskCount(userId);
    }

    @Override
    public UserOverview.TaskStatusStats getManagerTaskStats(Integer userId) {
        log.debug("获取项目经理任务状态统计，userId={}", userId);

        UserOverview.TaskStatusStats stats = new UserOverview.TaskStatusStats();

        try {
            List<Map<String, Object>> statusList = tasksMapper.getManagerTaskStats(userId);

            stats.setNotStarted(0);
            stats.setInProgress(0);
            stats.setDelay(0);
            stats.setCompleted(0);
            stats.setCancelled(0);

            long totalTasks = 0;

            for (Map<String, Object> map : statusList) {
                String status = (String) map.get("status");
                Long count = (Long) map.get("count");

                if (status != null && count != null) {
                    totalTasks += count;
                    switch (status) {
                        case "NOT_STARTED":
                            stats.setNotStarted(count);
                            break;
                        case "IN_PROGRESS":
                            stats.setInProgress(count);
                            break;
                        case "DELAY":
                            stats.setDelay(count);
                            break;
                        case "COMPLETED":
                            stats.setCompleted(count);
                            break;
                        case "CANCELLED":
                            stats.setCancelled(count);
                            break;
                        default:
                            log.warn("未知的任务状态: {}", status);
                            break;
                    }
                }
            }

            double totalHour = tasksMapper.getUserTotalHours(userId);
            stats.setTotalHour(totalHour);

            double delayRate = 0;
            if (totalTasks > 0) {
                delayRate = (double) stats.getDelay() / totalTasks * 100;
            }
            stats.setDelayRate(delayRate);

        } catch (Exception e) {
            log.error("获取项目经理任务状态统计失败", e);
            stats.setNotStarted(0);
            stats.setInProgress(0);
            stats.setDelay(0);
            stats.setCompleted(0);
            stats.setCancelled(0);
            stats.setDelayRate(0);
            stats.setTotalHour(0);
        }

        return stats;
    }

    @Override
    public PageBean pageMyTasks(Integer page,
                                Integer pageSize,
                                Integer projectId,
                                Integer Id,
                                String title,
                                String assigneeName,
                                Task.TaskStatus status,
                                LocalDate begin,
                                LocalDate end,
                                Integer currentUserId,
                                String userRole,
                                Boolean isAdmin) {
        log.debug("分页查询我的任务，参数：page={},pageSize={},projectId={},id={},title={},assigneeName={},status={},begin={},end={}, currentUserId={}, userRole={}",
                page, pageSize, projectId, Id, title, assigneeName, status, begin, end, currentUserId, userRole);

        PageInfo<Task> pageBean = PageHelperUtils.safePageQuery(page, pageSize,
                () -> tasksMapper.listMyTasks(
                        projectId,
                        Id,
                        title,
                        assigneeName,
                        status,
                        begin,
                        end,
                        currentUserId,
                        userRole,
                        isAdmin
                )
        );
        log.info("分页查询我的任务成功，结果：{}", pageBean);
        return new PageBean(pageBean.getTotal(), pageBean.getList());
    }




}
