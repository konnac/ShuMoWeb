package com.konnac.service;

import com.konnac.pojo.PageBean;
import com.konnac.pojo.Report;
import com.konnac.pojo.Task;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportsService {
    /**
     * 添加报告
     */
    void addReport(Report report);

    /**
     * 修改报告
     */
    void updateReport(Report report);

    /**
     * 查询报告
     */
    Report getReportById(Integer id);

    /**
     * 分页查询报告
     */
    PageBean page(Integer page,
                  Integer pageSize,
                  Integer userId,
                  Report.ReportType reportType,
                  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime begin,
                  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                  Integer currentUserId);

    /**
     * 获取指定时间段内的已完成任务
     */
    List<Task> getCompletedTasksByPeriod(Integer userId, LocalDateTime startTime, LocalDateTime endTime);
}
