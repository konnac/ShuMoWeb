package com.konnac.utils;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.konnac.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Supplier;

/**
 * 分页查询工具类
 */
@Slf4j
public class PageHelperUtils {
    /**
     * 安全执行分页查询
     * @param page 页码
     * @param pageSize 分页大小
     * @param querySupplier 查询方法
     * @param <T> 返回类型
     * @return 分页结果
     */
    public static <T> PageInfo<T> safePageQuery(Integer page, Integer pageSize, Supplier<List<T>> querySupplier){
        if(page == null || page < 1){
            page = 1;
        }
        if(pageSize == null || pageSize < 1){
            pageSize = 10;
        }
        if(pageSize > 1000){
            log.warn("分页大小超过限制,自动调整为1000");
        }

        try{
            PageHelper.startPage(page, pageSize);
            List<T> list = querySupplier.get();
            return new PageInfo<>(list);
        } catch (Exception e){
            log.error("分页查询异常: page={}, pageSize={}", page, pageSize, e);
            throw new BusinessException("分页查询异常:" + e.getMessage(), e);
        } finally {
            PageHelper.clearPage();
        }
    }

    /**
     * 验证分页参数
     */
    public static void validatePageParams(Integer page, Integer pageSize) {
        if (page == null || page < 1) {
            throw new IllegalArgumentException("页码必须大于0");
        }
        if (pageSize == null) {
            throw new IllegalArgumentException("分页大小不能为空");
        }
        if (pageSize < 0) {
            throw new IllegalArgumentException("分页大小必须大于等于0");
        }
        if (pageSize > 1000) {
            throw new IllegalArgumentException("分页大小不能超过1000");
        }
    }

    /**
     * 获取安全的页码
     */
    public static int getSafePage(Integer page) {
        return (page == null || page < 1) ? 1 : page;
    }

    /**
     * 获取安全的分页大小
     */
    public static int getSafePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 1000);
    }
}
