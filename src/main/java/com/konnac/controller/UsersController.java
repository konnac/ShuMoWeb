package com.konnac.controller;

/*
 * 员工管理(users表)
 */

import com.konnac.pojo.PageBean;
import com.konnac.pojo.Result;
import com.konnac.pojo.User;
import com.konnac.service.UsersService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
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

    //根据id查询员工
    @GetMapping("/{id}")
    public Result getUser(@PathVariable Integer id) {
        log.info("查询员工，员工id：{}", id);
        User user = usersService.getUserById(id);
        return Result.success(user);
    }

    //修改员工
    @PutMapping
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

}
