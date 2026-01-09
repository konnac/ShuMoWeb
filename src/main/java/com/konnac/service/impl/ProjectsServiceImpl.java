package com.konnac.service.impl;


import com.github.pagehelper.PageInfo;
import com.konnac.annotation.RequirePermission;
import com.konnac.context.UserContext;
import com.konnac.enums.PermissionType;
import com.konnac.exception.BusinessException;
import com.konnac.mapper.ProjectsMapper;
import com.konnac.mapper.TasksMapper;
import com.konnac.mapper.UsersMapper;
import com.konnac.mapper.ProjectsMemberMapper;
import com.konnac.mapper.TasksMemberMapper;
import com.konnac.pojo.PageBean;
import com.konnac.pojo.Project;
import com.konnac.pojo.ProjectMember;
import com.konnac.pojo.User;
import com.konnac.service.NotificationService;
import com.konnac.service.ProjectsService;
import com.konnac.utils.AuthUtils;
import com.konnac.utils.PageHelperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Slf4j
@Transactional(rollbackFor = Exception.class, timeout = 15)
@Service
public class ProjectsServiceImpl implements ProjectsService {
    @Autowired
    private ProjectsMapper projectsMapper;

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private TasksMapper tasksMapper;

    @Autowired
    private ProjectsMemberMapper projectsMemberMapper;

    @Autowired
    private TasksMemberMapper tasksMemberMapper;

//===============增删改项目==============

    /**
     *添加项目
     */
    @RequirePermission(value = PermissionType.PROJECT_ADD, checkProject = false)
    @Override
    public void addProject(Project project, Integer operatorId) {
        log.debug("添加项目，项目信息：{}", project);

        //  2.验证项目名称是否重复
        Project existingProject = projectsMapper.getProjectByName(project.getName());
        if (existingProject != null) {
            throw new BusinessException("项目名称重复");
        }

        //  3.获取操作人信息
        User operator = usersMapper.getUserById(operatorId);
        if (operator == null) {
            throw new BusinessException("操作人不存在");
        }

        //  4.根据操作人角色设置项目经理
        if (User.UserRole.PROJECT_MANAGER.equals(operator.getRole())) {
            // 项目经理添加项目，自动将自己设为项目经理
            project.setManagerId(operatorId);
            log.info("项目经理添加项目，自动将自己设为项目经理，项目经理id：{}", operatorId);
        } else if (User.UserRole.ADMIN.equals(operator.getRole())) {
            // 管理员添加项目，必须指定项目经理
            if (project.getManagerId() == null) {
                throw new BusinessException("管理员添加项目必须指定项目经理");
            }
            // 验证指定的项目经理是否存在且角色正确
            User manager = usersMapper.getUserById(project.getManagerId());
            if (manager == null) {
                throw new BusinessException("指定的项目经理不存在");
            }
            if (!User.UserRole.PROJECT_MANAGER.equals(manager.getRole())) {
                throw new BusinessException("指定的用户不是项目经理");
            }
            log.info("管理员添加项目，指定项目经理id：{}", project.getManagerId());
        } else {
            throw new BusinessException("只有项目经理或管理员可以添加项目");
        }

        project.setCreatedTime(LocalDateTime.now());
        project.setUpdateTime(LocalDateTime.now());
        projectsMapper.addProject(project);
        log.info("添加项目成功，项目id：{}", project.getId());

        //  5.自动将项目经理添加为项目成员（直接使用Mapper绕过权限检查）
        try {
            ProjectMember projectMember = new ProjectMember();
            projectMember.setProjectId(project.getId());
            projectMember.setUserId(project.getManagerId());
            projectMember.setProjectRole("PROJECT_MANAGER");
            projectMember.setJoinBy(operatorId);
            projectMember.setStatus(ProjectMember.MemberStatus.ACTIVE);
            projectMember.setJoinDate(LocalDateTime.now());
            projectsMemberMapper.addProjectMember(projectMember);
            log.info("自动将项目经理添加为项目成员成功，项目id：{}，项目经理id：{}", project.getId(), project.getManagerId());
        } catch (Exception e) {
            log.error("添加项目经理为项目成员失败，项目id：{}，项目经理id：{}", project.getId(), project.getManagerId(), e);
            throw new BusinessException("添加项目经理为项目成员失败：" + e.getMessage());
        }

    }

