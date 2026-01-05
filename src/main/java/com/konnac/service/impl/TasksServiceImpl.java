package com.konnac.service.impl;

import com.github.pagehelper.PageInfo;
import com.konnac.annotation.RequirePermission;
import com.konnac.enums.PermissionType;
import com.konnac.exception.BusinessException;
import com.konnac.mapper.TasksMapper;
import com.konnac.pojo.*;
import com.konnac.service.TaskMemberService;
import com.konnac.service.TasksService;
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
                taskMemberService.addTaskMember(task.getId(), task.getAssigneeId(), "ASSIGNEE", null);
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
                //3.验证任务是否已完成
                if (!(task.getStatus() == Task.TaskStatus.COMPLETED)) {
                    log.warn("任务id：{}，任务未完成，不能删除", id);
                    throw new BusinessException("任务未完成，不能删除");
                }
                log.info("任务id：{}，任务删除成功", id);
                task.setStatus(Task.TaskStatus.CANCELLED);
                tasksMapper.updateTask(task); //软删除
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
    @RequirePermission(value = PermissionType.TASK_UPDATE, checkTask = true, projectIdParam = "projectId")
    @Override
    public void updateTask(Task task) {
        //2.更新任务
        log.debug("修改任务，任务信息：{}", task);
        if (task == null) {
            log.warn("修改任务失败，任务信息为空");
            throw new BusinessException("修改任务失败，任务信息为空");
        }
        task.setUpdateTime(LocalDateTime.now());
        tasksMapper.updateTask(task);

        //3.发送通知
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
                         Integer currentUserId) throws BusinessException {
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
                            end
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
                        currentUserId
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
                                String userRole) {
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
                        userRole
                )
        );
        log.info("分页查询我的任务成功，结果：{}", pageBean);
        return new PageBean(pageBean.getTotal(), pageBean.getList());
    }




}
