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
import java.text.SimpleDateFormat;
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
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        String fileName = generateFileName(extension);

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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String datePath = sdf.format(new Date());
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return datePath + "/" + uuid + extension;
    }

    private String generateProjectFileName(Integer projectId, String extension) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String datePath = sdf.format(new Date());
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return "projects/" + projectId + "/" + datePath + "/" + uuid + extension;
    }
}
