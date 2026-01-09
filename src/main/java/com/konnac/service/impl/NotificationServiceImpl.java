package com.konnac.service.impl;

import com.github.pagehelper.PageInfo;
import com.konnac.annotation.RequirePermission;
import com.konnac.context.UserContext;
import com.konnac.enums.PermissionType;
import com.konnac.exception.BusinessException;
import com.konnac.mapper.*;
import com.konnac.pojo.*;
import com.konnac.service.NotificationService;
import com.konnac.utils.PageHelperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class, timeout = 10)
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private ProjectsMemberMapper projectsMemberMapper;

    @Autowired
    private TasksMapper tasksMapper;

    @Autowired
    private TasksMemberMapper tasksMemberMapper;

    @Autowired
    private ProjectsMapper projectsMapper;

    @Autowired
    private UsersMapper usersMapper;

//============发送通知============

    /**
     * 发送通知
     */
    @Override
    public void sendNotification(Notification notification) {
        log.debug("正在发送通知: {}", notification);
        notification.setIsRead(false);
        notification.setIsDeleted(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(notification);
        log.info("发送通知成功:");
    }

    /**
     * 发送自定义通知
     */
    public BatchResult sendCustomNotification(List<Integer> userIds, Notification notification) {
        log.debug("正在发送自定义通知: userIds={}, notification={}", userIds, notification);
        log.info("接收到的用户ID列表大小: {}, 内容: {}", userIds.size(), userIds);
        
        //验证参数
        if (userIds == null || userIds.isEmpty()) {
            log.warn("用户列表不能为空");
            throw new BusinessException("用户列表不能为空");
        }

        //去重：移除重复的用户ID
        List<Integer> uniqueUserIds = userIds.stream().distinct().collect(java.util.stream.Collectors.toList());
        if (uniqueUserIds.size() < userIds.size()) {
            log.warn("用户ID列表中存在重复项，原始大小: {}, 去重后大小: {}", userIds.size(), uniqueUserIds.size());
        }

        //获取当前用户
        Integer currentUserId = UserContext.getCurrentUserId();
        User currentUser = usersMapper.getUserById(currentUserId);

        //检查当前用户
        if (currentUser == null) {
            throw new BusinessException("当前用户不存在");
        }

        //1.包装批量结果
        BatchResult batchResult = new BatchResult();
        batchResult.setTotal(uniqueUserIds.size());

        for (Integer userId : uniqueUserIds) {
            try {
                sendNotificationToUser(
                        userId,
                        notification.getTitle(),
                        notification.getContent(),
                        notification.getType(),
                        notification.getRelatedType(),
                        notification.getRelatedId());
                batchResult.addSuccess(userId);
            } catch (BusinessException e) {
                batchResult.addFailure(userId, e.getMessage());
                log.warn("发送自定义通知失败: userId={}", userId, e);
            }
        }

        if (batchResult.isAllFailure()) {
            throw new BusinessException("发送自定义通知失败");
        }

        log.info("批量添加任务成员结果: total={}, successCount={}, failureCount={}", batchResult.getTotal(), batchResult.getSuccessCount(), batchResult.getFailureCount());
        return batchResult;
    }

    /**
     * 发送通知给单个用户
     */
    @Override
    public void sendNotificationToUser(Integer userId, String title, String content,
                                       Notification.NotificationType type,
                                       String relatedType, Integer relatedId) {
        log.debug("正在发送通知给单个用户: userId={}, title={}, content={}, type={}, relatedType={}, relatedId={}", userId, title, content, type, relatedType, relatedId);
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRelatedType(relatedType);
        notification.setRelatedId(relatedId);

        sendNotification(notification);
        log.info("发送通知给单个用户成功");
    }

    /**
     * 批量发送通知给多个用户
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @RequirePermission(value = PermissionType.NOTIFICATION_SEND)
    @Override
    public void sendBatchNotificationToUsers(List<Integer> userIds,
                                             String title,
                                             String content,
                                             Notification.NotificationType type,
                                             String relatedType, Integer relatedId) {
        log.debug("正在批量发送通知给多个用户: userIds={}, title={}, content={}, type={}, relatedType={}, relatedId={}", userIds, title, content, type, relatedType, relatedId);
        //验证参数
        if (CollectionUtils.isEmpty(userIds)) {
            throw new BusinessException("用户ID列表不能为空");
        }

        //创建通知列表
        List<Notification> notifications = new ArrayList<>();

        for (Integer userId : userIds) {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setType(type);
            notification.setRelatedType(relatedType);
            notification.setRelatedId(relatedId);
            notification.setIsRead(false);
            notification.setIsDeleted(false);
            notification.setCreatedAt(LocalDateTime.now());
            notifications.add(notification);
        }

        //批量插入
        notificationMapper.insertBatch(notifications);
        log.info("批量发送通知给多个用户成功");
    }

    /**
     * 发送通知给项目成员
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @RequirePermission(value = PermissionType.NOTIFICATION_SEND)
    @Override
    public void sendNotificationToProjectMembers(Integer projectId, String title, String content,
                                                 Notification.NotificationType type) {
        // 需要调用项目服务获取项目成员ID列表
        log.debug("正在发送通知给项目成员: projectId={}, title={}, content={}, type={}", projectId, title, content, type);
        Project project = projectsMapper.getProjectById(projectId);

        if (project == null) {
            log.warn("项目不存在,无法发送通知: projectId={}", projectId);
            throw new BusinessException("项目不存在");
        }

        List<Integer> memberIds = projectsMemberMapper.getProjectMembersIds(projectId);

        if (CollectionUtils.isEmpty(memberIds)) {
            log.warn("项目成员为空,无法发送通知: projectId={}", projectId);
            log.info("项目成员为空，跳过发送通知: projectId={}", projectId);
            throw new BusinessException("项目成员为空");
        }

        sendBatchNotificationToUsers(memberIds, title, content, type, "project", projectId);
        log.info("发送通知给项目成员成功: projectId={}, 成员数{}", projectId, memberIds.size());

    }

    /**
     * 发送通知给任务成员
     */
    @RequirePermission(value = PermissionType.NOTIFICATION_SEND_TASK)
    @Override
    public void sendNotificationToTaskMembers(Integer projectId, Integer taskId, String title, String content,
                                              Notification.NotificationType type) {
        log.debug("正在发送通知给任务成员: projectId={}, taskId={}, title={}, content={}, type={}", projectId, taskId, title, content, type);
        List<Integer> memberIds = tasksMemberMapper.getTaskMembersIds(taskId);
        if (CollectionUtils.isEmpty(memberIds)) {
            log.warn("任务成员为空,无法发送通知: taskId={}", taskId);
            log.info("任务成员为空，跳过发送通知: taskId={}", taskId);
            throw new BusinessException("任务成员为空");
        }
        sendBatchNotificationToUsers(memberIds, title, content, type, "task", taskId);
        log.info("发送通知给任务成员成功: taskId={}, 成员数{}", taskId, memberIds.size());
    }

//================操作通知================

    /**
     * 标为已读
     */
    @Transactional(timeout = 5, rollbackFor = Exception.class)
    @Override
    public void markAsRead(Integer notificationId, Integer userId) {
        log.debug("正在标记通知为已读: notificationId={}, userId={}", notificationId, userId);
        notificationMapper.markAsRead(notificationId, userId);
        log.info("标记通知为已读成功");
    }

    /**
     * 获取用户未读通知数量
     */
    @Override
    public Integer getUnreadCount(Integer userId) {
        log.debug("正在获取用户未读通知数量: userId={}", userId);
        log.info("获取用户未读通知数量成功");
        return notificationMapper.countUnreadByUserId(userId);
    }

    /**
     * 批量标记通知为已读
     */
    @Override
    public void markAsReadBatch(List<Integer> notificationIds, Integer userId) {
        log.debug("正在批量标记通知为已读: notificationIds={}, userId={}", notificationIds, userId);
        for (Integer notificationId : notificationIds) {
            markAsRead(notificationId, userId);
        }
        log.info("批量标记通知为已读成功");
    }

    /**
     * 全部已读
     */
    @Override
    public void markAllAsRead(Integer userId) {
        log.debug("正在全部已读: userId={}", userId);
        notificationMapper.markAllAsRead(userId);
        log.info("全部已读成功");
    }

//=============查询通知=============

    /**
     * 分页查询
     */
    @Override
    public PageBean page(Integer page,
                         Integer pageSize,
                         Integer userId,
                         String title,
                         Notification.NotificationType type,
                         String relatedType,
                         Integer relatedId,
                         Boolean isRead,
                         LocalDate begin,
                         LocalDate end) throws BusinessException {
        try {
            log.debug("正在分页查询通知: page={}, pageSize={}, userId={}, title={}, type={}, relatedType={}, relatedId={}, isRead={}, begin={}, end={}", page, pageSize, userId, title, type, relatedType, relatedId, isRead, begin, end);
            PageInfo<Notification> pageBean = PageHelperUtils.safePageQuery(page, pageSize, () -> notificationMapper.list(userId, title, type, relatedType, relatedId, isRead, begin, end));
            log.info("分页查询通知成功");
            return new PageBean(pageBean.getTotal(), pageBean.getList());
        } catch (Exception e) {
            log.warn("分页查询通知失败: {}", e.getMessage(), e);
            throw new BusinessException("分页查询通知失败:" + e.getMessage(), e);
        }

    }

    @Override
    public Notification getById(Integer notificationId) {
        try {
            log.debug("正在获取通知详情: notificationId={}", notificationId);
            Notification notification = notificationMapper.getById(notificationId);
            log.info("获取通知详情成功: notificationId={}", notificationId);
            return notification;
        } catch (Exception e) {
            log.warn("获取通知详情失败: {}", e.getMessage(), e);
            throw new BusinessException("获取通知详情失败:" + e.getMessage(), e);
        }
    }


//=============删除通知=============

    /**
     * 批量删除通知
     */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            rollbackFor = Exception.class,
            timeout = 30)
    @Override
    public BatchResult deleteBatch(List<Integer> notificationIds, Integer userId) {
        if(notificationIds == null || notificationIds.isEmpty()){
            log.warn("批量删除通知失败: notificationIds为空");
            throw new BusinessException("通知列表不能为空");
        }

        // 包装批量结果
        BatchResult batchResult = new BatchResult();
        batchResult.setTotal(notificationIds.size());

        // 批量删除
        for(Integer notificationId : notificationIds){
            try{
                notificationMapper.delete(notificationId, userId);
                batchResult.addSuccess(notificationId);
            } catch (Exception e){
                batchResult.addFailure(notificationId, e.getMessage());
                log.warn("批量删除通知失败: {}", e.getMessage(), e);
            }
        }

        // 如果全部失败，抛出异常
        if (batchResult.isAllFailure()) {
            throw new BusinessException("所有成员添加失败: " + batchResult.getFailureDetails());
        }

        log.info("批量添加项目成员结果: total={}, successCount={}, failureCount={}", batchResult.getTotal(), batchResult.getSuccessCount(), batchResult.getFailureCount());

        return batchResult;

    }

    /**
     * 批量删除已读通知
     */
    @Transactional(timeout = 30, rollbackFor = Exception.class)
    @Override
    public void deleteAllReadNotifications(Integer userId) {
        log.debug("正在批量删除已读通知: userId={}", userId);
        notificationMapper.deleteAllReadByUserId(userId);
        log.info("批量删除已读通知成功");
    }


