package com.konnac.service;

import com.konnac.pojo.BatchResult;
import com.konnac.pojo.PageBean;
import com.konnac.pojo.ProjectMember;

import java.util.List;
import java.util.Map;

public interface ProjectsMemberService {
    // ======================增删改功能======================

    /**
     *  添加项目成员
     */
    void addProjectMember(Integer projectId, Integer userId, String projectRole, Integer operatorId);

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
    PageBean page(Integer page, Integer pageSize, Integer projectId, String name, String realName, String userRole, String department, Boolean isAdmin);

    /**
     * 激活项目成员
     */
    void activateMember(Integer projectId, Integer userId, Integer operatorId);
}
