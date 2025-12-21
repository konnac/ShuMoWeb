// NotificationMapper.java
package com.konnac.mapper;

import com.konnac.pojo.Notification;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface NotificationMapper {

//============================增删改功能============================
    /**
     * 添加通知
     */
    void insert(Notification notification);

    /**
     * 标记通知为已读
     */
    void markAsRead(@Param("notificationId") Integer notificationId,
                    @Param("userId") Integer userId);
    /**
     * 批量添加通知
     */
    void insertBatch(List<Notification> notifications);

    /**
     * 批量删除通知
     */
    void deleteBatch(List<Integer> notificationIds, Integer userId);

    /**
     * 批量删除已读通知
     */
    void deleteAllReadByUserId(Integer userId);

    /**
     * 批量删除某时间前的通知
     */
    void deleteByUserIdAndBeforeDate(Integer userId, LocalDateTime beforeDate);

//============================查询功能============================
    /**
     * 获取用户通知列表(分页查询)
     */
    List<Notification> list(Integer userId,
                            String title,
                            Notification.NotificationType type,
                            String relatedType,
                            Integer relatedId,
                            Boolean isRead,
                            LocalDate begin,
                            LocalDate end);
    /**
     * 获取用户未读通知数量
     */
    Integer countUnreadByUserId(Integer userId);


}