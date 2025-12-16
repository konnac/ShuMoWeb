package com.konnac.mapper;

import com.konnac.pojo.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ProjectsMapper {


    //添加项目
    void addProject(Project project);

    //删除项目
    void deleteProject(Integer[] ids);

    //根据id查询项目
    @Select("select * from projects where id = #{id}")
    Project getProjectById(Integer id);

    //修改员工
    void updateProject(Project project);

    //分页查询
    List<Project> list(Integer id, String name, String description, Project.Priority priority, Project.ProjectStatus status, LocalDate begin, LocalDate end);
}
