package com.rental.file.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件分类DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileCategoryDTO {
    
    /**
     * 分类ID
     */
    private Long id;
    
    /**
     * 分类名称
     */
    private String name;
    
    /**
     * 分类代码
     */
    private String code;
    
    /**
     * 分类描述
     */
    private String description;
    
    /**
     * 允许的扩展名
     */
    private String allowedExtensions;
    
    /**
     * 允许的扩展名数组
     */
    private String[] allowedExtensionsArray;
    
    /**
     * 最大文件大小（字节）
     */
    private Long maxFileSize;
    
    /**
     * 最大文件大小的友好显示
     */
    private String maxFileSizeDisplay;
    
    /**
     * 是否启用
     */
    private Boolean isActive;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
    
    /**
     * 该分类下的文件数量
     */
    private Long fileCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}