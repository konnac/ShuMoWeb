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

    // 阿里云OSS 客户端: 与阿里云OSS交互
    @Autowired
    private OSS ossClient;

    // 阿里云 OSS配置
    @Autowired
    private com.konnac.config.AliyunOSSConfig aliyunOSSConfig;

    /**
     * 上传文件
     * @param file:要上传的文件
     * @return 文件完整URL
     */
    public String uploadFile(MultipartFile file) throws IOException {
        log.info("开始上传文件到OSS: {}", file.getOriginalFilename());

        //获取原始文件名
        String originalFilename = file.getOriginalFilename();
        //获取文件后缀
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        //生成文件名 uuid
        String fileName = generateFileName(extension);
        log.info("生成的文件名: {}", fileName);

        //创建上传文件的输入流
        InputStream inputStream = file.getInputStream();
        //创建上传Object的Metadata
        ObjectMetadata metadata = new ObjectMetadata();

        // 设置内容长度
        metadata.setContentLength(file.getSize());
        // 设置内容类型（MIME类型）
        metadata.setContentType(file.getContentType());

        //构建上传对象的请求 封装上传文件所需的所有参数
        PutObjectRequest putObjectRequest = new PutObjectRequest(
                aliyunOSSConfig.getBucketName(), // Bucket名称
                fileName, // 文件名
                inputStream, // 文件输入流
                metadata // 元数据: 文件的附加信息
        );

        log.info("开始上传到OSS Bucket: {}", aliyunOSSConfig.getBucketName());
        //执行请求 上传文件
        ossClient.putObject(putObjectRequest);

        // 返回文件完整URL
        String fileUrl = aliyunOSSConfig.getBaseUrl() + fileName;
        log.info("文件上传成功，完整URL: {}", fileUrl);
        
        return fileUrl;
    }

    /**
     * 上传项目文件(不带分类)
     */
    public String uploadFileToProject(MultipartFile file, Integer projectId) throws IOException {
        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        // 获取文件拓展名
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 生成文件名:uuid
        String fileName = generateProjectFileName(projectId, extension);

        //创建上传文件的输入流
        InputStream inputStream = file.getInputStream();
        //创建上传Object的Metadata
        ObjectMetadata metadata = new ObjectMetadata();
        //设置内容长度
        metadata.setContentLength(file.getSize());
        //设置内容类型:MIME类型
        metadata.setContentType(file.getContentType());

        //枸杞请求
        PutObjectRequest putObjectRequest = new PutObjectRequest(
                aliyunOSSConfig.getBucketName(),
                fileName,
                inputStream,
                metadata
        );

        //执行请求
        ossClient.putObject(putObjectRequest);
        //返回文件完整URL
        return aliyunOSSConfig.getBaseUrl() + fileName;
    }

    /**
     * 上传项目文件（带分类）
     * @param projectId: 项目ID
     * @param category: 分类
     * @return 文件完整URL
     */
    public String uploadFileToProject(MultipartFile file, Integer projectId, String category) throws IOException {
        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        // 获取文件后缀
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 生成文件名: uuid
        String fileName = generateProjectFileNameWithCategory(projectId, category, extension);

        //创建上传文件的输入流
        InputStream inputStream = file.getInputStream();
        //创建上传Object的Metadata
        ObjectMetadata metadata = new ObjectMetadata();
        //设置信息
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        //构建上传请求
        PutObjectRequest putObjectRequest = new PutObjectRequest(
                aliyunOSSConfig.getBucketName(),
                fileName,
                inputStream,
                metadata
        );
        //执行请求
        ossClient.putObject(putObjectRequest);

        return aliyunOSSConfig.getBaseUrl() + fileName;
    }


    /**
     * 删除文件
     */
    public void deleteFile(String fileUrl) {
        //去掉url中多余的部分:base url
        String fileName = fileUrl.replace(aliyunOSSConfig.getBaseUrl(), "");
        //删除文件
        ossClient.deleteObject(aliyunOSSConfig.getBucketName(), fileName);
    }

    /**
     * 下载文件
     */
    public InputStream downloadFile(String fileUrl) {
        // 获取配置中的OSS基础URL
        String baseUrl = aliyunOSSConfig.getBaseUrl();
        // 检查baseUrl是否以斜杠结尾，如果没有则添加斜杠
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
        // 从完整URL中提取文件名
        String fileName = fileUrl.replace(baseUrl, "");
        log.info("下载文件 - fileUrl: {}, baseUrl: {}, fileName: {}", fileUrl, aliyunOSSConfig.getBaseUrl(), fileName);
        // 返回ossobject对象，包含文件的元数据和内容
        OSSObject ossObject = ossClient.getObject(aliyunOSSConfig.getBucketName(), fileName);
        // 返回文件的InputStream 直接返回给前端下载
        return ossObject.getObjectContent();
    }

    /**
     * 获取预签名URL
     * @param fileUrl 文件URL
     * @param expirationTime 过期时间
     * @return 预签名URL
     */
    public String getPresignedUrl(String fileUrl, long expirationTime) {
        String fileName = fileUrl.replace(aliyunOSSConfig.getBaseUrl(), "");
        // 计算过期时间
        Date expiration = new Date(System.currentTimeMillis() + expirationTime);
        // 生成预签名URL
        URL url = ossClient.generatePresignedUrl(aliyunOSSConfig.getBucketName(), fileName, expiration);
        log.info("获取预签名URL: {}", url);
        return url.toString();
    }

    /**
     * 生成文件名: uuid
     */
    private String generateFileName(String extension) {
        //生成uuid并移除id分割的小横线
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return "avatar/" + uuid + extension;
    }

    /**
     * 生成项目文件名(不带分类)
     */
    private String generateProjectFileName(Integer projectId, String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return projectId + "/" + uuid + extension;
    }

    /**
     * 生成项目文件名(带分类)
     */
    private String generateProjectFileNameWithCategory(Integer projectId, String category, String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        //这样根目录的结构就会 projectId/category/uuid.extension
        return projectId + "/" + category + "/" + uuid + extension;
    }
}
