package com.konnac.enums;

import lombok.Getter;

/**
 * 权限类型枚举
 */
@Getter
public enum PermissionType {
    // 项目相关权限
    PROJECT_ADD("添加项目"),
    PROJECT_UPDATE("修改项目"),
    PROJECT_DELETE("删除项目"),
    PROJECT_VIEW("查看自己参与项目"),
    PROJECT_VIEW_ALL("查看所有项目(管理员)"),
    PROJECT_EXPORT("导出项目"),

    // 项目成员权限
    MEMBER_ADD("添加成员"),
    MEMBER_REMOVE("移除成员"),
    MEMBER_UPDATE("更新成员"),
    MEMBER_VIEW("查看成员"),
    MEMBER_ROLE_CHANGE("修改成员角色"),

    // 任务权限
    TASK_ADD("添加任务"),
    TASK_UPDATE("修改任务"),
    TASK_DELETE("删除任务"),
    TASK_ASSIGN("分配任务"),

    // 通知权限
    NOTIFICATION_SEND("发送通知(管理)"),
    NOTIFICATION_SEND_TASK("发送通知(任务)"),
    NOTIFACATION_SEND_ADMIN("发送系统通知(管理员)"),

    // 用户管理权限
    USER_ADD("添加用户"),
    USER_DELETE("删除用户"),
    USER_VIEW_SIMPLE("查看用户信息"),
    USER_VIEW_DETAIL("查看用户详情"),
    USER_UPDATE_ADMIN("修改用户信息(管理员)"),
    USER_UPDATE("修改用户信息(普通用户)"),

    // 文件管理权限
    FILE_UPLOAD("上传文件"),
    FILE_DOWNLOAD("下载文件"),
    FILE_DELETE("删除文件"),
    FILE_VIEW("查看文件"),


    // 其他通用权限
    EXPORT_DATA("导出数据"),
    VIEW_STATS("查看统计"),
    SYSTEM_CONFIG("系统配置");

    private final String description;

    PermissionType(String description) {
        this.description = description;
    }
}