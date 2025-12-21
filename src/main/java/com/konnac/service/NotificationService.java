package com.konnac.service;

import com.konnac.pojo.Notification;
import com.konnac.pojo.PageBean;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface NotificationService {
    /**
     * 发送通知
     */
    void sendNotification(Notification notification);

    /**
     * 发送通知给用户
     */
    void sendNotificationToUser(Integer userId, String title, String content,
                                Notification.NotificationType type,
                                String relatedType, Integer relatedId);

    /**
     * 标记通知为已读
     */
    void markAsRead(Integer notificationId, Integer userId);

    /**
     * 获取用户通知(分页查询)
     */
    PageBean page(Integer page, Integer pageSize, Integer userId, String title, Notification.NotificationType type, String relatedType, Integer relatedId, Boolean isRead,
                  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end);

    /**
     * 获取用户未读通知数量
     */
    Integer getUnreadCount(Integer userId);

    /**
     * 批量发送通知给多个用户
     */
    void sendBatchNotificationToUsers(List<Integer> userIds, String title, String content,
                                      Notification.NotificationType type,
                                      String relatedType, Integer relatedId);

    /**
     * 发送通知给项目所有成员
     */
    void sendNotificationToProjectMembers(Integer projectId, String title, String content,
                                          Notification.NotificationType type);

    /**
     * 发送通知给任务所有成员
     */
    void sendNotificationToTaskMembers(Integer projectId, Integer taskId, String title, String content,
                                       Notification.NotificationType type);
    /**
     * 批量删除通知
     */
    void deleteBatch(List<Integer> notificationIds, Integer userId);

    /**
     * 删除用户所有已读通知
     */
    void deleteAllReadNotifications(Integer userId);

    /**
     * 删除用户某个时间前的通知
     */
    void deleteNotificationsBeforeDate(Integer userId, LocalDateTime beforeDate);

}