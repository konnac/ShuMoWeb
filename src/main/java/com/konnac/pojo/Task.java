package com.konnac.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
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
    private Integer projectId;
    private String title;
    private String description;
    private Integer assigneeId;
    private Integer estimatedHours;
    private Integer actualHours;
    private LocalDate deadline;
    private TaskStatus status;
    private LocalDateTime createdTime;
    private LocalDateTime updateTime;

    public enum TaskStatus {
        NOT_STARTED("未开始"),
        IN_PROGRESS("进行中"),
        PENDING_REVIEW("待评审"),
        COMPLETED("已完成");

        private final String description;

        TaskStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

}
