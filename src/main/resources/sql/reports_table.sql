-- 周报/月报表
CREATE TABLE IF NOT EXISTS reports (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '报表ID',
    user_id INT NOT NULL COMMENT '提交人ID',
    user_name VARCHAR(50) NOT NULL COMMENT '提交人姓名',
    report_type VARCHAR(20) NOT NULL COMMENT '报表类型：WEEKLY-周报，MONTHLY-月报',
    work_content TEXT COMMENT '本周/本月工作内容',
    completed_tasks TEXT COMMENT '已完成任务列表（JSON格式存储任务ID和名称）',
    next_plan TEXT COMMENT '下周/下月计划',
    problems TEXT COMMENT '遇到的问题',
    submit_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_report_type (report_type),
    INDEX idx_submit_time (submit_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='周报/月报表';
