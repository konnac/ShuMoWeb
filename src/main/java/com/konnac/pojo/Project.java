package com.konnac.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
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
    private String managerName;     // 项目经理姓名
    private LocalDate startDate;     // 项目开始日期
    private LocalDate endDate;       // 项目结束日期
    private Priority priority;       // 优先级枚举
    private ProjectStatus status;    // 项目状态枚举
    private LocalDateTime createdTime;
    private LocalDateTime updateTime;

    @Getter
    public enum Priority {
        HIGH("高", 3),
        MEDIUM("中", 2),
        LOW("低", 1);

        private final String description;
        private final Integer level;  // 优先级等级，数字越大优先级越高

        // 构造函数
        Priority(String description, Integer level) {
            this.description = description;
            this.level = level;
        }

        // 根据等级获取优先级
        public static Priority getByLevel(Integer level) {
            for (Priority priority : values()) {
                if (priority.getLevel().equals(level)) {
                    return priority;
                }
            }
            return MEDIUM;  // 默认返回中等
        }

        // 获取描述
        public String getDescription() {
            return description;
        }

        // 获取等级
        public Integer getLevel() {
            return level;
        }

        // 检查是否为最高优先级
        public boolean isHighest() {
            return this == HIGH;
        }

        // 检查是否为最低优先级
        public boolean isLowest() {
            return this == LOW;
        }
    }

    @Getter
    public enum ProjectStatus {
        IN_PROGRESS("进行中"),
        COMPLETED("已完成"),
        DELAYED("已延期"),
        TERMINATED("已取消");

        private final String description;

        ProjectStatus(String description) {
            this.description = description;
        }
    }
}


