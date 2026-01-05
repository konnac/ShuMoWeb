package com.konnac.service.impl;

import com.github.pagehelper.PageInfo;
import com.konnac.exception.BusinessException;
import com.konnac.mapper.ReportsMapper;
import com.konnac.pojo.PageBean;
import com.konnac.pojo.Report;
import com.konnac.pojo.Task;
import com.konnac.pojo.User;
import com.konnac.service.ReportsService;
import com.konnac.utils.AuthUtils;
import com.konnac.utils.PageHelperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Transactional(rollbackFor = Exception.class, timeout = 15)
@Service
public class ReportsServiceImpl implements ReportsService {
    @Autowired
    private ReportsMapper reportsMapper;

    /**
     * 添加报表
     */
    @Override
    public void addReport(Report report) {
        log.debug("添加报表，报表信息：{}", report);

        User currentUser = AuthUtils.getCurrentUser();
        report.setUserId(currentUser.getId());
        report.setUserName(currentUser.getRealName());
        report.setSubmitTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());

        reportsMapper.addReport(report);
        log.info("添加报表成功，报表id：{}", report.getId());
    }

    /**
     * 修改报表
     */
    @Override
    public void updateReport(Report report) {
        log.debug("修改报表，报表信息：{}", report);

        Report existingReport = reportsMapper.getReportById(report.getId());
        if (existingReport == null) {
            throw new BusinessException("报表不存在");
        }

        User currentUser = AuthUtils.getCurrentUser();
        if (!currentUser.getId().equals(existingReport.getUserId()) &&
            User.UserRole.ADMIN != currentUser.getRole()) {
            throw new BusinessException("无权修改此报表");
        }

        report.setUpdateTime(LocalDateTime.now());
        reportsMapper.updateReport(report);
        log.info("修改报表成功，报表id：{}", report.getId());
    }

    /**
     * 根据id查询报表
     */
    @Override
    public Report getReportById(Integer id) {
        log.debug("查询报表，报表id：{}", id);
        Report report = reportsMapper.getReportById(id);
        if (report == null) {
            log.warn("报表id：{}，报表不存在", id);
            throw new BusinessException("报表不存在");
        }
        return report;
    }

    /**
     * 分页查询报表
     */
    @Override
    public PageBean page(Integer page,
                         Integer pageSize,
                         Integer userId,
                         Report.ReportType reportType,
                         LocalDateTime begin,
                         LocalDateTime end,
                         Integer currentUserId) {
        log.debug("分页查询报表，参数：page={},pageSize={},userId={},reportType={},begin={},end={}, currentUserId={}",
                page, pageSize, userId, reportType, begin, end, currentUserId);

        User currentUser = AuthUtils.getCurrentUser();
        String userRole = currentUser.getRole().name();

        PageInfo<Report> pageBean = PageHelperUtils.safePageQuery(page, pageSize,
                () -> reportsMapper.list(
                        userId,
                        reportType,
                        begin,
                        end,
                        currentUserId,
                        userRole
                )
        );
        log.info("分页查询报表成功，结果：{}", pageBean);
        return new PageBean(pageBean.getTotal(), pageBean.getList());
    }

    /**
     * 获取指定时间段内的已完成任务
     */
    @Override
    public List<Task> getCompletedTasksByPeriod(Integer userId, LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("获取已完成任务，参数：userId={}, startTime={}, endTime={}", userId, startTime, endTime);
        List<Task> tasks = reportsMapper.getCompletedTasksByPeriod(userId, startTime, endTime);
        log.info("获取已完成任务成功，结果数量：{}", tasks.size());
        return tasks;
    }
}
