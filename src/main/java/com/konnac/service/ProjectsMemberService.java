package com.konnac.service;

import com.konnac.pojo.ProjectMember;

import java.util.List;

public interface ProjectsMemberService {

    //添加项目成员
    void addProjectMember(Integer projectId, Integer userId, String projectRole, Integer operatorId);

    //批量添加项目成员
    void addProjectMembers(Integer projectId, List<ProjectMember> members);

    //删除项目成员
    void deleteProjectMembers(Integer projectId, Integer[] userIds, String projectRole, Integer operatorId);

}
