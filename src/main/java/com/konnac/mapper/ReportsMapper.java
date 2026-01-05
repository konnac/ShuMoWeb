package com.konnac.mapper;

import com.konnac.pojo.Report;
import com.konnac.pojo.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ReportsMapper {
    /**
     * 添加报告
     */
    void addReport(Report report);

    /**
     * 更新报告
     */
    void updateReport(Report report);

    /**
     * 根据id获取报告
     */
    @Select("select * from reports where id = #{id}")
    Report getReportById(Integer id);

    /**
     * 分页查询报表列表
     */
    List<Report> list(Integer userId,
                      Report.ReportType reportType,
                      LocalDateTime begin,
                      LocalDateTime end,
                      Integer currentUserId,
                      String userRole);

    /**
     * 获取用户报告数量
     */
    @Select("select count(*) from reports where user_id = #{userId}")
    int getUserReportCount(Integer userId);

    /**
     * 获取指定时间段内的已完成任务
     */
    List<Task> getCompletedTasksByPeriod(Integer userId, LocalDateTime startTime, LocalDateTime endTime);
}
