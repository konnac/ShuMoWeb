package com.konnac.annotation;

import com.konnac.enums.PermissionType;

import java.lang.annotation.*;

/**
 * 权限验证注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 权限类型（必填）
     * 从PermissionType(枚举类)中选择
     */
    PermissionType value();

    /**
     * 是否需要检查项目ID
     * 默认为true，表示需要验证用户对该项目的权限,否则表示不需要验证用户对该项目的权限
     */
    boolean checkProject() default true;

    /**
     * 是否需要检查任务ID
     * 默认为false,表示不需要验证用户对该任务的权限,否则表示需要验证用户对该任务的权限
     */
    boolean checkTask() default false;

    /**
     * 项目ID参数名称
     * 当checkProject为true时，需要指定哪个参数是项目ID
     * 其实没怎么用上 自动提取自己从参数中识别,或者url中的参数都是可以的,不然有点麻烦
     */
    String projectIdParam() default "";

    /**
     * 操作者ID参数名称
     * 默认从第一个参数中自动识别
     * 冗余定义,因为operator一般都是cumrrentUser
     */
    String operatorParam() default "";

    /**
     * 权限失败时的错误信息
     */
    String errorMessage() default "无权限执行此操作";
}