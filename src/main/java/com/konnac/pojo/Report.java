package com.konnac.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    private Integer id; // 报表id
    private Integer userId; // 用户id
    private String userName; // 用户名称
    private ReportType reportType; // 报表类型
    private String workContent; // 工作内容
    private String completedTasks; // 已完成任务
    private String nextPlan; // 下周计划
    private String problems; // 遇到的问题
    private LocalDateTime submitTime; // 提交时间
    private LocalDateTime updateTime; // 更新时间

    @Getter
    public enum ReportType {
        WEEKLY("周报"),
        MONTHLY("月报");

        private final String description;

        ReportType(String description) {
            this.description = description;
        }

    }
}
