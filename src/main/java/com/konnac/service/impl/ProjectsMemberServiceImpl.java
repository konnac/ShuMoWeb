package com.konnac.service.impl;

import com.github.pagehelper.PageInfo;
import com.konnac.annotation.RequirePermission;
import com.konnac.enums.PermissionType;
import com.konnac.exception.BusinessException;
import com.konnac.mapper.ProjectsMapper;
import com.konnac.mapper.ProjectsMemberMapper;
import com.konnac.mapper.TasksMapper;
import com.konnac.mapper.UsersMapper;
import com.konnac.pojo.*;
import com.konnac.service.NotificationService;
import com.konnac.service.ProjectsMemberService;
import com.konnac.utils.PageHelperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;


@Slf4j
@Transactional(rollbackFor = Exception.class, timeout = 15)
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

    @Autowired
    private NotificationService notificationService;


    // ======================增删改功能======================

    /**
     * 添加项目成员
     */
    @RequirePermission(value = PermissionType.MEMBER_ADD)
    @Override
    public void addProjectMember(Integer projectId, Integer userId, String projectRole, Integer operatorId) {
        log.debug("添加项目成员: projectId={}, userId={}, projectRole={}, operatorId={}", projectId, userId, projectRole, operatorId);
        //1.验证项目存在
        Project project = projectsMapper.getProjectById(projectId);
        if (project == null) {
            log.warn("项目不存在: projectId={}", projectId);
            throw new BusinessException("项目不存在");
        }

        //2.验证用户存在
        User user = usersMapper.getUserById(userId);
        if (user == null) {
            log.warn("用户不存在: userId={}", userId);
            throw new BusinessException("用户不存在");
        }

        //3.验证项目角色合法性
        if (!ProjectRole.isValid(projectRole)) {
            throw new BusinessException("无效的项目角色");
        }

        //4.禁止通过添加成员的方式设置项目经理角色
        if ("PROJECT_MANAGER".equals(projectRole)) {
            throw new BusinessException("项目经理角色只能在项目编辑时设置，不能通过添加成员方式设置");
        }

        //4.检查用户是否已经是项目成员
        ProjectMember existingMember = projectsMemberMapper.getMemberByProjectIdAndUserId(projectId, userId);
        if (existingMember != null) {
            if (existingMember.getStatus() == ProjectMember.MemberStatus.ACTIVE) {
                throw new BusinessException("用户已是项目成员");
            } else {
                // 成员是 INACTIVE 状态，重新激活
                existingMember.setStatus(ProjectMember.MemberStatus.ACTIVE);
                existingMember.setProjectRole(projectRole);
                existingMember.setJoinBy(operatorId);
                existingMember.setJoinDate(LocalDateTime.now());
                existingMember.setUpdateTime(LocalDateTime.now());
                projectsMemberMapper.updateProjectMember(existingMember);
                log.info("重新激活项目成员: projectId={}, userId={}, projectRole={}, operatorId={}", projectId, userId, projectRole, operatorId);
                return;
            }
        }

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

        //8.发送通知
        notificationService.sendTaskAssignNotification(projectId, userId, operatorId);

        //9.日志
        log.info("添加项目成员: projectId={}, userId={}, projectRole={}, operatorId={}", projectId, userId, projectRole, operatorId);
    }

    /**
     * 删除项目成员
     */
    @RequirePermission(value = PermissionType.MEMBER_REMOVE)
    @Override
    public void deleteProjectMembers(Integer projectId, Integer[] userIds, Integer operatorId) {
        log.info("删除项目成员: projectId={}, userIds={}, operatorId={}", projectId, userIds, operatorId);
        if (userIds == null || userIds.length == 0) {
            throw new BusinessException("成员列表不能为空");
        }


        for (Integer userId : userIds) {
            try {
                //2.检查是否有未完成任务
                int uncompletedTaskCount = tasksMapper.getUncompletedTaskCountByProjectIdAndUserId(projectId, userId);
                if (uncompletedTaskCount > 0) {
                    throw new BusinessException("该用户:" + userId + "有" + uncompletedTaskCount + "未完成的任务，无法移除");
                }

                //3.从项目中移除成员
                ProjectMember projectMember = projectsMemberMapper.getMemberByProjectIdAndUserId(projectId, userId);
                if (projectMember != null) {
                    projectMember.setStatus(ProjectMember.MemberStatus.INACTIVE);
                    projectMember.setUpdateTime(LocalDateTime.now());
                    projectsMemberMapper.updateProjectMember(projectMember);
                }
                log.info("从项目中移除成员: projectId={}, userId={}", projectId, userId);

                //4.对被移除的员工发送通知
                notificationService.sendRemovalNotification(projectId, userId, operatorId);

            } catch (Exception e) {
                log.warn("移除项目成员异常: projectId={}, userId={}", projectId, userId, e);
                throw new BusinessException("移除成员异常: " + e.getMessage());
            }


        }
    }

    /**
     * 更新项目成员角色
     */
    @RequirePermission(value = PermissionType.MEMBER_UPDATE)
    @Override
    public void updateMemberRole(Integer projectId, Integer userId, String newProjectRole, Integer operatorId) {

        //2.更新角色
        log.debug("更新项目成员角色: projectId={}, userId={}, newProjectRole={}, operatorId={}", projectId, userId, newProjectRole, operatorId);
        ProjectMember projectMember = projectsMemberMapper.getMemberByProjectIdAndUserId(projectId, userId);
        if (projectMember == null) {
            log.warn("更新项目成员角色失败: 未找到该成员");
            throw new BusinessException("未找到改项目成员"); //后续改为自定义错误
        }

        //3.禁止将普通成员改为项目经理
        if ("PROJECT_MANAGER".equals(newProjectRole) && !"PROJECT_MANAGER".equals(projectMember.getProjectRole())) {
            throw new BusinessException("项目经理角色只能在项目编辑时设置，不能通过修改成员角色方式设置");
        }

        //4.重复角色抛出异常
        String oldProjectRole = projectMember.getProjectRole();
        projectMember.setProjectRole(newProjectRole);
        if (oldProjectRole.equals(newProjectRole)) {
            log.warn("更新项目成员角色失败: 角色未改变");
            throw new BusinessException("角色未改变");
        }
        projectMember.setUpdateTime(LocalDateTime.now());
        projectsMemberMapper.updateProjectMember(projectMember);

        //5.发送通知给被修改的成员
        notificationService.sendUpdateMemberRoleNotification(projectId, userId, operatorId, ProjectRole.valueOf(newProjectRole));

    }

    //  ======================查询功能======================

    /**
     * 分页查询
     */
    @RequirePermission(value = PermissionType.MEMBER_VIEW)
    public PageBean page(Integer page,
                         Integer pageSize,
                         Integer projectId,
                         String name,
                         String realName,
                         String userRole,
                         String department,
                         Boolean isAdmin) throws BusinessException {
        PageInfo<ProjectMember> pageInfo = PageHelperUtils.safePageQuery(page, pageSize, () -> projectsMemberMapper.list(projectId, name, realName, userRole, department, isAdmin));
        return new PageBean(pageInfo.getTotal(), pageInfo.getList());
    }

    // ======================统计功能======================


    /**
     * 激活项目成员
     */
    @RequirePermission(value = PermissionType.MEMBER_UPDATE)
    @Override
    public void activateMember(Integer projectId, Integer userId, Integer operatorId) {
        log.debug("激活项目成员: projectId={}, userId={}, operatorId={}", projectId, userId, operatorId);
        Project project = projectsMapper.getProjectById(projectId);
        if (project == null) {
            log.warn("项目不存在: projectId={}", projectId);
            throw new BusinessException("项目不存在");
        }

        if (project.getStatus() == Project.ProjectStatus.TERMINATED) {
            log.warn("项目已取消，不能激活成员: projectId={}", projectId);
            throw new BusinessException("项目已取消，不能激活成员");
        }

        ProjectMember projectMember = projectsMemberMapper.getMemberByProjectIdAndUserId(projectId, userId);
        if (projectMember == null) {
            log.warn("项目成员不存在: projectId={}, userId={}", projectId, userId);
            throw new BusinessException("项目成员不存在");
        }



        if (projectMember.getStatus() == ProjectMember.MemberStatus.ACTIVE) {
            log.warn("项目成员已经是激活状态: projectId={}, userId={}", projectId, userId);
            throw new BusinessException("项目成员已经是激活状态");
        }

        if (ProjectRole.PROJECT_MANAGER.name().equals(projectMember.getProjectRole())) {
            long activeManagerCount = projectsMemberMapper.countActiveManagers(projectId);
            if (activeManagerCount >= 1) {
                log.warn("项目已有一个项目经理，不能再激活新的项目经理: projectId={}, userId={}", projectId, userId);
                throw new BusinessException("一个项目只能有一个项目经理");
            }
        }

        projectMember.setStatus(ProjectMember.MemberStatus.ACTIVE);
        projectMember.setUpdateTime(LocalDateTime.now());
        projectsMemberMapper.updateProjectMember(projectMember);

        log.info("激活项目成员成功: projectId={}, userId={}, operatorId={}", projectId, userId, operatorId);
    }


}
