package com.konnac.pojo;

import lombok.Data;

@Data
public class UserOverview {
    private long totalProjects;      // 参与项目总数
    private long totalTasks;          // 我的任务总数
    private long activeProjects;      // 活跃项目数
    private TaskStatusStats taskStats; // 任务状态统计

    @Data
    public static class TaskStatusStats {
        private long notStarted;    // 未开始
        private long inProgress;    // 进行中
        private long delay;         // 已延期
        private long completed;     // 已完成
        private long cancelled;     // 已取消

        private double delayRate;   // 延期率
        private double totalHour;   // 总工时
    }
}
