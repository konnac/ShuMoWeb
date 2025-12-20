package com.konnac.pojo;

import lombok.Data;

@Data
public class TaskStats {
    private Integer totalTasks; // 总任务数
    private Integer notStartedTasks; // 未开始的任务数
    private Integer inProgressTasks; // 进行中的任务数
    private Integer pendingReviewTasks; // 等待审核的任务数
    private Integer completedTasks; // 已完成任务数
    private Integer totalHours; // 总工时
}
