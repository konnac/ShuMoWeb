package com.konnac.service;

public interface ProjectsMemberService {

    //添加项目成员
    void addProjectMember(Integer projectId, Integer userId, String projectRole, int operatorId);

    //批量添加项目成员

    //删除项目成员

}
