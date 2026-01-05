package com.konnac.pojo;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
public class Notification {
    private Integer id;
    private Integer userId;          // 接收用户ID
    private String title;            // 通知标题
    private String content;          // 通知内容
    private NotificationType type;   // 通知类型(枚举)
    private String relatedType;      // 关联类型（项目、任务等）
    private Integer relatedId;       // 关联ID
    private Boolean isRead;          // 是否已读
    private LocalDateTime createdAt; // 创建时间
    private Boolean isDeleted; // 状态

    @Getter
    public enum NotificationType {
        PROJECT_MEMBER_ADDED("项目成员添加"),
        PROJECT_MEMBER_REMOVED("项目成员移除"),
        PROJECT_MEMBER_ROLE_CHANGED("项目成员角色变更"),
        PROJECT_CREATED("项目创建"),
        TASK_ASSIGNED("任务分配"),
        TASK_COMPLETED("任务完成"),
        TASK_MEMBER_REMOVED("任务成员移除"),
        TASK_MEMBER_ROLE_CHANGED("任务成员角色变更"),
        TASK_DUE_SOON("任务即将到期"),
        ANNOUNCEMENT("普通通知"), // relatedID : -1
        SYSTEM_ANNOUNCEMENT("系统通知"); // relatedID : 0

        private final String description;

        NotificationType(String description) {
            this.description = description;
        }
    }

}