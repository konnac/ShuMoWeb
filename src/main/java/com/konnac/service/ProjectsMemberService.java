package com.konnac.service;

import com.konnac.pojo.BatchResult;
import com.konnac.pojo.PageBean;
import com.konnac.pojo.ProjectMember;
import com.konnac.pojo.UserProject;

import java.util.List;
import java.util.Map;

public interface ProjectsMemberService {
    // ======================增删改功能======================

    /**
     *  添加项目成员
     */
    void addProjectMember(Integer projectId, Integer userId, String projectRole, Integer operatorId);

    /**
     * 批量添加项目成员
     */
    BatchResult addProjectMembers(Integer projectId, List<ProjectMember> members);

    /**
     * 删除项目成员
     */
    void deleteProjectMembers(Integer projectId, Integer[] userIds, Integer operatorId);

    /**
     * 更新项目成员角色
     */
    void updateMemberRole(Integer projectId, Integer userId, String newProjectRole, Integer operatorId);

    //  ======================查询功能======================
    /**
     * 分页查询项目成员
     */
    PageBean page(Integer page, Integer pageSize, Integer projectId, String name, String realName, String userRole, String department);

    /**
     * 获取项目成员列表
     */
    List<ProjectMember> getProjectMembers(Integer projectId);

    /**
     * 获取项目成员id
     */
    List<Integer> getProjectMembersIds(Integer projectId);

    /**
     * 获取项目成员角色详细 (待完成)
     */
    List<ProjectMember> getMembersDetails(Integer projectId);

//    /**
//     * 获取用户参与的所有项目
//     */
//    List<UserProject> getUserProjects(Integer userId);

    /**
     * 获取项目中的特定角色成员
     */
    List<Integer> getProjectMembersByRole(Integer projectId, String projectRole);

    /**
     * 获取项目成员统计
     */
    Map<String, Integer> getProjectMemberStats(Integer projectId);
}
