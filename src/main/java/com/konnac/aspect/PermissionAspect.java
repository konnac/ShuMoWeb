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
@Order(1) // 在事务之前执行,防止性能浪费\扰乱日志\或者有些数据bug
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


    //
    @Before("@annotation(requirePermission)") //目标方法执行之前执行切面逻辑,匹配带有特定注解的方法:@RequirePermission
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        // 1.从请求头获取token并解析
            //RequestContextHolder 获取当前线程的请求上下文信息
            //getRequestAttributes() 获取当前线程的Servlet请求属性
            //请求属性（ServletRequestAttributes）是spring对HTTP请求的封装,它确实代表了前端发送的HTTP请求
            //Authorization 请求头用于身份验证的部分
        // 切面中无法直接获取ServletRequest 需要获取当前线程的Servlet请求属性
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // 如果当前线程的请求属性为空
        if (attributes == null) {
            throw new BusinessException("请求属性获取失败");
        }
        // 2. 获取HTTP请求对象
        HttpServletRequest request = attributes.getRequest();

        // 3. 获取请求头中的Authorization
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
            throw new BusinessException(403, requirePermission.errorMessage());
        }

        log.debug("权限验证通过: 用户[{}] 权限[{}]",
                currentUser.getUsername(), requirePermission.value());
    }

    /**
     * 从方法参数中提取项目ID
     * @param joinPoint:可以被 AOP 控制的方法
     */
    private Integer extractProjectId(JoinPoint joinPoint, RequirePermission annotation) {
        // 获取方法参数值
        Object[] args = joinPoint.getArgs();
        // 获取方法参数名
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();

    //====从方法参数中提取项目id====

        // 指定了参数名(projectIdParam)，直接获取
        if (!annotation.projectIdParam().isEmpty()) {
            for (int i = 0; i < paramNames.length; i++) {
                // 匹配参数名,相等则获取
                if (annotation.projectIdParam().equals(paramNames[i])) {
                    Object arg = args[i];
                    return extractInteger(arg);
                }
            }
        }

        // 尝试从常见参数名中查找
        String[] commonNames = {"projectId", "id", "pid"};
        // 遍历参数名
        for (int i = 0; i < paramNames.length; i++) {
            for (String commonName : commonNames) {
                if (commonName.equals(paramNames[i])) {
                    Object arg = args[i];
                    // 匹配成功则获取
                    return extractInteger(arg);
                }
            }
        }

        // 尝试从对象中查找 (反射) 遍历参数对象
        for (Object arg : args) {
            if (arg != null && arg.getClass().getSimpleName().contains("Project")) {
                //获取到项目对象则尝试获取项目ID
                try {
                    //获取getID的方法
                    Method getIdMethod = arg.getClass().getMethod("getId");
                    //调用getId方法获取项目ID
                    Object idObj = getIdMethod.invoke(arg);
                    return extractInteger(idObj);
                } catch (Exception e) {
                    // 忽略异常，继续查找
                }
            }
        }

        // 如果从参数中找不到,尝试从URL路径中提取,几乎所有项目接口都满足这个条件
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // 如果当前线程的请求属性不为空
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            // 获取请求URI即路径部分
            String uri = request.getRequestURI();
            // 匹配 /projects/{projectId}/... 格式的URL
            if (uri.contains("/projects/")) {
                //按照分隔符（/）将字符串拆分成数组
                String[] parts = uri.split("/");
                for (int i = 0; i < parts.length; i++) {
                    if ("projects".equals(parts[i]) && i + 1 < parts.length) {
                        // projects 后一个元素是项目ID
                        String idStr = parts[i + 1];
                        // 处理批量删除的情况（逗号分隔的IDs）
                        if (idStr.contains(",")) {
                            // 批量删除时，返回第一个ID用于权限检查
                            String[] ids = idStr.split(",");
                            if (ids.length > 0) {
                                return extractInteger(ids[0]);
                            }
                        }
                        return extractInteger(idStr);
                    }
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

        //====从方法参数中提取任务id==== 几乎不用所以没写

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
                    //获取getID的方法
                    Method getIdMethod = arg.getClass().getMethod("getId");
                    //调用getId方法获取任务ID
                    Object idObj = getIdMethod.invoke(arg);
                    return extractInteger(idObj);
                } catch (Exception e) {
                    // 忽略异常，继续查找
                }
            }
        }

        // 如果从参数中找不到，尝试从URL路径中提取
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            // 获取请求URI即路径部分
            HttpServletRequest request = attributes.getRequest();
            // 获取请求URI即路径部分
            String uri = request.getRequestURI();
            // 匹配 /projects/{projectId}/tasks/{taskId} 格式的URL
            if (uri.contains("/tasks/")) {
                String[] parts = uri.split("/");
                for (int i = 0; i < parts.length; i++) {
                    if ("tasks".equals(parts[i]) && i + 1 < parts.length) {
                        // tasks 后一个元素是任务ID
                        return extractInteger(parts[i + 1]);
                    }
                }
            }
        }
        return null;
    }

    /**
     * 普通的安全转为整数~
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
     * @param permissionType 权限类型
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
                // 查看所有项目权限,没用上,因为管理员在上面就验证过了..不过不改了怕出什么bug
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
                    return checkProjectManagerPermission(projectId, userId);
                case TASK_UPDATE:
                case TASK_DELETE:
                case TASK_ASSIGN:
                    if (taskId == null) return false;
                    // 任务负责人或项目经理都可以操作
                    if(checkProjectManagerPermissionByTaskId(taskId, userId)){
                        return true;
                    }
                    return checkTaskLeaderPermission(taskId, userId);

                case USER_ADD:
                case USER_UPDATE_ADMIN:
                case USER_DELETE:
                case USER_VIEW_DETAIL:
                    return checkAdminPermission(userId);
                case USER_UPDATE:
                    checkUserId(userId);
                case USER_VIEW_SIMPLE:
                    return true;

                case FILE_UPLOAD:
                case FILE_VIEW:
                case FILE_DELETE:
                case FILE_DOWNLOAD:
                    return checkNomalMemberPermission(projectId, userId);


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
        return tasksMemberMapper.getTaskMember(taskId, userId).getTaskRole().equals("ASSIGNEE");
    }

    /**
     * 验证是否是任务所属项目的项目经理
     */
    private boolean checkProjectManagerPermissionByTaskId(Integer taskId, Integer userId) {
        try {
            // 获取任务信息
            com.konnac.pojo.Task task = tasksMapper.getTaskById(taskId);
            if (task == null || task.getProjectId() == null) {
                return false;
            }
            // 检查用户是否是该项目的项目经理
            return checkProjectManagerPermission(task.getProjectId(), userId);
        } catch (Exception e) {
            log.error("检查项目经理权限失败", e);
            return false;
        }
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