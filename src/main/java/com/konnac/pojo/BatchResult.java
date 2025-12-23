package com.konnac.pojo;

import lombok.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 批量操作结果类
 */
@Data
public class BatchResult {
    private int total;                         // 总操作数
    private int successCount;                  // 成功数
    private int failureCount;                  // 失败数
    private List<Integer> successUserIds = new ArrayList<>();      // 成功的用户ID列表
    private Map<Integer, String> failureDetails = new HashMap<>(); // 失败详情：用户ID -> 错误信息

    /**
     * 添加成功记录
     */
    public void addSuccess(Integer userId) {
        successCount++;
        successUserIds.add(userId);
        total++;
    }

    /**
     * 添加失败记录
     */
    public void addFailure(Integer userId, String error) {
        failureCount++;
        failureDetails.put(userId, error);
        total++;
    }

    /**
     * 是否全部成功
     */
    public boolean isAllSuccess() {
        return failureCount == 0 && total > 0;
    }

    /**
     * 是否全部失败
     */
    public boolean isAllFailure() {
        return successCount == 0 && total > 0;
    }

}