package com.konnac.mapper;

import com.konnac.pojo.ProjectMember;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProjectsMemberMapper {

    //验证项目成员是否存在
    boolean isMemberExist(Integer projectId, Integer userId);

    //添加项目成员
    void addProjectMember(ProjectMember projectMember);
}
