package com.konnac.controller;

import com.konnac.annotation.RequirePermission;
import com.konnac.enums.PermissionType;
import com.konnac.pojo.BatchResult;
import com.konnac.pojo.Notification;
import com.konnac.pojo.PageBean;
import com.konnac.pojo.Result;
import com.konnac.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    /**
     * 发送普通公告(管理员和项目经理)
     */
    @PostMapping("/send/nor-noti")
    @RequirePermission(value = PermissionType.NOTIFICATION_SEND, checkProject = false, checkTask = false)
    public Result sendNorNotification(@RequestParam List<Integer> userIds, @RequestBody Notification notification) {
        log.info("发送普通公告，userIds={}, notification={}", userIds, notification);
        // 调用Service，得到批量操作结果
        BatchResult batchResult = notificationService.sendCustomNotification(userIds, notification);
        // 包装为统一的Result返回给前端
        if (batchResult.isAllSuccess()) {
            return Result.success("全部添加成功", batchResult);
        } else if (batchResult.getFailureCount() > 0) {
            // 部分成功，使用特定状态码
            return Result.error(201, "部分添加成功", batchResult);
        } else {
            return Result.error("添加失败", batchResult);
        }
    }

    /**
     * 发送系统公告(管理员)
     */
    @PostMapping("/send/sys-noti")
    @RequirePermission(value = PermissionType.NOTIFACATION_SEND_ADMIN, checkProject = false, checkTask = false)
    public Result sendSysNotification(@RequestParam List<Integer> userIds, @RequestBody Notification notification) {
        log.info("发送系统公告，userIds={}, notification={}", userIds, notification);
        // 调用Service，得到批量操作结果
        BatchResult batchResult = notificationService.sendCustomNotification(userIds, notification);
        // 包装为统一的Result返回给前端
        if (batchResult.isAllSuccess()) {
            return Result.success("全部添加成功", batchResult);
        } else if (batchResult.getFailureCount() > 0) {
            // 部分成功，使用特定状态码
            return Result.error(201, "部分添加成功", batchResult);
        } else {
            return Result.error("添加失败", batchResult);
        }
    }





    /**
     * 批量发送通知给项目成员
     */
    @PostMapping("/send/project-members")
    public Result sendNotificationToProjectMembers(Integer projectId, String title, String content,
                                                   Notification.NotificationType type) {
        log.info("批量发送通知给项目成员，参数：projectId={},title={},content={},type={}", projectId, title, content, type);
        notificationService.sendNotificationToProjectMembers(projectId, title, content, type);
        return Result.success();
    }

    /**
     * 批量发送通知给任务成员
     */
    @PostMapping("/send/task-members")
    public Result sendNotificationToTaskMembers(Integer projectId, Integer taskId, String title, String content,
                                                Notification.NotificationType type) {
        log.info("批量发送通知给任务成员，参数：projectId={},taskId={},title={},content={},type={}", projectId, taskId, title, content, type);
        notificationService.sendNotificationToTaskMembers(projectId, taskId, title, content, type);
        return Result.success();
    }

//================操作通知================

    /**
     * 获取用户未读通知数量
     */
    @RequestMapping
    public Result getUnreadCount(Integer userId) {
        log.info("获取用户未读通知数量，参数：userId={}", userId);
        Integer count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 标为已读
     */
    @PostMapping("/{id}/read")
    public Result markAsRead(@PathVariable Integer id, @RequestParam Integer userId) {
        log.info("标记通知为已读: notificationId={}, userId={}", id, userId);
        notificationService.markAsRead(id, userId);
        return Result.success();
    }



    /**
     * 全部已读
     */
    @PostMapping("/read-all")
    public Result markAllAsRead(@RequestParam Integer userId) {
        log.info("全部已读，参数：userId={}", userId);
        notificationService.markAllAsRead(userId);
        return Result.success();
    }


//=============查询通知=============

    /**
     * 分页查询
     */
    @RequestMapping("/page")
    public Result page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            Integer userId,
            String title,
            Notification.NotificationType type,
            String relatedType,
            Integer relatedId,
            Boolean isRead,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ) {
        log.info("分页查询，参数：page={},pageSize={},userId={},title={},type={},relatedType={},relatedId={},isRead={},begin={},end={}", page, pageSize, userId, title, type, relatedType, relatedId, isRead, begin, end);
        PageBean pageBean = notificationService.page(page, pageSize, userId, title, type, relatedType, relatedId, isRead, begin, end);

        return Result.success(pageBean);
    }

    /**
     * 获取通知详情
     */
    @GetMapping("/detail/{id}")
    public Result getById(@PathVariable Integer id) {
        log.info("获取通知详情: notificationId={}", id);
        Notification notification = notificationService.getById(id);
        return Result.success(notification);
    }

    //=============删除通知=============

    /**
     * 批量删除通知
     */
    @PostMapping("/batch")
    public Result deleteBatch(@RequestBody List<Integer> notificationIds, @RequestParam Integer userId) {
        log.info("批量删除通知: notificationIds={}, userId={}", notificationIds, userId);
        BatchResult batchResult = notificationService.deleteBatch(notificationIds, userId);

        // 包装为统一的Result返回给前端
        if (batchResult.isAllSuccess()) {
            return Result.success("全部删除成功", batchResult);
        } else if (batchResult.getFailureCount() > 0) {
            // 部分成功，使用特定状态码
            return Result.error(201, "部分删除成功", batchResult);
        } else {
            return Result.error("添加失败", batchResult);
        }
    }

    /**
     * 删除用户所有已读通知
     */
    @DeleteMapping("/read")
    public Result deleteAllReadNotifications(@RequestParam Integer userId) {
        log.info("删除用户所有已读通知: userId={}", userId);
        notificationService.deleteAllReadNotifications(userId);
        return Result.success();
    }



}
