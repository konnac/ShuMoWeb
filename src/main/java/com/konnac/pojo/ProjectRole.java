package com.konnac.pojo;

import lombok.Getter;

// 项目角色枚举
@Getter
public enum ProjectRole {
    PROJECT_MANAGER("项目经理"),
    DEVELOPER("开发人员"),
    TESTER("测试人员"),
    PRODUCT_MANAGER("产品经理"),
    DESIGNER("设计师"),
    DOCUMENT_WRITER("文档专员"),
    ANALYST("分析师"),
    OBSERVER("观察者");

    private final String description;

    ProjectRole(String description) {
        this.description = description;
    }

    // 验证项目角色是否合法
    public static boolean isValid(String role) {
        try {
            ProjectRole.valueOf(role); // 尝试将字符串转换为枚举值
            return true; // 如果成功，则返回 true
        } catch (IllegalArgumentException e) {
            return false; // 如果失败，则返回 false
        }
    }
}
