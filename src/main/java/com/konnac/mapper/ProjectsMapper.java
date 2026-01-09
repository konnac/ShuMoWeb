package com.konnac.mapper;

import com.konnac.pojo.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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
    List<Project> list(
            @Param("id") Integer id,
            @Param("name") String name,
            @Param("description") String description,
            @Param("priority") Project.Priority priority,
            @Param("status") Project.ProjectStatus status,
            @Param("begin") LocalDate begin,  // 项目开始时间
            @Param("end") LocalDate end,      // 项目结束时间
            @Param("currentUserId") Integer currentUserId
    );

    /**
     *  分页查询项目(查询所有项目)
     */
    List<Project> listAll(
            @Param("id") Integer id,
            @Param("name") String name,
            @Param("description") String description,
            @Param("priority") Project.Priority priority,
            @Param("status") Project.ProjectStatus status,
            @Param("begin") LocalDate begin,  // 项目开始时间
            @Param("end") LocalDate end       // 项目结束时间
    );


    /**
     * 统计项目总数
     */
    long count();

    /**
     * 获取指定用户参与的项目总数
     */
    @Select("SELECT COUNT(DISTINCT p.id) FROM projects p " +
            "WHERE p.manager_id = #{userId} " +
            "OR EXISTS (SELECT 1 FROM project_members pm WHERE pm.project_id = p.id AND pm.user_id = #{userId})")
    long getUserProjectCount(Integer userId);

    /**
     * 获取指定用户参与的活跃项目数 子查询返回任何行 EXISTS返回 true 否则返回 false
     */
    @Select("SELECT COUNT(DISTINCT p.id) FROM projects p " +
            "WHERE (p.manager_id = #{userId} " +
            "OR EXISTS (SELECT 1 FROM project_members pm WHERE pm.project_id = p.id AND pm.user_id = #{userId})) " +
            "AND p.status IN ('IN_PROGRESS', 'DELAYED')")
    long getUserActiveProjectCount(Integer userId);
}
