package com.konnac.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
* 任务
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private Integer id;
    private Integer projectId; // 项目ID
    private String title; // 任务标题
    private String description; // 任务描述
    private Integer assigneeId; // 任务执行人ID
    private Integer estimatedHours; // 任务预计工时
    private Integer actualHours; // 任务实际工时
    private LocalDate deadline; // 任务截止日期
    private TaskStatus status; // 任务状态枚举
    private LocalDateTime createdTime; // 创建时间
    private LocalDateTime updateTime; // 修改时间

    @Getter
    public enum TaskStatus {
        NOT_STARTED("未开始"),
        IN_PROGRESS("进行中"),
        PENDING_REVIEW("待评审"),
        COMPLETED("已完成");

        private final String description;

        TaskStatus(String description) {
            this.description = description;
        }

    }

}
