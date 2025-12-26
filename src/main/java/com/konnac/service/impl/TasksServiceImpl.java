package com.konnac.service.impl;

import com.github.pagehelper.PageInfo;
import com.konnac.exception.BusinessException;
import com.konnac.mapper.TasksMapper;
import com.konnac.pojo.PageBean;
import com.konnac.pojo.Task;
import com.konnac.service.TasksService;
import com.konnac.utils.PageHelperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class TasksServiceImpl implements TasksService {
    @Autowired
    private TasksMapper tasksMapper;

    //添加任务
    @Transactional(rollbackFor = Exception.class, timeout = 10)
    @Override
    public void addTask(Task task) {
        log.debug("添加任务，任务信息：{}", task);
        //1.验证添加权限

        task.setCreatedTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        tasksMapper.addTask(task);
        log.info("添加任务成功，任务id：{}", task.getId());
    }

    //批量删除任务
    @Override
    public void deleteTask(Integer[] ids) {
        log.debug("删除任务，任务id：{}", ids);
        // 验证任务列表是否为空
        if(ids == null || ids.length == 0){
            throw new BusinessException("要删除的任务列表不能为空");
        }
        for(Integer id : ids){
            try {
                //验证删除权限
                Task task = tasksMapper.getTaskById(id);
                //2.验证任务是否存在
                if(task == null){
                    log.warn("任务id：{}，任务不存在", id);
                    throw new BusinessException("要删除的任务不存在");
                }
                //3.验证任务是否已完成
                if(!(task.getStatus() == Task.TaskStatus.COMPLETED)){
                    log.warn("任务id：{}，任务未完成，不能删除", id);
                    throw new BusinessException("任务未完成，不能删除");
                }
                log.info("任务id：{}，任务删除成功", id);
                task.setStatus(Task.TaskStatus.CANCELLED);
                tasksMapper.updateTask(task); //软删除
            } catch (Exception e) {
                log.warn("任务id：{}，任务删除失败", id, e);
                throw new RuntimeException(e);
            }
        }
    }

    //修改任务
    @Override
    public void updateTask(Task task) {
        //1.验证权限

        //2.更新任务
        log.debug("修改任务，任务信息：{}", task);
        if(task == null){
            log.warn("修改任务失败，任务信息为空");
            throw new BusinessException("修改任务失败，任务信息为空");
        }
        task.setUpdateTime(LocalDateTime.now());
        tasksMapper.updateTask(task);

        //3.发送通知
    }

//    //根据id查询任务
//    @Override
//    public Task getTaskById(Integer id) {
//        return tasksMapper.getTaskById(id);
//    }

    /**
     * 分页查询
     */
    @Override
    public PageBean page(Integer page,
                         Integer pageSize,
                         Integer projectId,
                         Integer Id,
                         String title,
                         String assigneeName,
                         Task.TaskStatus status,
                         LocalDate begin,
                         LocalDate end) throws BusinessException {
        PageInfo<Task> pageBean = PageHelperUtils.safePageQuery(page, pageSize, () -> tasksMapper.list(projectId, Id, title, assigneeName, status, begin, end));
        return new PageBean(pageBean.getTotal(), pageBean.getList());
    }

    //获取任务中的成员id
    @Override
    public List<Integer> getTaskMembersId(Integer projectId, Integer taskId){
        return tasksMapper.getTaskMembersId(projectId, taskId);
    }
}
