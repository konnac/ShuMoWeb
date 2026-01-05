package com.konnac.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    private Integer id; // 文档id
    private Integer projectId; // 项目id
    private String projectName; // 项目名
    private String fileName; // 文件名
    private String fileUrl; // 文件url
    private Long fileSize; // 文件大小
    private String fileType; // 文件类型
    private DocumentCategory category; // 文档类别
    private String description; // 文档描述
    private Integer uploaderId; // 上传者id
    private String uploaderName; // 上传者名称
    private LocalDateTime uploadTime; // 上传时间
    private LocalDateTime updateTime; // 更新时间

    public enum DocumentCategory {
        REQUIREMENT("需求文档"),
        MEETING("会议纪要"),
        DESIGN("设计文档"),
        TECHNICAL("技术文档"),
        TEST("测试文档"),
        CONTRACT("合同文档"),
        PROVEMENT("完成证明"),
        OTHER("其他文档");

        private final String description;

        DocumentCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
