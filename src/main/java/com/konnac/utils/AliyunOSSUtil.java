package com.konnac.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class AliyunOSSUtil {

    @Autowired
    private OSS ossClient;

    @Autowired
    private com.konnac.config.AliyunOSSConfig aliyunOSSConfig;

    /**
     * 上传文件
     */
    public String uploadFile(MultipartFile file) throws IOException {
        log.info("开始上传文件到OSS: {}", file.getOriginalFilename());
        
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        String fileName = generateFileName(extension);
        log.info("生成的文件名: {}", fileName);

        InputStream inputStream = file.getInputStream();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        PutObjectRequest putObjectRequest = new PutObjectRequest(
                aliyunOSSConfig.getBucketName(),
                fileName,
                inputStream,
                metadata
        );

        log.info("开始上传到OSS Bucket: {}", aliyunOSSConfig.getBucketName());
        ossClient.putObject(putObjectRequest);
        
        String fileUrl = aliyunOSSConfig.getBaseUrl() + fileName;
        log.info("文件上传成功，完整URL: {}", fileUrl);
        
        return fileUrl;
    }

    /**
     * 上传项目文件
     */
    public String uploadFileToProject(MultipartFile file, Integer projectId) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        String fileName = generateProjectFileName(projectId, extension);

        InputStream inputStream = file.getInputStream();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        PutObjectRequest putObjectRequest = new PutObjectRequest(
                aliyunOSSConfig.getBucketName(),
                fileName,
                inputStream,
                metadata
        );

        ossClient.putObject(putObjectRequest);

        return aliyunOSSConfig.getBaseUrl() + fileName;
    }

    /**
     * 上传项目文件（带分类）
     */
    public String uploadFileToProject(MultipartFile file, Integer projectId, String category) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        String fileName = generateProjectFileNameWithCategory(projectId, category, extension);

        InputStream inputStream = file.getInputStream();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        PutObjectRequest putObjectRequest = new PutObjectRequest(
                aliyunOSSConfig.getBucketName(),
                fileName,
                inputStream,
                metadata
        );

        ossClient.putObject(putObjectRequest);

        return aliyunOSSConfig.getBaseUrl() + fileName;
    }


    /**
     * 删除文件
     */
    public void deleteFile(String fileUrl) {
        String fileName = fileUrl.replace(aliyunOSSConfig.getBaseUrl(), "");
        ossClient.deleteObject(aliyunOSSConfig.getBucketName(), fileName);
    }

    /**
     * 下载文件
     */
    public InputStream downloadFile(String fileUrl) {
        String baseUrl = aliyunOSSConfig.getBaseUrl();
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
        String fileName = fileUrl.replace(baseUrl, "");
        log.info("下载文件 - fileUrl: {}, baseUrl: {}, fileName: {}", fileUrl, aliyunOSSConfig.getBaseUrl(), fileName);
        OSSObject ossObject = ossClient.getObject(aliyunOSSConfig.getBucketName(), fileName);
        return ossObject.getObjectContent();
    }

    public String getPresignedUrl(String fileUrl, long expirationTime) {
        String fileName = fileUrl.replace(aliyunOSSConfig.getBaseUrl(), "");
        Date expiration = new Date(System.currentTimeMillis() + expirationTime);
        URL url = ossClient.generatePresignedUrl(aliyunOSSConfig.getBucketName(), fileName, expiration);
        return url.toString();
    }

    private String generateFileName(String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return "avatar/" + uuid + extension;
    }

    private String generateProjectFileName(Integer projectId, String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return projectId + "/" + uuid + extension;
    }

    private String generateProjectFileNameWithCategory(Integer projectId, String category, String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return projectId + "/" + category + "/" + uuid + extension;
    }
}
