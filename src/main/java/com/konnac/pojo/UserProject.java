package com.konnac.pojo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserProject {
    private Integer id;
    private String projectName;
    private String projectDescription;
    private String projectStatus;
    private String projectRole;
    private LocalDateTime joinDate;
    private TaskStats taskStats; // 我的任务统计
    private Double projectProgress; // 项目总进度
}
