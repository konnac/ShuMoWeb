package com.konnac.pojo;

import lombok.Data;

@Data
public class AdminOverview {
    private long totalUsers;
    private long totalProjects;
    private long totalTasks;
    private long totalValidProjects;
    private TaskStatusStats taskStats;

    @Data
    public class TaskStatusStats {
        private long notStarted;    // 未开始
        private long inProgress;    // 操作中
        private long pending;       // 待完成
        private long completed;     // 已完成
        private long cancelled;     // 已取消
        private double totalHour;   // 总工时
    }
}
