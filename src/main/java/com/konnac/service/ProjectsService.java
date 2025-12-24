package com.konnac.service;

import com.konnac.pojo.PageBean;
import com.konnac.pojo.Project;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public interface ProjectsService {
//==============增删改项目==============
    /**
     *  添加项目
     */
    void addProject(Project project, Integer operatorId);

    /**
     *  删除项目
     */
    void deleteProject(Integer[] ids, Integer operatorId);

    /**
     *  修改项目
     */
    void updateProject(Project project, Integer operatorId);

//=============查询项目================
    /**
     *  根据id查询项目
     */
    Project getProjectById(Integer id);

    /**
     *  分页查询项目
     */
    PageBean page(Integer page, Integer pageSize, Integer id, String name, String description, Project.Priority priority, Project.ProjectStatus status,
                  @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate begin,
                  @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate end);
}
