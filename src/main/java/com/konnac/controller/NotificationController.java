package com.konnac.controller;

import com.konnac.pojo.Notification;
import com.konnac.pojo.PageBean;
import com.konnac.pojo.Result;
import com.konnac.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

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
    ){
        log.info("分页查询，参数：page={},pageSize={},userId={},title={},type={},relatedType={},relatedId={},isRead={},begin={},end={}", page, pageSize, userId, title, type, relatedType, relatedId, isRead, begin, end);
        PageBean pageBean = notificationService.page(page, pageSize, userId, title, type, relatedType, relatedId, isRead, begin, end);

        return Result.success(pageBean);
    }

    /**
     * 获取用户未读通知数量
     */
    @RequestMapping("/unreadCount")
    public Result getUnreadCount(Integer userId){
        log.info("获取用户未读通知数量，参数：userId={}", userId);
        Integer count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 标为已读
     */

}
