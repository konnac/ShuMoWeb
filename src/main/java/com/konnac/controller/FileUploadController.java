package com.konnac.controller;

import com.konnac.pojo.Result;
import com.konnac.utils.AliyunOSSUtil;
import jakarta.servlet.http.HttpServletResponse;
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

    @Autowired
    private AliyunOSSUtil aliyunOSSUtil;

    @Value("${document.allowed-types}")
    private String allowedTypes;

    @PostMapping
    public Result uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                return Result.error("文件名格式不正确");
            }

            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            List<String> allowedTypeList = Arrays.asList(allowedTypes.split(","));

            if (!allowedTypeList.contains(extension)) {
                return Result.error("不支持的文件类型: " + extension);
            }

            String fileUrl = aliyunOSSUtil.uploadFile(file);

            Map<String, Object> data = new HashMap<>();
            data.put("url", fileUrl);
            data.put("filename", originalFilename);
            data.put("size", file.getSize());

            return Result.success("文件上传成功", data);
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @DeleteMapping
    public Result deleteFile(@RequestParam("url") String fileUrl) {
        try {
            aliyunOSSUtil.deleteFile(fileUrl);
            return Result.success("文件删除成功");
        } catch (Exception e) {
            return Result.error("文件删除失败: " + e.getMessage());
        }
    }

    @GetMapping("/download")
    public void downloadFile(@RequestParam("url") String fileUrl,
                            @RequestParam(value = "filename", required = false) String filename,
                            HttpServletResponse response) {
        try {
            InputStream inputStream = aliyunOSSUtil.downloadFile(fileUrl);

            String downloadFilename = filename;
            if (downloadFilename == null || downloadFilename.isEmpty()) {
                downloadFilename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            }

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + 
                    URLEncoder.encode(downloadFilename, StandardCharsets.UTF_8));

            OutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }

    @GetMapping("/download-url")
    public Result getDownloadUrl(@RequestParam("url") String fileUrl,
                                  @RequestParam(value = "expiration", defaultValue = "3600000") Long expiration) {
        try {
            String downloadUrl = aliyunOSSUtil.getPresignedUrl(fileUrl, expiration);
            Map<String, Object> data = new HashMap<>();
            data.put("url", downloadUrl);
            data.put("expiration", expiration);
            return Result.success("获取下载链接成功", data);
        } catch (Exception e) {
            return Result.error("获取下载链接失败: " + e.getMessage());
        }
    }
}
