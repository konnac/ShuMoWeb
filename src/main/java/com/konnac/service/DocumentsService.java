package com.konnac.service;

import com.konnac.pojo.Document;
import com.konnac.pojo.PageBean;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface DocumentsService {

    /**
     * 上传文档
     */
    String uploadDocument(Integer projectId, MultipartFile file, String category, String description, Integer uploaderId) throws Exception;

    /**
     * 修改文档信息
     */
    void updateDocument(Integer documentId, String fileName, String category, String description, Integer uploaderId);

    /**
     * 删除文档
     */
    void deleteDocument(Integer documentId, Integer uploaderId);

    /**
     * 根据文档id查询文档信息
     */
    Document getDocumentById(Integer documentId);

    /**
     * 根据项目id查询项目下的所有文档
     */
    List<Document> listByProjectId(Integer projectId, String category, String fileName);

    /**
     * 获取项目下的所有文档
     */
    List<Document> listAllByProjectId(Integer projectId);

    /**
     * 统计项目下的文档数量
     */
    long countByProjectId(Integer projectId);

    /**
     * 删除项目下的所有文档
     */
    void deleteByProjectId(Integer projectId);

    /**
     * 分页查询
     */
    PageBean page(Integer page, Integer pageSize, Integer projectId, String fileType, Document.DocumentCategory category, String fileName, LocalDate begin, LocalDate end);
}
