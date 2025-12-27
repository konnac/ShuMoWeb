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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        //4.验证用户是否已经加入项目
        if (projectsMemberMapper.isMemberExist(projectId, userId)) {
            throw new BusinessException("用户已是项目成员");
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
     * 批量添加项目成员(允许部分失败)
     */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            rollbackFor = Exception.class,
            timeout = 30)
    @RequirePermission(value = PermissionType.MEMBER_ADD)
    @Override
    public BatchResult addProjectMembers(Integer projectId, List<Integer> userIds, Integer operatorId) {
        if (userIds == null || userIds.isEmpty()) {
            log.warn("成员列表不能为空");
            throw new BusinessException("成员列表不能为空");
        }

        //  1.包装批量结果
        BatchResult batchResult = new BatchResult();
        batchResult.setTotal(userIds.size());

        // 2.批量添加项目成员,失败跳过且添加失败的成员记录
        for (Integer userId : userIds) {
            try {
                addProjectMember(projectId, userId, "  ", operatorId);
                batchResult.addSuccess(userId);
            } catch (BusinessException e) {
                batchResult.addFailure(userId, e.getMessage());
                log.warn("成员添加失败跳过: userId={}, error={}", userId, e.getMessage());
            }
        }

        // 如果全部失败，抛出异常
        if (batchResult.isAllFailure()) {
            throw new BusinessException("所有成员添加失败: " + batchResult.getFailureDetails());
        }

        log.info("批量添加项目成员结果: total={}, successCount={}, failureCount={}", batchResult.getTotal(), batchResult.getSuccessCount(), batchResult.getFailureCount());

        return batchResult;
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

        //3.重复角色抛出异常
        String oldProjectRole = projectMember.getProjectRole();
        projectMember.setProjectRole(newProjectRole);
        if (oldProjectRole.equals(newProjectRole)) {
            log.warn("更新项目成员角色失败: 角色未改变");
            throw new BusinessException("角色未改变");
        }
        projectMember.setUpdateTime(LocalDateTime.now());
        projectsMemberMapper.updateProjectMember(projectMember);

        //3.发送通知给被修改的成员
        notificationService.sendUpdateMemberRoleNotification(projectId, userId, operatorId, ProjectRole.valueOf(newProjectRole));

    }

    //  ======================查询功能======================

    /**
     * 获取项目成员列表
     */
    @RequirePermission(value = PermissionType.MEMBER_VIEW)
    public List<ProjectMember> getProjectMembers(Integer projectId) {
        log.debug("获取项目成员列表: projectId={}", projectId);
        log.info("获取项目成员列表成功");
        return projectsMemberMapper.findActiveByProjectId(projectId);
    }

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
                         String department) throws BusinessException {
        PageInfo<ProjectMember> pageInfo = PageHelperUtils.safePageQuery(page, pageSize, () -> projectsMemberMapper.list(projectId, name, realName, userRole, department));
        return new PageBean(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 获取项目成员角色详细 (待完成)
     */
    public List<ProjectMember> getMembersDetails(Integer projectId) {
        log.debug("获取项目成员角色详细: projectId={}", projectId);
        List<ProjectMember> projectMembers = getProjectMembers(projectId);

        //获取任务统计信息
        for (ProjectMember projectMember : projectMembers) {
            TaskStats taskStats = tasksMapper.getUserTaskStatsInProject(projectId, projectMember.getUserId());
            projectMember.setTaskStats(taskStats);
        }
        log.info("获取项目成员角色详细成功");
        return projectMembers;
    }

    /**
     * 获取用户参与的所有项目
     */
    public List<UserProject> getUserProjects(Integer userId) {
        log.debug("获取用户参与项目: userId={}", userId);
        List<ProjectMember> projectMembers = projectsMemberMapper.findByUserId(userId);

        List<UserProject> userProjects = new ArrayList<>();
        for (ProjectMember projectMember : projectMembers) {
            Project project = projectsMapper.getProjectById(projectMember.getProjectId());
            if (project != null) {
                UserProject userProject = new UserProject();
                userProject.setId(project.getId());
                userProject.setProjectName(project.getName());
                userProject.setProjectStatus(project.getStatus().toString());
                userProject.setProjectDescription(project.getDescription());
                userProject.setProjectRole(projectMember.getProjectRole());
                userProject.setJoinDate(projectMember.getJoinDate());
                userProject.setTaskStats(tasksMapper.getUserTaskStatsInProject(project.getId(), userId));

                //获取用户在该项目的任务统计
                TaskStats taskStats = tasksMapper.getUserTaskStatsInProject(project.getId(), userId);
                userProject.setTaskStats(taskStats);
                userProjects.add(userProject);
            }
        }
        log.info("获取用户参与项目成功");
        return userProjects;
    }

    /**
     * 获取项目中的特定角色成员
     */
    public List<Integer> getProjectMembersByRole(Integer projectId, String projectRole) {
        log.debug("获取项目成员角色: projectId={}, projectRole={}", projectId, projectRole);
        List<ProjectMember> projectMembers = projectsMemberMapper.findActiveByProjectId(projectId);
        List<Integer> userIds = new ArrayList<>();
        //筛选特定角色的成员
        for (ProjectMember projectMember : projectMembers) {
            if (projectMember.getProjectRole().equals(projectRole)) {
                userIds.add(projectMember.getUserId());
            }
        }
        log.info("获取项目成员角色成功");
        return userIds;
    }

    /**
     * 获取项目成员角色id
     */
    public List<Integer> getProjectMembersIds(Integer projectId) {
        log.debug("获取项目成员角色id: projectId={}", projectId);
        log.info("获取项目成员角色id成功");
        return projectsMemberMapper.getProjectMembersIds(projectId);
    }

    // ======================统计功能======================

    /**
     * 获取项目成员统计
     */
    public Map<String, Integer> getProjectMemberStats(Integer projectId) {
        log.debug("获取项目成员统计:projectId={}", projectId);
        List<ProjectMember> members = projectsMemberMapper.findActiveByProjectId(projectId);

        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", members.size());

        //按角色统计
        Map<String, Integer> roleStats = members.stream().collect(Collectors.groupingBy(ProjectMember::getProjectRole,
                Collectors.collectingAndThen(
                        Collectors.counting(),
                        Long::intValue
                )
        ));

        stats.putAll(roleStats);
        log.info("获取项目成员统计成功");
        return stats;
    }


}
