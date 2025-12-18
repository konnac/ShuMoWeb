package com.konnac.service.impl;

import com.konnac.mapper.ProjectsMapper;
import com.konnac.mapper.ProjectsMemberMapper;
import com.konnac.mapper.TasksMapper;
import com.konnac.mapper.UsersMapper;
import com.konnac.pojo.Project;
import com.konnac.pojo.ProjectMember;
import com.konnac.pojo.ProjectRole;
import com.konnac.pojo.User;
import com.konnac.service.ProjectsMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectsMemberServiceImpl implements ProjectsMemberService {
    @Autowired
    private ProjectsMemberMapper projectsMemberMapper;

    @Autowired
    private ProjectsMapper projectsMapper;

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private TasksMapper tasksMapper;

    @Transactional
    @Override
    public void addProjectMember(Integer projectId, Integer userId, String projectRole, Integer operatorId) {
        //1.验证项目存在
        Project project = projectsMapper.getProjectById(projectId);
        if (project == null) {
            throw new RuntimeException("项目不存在"); //抛出异常
        }

        //2.验证用户存在
        User user = usersMapper.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        //3.验证项目角色合法性
        if (!ProjectRole.isValid(projectRole)) {
            throw new RuntimeException("无效的项目角色");
        }

        //4.验证用户是否已经加入项目
        if (projectsMemberMapper.isMemberExist(projectId, userId)) {
            throw new RuntimeException("用户已是项目成员");
        }

        //5.验证添加权限(待做)

        //6.创建项目成员记录
        ProjectMember projectMember = new ProjectMember();
        projectMember.setProjectId(projectId);
        projectMember.setUserId(userId);
        projectMember.setProjectRole(projectRole);
        projectMember.setJoinBy(operatorId);
        projectMember.setStatus(ProjectMember.MemberStatus.ACTIVE);
        projectMember.setJoinDate(LocalDateTime.now());

        //7.添加项目成员
        projectsMemberMapper.addProjectMember(projectMember);
    }

    /*
     * 批量添加项目成员
     */
    @Transactional
    @Override
    public void addProjectMembers(Integer projectId, List<ProjectMember> members) {
        for (ProjectMember member : members){
            addProjectMember(projectId, member.getUserId(), member.getProjectRole(), member.getJoinBy());
        }
    }

    /*
     * 删除项目成员
     */
    @Transactional
    @Override
    public void deleteProjectMembers(Integer projectId, Integer[] userIds, String projectRole, Integer operatorId) {
        for (Integer userId : userIds) {
            //1.验证权限(未完成)

            //2.检查是否有未完成任务
            int uncompletedTaskCount = tasksMapper.getUncompletedTaskCountByProjectIdAndUserId(projectId, userId);
            if (uncompletedTaskCount > 0) {
                throw new RuntimeException("该用户有" + uncompletedTaskCount + "未完成的任务，无法移除");
            }

            //3.从项目中移除成员
            ProjectMember projectMember = projectsMemberMapper.getMemberByProjectIdAndUserId(projectId, userId);
            if (projectMember != null) {
                projectMember.setStatus(ProjectMember.MemberStatus.INACTIVE);
                projectMember.setUpdateTime(LocalDateTime.now());
                projectsMemberMapper.updateProjectMember(projectMember);
            }


        }
    }
}
