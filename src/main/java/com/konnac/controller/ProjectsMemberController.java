package com.konnac.controller;

import com.konnac.pojo.Result;
import com.konnac.service.ProjectsMemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Result addProjectMember(@PathVariable Integer projectId, Integer userId, String projectRole, int operatorId) {
        log.info("添加项目成员，项目id：{}，用户id：{}，项目角色：{}，操作人id：{}", projectId, userId, projectRole, operatorId);
        projectsMemberService.addProjectMember(projectId, userId, projectRole, operatorId);
        return Result.success();
    }
}
