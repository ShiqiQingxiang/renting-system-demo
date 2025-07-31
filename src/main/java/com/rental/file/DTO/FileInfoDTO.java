package com.rental.file.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfoDTO {
    
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
     * 文件哈希值
     */
    private String fileHash;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 分类代码
     */
    private String categoryCode;
    
    /**
     * 上传者ID
     */
    private Long uploaderId;
    
    /**
     * 上传者用户名
     */
    private String uploaderUsername;
    
    /**
     * 关联实体类型
     */
    private String relatedEntityType;
    
    /**
     * 关联实体ID
     */
    private Long relatedEntityId;
    
    /**
     * 访问级别
     */
    private String accessLevel;
    
    /**
     * 下载次数
     */
    private Integer downloadCount;
    
    /**
     * 是否为图片文件
     */
    private Boolean isImageFile;
    
    /**
     * 是否为文档文件
     */
    private Boolean isDocumentFile;
    
    /**
     * 下载URL
     */
    private String downloadUrl;
    
    /**
     * 预览URL
     */
    private String previewUrl;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}