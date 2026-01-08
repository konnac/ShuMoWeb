package com.konnac.pojo;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;


@Data
public class ProjectMember {

    private Integer id;
    private Integer projectId; // 项目ID
    private Integer userId; // 用户ID
    private String username; // 用户名
    private String realName; // 真实姓名
    private String email; // 邮箱
    private String avatar; // 头像
    private String projectRole; // 项目角色
    private LocalDateTime joinDate; // 加入时间
    private Integer joinBy; // 添加人ID
    private String joinByName; // 添加人姓名
    private MemberStatus status; // 状态
    private User.UserRole userRole; // 系统角色
    private String department; // 部门
    private String phone; // 电话
    private Integer taskCompletionRate; // 任务完成率
    private LocalDateTime updateTime;
    private LocalDateTime createdTime;

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

