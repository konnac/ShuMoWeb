package com.konnac.pojo;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class ProjectMember {

    private Integer projectId;
    private Integer userId;
    private String username;
    private String realName;
    private String email;
    private String avatar;
    private String projectRole;
    private LocalDateTime joinDate;
    private Integer joinBy; // 添加人ID
    private String joinByName; // 添加人姓名
    private MemberStatus status;
    private String userRole; // 系统角色
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



//    private TaskStats taskStats; // 任务统计

