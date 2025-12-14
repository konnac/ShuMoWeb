package com.konnac.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Project {
    private Integer id;
    private String name;
    private String description;
    private Integer managerId;       // 项目经理ID
    private LocalDate startDate;     // 项目开始日期
    private LocalDate endDate;       // 项目结束日期
    private Priority priority;       // 优先级枚举
    private ProjectStatus status;    // 项目状态枚举
    private LocalDateTime createdTime;
    private LocalDateTime updateTime;
}
