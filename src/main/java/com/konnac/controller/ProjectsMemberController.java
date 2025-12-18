package com.konnac.controller;

import com.konnac.pojo.ProjectMember;
import com.konnac.pojo.Result;
import com.konnac.service.ProjectsMemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("projects/{projectId}/members")
public class ProjectsMemberController {
    @Autowired
    private ProjectsMemberService projectsMemberService;


    /*
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

    /*
    * 批量添加项目成员
    */
    @PostMapping("/batch")
    public Result addProjectMembers(@PathVariable Integer projectId, @RequestBody List<ProjectMember> projectMembers){
        log.info("批量添加项目成员，项目id：{}，项目成员信息：{}", projectId, projectMembers);
        projectsMemberService.addProjectMembers(projectId, projectMembers);
        return Result.success();
    }
}
