package com.konnac.pojo;

import lombok.Getter;

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

    // 检查是否为最高优先级
    public boolean isHighest() {
        return this == HIGH;
    }

    // 检查是否为最低优先级
    public boolean isLowest() {
        return this == LOW;
    }
}
