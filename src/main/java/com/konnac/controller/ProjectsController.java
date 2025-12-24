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

//===============增删改项目==============

    /**
     *  添加项目
     */
    @PostMapping
    public Result addProject(@RequestBody Project project) {
        log.info("添加项目，项目信息：{}", project);
        projectsService.addProject(project, project.getManagerId());
        return Result.success();
    }

    /**
     *  删除项目
     */
    @DeleteMapping("/{ids}")
    public Result deleteProject(@PathVariable Integer[] ids, Integer operatorId) {
        log.info("删除项目，项目id：{},操作人id: {} ", ids, operatorId);
        projectsService.deleteProject(ids, operatorId);
        return Result.success();
    }

    /**
     *  修改项目
     */
    @PutMapping
    public Result updateProject( @PathVariable Integer operatorId, @RequestBody Project project) {
        log.info("修改项目， 项目信息：{}, 操作人id: {}", project, operatorId);
        projectsService.updateProject(project, project.getManagerId());
        return Result.success();
    }

//==============查询项目================
    /**
     *  根据id查询项目
     */
    @GetMapping("/{id}")
    public Result getProject(@PathVariable Integer id) {
        log.info("查询项目，项目id：{}", id);
        Project project = projectsService.getProjectById(id);
        return Result.success(project);
    }



    /**
     *  分页查询项目
     */
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
