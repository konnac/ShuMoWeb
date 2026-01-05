package com.konnac.pojo;

import lombok.Getter;

@Getter
public enum TaskRole {
    ASSIGNEE("负责人"),
    COLLABORATOR("协作者");

    private final String description;

    TaskRole(String description) {
        this.description = description;
    }

    public static boolean isValid(String role) {
        try {
            TaskRole.valueOf(role);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
