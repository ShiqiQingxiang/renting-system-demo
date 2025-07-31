package com.rental.file.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件统计信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileStatisticsDTO {
    
    /**
     * 总文件数
     */
    private Long totalFiles;
    
    /**
     * 总大小（字节）
     */
    private Long totalSize;
    
    /**
     * 总大小的友好显示
     */
    private String totalSizeDisplay;
    
    /**
     * 图片文件数
     */
    private Long imageFiles;
    
    /**
     * 文档文件数
     */
    private Long documentFiles;
    
    /**
     * 其他文件数
     */
    private Long otherFiles;
    
    /**
     * 总下载次数
     */
    private Long totalDownloads;
    
    /**
     * 平均文件大小
     */
    private Long averageFileSize;
    
    /**
     * 最大文件大小
     */
    private Long maxFileSize;
    
    /**
     * 最常用的文件类型
     */
    private String mostUsedFileType;
} 