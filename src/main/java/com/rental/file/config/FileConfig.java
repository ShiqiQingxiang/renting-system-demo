package com.rental.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件上传配置类
 */
@ConfigurationProperties(prefix = "file.upload")
@Data
@Component
public class FileConfig {
    
    /**
     * 文件存储基础路径
     */
    private String basePath = "uploads/";
    
    /**
     * 允许的文件扩展名
     */
    private String[] allowedExtensions = {"jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xls", "xlsx", "txt"};
    
    /**
     * 最大文件大小（字节）
     */
    private long maxFileSize = 20 * 1024 * 1024; // 20MB
    
    /**
     * 是否启用文件哈希检查（去重）
     */
    private boolean enableHashCheck = true;
}