    /**
     *删除项目
     */
    @Override
    @RequirePermission(value = PermissionType.PROJECT_DELETE)
    public void deleteProject(Integer[] ids, Integer operatorId) {
        log.debug("删除项目，项目id：{}", ids);
        //  验证项目列表是否为空
        if(ids == null || ids.length == 0){
            throw new BusinessException("要删除的项目列表不能为空");
        }
        for(Integer id : ids){
            try {
                //  2.验证项目是否存在
                Project project = projectsMapper.getProjectById(id);
                if (project == null) {
                    log.warn("项目id：{}，项目不存在", id);
                    throw new BusinessException("项目id:" + id + ",项目不存在");
                }

                //  3.取消项目下所有任务
                tasksMapper.cancelTasksByProjectId(id);
                log.info("取消项目下所有任务成功，项目id：{}", id);

                //  4.禁用项目成员
                projectsMemberMapper.disableMembersByProjectId(id);
                log.info("禁用项目成员成功，项目id：{}", id);

                //  5.禁用任务成员
                tasksMemberMapper.disableTaskMembersByProjectId(id);
                log.info("禁用任务成员成功，项目id：{}", id);

                //  6.删除项目（软删除）
                project.setStatus(Project.ProjectStatus.TERMINATED);
                project.setUpdateTime(LocalDateTime.now());
                projectsMapper.updateProject(project);
                log.info("删除项目成功，项目id：{}", id);

            } catch (BusinessException e){
                throw e;
            } catch (Exception e) {
                log.warn("删除项目失败，项目id：{}", id, e);
                throw new BusinessException("删除项目失败" + e.getMessage(), e);
            }
        }
    }

    /**
     *修改项目
     */
    @Override
    @RequirePermission(value = PermissionType.PROJECT_UPDATE)
    public void updateProject(Project project, Integer operatorId) {
        log.debug("修改项目，项目信息：{}", project);
        try {
            //  2.验证项目是否存在
            Project existingProject = projectsMapper.getProjectById(project.getId());
            if (existingProject == null) {
                log.warn("项目id：{}，项目不存在", project.getId());
                throw new BusinessException("项目id:" + project.getId() + ",项目不存在");
            }

            // 3.验证项目状态：已取消的项目只能修改状态来恢复项目
            boolean isRestoringTerminatedProject = false;
            if (Project.ProjectStatus.TERMINATED.equals(existingProject.getStatus())) {
                if (project.getStatus() == null || Project.ProjectStatus.TERMINATED.equals(project.getStatus())) {
                    throw new BusinessException("项目已取消，只能修改状态来恢复项目");
                }
                isRestoringTerminatedProject = true;
            }

            // 4.验证项目名称是否重复（如果修改了名称）- 恢复项目时跳过
            if (!isRestoringTerminatedProject && project.getName() != null && !project.getName().trim().isEmpty()) {
                Project sameNameProject = projectsMapper.getProjectByName(project.getName());
                // 如果存在同名项目，且不是当前项目
                if (sameNameProject != null && !sameNameProject.getId().equals(project.getId())) {
                    throw new BusinessException("项目名称重复");
                }
            }

            // 5.验证项目经理是否存在（如果修改了项目经理）- 恢复项目时跳过
            if (!isRestoringTerminatedProject && project.getManagerId() != null) {
                User manager = usersMapper.getUserById(project.getManagerId());
                if (manager == null) {
                    throw new BusinessException("项目经理不存在");
                }
                // 验证项目经理角色（可选）
                if (!User.UserRole.PROJECT_MANAGER.equals(manager.getRole())) {
                    throw new BusinessException("指定的用户不是项目经理");
                }
            }

            // 5.5.如果修改了项目经理，需要更新项目成员表 - 恢复项目时跳过
            if (!isRestoringTerminatedProject && project.getManagerId() != null && !project.getManagerId().equals(existingProject.getManagerId())) {
                // 移除旧的项目经理角色（软删除）
                if (existingProject.getManagerId() != null) {
                    ProjectMember oldManagerMember = projectsMemberMapper.getMemberByProjectIdAndUserId(project.getId(), existingProject.getManagerId());
                    if (oldManagerMember != null && "PROJECT_MANAGER".equals(oldManagerMember.getProjectRole())) {
                        // 将旧项目经理软删除
                        oldManagerMember.setStatus(ProjectMember.MemberStatus.INACTIVE);
                        oldManagerMember.setUpdateTime(LocalDateTime.now());
                        projectsMemberMapper.updateProjectMember(oldManagerMember);
                        log.info("软删除旧项目经理，项目id：{}，旧项目经理id：{}", project.getId(), existingProject.getManagerId());
                    }
                }

                // 为新项目经理设置角色
                ProjectMember newManagerMember = projectsMemberMapper.getMemberByProjectIdAndUserId(project.getId(), project.getManagerId());
                if (newManagerMember != null) {
                    // 如果新项目经理已经是项目成员，更新其角色
                    newManagerMember.setProjectRole("PROJECT_MANAGER");
                    newManagerMember.setStatus(ProjectMember.MemberStatus.ACTIVE);
                    newManagerMember.setUpdateTime(LocalDateTime.now());
                    projectsMemberMapper.updateProjectMember(newManagerMember);
                    log.info("更新新项目经理角色，项目id：{}，新项目经理id：{}", project.getId(), project.getManagerId());
                } else {
                    // 如果新项目经理不是项目成员，添加为项目经理
                    ProjectMember projectMember = new ProjectMember();
                    projectMember.setProjectId(project.getId());
                    projectMember.setUserId(project.getManagerId());
                    projectMember.setProjectRole("PROJECT_MANAGER");
                    projectMember.setJoinBy(operatorId);
                    projectMember.setStatus(ProjectMember.MemberStatus.ACTIVE);
                    projectMember.setJoinDate(LocalDateTime.now());
                    projectsMemberMapper.addProjectMember(projectMember);
                    log.info("添加新项目经理为项目成员，项目id：{}，新项目经理id：{}", project.getId(), project.getManagerId());
                }
            }

            // 6.验证时间逻辑：开始时间不能晚于结束时间
            if (project.getStartDate() != null && project.getEndDate() != null) {
                if (project.getStartDate().isAfter(project.getEndDate())) {
                    throw new BusinessException("开始时间不能晚于结束时间");
                }
            }

            // 7.设置更新时间
            project.setUpdateTime(LocalDateTime.now());

            // 8.更新项目
            int rows = projectsMapper.updateProject(project);
            if (rows == 0) {
                throw new BusinessException("更新项目失败，可能项目已被删除");
            }

            log.info("修改项目成功，项目id：{}", project.getId());
         }catch (BusinessException e){
            throw e;
        } catch (Exception e) {
            throw new BusinessException("修改项目失败" + e.getMessage(), e);
        }

    }

