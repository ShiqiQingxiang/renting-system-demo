package com.rental.file.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件上传响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    
    /**
     * 文件ID
     */
    private Long id;
    
    /**
     * 原始文件名
     */
    private String originalName;
    
    /**
     * 存储文件名
     */
    private String storedName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 文件大小的友好显示
     */
    private String fileSizeDisplay;
    
    /**
     * 文件类型
     */
    private String contentType;
    
    /**
     * 文件扩展名
     */
    private String fileExtension;
    
    /**
     * 文件分类名称
     */
    private String categoryName;
    
    /**
     * 访问级别
     */
    private String accessLevel;
    
    /**
     * 下载URL
     */
    private String downloadUrl;
    
    /**
     * 预览URL（用于图片等）
     */
    private String previewUrl;
    
    /**
     * 是否为图片文件
     */
    private Boolean isImageFile;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}