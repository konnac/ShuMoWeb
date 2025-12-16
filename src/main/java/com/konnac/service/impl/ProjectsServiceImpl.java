package com.konnac.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.konnac.mapper.ProjectsMapper;
import com.konnac.pojo.PageBean;
import com.konnac.pojo.Project;
import com.konnac.service.ProjectsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectsServiceImpl implements ProjectsService {
    @Autowired
    private ProjectsMapper projectsMapper;

    @Override
    public void addProject(Project project) {
        project.setCreatedTime(LocalDateTime.now());
        project.setUpdateTime(LocalDateTime.now());
        projectsMapper.addProject(project);
    }

    @Override
    public void deleteProject(Integer[] ids) {
        projectsMapper.deleteProject(ids);
    }

    @Override
    public void updateProject(Project project) {
        project.setUpdateTime(LocalDateTime.now());
        projectsMapper.updateProject(project);
    }

    @Override
    public Project getProjectById(Integer id) {
        return projectsMapper.getProjectById(id);
    }

    @Override
    public PageBean page(Integer page, Integer pageSize, Integer id, String name, String description, Project.Priority priority, Project.ProjectStatus status, LocalDate begin, LocalDate end) {
        //1.设置分页参数
        PageHelper.startPage(page, pageSize);
        //2.执行查询
        List<Project> projectsList = projectsMapper.list(id, name, description, priority, status, begin, end);
        Page<Project> pageBean = (Page<Project>) projectsList;

        //3.获取分页结果
        return new PageBean(pageBean.getTotal(), pageBean.getResult());
    }
}
