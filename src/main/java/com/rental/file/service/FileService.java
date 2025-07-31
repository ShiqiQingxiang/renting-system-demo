package com.rental.file.service;

import com.rental.common.response.PageResponse;
import com.rental.file.DTO.*;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件服务接口
 */
public interface FileService {
    
    /**
     * 批量上传文件
     * 
     * @param files 文件数组
     * @param categoryId 分类ID
     * @param entityType 关联实体类型
     * @param entityId 关联实体ID
     * @param uploaderId 上传者ID
     * @return 上传结果列表
     */
    List<FileUploadResponse> uploadFiles(MultipartFile[] files, Long categoryId, 
                                        String entityType, Long entityId, Long uploaderId);
    
    /**
     * 单文件上传
     * 
     * @param file 文件
     * @param categoryId 分类ID
     * @param entityType 关联实体类型
     * @param entityId 关联实体ID
     * @param uploaderId 上传者ID
     * @return 上传结果
     */
    FileUploadResponse uploadSingleFile(MultipartFile file, Long categoryId, 
                                       String entityType, Long entityId, Long uploaderId);
    
    /**
     * 下载文件
     * 
     * @param fileId 文件ID
     * @param currentUserId 当前用户ID
     * @return 文件资源
     */
    Resource downloadFile(Long fileId, Long currentUserId);
    
    /**
     * 预览文件（主要用于图片）
     * 
     * @param fileId 文件ID
     * @param currentUserId 当前用户ID
     * @return 文件资源
     */
    Resource previewFile(Long fileId, Long currentUserId);
    
    /**
     * 搜索文件
     * 
     * @param request 搜索条件
     * @param pageable 分页参数
     * @param currentUserId 当前用户ID
     * @return 分页的文件信息
     */
    PageResponse<FileInfoDTO> searchFiles(FileSearchRequest request, Pageable pageable, Long currentUserId);
    
    /**
     * 删除文件
     * 
     * @param fileId 文件ID
     * @param currentUserId 当前用户ID
     */
    void deleteFile(Long fileId, Long currentUserId);
    
    /**
     * 批量删除文件
     * 
     * @param fileIds 文件ID列表
     * @param currentUserId 当前用户ID
     */
    void deleteFiles(List<Long> fileIds, Long currentUserId);
    
    /**
     * 根据关联实体获取文件列表
     * 
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @param currentUserId 当前用户ID
     * @return 文件信息列表
     */
    List<FileInfoDTO> getFilesByEntity(String entityType, Long entityId, Long currentUserId);
    
    /**
     * 获取文件详细信息
     * 
     * @param fileId 文件ID
     * @param currentUserId 当前用户ID
     * @return 文件信息
     */
    FileInfoDTO getFileInfo(Long fileId, Long currentUserId);
    
    /**
     * 更新文件访问级别
     * 
     * @param fileId 文件ID
     * @param accessLevel 新的访问级别
     * @param currentUserId 当前用户ID
     */
    void updateAccessLevel(Long fileId, String accessLevel, Long currentUserId);
    
    /**
     * 移动文件到新分类
     * 
     * @param fileId 文件ID
     * @param newCategoryId 新分类ID
     * @param currentUserId 当前用户ID
     */
    void moveFileToCategory(Long fileId, Long newCategoryId, Long currentUserId);
    
    /**
     * 批量操作文件
     * 
     * @param request 批量操作请求
     * @param currentUserId 当前用户ID
     */
    void batchOperateFiles(FileBatchOperationRequest request, Long currentUserId);
    
    /**
     * 获取用户的文件统计信息
     * 
     * @param userId 用户ID
     * @return 统计信息
     */
    FileStatisticsDTO getUserFileStatistics(Long userId);
    
    /**
     * 获取热门下载文件
     * 
     * @param limit 返回数量限制
     * @return 热门文件列表
     */
    List<FileInfoDTO> getPopularFiles(int limit);
    
    /**
     * 清理无效文件（物理文件不存在但数据库有记录）
     * 
     * @return 清理的文件数量
     */
    int cleanupInvalidFiles();
}

 