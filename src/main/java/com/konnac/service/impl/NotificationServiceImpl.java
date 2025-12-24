package com.konnac.service.impl;

import com.github.pagehelper.PageInfo;
import com.konnac.exception.BusinessException;
import com.konnac.mapper.*;
import com.konnac.pojo.*;
import com.konnac.service.NotificationService;
import com.konnac.service.ProjectsMemberService;
import com.konnac.service.TasksService;
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
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    //    @Autowired
//    private ProjectsMemberService projectsMemberService;
    @Autowired
    private ProjectsMemberMapper projectsMemberMapper;

    @Autowired
    private TasksMapper tasksMapper;

    @Autowired
    private ProjectsMapper projectsMapper;

    @Autowired
    private UsersMapper usersMapper;

//============发送通知============

    /**
     * 发送通知
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void sendNotification(Notification notification) {
        log.debug("正在发送通知: {}", notification);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(notification);
        log.info("发送通知成功:");
    }

    /**
     * 发送通知给单个用户
     */
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    @Override
    public void sendBatchNotificationToUsers(List<Integer> userIds, String title, String content,
                                             Notification.NotificationType type,
                                             String relatedType, Integer relatedId) {
        log.debug("正在批量发送通知给多个用户: userIds={}, title={}, content={}, type={}, relatedType={}, relatedId={}", userIds, title, content, type, relatedType, relatedId);
        if (CollectionUtils.isEmpty(userIds)) {
            throw new BusinessException("用户ID列表不能为空");
        }

        List<Notification> notifications = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Integer userId : userIds) {
            Notification notification = new Notification();

            notification.setUserId(userId);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setType(type);
            notification.setRelatedType(relatedType);
            notification.setRelatedId(relatedId);
            notification.setIsRead(false);
            notification.setCreatedAt(now);

            notifications.add(notification);
        }

        notificationMapper.insertBatch(notifications);
        log.info("批量发送通知给多个用户成功");
    }

    /**
     * 发送通知给项目成员
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void sendNotificationToProjectMembers(Integer projectId, String title, String content,
                                                 Notification.NotificationType type) {
        //验证权限(未做)
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
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void sendNotificationToTaskMembers(Integer projectId, Integer taskId, String title, String content,
                                              Notification.NotificationType type) {
        log.debug("正在发送通知给任务成员: projectId={}, taskId={}, title={}, content={}, type={}", projectId, taskId, title, content, type);
        // 需要调用任务服务获取任务成员ID列表
        List<Integer> memberIds = tasksMapper.getTaskMembersId(projectId, taskId);
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
    @Transactional(readOnly = true)
    @Override
    public Integer getUnreadCount(Integer userId) {
        log.debug("正在获取用户未读通知数量: userId={}", userId);
        log.info("获取用户未读通知数量成功");
        return notificationMapper.countUnreadByUserId(userId);
    }

    /**
     * 批量标记通知为已读
     */
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(readOnly = true, timeout = 10)
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


//=============删除通知=============

    /**
     * 批量删除通知
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteBatch(List<Integer> notificationIds, Integer userId) {
        log.debug("正在批量删除通知: notificationIds={}, userId={}", notificationIds, userId);
        notificationMapper.deleteBatch(notificationIds, userId);
        log.info("批量删除通知成功");
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

    /**
     * 批量删除某时间前通知
     */
    @Transactional(timeout = 30, rollbackFor = Exception.class)
    @Override
    public void deleteNotificationsBeforeDate(Integer userId, LocalDateTime beforeDate) {
        log.debug("正在批量删除某时间前通知: userId={}, beforeDate={}", userId, beforeDate);
        notificationMapper.deleteByUserIdAndBeforeDate(userId, beforeDate);
        log.info("批量删除某时间前通知成功");
    }

//============构建通知=============

    /**
     * 构建移除通知
     */
    public void sendRemovalNotification(Integer projectId, Integer userId, Integer operatorId) {
        try {
            log.debug("正在发送移除通知: projectId={}, userId={}, operatorId={}", projectId, userId, operatorId);
            Project project = projectsMapper.getProjectById(projectId);
            User operator = usersMapper.getUserById(operatorId);

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
    public void sendAddNotification(Integer projectId, Integer userId, Integer operatorId){
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
            User operator = usersMapper.getUserById(operatorId);

            String title = "项目成员角色已更新";
            String content = String.format("项目【%s】的成员【%s】角色已更新为【%s】",
                    project != null ? project.getName() : projectId,
                    operator != null ? operator.getRealName() : "系统",
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

    /**
     * 构建任务完成通知
     */

    /**
     * 构建自定义通知
     */
    public void sendCustomNotification(Integer projectId, Integer userId, String title, String content){
        try {
            //验证发送通知权限(未做)
            log.debug("正在发送自定义通知: projectId={}, userId={}, title={}, content={}", projectId, userId, title, content);
            sendNotificationToUser(
                    userId, title, content,
                    Notification.NotificationType.PROJECT_MEMBER_ROLE_CHANGED,
                    "project", projectId
            );
        } catch (Exception e) {
            log.warn("发送自定义通知失败: projectId={}, userId={}", projectId, userId, e);
        }
    }

}