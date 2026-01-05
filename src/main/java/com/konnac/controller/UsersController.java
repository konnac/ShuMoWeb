package com.konnac.controller;

/*
 * 员工管理(users表)
 */

import com.konnac.annotation.RequirePermission;
import com.konnac.pojo.PageBean;
import com.konnac.pojo.Result;
import com.konnac.pojo.User;
import com.konnac.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/users")
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

    //修改员工(管理员)
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
    @RequestMapping
    public Result page(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       Integer id,
                       String username,
                       String realName,
                       User.UserRole role,
                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("分页查询，参数：page={},pageSize={},id={},username={},realName={},role={},begin={},end={}", page, pageSize, id, username, realName, role, begin, end);
        PageBean pageBean = usersService.page(page, pageSize, id, username, realName, role, begin, end);

        return Result.success(pageBean);
    }

    //检查用户名是否存在
    @GetMapping("/check-username")
    public Result checkUsername(@RequestParam String username, @RequestParam(required = false) Integer excludeId) {
        log.info("检查用户名是否存在，参数：username={},excludeId={}", username, excludeId);
        boolean exists = usersService.existsByUsername(username, excludeId);
        return Result.success(!exists);
    }

}
