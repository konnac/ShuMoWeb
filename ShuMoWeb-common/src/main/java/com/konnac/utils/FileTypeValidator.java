package com.konnac.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class FileTypeValidator {

    @Value("${document.allowed-types}")
    private String allowedTypes;

    public boolean validate(MultipartFile file) {
        // 验证文件是否为空
        if (file.isEmpty()) {
            log.warn("上传的文件为空");
            return false;
        }

        // 验证文件名格式是否正确
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            log.warn("文件名格式不正确: {}", originalFilename);
            return false;
        }
        // 提取文件扩展名并转换为小写
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        List<String> allowedTypeList = Arrays.asList(allowedTypes.split(","));

        log.info("文件扩展名: {}", extension);
        log.info("允许的文件类型: {}", allowedTypes);
        // 检查文件扩展名是否在允许的类型列表中
        if (!allowedTypeList.contains(extension)) {
            log.warn("不支持的文件类型: {}", extension);
            return false;
        }
        log.info("文件类型验证成功");
        return true;
    }
}