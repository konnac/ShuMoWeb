package com.konnac.controller;

import com.konnac.annotation.RequirePermission;
import com.konnac.enums.PermissionType;
import com.konnac.context.UserContext;
import com.konnac.pojo.BatchResult;
import com.konnac.pojo.PageBean;
import com.konnac.pojo.Result;
import com.konnac.pojo.TaskMember;
import com.konnac.service.TaskMemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/projects/{projectId}/tasks/{taskId}/members")
public class TaskMemberController {
    @Autowired
    private TaskMemberService taskMemberService;

    //===========增删改任务成员=============
    /**
     * 添加任务成员
     */
    @RequirePermission(value = PermissionType.TASK_ASSIGN, checkTask = true)
    @PostMapping
    public Result addTaskMember(@PathVariable Integer projectId, @PathVariable Integer taskId, @RequestBody TaskMember taskMember){
        log.info("添加任务成员，项目id：{}，任务id：{}，任务成员信息：{}", projectId, taskId, taskMember);
        taskMemberService.addTaskMember(
                taskId,
                taskMember.getUserId(),
                taskMember.getTaskRole(),
                UserContext.getCurrentUserId()
        );
        return Result.success();
    }

    /**
     * 批量添加任务成员
     */
    @RequirePermission(value = PermissionType.TASK_ASSIGN, checkTask = true)
    @PostMapping("/batch")
    public Result addTaskMembers(@PathVariable Integer projectId, @PathVariable Integer taskId, @RequestBody List<Integer> taskMembers){
        log.info("批量添加任务成员，项目id：{}，任务id：{}，任务成员id列表：{}", projectId, taskId, taskMembers);
        // 调用Service，得到批量操作结果
        BatchResult batchResult = taskMemberService.addTaskMembers(taskId, taskMembers, UserContext.getCurrentUserId());

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
     * 删除任务成员
     */
    @RequirePermission(value = PermissionType.TASK_ASSIGN, checkTask = true)
    @DeleteMapping
    public Result deleteTaskMembers(@PathVariable Integer projectId, @PathVariable Integer taskId, @RequestParam List<Integer> userIds){
        Integer operatorId = UserContext.getCurrentUserId();
        log.info("删除任务成员，项目id：{}，任务id：{}，用户id：{}，操作人id：{}", projectId, taskId, userIds, operatorId);
        taskMemberService.deleteTaskMembers(taskId, userIds, operatorId);
        return Result.success();
    }

    /**
     * 更新任务成员角色
     */
    @RequirePermission(value = PermissionType.TASK_UPDATE, checkTask = true)
    @PutMapping("/{userId}")
    public Result updateMemberRole(@PathVariable Integer projectId, @PathVariable Integer taskId, @PathVariable Integer userId, @RequestParam Integer operatorId, @RequestParam String newRole){
        log.info("更新任务成员角色，项目id：{}，任务id：{}，用户id：{}，新角色：{}，操作人id：{}", projectId, taskId, userId, newRole, operatorId);
        taskMemberService.updateMemberRole(taskId, userId, newRole, operatorId);
        return Result.success();
    }

    /**
     * 分页查询任务成员
     */
    @RequirePermission(value = PermissionType.MEMBER_VIEW, checkProject = true)
    @RequestMapping("/{id}/assignable")
    public Result page(@PathVariable Integer projectId,
                       @PathVariable Integer taskId,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) String realName,
                       @RequestParam(required = false) String taskRole,
                       @RequestParam(required = false) String department
                       ) {
        log.info("获取任务成员列表，项目id：{}，任务id：{}", projectId, taskId);
        PageBean pageBean = taskMemberService.page(page, pageSize, taskId, name, realName, taskRole, department);
        return Result.success(pageBean);
    }

    /**
     * 发送任务完成通知给任务成员
     */
    @RequirePermission(value = PermissionType.MEMBER_VIEW, checkProject = true)
    @PostMapping("/{id}/")
    public Result sendTaskCompleteNotice(@PathVariable Integer projectId, @PathVariable Integer taskId){
        log.info("发送任务完成通知给任务成员，项目id：{}，任务id：{}", projectId, taskId);
        // 调用Service，得到批量操作结果
        BatchResult batchResult = taskMemberService.sendTaskCompleteNotification(taskId);

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


}
