package com.konnac.controller;

import com.konnac.pojo.PageBean;
import com.konnac.pojo.Project;
import com.konnac.pojo.Result;
import com.konnac.service.ProjectsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/*
*  项目管理
*/
@Slf4j
@RestController
@RequestMapping("/projects")
public class ProjectsController {
    @Autowired
    private ProjectsService projectsService;

    //添加项目
    @PostMapping
    public Result addProject(@RequestBody Project project) {
        log.info("添加项目，项目信息：{}", project);
        projectsService.addProject(project);
        return Result.success();
    }

    //批量删除项目
    @DeleteMapping("/{ids}")
    public Result deleteProject(@PathVariable Integer[] ids) {
        log.info("删除项目，项目id：{}", ids);
        projectsService.deleteProject(ids);
        return Result.success();
    }

    //根据id查询项目
    @GetMapping("/{id}")
    public Result getProject(@PathVariable Integer id) {
        log.info("查询项目，项目id：{}", id);
        Project project = projectsService.getProjectById(id);
        return Result.success(project);
    }

    //修改项目
    @PutMapping
    public Result updateProject(@RequestBody Project project) {
        log.info("修改项目，项目信息：{}", project);
        projectsService.updateProject(project);
        return Result.success();
    }

    //分页条件查询
    @RequestMapping
    public Result page(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       Integer id,
                       String name,
                       String description,
                       Project.Priority priority,
                       Project.ProjectStatus status,
                       @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate begin,
                       @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate end){
        log.info("分页查询，参数：page={},pageSize={},id={},name={},description={},priority={},status={},begin={},end={}", page, pageSize, id, name, description, priority, status, begin, end);
        PageBean pageBean = projectsService.page(page, pageSize, id, name, description, priority, status, begin, end);

        return Result.success(pageBean);
    }

}
