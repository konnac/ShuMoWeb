package com.konnac.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskMember {
    private Integer id; // 成员ID
    private Integer taskId; // 任务ID
    private Integer userId; // 用户ID
    private String username; // 用户名
    private String realName; // 真实姓名
    private String email; // 邮箱
    private String avatar; // 头像
    private String taskRole; // 任务角色
    private String department; // 部门
    private LocalDateTime joinDate; // 加入时间
    private Integer joinBy; // 加入者ID
    private String joinByName; // 加入者名称
    private MemberStatus status; // 状态
    private User.UserRole userRole; // 用户角色
    private LocalDateTime updateTime; // 更新时间
    private LocalDateTime createdTime; // 创建时间

    @Getter
    public enum MemberStatus {
        ACTIVE("正常"),
        INACTIVE("禁用");

        private final String description;

        MemberStatus(String description) {
            this.description = description;
        }
    }
}
