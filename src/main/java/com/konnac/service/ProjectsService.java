package com.konnac.service;

import com.konnac.pojo.PageBean;
import com.konnac.pojo.Project;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public interface ProjectsService {

    void addProject(Project project);

    void deleteProject(Integer[] ids);

    void updateProject(Project project);

    Project getProjectById(Integer id);

    PageBean page(Integer page, Integer pageSize, Integer id, String name, String description, Project.Priority priority, Project.ProjectStatus status,
                  @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate begin,
                  @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate end);
}
