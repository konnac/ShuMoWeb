package com.konnac.mapper;

import com.konnac.pojo.ProjectMember;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProjectsMemberMapper {

    //验证项目成员是否存在
    boolean isMemberExist(Integer projectId, Integer userId);

    //添加项目成员
    void addProjectMember(ProjectMember projectMember);

    //根据项目id和用户id获取项目成员
    ProjectMember getMemberByProjectIdAndUserId(Integer projectId, Integer userId);

    //软删除项目成员
    void updateProjectMember(ProjectMember projectMember);

    //获取项目成员列表
    List<ProjectMember> findActiveByProjectId(Integer projectId);

    //根据用户id获取项目成员
    List<ProjectMember> findByUserId(Integer userId);

    //获取项目成员id列表
    List<Integer> getProjectMembersIds(Integer projectId);

    //分页获取项目成员
    List<ProjectMember> list(Integer projectId, String name, String realName, String userRole, String department);
}
