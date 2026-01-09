package com.konnac.controller;

import com.konnac.pojo.Result;
import com.konnac.utils.AliyunOSSUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    @Autowired
    private AliyunOSSUtil aliyunOSSUtil;

    @Value("${avatar.allowed-types}")
    private String allowedTypes;

    @PostMapping
    public Result uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            log.info("开始上传文件: {}", file.getOriginalFilename());
            log.info("文件大小: {} bytes", file.getSize());
            log.info("文件类型: {}", file.getContentType());

            // 验证文件是否为空
            if (file.isEmpty()) {
                log.warn("上传的文件为空");
                return Result.error("文件不能为空");
            }

            // 验证文件名格式是否正确
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                log.warn("文件名格式不正确: {}", originalFilename);
                return Result.error("文件名格式不正确");
            }

            // 提取文件扩展名并转换为小写
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            // 将配置的允许类型字符串转换为列表
            List<String> allowedTypeList = Arrays.asList(allowedTypes.split(","));

            log.info("文件扩展名: {}", extension);
            log.info("允许的文件类型: {}", allowedTypes);

            // 检查文件扩展名是否在允许的类型列表中
            if (!allowedTypeList.contains(extension)) {
                log.warn("不支持的文件类型: {}", extension);
                return Result.error("不支持的文件类型: " + extension);
            }

            // 上传文件
            String fileUrl = aliyunOSSUtil.uploadFile(file);
            log.info("文件上传成功，URL: {}", fileUrl);

            // 返回结果
            Map<String, Object> data = new HashMap<>();
            data.put("url", fileUrl);
            data.put("filename", originalFilename);
            data.put("size", file.getSize());

            return Result.success("文件上传成功", data);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("文件上传发生异常", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

}
