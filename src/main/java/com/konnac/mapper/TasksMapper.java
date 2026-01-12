package com.konnac.mapper;

import com.konnac.pojo.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface TasksMapper {
    //添加任务
    void addTask(Task task);

    //根据id查询任务
    Task getTaskById(Integer id);

    //修改任务
    void updateTask(Task task);

    //查询某用户未完成的任务数
    int getUncompletedTaskCountByProjectIdAndUserId(Integer projectId, Integer userId);

    //查询一个项目中未完成的任务数
    int getUncompletedTaskCountByProjectId(Integer projectId);

    //批量取消项目下的所有任务
    void cancelTasksByProjectId(Integer projectId);

    //分页查询所有任务（管理员）
    List<Task> listAll(Integer projectId,
                       Integer Id,
                       String title,
                       String assigneeName,
                       Task.TaskStatus status,
                       LocalDate begin,
                       LocalDate end,
                       Boolean isAdmin);

    //分页查询（项目经理和普通员工：查看自己所在项目的任务）
    List<Task> list(Integer projectId,
                    Integer Id,
                    String title,
                    String assigneeName,
                    Task.TaskStatus status,
                    LocalDate begin,
                    LocalDate end,
                    Integer currentUserId,
                    Boolean isAdmin);

    /**
     * 获取任务数量
     */
    @Select("select count(1) from tasks")
    long getTaskCount();

    /**
     * 一次性获取所有状态的任务数量
     */
    @Select("SELECT status, COUNT(*) as count FROM tasks GROUP BY status")
    List<Map<String, Object>> countAllStatus();

    /**
     * 获取所有任务的总工时（实际工时总和）
     */
    @Select("SELECT COALESCE(SUM(actual_hours), 0) FROM tasks WHERE actual_hours IS NOT NULL")
    double getAllTotalHours();

    /**
     * 获取指定用户的任务总数
     */
    @Select("SELECT COUNT(*) FROM tasks WHERE assignee_id = #{userId}")
    long getUserTaskCount(Integer userId);

    /**
     * 获取指定用户在参与项目中的任务状态统计
     */
    @Select("SELECT status, COUNT(*) as count FROM tasks WHERE assignee_id = #{userId} GROUP BY status")
    List<Map<String, Object>> getUserTaskStats(Integer userId);

    /**
     * 获取指定用户的总工时（实际工时总和）
     */
    @Select("SELECT COALESCE(SUM(actual_hours), 0) FROM tasks WHERE assignee_id = #{userId} AND actual_hours IS NOT NULL")
    double getUserTotalHours(Integer userId);

    /**
     * 获取项目经理负责项目下的任务总数
     */
    @Select("SELECT COUNT(*) FROM tasks t WHERE t.project_id IN (SELECT id FROM projects WHERE manager_id = #{userId})")
    long getManagerTaskCount(Integer userId);

    /**
     * 获取项目经理负责项目下的任务状态统计
     */
    @Select("SELECT status, COUNT(*) as count FROM tasks t WHERE t.project_id IN (SELECT id FROM projects WHERE manager_id = #{userId}) GROUP BY status")
    List<Map<String, Object>> getManagerTaskStats(Integer userId);

    /**
     * 获取项目经理负责项目下的总工时（实际工时总和）
     */
    @Select("SELECT COALESCE(SUM(actual_hours), 0) FROM tasks t WHERE t.project_id IN (SELECT id FROM projects WHERE manager_id = #{userId}) AND actual_hours IS NOT NULL")
    double getManagerTotalHours(Integer userId);

    /**
     * 查询"我的任务" - 根据用户角色返回不同的任务列表
     * 管理员：所有任务
     * 项目经理：自己负责的项目下的所有任务
     * 普通用户：自己负责的任务
     */
    List<Task> listMyTasks(Integer projectId,
                           Integer Id,
                           String title,
                           String assigneeName,
                           Task.TaskStatus status,
                           LocalDate begin,
                           LocalDate end,
                           Integer currentUserId,
                           String userRole,
                           Boolean isAdmin);
}
