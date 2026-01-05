package com.konnac.controller;

import com.konnac.annotation.RequirePermission;
import com.konnac.enums.PermissionType;
import com.konnac.pojo.Document;
import com.konnac.pojo.PageBean;
import com.konnac.pojo.Result;
import com.konnac.service.DocumentsService;
import com.konnac.utils.AliyunOSSUtil;
import com.konnac.utils.AuthUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/documents")
public class DocumentsController {

    @Autowired
    private DocumentsService documentsService;

    @Autowired
    private AliyunOSSUtil aliyunOSSUtil;

    /**
     * 上传文档
     */
    @RequirePermission(PermissionType.FILE_UPLOAD)
    @PostMapping("/upload")
    public Result uploadDocument(@RequestParam("projectId") Integer projectId,
                                  @RequestParam("file") MultipartFile file,
                                  @RequestParam(value = "category", required = false, defaultValue = "OTHER") String category,
                                  @RequestParam(value = "description", required = false) String description) {
        try {
            Integer currentUserId = AuthUtils.getCurrentUserId();
            String fileUrl = documentsService.uploadDocument(projectId, file, category, description, currentUserId);

            Map<String, Object> data = new HashMap<>();
            data.put("url", fileUrl);
            data.put("fileName", file.getOriginalFilename());
            data.put("fileSize", file.getSize());
            data.put("category", category);

            return Result.success("文档上传成功", data);
        } catch (Exception e) {
            log.error("文档上传失败", e);
            return Result.error("文档上传失败: " + e.getMessage());
        }
    }

    /**
     * 修改文档
     */
    @RequirePermission(PermissionType.FILE_UPLOAD)
    @PutMapping
    public Result updateDocument(@RequestBody Document document) {
        try {
            Integer currentUserId = AuthUtils.getCurrentUserId();
            documentsService.updateDocument(document.getId(), document.getFileName(), 
                                           document.getCategory().toString(), document.getDescription(), currentUserId);
            return Result.success("文档更新成功");
        } catch (Exception e) {
            log.error("文档更新失败", e);
            return Result.error("文档更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除文档
     */
    @RequirePermission(PermissionType.FILE_DELETE)
    @DeleteMapping("/{id}")
    public Result deleteDocument(@PathVariable Integer id) {
        try {
            Integer currentUserId = AuthUtils.getCurrentUserId();
            documentsService.deleteDocument(id, currentUserId);
            return Result.success("文档删除成功");
        } catch (Exception e) {
            log.error("文档删除失败", e);
            return Result.error("文档删除失败: " + e.getMessage());
        }
    }

    /**
     * 查询文档
     */
    @RequirePermission(PermissionType.FILE_VIEW)
    @GetMapping("/{id}")
    public Result getDocumentById(@PathVariable Integer id) {
        try {
            Document document = documentsService.getDocumentById(id);
            return Result.success(document);
        } catch (Exception e) {
            log.error("查询文档失败", e);
            return Result.error("查询文档失败: " + e.getMessage());
        }
    }

    /**
     * 下载项目文档
     */
    @RequirePermission(PermissionType.FILE_DOWNLOAD)
    @GetMapping("/download/{id}")
    public void downloadDocument(@PathVariable Integer id, HttpServletResponse response) {
        InputStream inputStream = null;
        try {
            log.info("开始下载文档，文档ID: {}", id);
            
            Document document = documentsService.getDocumentById(id);
            if (document == null) {
                log.error("文档不存在，文档ID: {}", id);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            log.info("文档信息: fileName={}, fileUrl={}", document.getFileName(), document.getFileUrl());
            
            inputStream = aliyunOSSUtil.downloadFile(document.getFileUrl());
            
            if (inputStream == null) {
                log.error("从OSS获取文件流失败，文件URL: {}", document.getFileUrl());
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            String downloadFilename = document.getFileName();
            String encodedFilename = URLEncoder.encode(downloadFilename, StandardCharsets.UTF_8);
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename);
            response.setHeader("Content-Length", String.valueOf(document.getFileSize()));

            OutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            int totalBytes = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            outputStream.flush();
            outputStream.close();
            
            log.info("文档下载成功，总共传输字节数: {}", totalBytes);
        } catch (IOException e) {
            log.error("文档下载失败，文档ID: {}", id, e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "文档下载失败: " + e.getMessage());
            } catch (IOException ex) {
                log.error("发送错误响应失败", ex);
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("关闭输入流失败", e);
                }
            }
        }
    }

    /**
     * 根据项目id查询项目文档
     */
    @RequirePermission(PermissionType.FILE_VIEW)
    @GetMapping("/project/{projectId}")
    public Result listByProjectId(@PathVariable Integer projectId,
                                   @RequestParam(value = "category", required = false) String category,
                                   @RequestParam(value = "fileName", required = false) String fileName,
                                   @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                   @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        try {
            log.info("查询项目文档 - projectId: {}, category: {}, fileName: {}, page: {}, pageSize: {}", 
                    projectId, category, fileName, page, pageSize);
            
            List<Document> documents = documentsService.listByProjectId(projectId, category, fileName);
            long total = documentsService.countByProjectId(projectId);
            
            log.info("查询结果 - 文档总数: {}, 分页后数量: {}", total, documents.size());
            
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, documents.size());
            List<Document> pageData = documents.subList(start, end);
            
            Map<String, Object> result = new HashMap<>();
            result.put("rows", pageData);
            result.put("total", total);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询项目文档失败", e);
            return Result.error("查询项目文档失败: " + e.getMessage());
        }
    }

    /**
     * 统计项目文档数量
     */
    @RequirePermission(PermissionType.FILE_VIEW)
    @GetMapping("/project/{projectId}/count")
    public Result countByProjectId(@PathVariable Integer projectId) {
        try {
            long count = documentsService.countByProjectId(projectId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("统计文档数量失败", e);
            return Result.error("统计文档数量失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询项目文档
     */
    @RequirePermission(PermissionType.FILE_VIEW)
    @RequestMapping
    public Result page(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       Integer projectId,
                       String fileType,
                       Document.DocumentCategory category,
                       String fileName,
                       @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate begin,
                       @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate end
                       ){
        log.info("分页查询, 参数: page={},pageSize={}, projectId={},fileType={},category={},fileName={},begin={},end={}",
                page, pageSize, projectId, fileType, category, fileName, begin, end
                );
        PageBean pageBean = documentsService.page(page, pageSize, projectId, fileType, category, fileName, begin, end);
        return Result.success(pageBean);
    }
}
