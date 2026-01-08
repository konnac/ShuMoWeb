package com.konnac.controller;

import com.konnac.annotation.RequirePermission;
import com.konnac.context.UserContext;
import com.konnac.enums.PermissionType;
import com.konnac.pojo.BatchResult;
import com.konnac.pojo.PageBean;
import com.konnac.pojo.ProjectMember;
import com.konnac.pojo.Result;
import com.konnac.service.ProjectsMemberService;
import com.konnac.utils.AuthUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/projects/{projectId}/members")
public class ProjectsMemberController {
    @Autowired
    private ProjectsMemberService projectsMemberService;


//===========增删改项目成员=============

    /**
     * 添加项目成员
     */
    @PostMapping
    public Result addProjectMember(@PathVariable Integer projectId, @RequestBody ProjectMember projectMember) {
        log.info("添加项目成员，项目id：{}，项目成员信息：{}", projectId, projectMember);
        projectsMemberService.addProjectMember(
                projectId,
                projectMember.getUserId(),
                projectMember.getProjectRole(),
                projectMember.getJoinBy()
        );
        return Result.success();
    }

    /**
     * 批量添加项目成员
     */
    @PostMapping("/batch")
    public Result addProjectMembers(@PathVariable Integer projectId, @RequestBody List<Integer> projectMembers) {
        // 调用Service，得到批量操作结果
        BatchResult batchResult = projectsMemberService.addProjectMembers(projectId, projectMembers, UserContext.getCurrentUserId());

        // 包装为统一的Result返回给前端
        if (batchResult.isAllSuccess()) {
            return Result.success("全部添加成功", batchResult);
        } else if (batchResult.getFailureCount() > 0) {
            // 部分成功，使用特定状态码
            return Result.error(201, "部分添加成功", batchResult);
        } else {
            return Result.error("添加失败", batchResult);
        }
    }

    /**
     * 删除项目成员
     */
    @DeleteMapping("/{userIds}")
    public Result deleteProjectMembers(@PathVariable Integer projectId, Integer[] userIds, Integer operatorId) {
        log.info("删除项目成员，项目id：{}，用户id：{}，操作人id：{}", projectId, userIds, operatorId);
        projectsMemberService.deleteProjectMembers(projectId, userIds, operatorId);
        return Result.success();
    }

    /**
     * 更新项目成员角色
     */
    @PutMapping("/{userId}")
    public Result updateMemberRole(@PathVariable Integer projectId, @PathVariable Integer userId, @RequestBody ProjectMember projectMember) {
        log.info("更新项目成员角色，项目id：{}，用户id：{}，新角色：{}，操作人id：{}", projectId, userId, projectMember.getProjectRole(), AuthUtils.getCurrentUserId());
        projectsMemberService.updateMemberRole(projectId, userId, projectMember.getProjectRole(), AuthUtils.getCurrentUserId());
        return Result.success();
    }

    /**
     * 分页查询项目成员
     */
    @RequirePermission(value = PermissionType.MEMBER_VIEW)
    @RequestMapping
    public Result page(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @PathVariable Integer projectId,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) String realName,
                       @RequestParam(required = false) String userRole,
                       @RequestParam(required = false) String department
                       ){

        log.info("获取项目成员列表，项目id：{}", projectId);
        Boolean isAdmin = AuthUtils.getCurrentUser().getRole() == com.konnac.pojo.User.UserRole.ADMIN;
        PageBean pageBean = projectsMemberService.page(page, pageSize, projectId, name, realName, userRole, department, isAdmin);

        return Result.success(pageBean);
    }

    /**
     * 获取项目成员统计
     */
    @GetMapping("/stats")
    public Result getProjectMemberStats(@PathVariable Integer projectId) {
        log.info("获取项目成员统计，项目id：{}", projectId);
        return Result.success(projectsMemberService.getProjectMemberStats(projectId));
    }

    /**
     * 获取项目中的特定角色成员
     */
    @GetMapping("/role/{projectRole}")
    public Result getProjectMembersByRole(@PathVariable Integer projectId, String projectRole) {
        log.info("获取项目中的特定角色成员，项目id：{}，角色：{}", projectId, projectRole);
        return Result.success(projectsMemberService.getProjectMembersByRole(projectId, projectRole));
    }

    /**
     * 激活项目成员
     */
    @PutMapping("/{userId}/activate")
    public Result activateMember(@PathVariable Integer projectId, @PathVariable Integer userId) {
        Integer operatorId = UserContext.getCurrentUserId();
        log.info("激活项目成员，项目id：{}，用户id：{}，操作人id：{}", projectId, userId, operatorId);
        projectsMemberService.activateMember(projectId, userId, operatorId);
        return Result.success();
    }

}