//============构建通知=============

    /**
     * 构建移除通知
     */
    public void sendRemovalNotification(Integer projectId, Integer userId, Integer operatorId) {
        try {
            // 获取项目信息
            log.debug("正在发送移除通知: projectId={}, userId={}, operatorId={}", projectId, userId, operatorId);
            Project project = projectsMapper.getProjectById(projectId);
            User operator = usersMapper.getUserById(operatorId);

            // 构建通知
            String title = "您已被移出项目";
            String content = String.format("您已被%s从项目【%s】中移除",
                    operator != null ? operator.getRealName() : "系统",
                    project != null ? project.getName() : projectId);

            sendNotificationToUser(
                    userId, title, content,
                    Notification.NotificationType.PROJECT_MEMBER_REMOVED,
                    "project", projectId
            );
            log.info("发送移除通知成功: projectId={}, userId={}", projectId, userId);
        } catch (Exception e) {
            log.warn("发送移除通知失败: projectId={}, userId={}", projectId, userId, e);
        }
    }

    /**
     * 构建受添加通知
     */
    public void sendAddNotification(Integer projectId, Integer userId, Integer operatorId) {
        try {
            log.debug("正在发送添加通知: projectId={}, userId={}, operatorId={}", projectId, userId, operatorId);
            Project project = projectsMapper.getProjectById(projectId);
            User operator = usersMapper.getUserById(operatorId);

            String title = "您已被添加项目";
            String content = String.format("您已被%s添加到项目【%s】中",
                    operator != null ? operator.getRealName() : "系统",
                    project != null ? project.getName() : projectId);

            sendNotificationToUser(
                    userId, title, content,
                    Notification.NotificationType.PROJECT_MEMBER_ADDED,
                    "project", projectId
            );
            log.info("发送添加通知成功: projectId={}, userId={}", projectId, userId);
        } catch (Exception e) {
            log.warn("发送添加通知失败: projectId={}, userId={}", projectId, userId, e);
        }
    }

    /**
     * 构建更新项目成员角色通知
     */
    public void sendUpdateMemberRoleNotification(Integer projectId, Integer userId, Integer operatorId, ProjectRole newRole) {
        try {
            log.debug("正在发送更新项目成员角色通知: projectId={}, userId={}, operatorId={}, newRole={}", projectId, userId, operatorId, newRole);
            Project project = projectsMapper.getProjectById(projectId);
            User member = usersMapper.getUserById(userId);

            String title = "项目成员角色已更新";
            String content = String.format("项目【%s】的成员【%s】角色已更新为【%s】",
                    project != null ? project.getName() : projectId,
                    member != null ? member.getRealName() : "系统",
                    newRole.getDescription());

            sendNotificationToUser(
                    userId, title, content,
                    Notification.NotificationType.PROJECT_MEMBER_ROLE_CHANGED,
                    "project", projectId
            );
        } catch (Exception e) {
            log.warn("发送更新项目成员角色通知失败: projectId={}, userId={}", projectId, userId, e);
        }
    }

    /**
     * 构建任务分配通知
     */
    public void sendTaskAssignNotification(Integer taskId, Integer userId, Integer operatorId) {
        try {
            log.debug("正在发送任务分配通知: taskId={}, userId={}, operatorId={}", taskId, userId, operatorId);
            Task task = tasksMapper.getTaskById(taskId);
            User user = usersMapper.getUserById(userId);

            String title = "任务已分配";
            String content = String.format("任务【%s】已分配给【%s】",
                    task != null ? task.getTitle() : taskId,
                    user != null ? user.getRealName() : "未知");
            sendNotificationToUser(
                    userId, title, content,
                    Notification.NotificationType.TASK_ASSIGNED,
                    "task", taskId
            );
        } catch (Exception e) {
            log.warn("发送任务分配通知失败: taskId={}, userId={}", taskId, userId, e);
        }

    }

    /**
     * 构建从任务移除通知
     */
    public void sendTaskRemovalNotification(Integer taskId, Integer userId, Integer operatorId) {
        try {
            // 获取任务信息
            log.debug("正在发送从任务移除通知: taskId={}, userId={}, operatorId={}", taskId, userId, operatorId);
            Task task = tasksMapper.getTaskById(taskId);
            User operator = usersMapper.getUserById(operatorId);

            //  构建通知
            String title = "您已被移出任务";
            String content = String.format("任务【%s】已被%s从任务中移除",
                    task != null ? task.getTitle() : taskId,
                    operator != null ? operator.getRealName() : "系统");
            sendNotificationToUser(
                    userId, title, content,
                    Notification.NotificationType.TASK_MEMBER_REMOVED,
                    "task", taskId
            );
            log.info("发送从任务移除通知成功: taskId={}, userId={}", taskId, userId);
        } catch (Exception e) {
            log.warn("发送从任务移除通知失败: taskId={}, userId={}", taskId, userId, e);
        }

    }

    /**
     * 构建任务完成通知
     */
    @Override
    public void sendTaskCompleteNotification(Integer taskId, Integer userId) {
        try {
            log.debug("正在发送任务完成通知: taskId={}, userId={}", taskId, userId);
            Task task = tasksMapper.getTaskById(taskId);
            String title = "任务已完成";
            String content = String.format("任务【%s】已完成，请查看详情"
                    , task != null ? task.getTitle() : taskId
            );
            sendNotificationToUser(
                    userId, title, content,
                    Notification.NotificationType.TASK_COMPLETED,
                    "task", taskId
            );
            log.info("发送任务完成通知成功: taskId={}, userId={}", taskId, userId);
        } catch (Exception e) {
            log.warn("发送任务完成通知失败: taskId={}, userId={}", taskId, userId, e);
        }
    }

    /**
     * 构建更新任务成员角色通知
     */
    @Override
    public void sendUpdateTaskMemberRoleNotification(Integer taskId, Integer userId, Integer operatorId, TaskRole newRole) {
        try {
            log.debug("正在发送更新任务成员角色通知: taskId={}, userId={}, operatorId={}, newRole={}", taskId, userId, operatorId, newRole);
            Task task = tasksMapper.getTaskById(taskId);
            User operator = usersMapper.getUserById(operatorId);

            String title = "任务成员角色已更新";
            String content = String.format("任务【%s】的成员角色已更新为【%s】",
                    task != null ? task.getTitle() : taskId,
                    newRole.getDescription());

            sendNotificationToUser(
                    userId, title, content,
                    Notification.NotificationType.TASK_MEMBER_ROLE_CHANGED,
                    "task", taskId
            );
        } catch (Exception e) {
            log.warn("发送更新任务成员角色通知失败: taskId={}, userId={}", taskId, userId, e);
        }
    }
}