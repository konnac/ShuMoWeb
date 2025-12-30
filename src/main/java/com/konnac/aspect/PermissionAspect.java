package com.konnac.aspect;

import com.konnac.annotation.RequirePermission;
import com.konnac.context.UserContext;
import com.konnac.enums.PermissionType;
import com.konnac.exception.BusinessException;
import com.konnac.mapper.*;
import com.konnac.pojo.ProjectMember;
import com.konnac.pojo.User;
import com.konnac.utils.AuthUtils;
import com.konnac.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 权限验证切面
 */
@Slf4j
@Aspect
@Component
@Order(1) // 在事务之前执行
public class PermissionAspect {

    @Autowired
    private TasksMemberMapper tasksMemberMapper;

    @Autowired
    private ProjectsMapper projectsMapper;

    @Autowired
    private ProjectsMemberMapper projectsMemberMapper;
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private TasksMapper tasksMapper;


    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        // 1. 从请求头获取token并解析
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException("请求属性获取失败");
        }
        HttpServletRequest request = attributes.getRequest();
        String authorization = request.getHeader("Authorization");
        
        // 处理Bearer前缀
        String token = null;
        if (StringUtils.hasLength(authorization) && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }
        
        // 检查token是否存在
        if (!StringUtils.hasLength(token)) {
            throw new BusinessException("用户未登录");
        }
        
        // 解析token获取用户ID
        Integer userId = JwtUtils.getUserIdFromToken(token);
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 从数据库获取用户信息
        User currentUser = usersMapper.getUserById(userId);
        if (currentUser == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 设置UserContext
        UserContext.setCurrentUser(currentUser);
        UserContext.setCurrentUserId(userId);
        UserContext.setCurrentUserRole(currentUser.getRole().toString());

        Integer projectId = null;
        Integer taskId = null;

        // 2. 如果需要检查项目权限，从方法参数中提取项目ID
        if (requirePermission.checkProject()) {
            projectId = extractProjectId(joinPoint, requirePermission);
            if (projectId == null) {
                throw new BusinessException("未找到项目ID");
            }
        }

        // 如果需要检查任务成员权限, 从方法参数中提取任务ID
        if (requirePermission.checkTask()){
            taskId = extractTaskId(joinPoint, requirePermission);
            if (taskId == null){
                throw new BusinessException("未找到任务ID");
            }
        }


        // 3. 验证权限
        boolean hasPermission = checkPermissionInternal(
                requirePermission.value(),
                userId,
                projectId,
                taskId,
                currentUser
        );

        if (!hasPermission) {
            log.warn("权限验证失败: 用户[{}] 无权限[{}] 项目[{}]",
                    currentUser.getUsername(), requirePermission.value(), projectId);
            throw new BusinessException(requirePermission.errorMessage());
        }

        log.debug("权限验证通过: 用户[{}] 权限[{}]",
                currentUser.getUsername(), requirePermission.value());
    }

    /**
     * 从方法参数中提取项目ID
     */
    private Integer extractProjectId(JoinPoint joinPoint, RequirePermission annotation) {
        // 获取方法参数值
        Object[] args = joinPoint.getArgs();
        // 获取方法参数名
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();

    //====从方法参数中提取项目id====

        // 指定了参数名，直接获取
        if (!annotation.projectIdParam().isEmpty()) {
            for (int i = 0; i < paramNames.length; i++) {
                if (annotation.projectIdParam().equals(paramNames[i])) {
                    Object arg = args[i];
                    return extractInteger(arg);
                }
            }
        }

        // 尝试从常见参数名中查找
        String[] commonNames = {"projectId", "id", "pid"};
        for (int i = 0; i < paramNames.length; i++) {
            for (String commonName : commonNames) {
                if (commonName.equals(paramNames[i])) {
                    Object arg = args[i];
                    return extractInteger(arg);
                }
            }
        }

        // 尝试从对象中查找 (反射)
        for (Object arg : args) {
            if (arg != null && arg.getClass().getSimpleName().contains("Project")) {
                try {
                    Method getIdMethod = arg.getClass().getMethod("getId");
                    Object idObj = getIdMethod.invoke(arg);
                    return extractInteger(idObj);
                } catch (Exception e) {
                    // 忽略异常，继续查找
                }
            }
        }

        return null;
    }

    /**
     * 从方法参数中提取任务ID
     */
    private Integer extractTaskId(JoinPoint joinPoint, RequirePermission annotation) {
        // 获取方法参数值
        Object[] args = joinPoint.getArgs();
        // 获取方法参数名
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();

        //====从方法参数中提取任务id====

        // 指定了参数名，直接获取
        if (!annotation.projectIdParam().isEmpty()) {
            for (int i = 0; i < paramNames.length; i++) {
                if (annotation.projectIdParam().equals(paramNames[i])) {
                    Object arg = args[i];
                    return extractInteger(arg);
                }
            }
        }

        // 尝试从常见参数名中查找
        String[] commonNames = {"taskId", "id", "pid"};
        for (int i = 0; i < paramNames.length; i++) {
            for (String commonName : commonNames) {
                if (commonName.equals(paramNames[i])) {
                    Object arg = args[i];
                    return extractInteger(arg);
                }
            }
        }

        // 尝试从对象中查找 (反射)
        for (Object arg : args) {
            if (arg != null && arg.getClass().getSimpleName().contains("Task")) {
                try {
                    Method getIdMethod = arg.getClass().getMethod("getId");
                    Object idObj = getIdMethod.invoke(arg);
                    return extractInteger(idObj);
                } catch (Exception e) {
                    // 忽略异常，继续查找
                }
            }
        }

        return null;
    }

    /**
     * 安全转为整数
     */
    private Integer extractInteger(Object obj) {
        if (obj instanceof Integer) {
            return (Integer) obj;
        } else if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        return null;
    }

    /**
     * 内部权限验证逻辑
     */
    private boolean checkPermissionInternal(PermissionType permissionType,
                                            Integer userId,
                                            Integer projectId,
                                            Integer taskId,
                                            User user) {
        try {
            // 系统管理员拥有所有权限
            if (User.UserRole.ADMIN.equals(user.getRole())) {
                return true;
            }

            // 根据权限类型验证
            switch (permissionType) {
                // 项目经理可以添加项目
                case PROJECT_ADD:
                    return User.UserRole.PROJECT_MANAGER.equals(user.getRole());

                // 更新、删除项目权限
                case PROJECT_UPDATE:
                case PROJECT_DELETE:
                    if (projectId == null) return false;
                    return checkProjectManagerPermission(projectId, userId);

                // 查看自己的项目列表不需要具体项目权限
                case PROJECT_VIEW:
                    return true;
                case PROJECT_VIEW_ALL:
                    return checkAdminPermission(userId);

                // 项目经理可以管理项目成员
                case MEMBER_ADD:
                case MEMBER_REMOVE:
                case MEMBER_UPDATE:
                case MEMBER_ROLE_CHANGE:
                    if (projectId == null) return false;
                    return checkProjectManagerPermission(projectId, userId);

                // 查看项目成员权限(项目内成员)
                case MEMBER_VIEW:
                    if (projectId == null) return false;
                    return checkNomalMemberPermission(projectId, userId);

                // 管理层可发布通知
                case NOTIFICATION_SEND:
                    return checkProjectManagerPermission(projectId, userId);
                case NOTIFICATION_SEND_TASK:
                    return checkNomalMemberPermission(projectId, userId);
                case NOTIFACATION_SEND_ADMIN:
                    return checkProjectManagerPermission(projectId, userId);

                // 任务权限(项目内成员)
                case TASK_ADD:
                    if (projectId == null) return false;
                    return checkNomalMemberPermission(projectId, userId);
                case TASK_UPDATE:
                case TASK_DELETE:
                case TASK_ASSIGN:
                    if (taskId == null) return false;
                    return checkTaskLeaderPermission(taskId, userId);

                //用户权限
                case USER_ADD:
                case USER_UPDATE_ADMIN:
                case USER_DELETE:
                case USER_VIEW_DETAIL:
                    return checkAdminPermission(userId);
                case USER_UPDATE:
                    checkUserId(userId);
                case USER_VIEW_SIMPLE:
                    return true;


                // 其他权限
                default:
                    return false;
            }
        } catch (Exception e) {
            log.error("权限验证异常", e);
            return false;
        }
    }

    /**
     * 验证是否是项目管理员
     */
    private boolean checkProjectManagerPermission(Integer projectId, Integer userId) {
        ProjectMember member = projectsMemberMapper.getMemberByProjectIdAndUserId(projectId, userId);
        return member != null && "PROJECT_MANAGER".equals(member.getProjectRole());
    }

    /**
     * 验证是否是项目成员（有查看权限）
     */
    private boolean checkNomalMemberPermission(Integer projectId, Integer userId) {
        ProjectMember member = projectsMemberMapper.getMemberByProjectIdAndUserId(projectId, userId);
        return member != null;
    }

    /**
     * 验证是否是任务负责人（普通成员）
     */
    private boolean checkTaskLeaderPermission(Integer taskId, Integer userId) {
        return tasksMapper.getTaskById(taskId).getAssigneeId().equals(userId);
    }

    /**
     * 验证是否是管理员
     */
    private boolean checkAdminPermission(Integer userId) {
        return usersMapper.getUserById(userId).getRole() == User.UserRole.ADMIN;
    }

    /**
     * 检查要修改的用户ID是否是当前用户
     */
    private void checkUserId(Integer userId) {
        Integer currentUserId = AuthUtils.getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            throw new BusinessException("无权限修改用户信息");
        }
    }


}