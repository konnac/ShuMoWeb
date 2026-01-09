package com.konnac.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.konnac.exception.BusinessException;
import com.konnac.mapper.DocumentsMapper;
import com.konnac.mapper.ProjectsMapper;
import com.konnac.mapper.ProjectsMemberMapper;
import com.konnac.pojo.Document;
import com.konnac.pojo.PageBean;
import com.konnac.pojo.Project;
import com.konnac.pojo.ProjectMember;
import com.konnac.service.DocumentsService;
import com.konnac.utils.AliyunOSSUtil;
import com.konnac.utils.PageHelperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class DocumentsServiceImpl implements DocumentsService {

    @Autowired
    private DocumentsMapper documentsMapper;

    @Autowired
    private ProjectsMapper projectsMapper;

    @Autowired
    private ProjectsMemberMapper projectsMemberMapper;

    @Autowired
    private AliyunOSSUtil aliyunOSSUtil;

    /**
     * 上传文档
     */
    @Override
    public String uploadDocument(Integer projectId, MultipartFile file, String category, String description, Integer uploaderId) throws Exception {
        // 检查项目是否存在
        Project project = projectsMapper.getProjectById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }

        // 如果去除空格后不为空就转成枚举 空则默认OTHER
        Document.DocumentCategory docCategory = category != null && !category.trim().isEmpty() ? Document.DocumentCategory.valueOf(category) : Document.DocumentCategory.OTHER;
        // 获取上传文件的url
        String fileUrl = aliyunOSSUtil.uploadFileToProject(file, projectId, docCategory.toString());

        // 创建文档对象
        Document document = new Document();
        document.setProjectId(projectId);
        document.setFileName(file.getOriginalFilename());
        document.setFileUrl(fileUrl);
        document.setFileSize(file.getSize());
        document.setFileType(file.getContentType());
        document.setCategory(docCategory);
        document.setDescription(description);
        document.setUploaderId(uploaderId);
        document.setUploadTime(LocalDateTime.now());
        document.setUpdateTime(LocalDateTime.now());

        // 添加文档
        documentsMapper.addDocument(document);

        log.info("用户{}上传文档到项目{}: {}", uploaderId, projectId, file.getOriginalFilename());

        // 返回文件url
        return fileUrl;
    }
    /**
     * 修改文档
     */
    @Override
    public void updateDocument(Integer documentId, Integer projectId, String fileName, String category, String description, Integer uploaderId) {
        // 检查文档是否存在
        Document document = documentsMapper.getDocumentById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        // 验证文档所属项目是否匹配
        if (!document.getProjectId().equals(projectId)) {
            throw new BusinessException("文档不属于该项目");
        }

        // 修改文档
        document.setFileName(fileName);
        document.setCategory(category != null && !category.trim().isEmpty() ? Document.DocumentCategory.valueOf(category) : document.getCategory());
        document.setDescription(description);
        document.setUpdateTime(LocalDateTime.now());

        documentsMapper.updateDocument(document);

        log.info("用户{}修改文档: {}", uploaderId, documentId);
    }

    /**
     * 删除文档
     */
    @Override
    public void deleteDocument(Integer documentId, Integer uploaderId) {
        Document document = documentsMapper.getDocumentById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        try {
            // 删除OSS中的文件
            aliyunOSSUtil.deleteFile(document.getFileUrl());
        } catch (Exception e) {
            log.warn("删除OSS文件失败: {}", document.getFileUrl(), e);
        }

        documentsMapper.deleteDocument(documentId);

        log.info("用户{}删除文档: {}", uploaderId, documentId);
    }

    /**
     * 根据文档id获取文档
     */
    @Override
    public Document getDocumentById(Integer documentId) {
        return documentsMapper.getDocumentById(documentId);
    }


    /**
     * 统计项目下的文档数量
     */
    @Override
    public long countByProjectId(Integer projectId) {
        return documentsMapper.countByProjectId(projectId);
    }


    /**
     * 分页列出项目下的文档
     */
    @Override
    public PageBean page(Integer page, Integer pageSize,Integer projectId, String fileType, Document.DocumentCategory category, String fileName, LocalDate begin, LocalDate end) {
        log.debug("分页查询项目，参数：projectId={},fileType={},category={},fileName={},begin={},end={}", projectId, fileType, category, fileName, begin, end);
        PageInfo<Document> pageInfo = PageHelperUtils.safePageQuery(page, pageSize,
                () -> documentsMapper.list(
                        projectId,
                        fileType,
                        category,
                        fileName,
                        begin,
                        end
                        )
                );
        log.info("分页查询项目成功，结果：{}", pageInfo);
        return new PageBean(pageInfo.getTotal(), pageInfo.getList());
    }
}
