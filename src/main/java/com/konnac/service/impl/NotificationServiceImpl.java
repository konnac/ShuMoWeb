// NotificationServiceImpl.java
package com.konnac.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.konnac.exception.BusinessException;
import com.konnac.mapper.NotificationMapper;
import com.konnac.pojo.Notification;
import com.konnac.pojo.PageBean;
import com.konnac.pojo.User;
import com.konnac.service.NotificationService;
import com.konnac.service.ProjectsMemberService;
import com.konnac.service.ProjectsService;
import com.konnac.service.TasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private ProjectsMemberService projectsMemberService;

    @Autowired
    private TasksService tasksService;

    /**
     * 发送通知
     */
    @Override
    public void sendNotification(Notification notification) {
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(notification);

        // 可以在这里添加其他通知方式，如WebSocket推送、邮件、短信等
        // 例如：webSocketService.sendToUser(notification.getUserId(), notification);
    }

    /**
     * 发送通知给单个用户
     */
    @Override
    public void sendNotificationToUser(Integer userId, String title, String content,
                                       Notification.NotificationType type,
                                       String relatedType, Integer relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRelatedType(relatedType);
        notification.setRelatedId(relatedId);

        sendNotification(notification);
    }


    /**
     * 标为已读
     */
    @Override
    public void markAsRead(Integer notificationId, Integer userId) {
        notificationMapper.markAsRead(notificationId, userId);
    }

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
                          LocalDate end){
        //1.设置分页参数
        PageHelper.startPage(page, pageSize);
        //2.执行查询
        List<Notification> notificationList = notificationMapper.list(userId, title, type, relatedType, relatedId, isRead, begin, end);
        Page<Notification> pageBean = (Page<Notification>) notificationList;

        //3.获取分页结果
        return new PageBean(pageBean.getTotal(), pageBean.getResult());
    }

    /**
     * 获取用户未读通知数量
     */
    @Override
    public Integer getUnreadCount(Integer userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    /**
     * 批量发送通知给多个用户
     */
    @Override
    public void sendBatchNotificationToUsers(List<Integer> userIds, String title, String content,
                                             Notification.NotificationType type,
                                             String relatedType, Integer relatedId) {
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
    }

    /**
     * 发送通知给项目成员
     */
    @Override
    public void sendNotificationToProjectMembers(Integer projectId, String title, String content,
                                                 Notification.NotificationType type) {
        //验证权限
        // 需要调用项目服务获取项目成员ID列表
        List<Integer> memberIds = projectsMemberService.getProjectMembersIds(projectId);
        sendBatchNotificationToUsers(memberIds, title, content, type, "project", projectId);
    }

    /**
     * 发送通知给任务成员
     */
    @Override
    public void sendNotificationToTaskMembers(Integer projectId, Integer taskId, String title, String content,
                                              Notification.NotificationType type) {
        // 需要调用任务服务获取任务成员ID列表
        List<Integer> memberIds = tasksService.getTaskMembersId(projectId, taskId);
        sendBatchNotificationToUsers(memberIds, title, content, type, "task", taskId);
    }

    /**
     * 批量删除通知
     */
    @Override
    public void deleteBatch(List<Integer> notificationIds, Integer userId) {
        notificationMapper.deleteBatch(notificationIds, userId);
    }

    /**
     * 批量删除已读通知
     */
    @Override
    public void deleteAllReadNotifications(Integer userId) {
        notificationMapper.deleteAllReadByUserId(userId);
    }

    /**
     * 批量删除某时间前通知
     */
    @Override
    public void deleteNotificationsBeforeDate(Integer userId, LocalDateTime beforeDate) {
        notificationMapper.deleteByUserIdAndBeforeDate(userId, beforeDate);
    }
}