package com.konnac.controller;

import com.konnac.pojo.PageBean;
import com.konnac.pojo.Report;
import com.konnac.pojo.Result;
import com.konnac.pojo.Task;
import com.konnac.service.ReportsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reports")
public class ReportsController {
    @Autowired
    private ReportsService reportsService;

    /**
     * 添加报表
     */
    @PostMapping
    public Result addReport(@RequestBody Report report) {
        log.info("添加报表，报表信息：{}", report);
        reportsService.addReport(report);
        return Result.success();
    }

    /**
     * 修改报表
     */
    @PutMapping("/{id}")
    public Result updateReport(@PathVariable Integer id, @RequestBody Report report) {
        log.info("修改报表，报表id：{}，报表信息：{}", id, report);
        report.setId(id);
        reportsService.updateReport(report);
        return Result.success();
    }

    /**
     * 查询报表
     */
    @GetMapping("/{id}")
    public Result getReport(@PathVariable Integer id) {
        log.info("查询报表，报表id：{}", id);
        Report report = reportsService.getReportById(id);
        return Result.success(report);
    }

    /**
     * 分页查询报表
     */
    @GetMapping
    public Result page(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       Integer userId,
                       Report.ReportType reportType,
                       @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime begin,
                       @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
        log.info("分页查询报表, 参数: page={}, pageSize={}, userId={}, reportType={}, begin={}, end={}",
                page, pageSize, userId, reportType, begin, end);
        PageBean pageBean = reportsService.page(page, pageSize, userId, reportType, begin, end, com.konnac.utils.AuthUtils.getCurrentUserId());
        return Result.success(pageBean);
    }

    /**
     * 获取指定时间段内的已完成任务
     */
    @GetMapping("/completed-tasks")
    public Result getCompletedTasks(@RequestParam Integer userId,
                                    @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                    @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        log.info("获取已完成任务, 参数: userId={}, startTime={}, endTime={}", userId, startTime, endTime);
        List<Task> tasks = reportsService.getCompletedTasksByPeriod(userId, startTime, endTime);
        return Result.success(tasks);
    }
}
