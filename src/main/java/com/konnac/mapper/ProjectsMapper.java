package com.konnac.mapper;

import com.konnac.pojo.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ProjectsMapper {

//==============增删改项目==============
    /**
     *  添加项目
     */
    void addProject(Project project);

    /**
     *  修改项目 (软删除)
     */
    Integer updateProject(Project project);

//==============查询项目================
    /**
     *  根据id查询项目
     */
    @Select("select * from projects where id = #{id}")
    Project getProjectById(Integer id);

    /**
     *  根据名称查询项目
     */
    @Select("select * from projects where name = #{name}")
    Project getProjectByName(String name);

    /**
     *  分页查询项目(查询我参与的项目)
     */
    List<Project> list(Integer id, String name, String description, Project.Priority priority, Project.ProjectStatus status, LocalDate begin, LocalDate end, Integer currentUserId);

    /**
     *  分页查询项目(查询所有项目)
     */
    List<Project> listAll(Integer id, String name, String description, Project.Priority priority, Project.ProjectStatus status, LocalDate begin, LocalDate end);

}
