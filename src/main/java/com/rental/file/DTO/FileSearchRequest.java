package com.rental.file.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件搜索请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileSearchRequest {
    
    /**
     * 文件名（模糊匹配）
     */
    private String fileName;
    
    /**
     * 文件分类ID
     */
    private Long categoryId;
    
    /**
     * 文件分类代码
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
     * 文件类型（MIME类型）
     */
    private String contentType;
    
    /**
     * 文件扩展名
     */
    private String fileExtension;
    
    /**
     * 最小文件大小（字节）
     */
    private Long minFileSize;
    
    /**
     * 最大文件大小（字节）
     */
    private Long maxFileSize;
    
    /**
     * 开始创建时间
     */
    private LocalDateTime createdAtStart;
    
    /**
     * 结束创建时间
     */
    private LocalDateTime createdAtEnd;
    
    /**
     * 是否只查询图片文件
     */
    private Boolean imageOnly;
    
    /**
     * 是否只查询文档文件
     */
    private Boolean documentOnly;
    
    /**
     * 最小下载次数
     */
    private Integer minDownloadCount;
    
    /**
     * 排序字段（name, size, createdAt, downloadCount）
     */
    private String sortBy = "createdAt";
    
    /**
     * 排序方向（asc, desc）
     */
    private String sortDirection = "desc";
}