    /**
     * 分页查询项目
     */
    @RequirePermission(value = PermissionType.PROJECT_VIEW, checkProject = false)
    @Override
    public PageBean page(Integer page,
                         Integer pageSize,
                         Integer id,
                         String name,
                         String description,
                         Project.Priority priority,
                         Project.ProjectStatus status,
                         LocalDate begin,
                         LocalDate end,
                         Integer currentUserId) throws BusinessException {
        log.debug("分页查询项目，参数：page={},pageSize={},id={},name={},description={},priority={},status={},begin={},end={}, currentUserId={}",
                page, pageSize, id, name, description, priority, status, begin, end, currentUserId);

        User currentUser = AuthUtils.getCurrentUser();

        if(User.UserRole.ADMIN == currentUser.getRole()){
            // 如果是管理员，查询所有项目
            PageInfo<Project> pageBean = PageHelperUtils.safePageQuery(page, pageSize,
                    () -> projectsMapper.listAll(
                            id,
                            name,
                            description,
                            priority,
                            status,
                            begin,
                            end
                    )
            );
            log.info("分页查询项目成功，结果：{}", pageBean);
            return new PageBean(pageBean.getTotal(), pageBean.getList());
        }

        // 如果不是管理员，只查询自己参与的项目
        PageInfo<Project> pageBean = PageHelperUtils.safePageQuery(page, pageSize,
                () -> projectsMapper.list(
                        id,
                        name,
                        description,
                        priority,
                        status,
                        begin,
                        end,
                        currentUserId
                )
        );
        log.info("分页查询项目成功，结果：{}", pageBean);
        return new PageBean(pageBean.getTotal(), pageBean.getList());
    }

    /**
     * 统计项目数量
     */
    @Override
    public long countProjects() {
        return projectsMapper.count();
    }

    /**
     * 获取指定用户参与项目数量
     */
    @Override
    public long getUserProjectCount(Integer userId) {
        return projectsMapper.getUserProjectCount(userId);
    }

    /**
     * 获取指定用户参与活跃项目数量
     */
    @Override
    public long getUserActiveProjectCount(Integer userId) {
        return projectsMapper.getUserActiveProjectCount(userId);
    }
}
