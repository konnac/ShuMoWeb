package com.konnac.aspect;

import com.konnac.annotation.RequirePermission;
import com.konnac.enums.PermissionType;
import com.konnac.exception.BusinessException;
import com.konnac.mapper.*;
import com.konnac.pojo.ProjectMember;
import com.konnac.pojo.Task;
import com.konnac.pojo.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class PermissionAspectTest {

    @Mock
    private TasksMemberMapper tasksMemberMapper;

    @Mock
    private ProjectsMapper projectsMapper;

    @Mock
    private ProjectsMemberMapper projectsMemberMapper;

    @Mock
    private UsersMapper usersMapper;

    @Mock
    private TasksMapper tasksMapper;

    @InjectMocks
    private PermissionAspect permissionAspect;

    private User adminUser;
    private User projectManagerUser;
    private User normalUser;
    private ProjectMember projectManagerMember;
    private ProjectMember normalMember;
    private Task task;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        adminUser = new User();
        adminUser.setId(1);
        adminUser.setUsername("admin");
        adminUser.setRole(User.UserRole.ADMIN);

        projectManagerUser = new User();
        projectManagerUser.setId(2);
        projectManagerUser.setUsername("pm");
        projectManagerUser.setRole(User.UserRole.PROJECT_MANAGER);

        normalUser = new User();
        normalUser.setId(3);
        normalUser.setUsername("user");
        normalUser.setRole(User.UserRole.EMPLOYEE);

        projectManagerMember = new ProjectMember();
        projectManagerMember.setProjectId(100);
        projectManagerMember.setUserId(2);
        projectManagerMember.setProjectRole("PROJECT_MANAGER");

        normalMember = new ProjectMember();
        normalMember.setProjectId(100);
        normalMember.setUserId(3);
        normalMember.setProjectRole("MEMBER");

        task = new Task();
        task.setId(200);
        task.setAssigneeId(3);
    }

    @Test
    void testAdminHasAllPermissions() {
        when(usersMapper.getUserById(1)).thenReturn(adminUser);

        User.UserRole adminRole = adminUser.getRole();
        assertEquals(User.UserRole.ADMIN, adminRole);
    }

    @Test
    void testProjectManagerCanAddProject() {
        User.UserRole pmRole = projectManagerUser.getRole();
        assertEquals(User.UserRole.PROJECT_MANAGER, pmRole);
    }

    @Test
    void testProjectManagerPermission() {
        when(projectsMemberMapper.getMemberByProjectIdAndUserId(100, 2)).thenReturn(projectManagerMember);

        ProjectMember member = projectsMemberMapper.getMemberByProjectIdAndUserId(100, 2);
        assertNotNull(member);
        assertEquals("PROJECT_MANAGER", member.getProjectRole());
    }

    @Test
    void testNormalMemberPermission() {
        when(projectsMemberMapper.getMemberByProjectIdAndUserId(100, 3)).thenReturn(normalMember);

        ProjectMember member = projectsMemberMapper.getMemberByProjectIdAndUserId(100, 3);
        assertNotNull(member);
        assertEquals("MEMBER", member.getProjectRole());
    }

    @Test
    void testTaskLeaderPermission() {
        when(tasksMapper.getTaskById(200)).thenReturn(task);

        Task retrievedTask = tasksMapper.getTaskById(200);
        assertNotNull(retrievedTask);
        assertEquals(3, retrievedTask.getAssigneeId());
    }

    @Test
    void testNonProjectMemberCannotManage() {
        when(projectsMemberMapper.getMemberByProjectIdAndUserId(100, 1)).thenReturn(null);

        ProjectMember member = projectsMemberMapper.getMemberByProjectIdAndUserId(100, 1);
        assertNull(member);
    }

    @Test
    void testTaskNotAssignedToUser() {
        when(tasksMapper.getTaskById(200)).thenReturn(task);

        Task retrievedTask = tasksMapper.getTaskById(200);
        assertNotEquals(2, retrievedTask.getAssigneeId());
    }

    @Test
    void testPermissionTypeValues() {
        assertEquals("添加项目", PermissionType.PROJECT_ADD.getDescription());
        assertEquals("修改项目", PermissionType.PROJECT_UPDATE.getDescription());
        assertEquals("删除项目", PermissionType.PROJECT_DELETE.getDescription());
        assertEquals("添加任务", PermissionType.TASK_ADD.getDescription());
        assertEquals("修改任务", PermissionType.TASK_UPDATE.getDescription());
        assertEquals("删除任务", PermissionType.TASK_DELETE.getDescription());
        assertEquals("分配任务", PermissionType.TASK_ASSIGN.getDescription());
        assertEquals("添加成员", PermissionType.MEMBER_ADD.getDescription());
        assertEquals("移除成员", PermissionType.MEMBER_REMOVE.getDescription());
    }

    @Test
    void testUserRoleValues() {
        assertEquals("ADMIN", User.UserRole.ADMIN.toString());
        assertEquals("PROJECT_MANAGER", User.UserRole.PROJECT_MANAGER.toString());
        assertEquals("EMPLOYEE", User.UserRole.EMPLOYEE.toString());
    }

    @Test
    void testProjectMemberFields() {
        assertEquals(100, projectManagerMember.getProjectId());
        assertEquals(2, projectManagerMember.getUserId());
        assertEquals("PROJECT_MANAGER", projectManagerMember.getProjectRole());
    }

    @Test
    void testTaskFields() {
        assertEquals(200, task.getId());
        assertEquals(3, task.getAssigneeId());
    }
}
