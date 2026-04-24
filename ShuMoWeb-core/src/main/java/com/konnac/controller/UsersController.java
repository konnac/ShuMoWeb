package com.konnac.controller;
/**
 * 员工管理(users表)
 */

import com.konnac.PageBean;
import com.konnac.Result;
import com.konnac.User;
import com.konnac.annotation.RequirePermission;

import com.konnac.enums.PermissionType;
import com.konnac.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@Tag(name = "员工管理", description = "员工账号创建、修改、删除、查询等管理接口")
public class UsersController {
    @Autowired
    private UsersService usersService;

    //添加员工
    @PostMapping
    public Result addUser(@RequestBody User user) {
        log.info("添加员工，员工信息：{}", user);
        usersService.addUser(user);
        return Result.success();
    }

    //批量删除员工
    @DeleteMapping("/{ids}")
    public Result deleteUser(@PathVariable Integer[] ids) {
        log.info("删除员工，员工id：{}", ids);
        usersService.deleteUser(ids);
        return Result.success();
    }

    @Operation(summary = "修改员工(管理员)", description = "管理员修改员工信息")
    @PutMapping("/admin/{id}")
    public Result updateUserAdmin(@RequestBody User user) {
        log.info("修改员工，员工信息：{}", user);
        usersService.updateUserAdmin(user);
        return Result.success();
    }

    //修改员工(普通员工)
    @PutMapping("/{id}")
    public Result updateUser(@RequestBody User user) {
        log.info("修改员工，员工信息：{}", user);
        usersService.updateUser(user);
        return Result.success();
    }

    //分页条件查询
    @RequirePermission(value = PermissionType.USER_VIEW_SIMPLE, checkProject = false)
    @RequestMapping
    public Result page(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       Integer id,
                       String username,
                       String realName,
                       User.UserRole role,
                       @RequestParam(required = false) List<User.UserRole> excludeRoles,
                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("分页查询，参数：page={},pageSize={},id={},username={},realName={},role={},excludeRoles={},begin={},end={}", page, pageSize, id, username, realName, role, excludeRoles, begin, end);
        PageBean pageBean = usersService.page(page, pageSize, id, username, realName, role, excludeRoles, begin, end);

        return Result.success(pageBean);
    }

    @Operation(summary = "检查用户名是否存在", description = "验证用户名是否已被使用")
    @GetMapping("/check-username")
    public Result checkUsername(@RequestParam String username, @RequestParam(required = false) Integer excludeId) {
        log.info("检查用户名是否存在，参数：username={},excludeId={}", username, excludeId);
        boolean exists = usersService.existsByUsername(username, excludeId);
        return Result.success(!exists);
    }

    @Operation(summary = "修改密码", description = "修改当前用户或指定用户的密码")
    @PutMapping("/change-password")
    public Result changePassword(@RequestBody User user) {
        log.info("修改密码，用户id：{}", user.getId());
        usersService.changePassword(user.getId(), user.getOldPassword(), user.getPassword());
        return Result.success();
    }

    //获取项目经理列表
    @GetMapping("/project-managers")
    public Result getProjectManagers() {
        log.info("获取项目经理列表");
        List<User> projectManagers = usersService.getProjectManagers();
        return Result.success(projectManagers);
    }

    //获取可用用户列表（用于项目成员选择）
    @RequirePermission(value = PermissionType.MEMBER_ADD)
    @GetMapping("/{projectId}/available-users")
    public Result getAvailableUsers(@PathVariable Integer projectId) {
        log.info("获取可用用户列表，项目ID：{}", projectId);
        List<User> availableUsers = usersService.getAvailableUsers();
        return Result.success(availableUsers);
    }

    @Operation(summary = "获取通知接收人列表", description = "获取所有可以接收通知的用户列表")
    @RequirePermission(value = PermissionType.NOTIFICATION_MEMBERVIEW,checkProject = false)
    @GetMapping("/notification-recipients")
    public Result getNotificationRecipients() {
        log.info("获取通知接收人列表");
        List<User> recipients = usersService.getNotificationRecipients();
        return Result.success(recipients);
    }
}

