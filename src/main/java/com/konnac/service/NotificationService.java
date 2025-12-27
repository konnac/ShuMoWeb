package com.konnac.service;

import com.konnac.pojo.BatchResult;
import com.konnac.pojo.Notification;
import com.konnac.pojo.PageBean;
import com.konnac.pojo.ProjectRole;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface NotificationService {
    //============发送通知==========================================

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
     * 构建移除通知
     */

//================操作通知================
    /**
     * 标记通知为已读
     */
    void markAsRead(Integer notificationId, Integer userId);

    /**
     * 获取用户未读通知数量
     */
    Integer getUnreadCount(Integer userId);

    /**
     * 批量已读通知
     */
    void markAsReadBatch(List<Integer> notificationIds, Integer userId);

    /**
     * 全部通知已读
     */
    void markAllAsRead(Integer userId);

//=============查询通知=============

    /**
     * 获取用户通知(分页查询)
     */
    PageBean page(Integer page, Integer pageSize, Integer userId, String title, Notification.NotificationType type, String relatedType, Integer relatedId, Boolean isRead,
                  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end);


//=============删除通知=============

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

//============构建通知=============
    /**
     * 构建移除通知
     */
    void sendRemovalNotification(Integer projectId, Integer userId, Integer operatorId);

    /**
     * 构建受添加通知
     */
    void sendAddNotification(Integer projectId, Integer userId, Integer operatorId);

    /**
     * 构建项目成员角色变更通知
     */
    void sendUpdateMemberRoleNotification(Integer projectId, Integer userId, Integer operatorId, ProjectRole newRole);

    /**
     * 构建任务分配通知
     */
    void sendTaskAssignNotification(Integer taskId, Integer userId, Integer operatorId);

    /**
     * 构建从任务移除通知
     */
    void sendTaskRemovalNotification(Integer taskId, Integer userId, Integer operatorId);

    /**
     * 构建任务完成通知
     */
    void sendTaskCompleteNotification(Integer projectId, Integer taskId, Integer userId, Integer operatorId);

    /**
     * 构建自定义通知
     */
    BatchResult sendCustomNotification(List<Integer> userIds, Notification  notification);
}
