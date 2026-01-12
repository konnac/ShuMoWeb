package com.konnac.mapper;

import com.konnac.pojo.ProjectMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProjectsMemberMapper {

    //添加项目成员
    void addProjectMember(ProjectMember projectMember);

    //根据项目id和用户id获取项目成员
    ProjectMember getMemberByProjectIdAndUserId(Integer projectId, Integer userId);

    //软删除项目成员
    void updateProjectMember(ProjectMember projectMember);

    //获取项目成员id列表
    List<Integer> getProjectMembersIds(Integer projectId);

    //分页获取项目成员
    List<ProjectMember> list(Integer projectId, String name, String realName, String userRole, String department, Boolean isAdmin);

    //批量禁用项目成员
    void disableMembersByProjectId(Integer projectId);

    /**
     * 获取项目中激活状态的项目经理数量
     */
    @Select("SELECT COUNT(*) FROM project_members WHERE project_id = #{projectId} AND project_role = 'PROJECT_MANAGER' AND status = 'ACTIVE'")
    long countActiveManagers(Integer projectId);
}
