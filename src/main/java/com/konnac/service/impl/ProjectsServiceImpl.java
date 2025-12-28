package com.konnac.service.impl;


import com.github.pagehelper.PageInfo;
import com.konnac.annotation.RequirePermission;
import com.konnac.context.UserContext;
import com.konnac.enums.PermissionType;
import com.konnac.exception.BusinessException;
import com.konnac.mapper.ProjectsMapper;
import com.konnac.mapper.TasksMapper;
import com.konnac.mapper.UsersMapper;
import com.konnac.pojo.PageBean;
import com.konnac.pojo.Project;
import com.konnac.pojo.User;
import com.konnac.service.ProjectsService;
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
//===============增删改项目==============

    /**
     *添加项目
     */
    @RequirePermission(value = PermissionType.PROJECT_ADD)
    @Override
    public void addProject(Project project, Integer operatorId) {
        log.debug("添加项目，项目信息：{}", project);

        //  2.验证项目名称是否重复
        Project existingProject = projectsMapper.getProjectByName(project.getName());
        if (existingProject != null) {
            throw new BusinessException("项目名称重复");
        }
        project.setCreatedTime(LocalDateTime.now());
        project.setUpdateTime(LocalDateTime.now());
        projectsMapper.addProject(project);
        log.info("添加项目成功，项目id：{}", project.getId());
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

                //  3.验证项目中是否有未完成的任务
                if (tasksMapper.getUncompletedTaskCountByProjectId(id) > 0) {
                    log.warn("项目id：{}，项目下有未完成的任务，不能删除", id);
                    throw new BusinessException("项目id:" + id + ",项目下有未完成的任务，不能删除");
                }
                //  3.删除项目
                project.setStatus(Project.ProjectStatus.CANCELED);
                project.setUpdateTime(LocalDateTime.now());
                projectsMapper.updateProject(project);

            } catch (BusinessException e){
                throw e;
            } catch (Exception e) {
                log.warn("验证权限失败，项目id：{}", id);
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

            // 3.验证项目状态：已取消的项目不允许修改
            if (Project.ProjectStatus.CANCELED.equals(existingProject.getStatus())) {
                throw new BusinessException("项目已取消，不允许修改");
            }

            // 4.验证项目名称是否重复（如果修改了名称）
            if (project.getName() != null && !project.getName().trim().isEmpty()) {
                Project sameNameProject = projectsMapper.getProjectByName(project.getName());
                // 如果存在同名项目，且不是当前项目
                if (sameNameProject != null && !sameNameProject.getId().equals(project.getId())) {
                    throw new BusinessException("项目名称重复");
                }
            }

            // 5.验证项目经理是否存在（如果修改了项目经理）
            if (project.getManagerId() != null) {
                User manager = usersMapper.getUserById(project.getManagerId());
                if (manager == null) {
                    throw new BusinessException("项目经理不存在");
                }
                // 验证项目经理角色（可选）
                if (!User.UserRole.PROJECT_MANAGER.equals(manager.getRole())) {
                    throw new BusinessException("指定的用户不是项目经理");
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
     *分页查询项目
     */
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
        log.debug("分页查询项目，参数：page={},pageSize={},id={},name={},description={},priority={},status={},begin={},end={}, currentUserId={}", page, pageSize, id, name, description, priority, status, begin, end, currentUserId);
        if(User.UserRole.ADMIN == UserContext.getCurrentUser().getRole()){
            PageInfo<Project> pageBean = PageHelperUtils.safePageQuery(page, pageSize, () -> projectsMapper.listAll(id, name, description, priority, status, begin, end));
            log.info("分页查询项目成功，结果：{}", pageBean);
            return new PageBean(pageBean.getTotal(), pageBean.getList());
        }
        PageInfo<Project> pageBean = PageHelperUtils.safePageQuery(page, pageSize, () -> projectsMapper.list(id, name, description, priority, status, begin, end, currentUserId));
        log.info("分页查询项目成功，结果：{}", pageBean);
        return new PageBean(pageBean.getTotal(), pageBean.getList());
    }
}
