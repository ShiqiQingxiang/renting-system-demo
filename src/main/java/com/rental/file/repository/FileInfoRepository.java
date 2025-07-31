package com.rental.file.repository;

import com.rental.file.model.FileInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 文件信息数据访问接口
 */
@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, Long>, JpaSpecificationExecutor<FileInfo> {
    
    /**
     * 根据文件哈希值查找文件
     * @param fileHash 文件哈希值
     * @return 文件信息
     */
    Optional<FileInfo> findByFileHash(String fileHash);
    
    /**
     * 根据关联实体查找文件列表
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @return 文件信息列表
     */
    List<FileInfo> findByRelatedEntityTypeAndRelatedEntityIdAndIsActiveTrue(String entityType, Long entityId);
    
    /**
     * 查找用户上传的有效文件
     * @param uploaderId 上传者ID
     * @return 文件信息列表
     */
    List<FileInfo> findByUploaderIdAndIsActiveTrueOrderByCreatedAtDesc(Long uploaderId);
    
    /**
     * 分页查找用户上传的文件
     * @param uploaderId 上传者ID
     * @param pageable 分页参数
     * @return 分页的文件信息
     */
    Page<FileInfo> findByUploaderIdAndIsActiveTrueOrderByCreatedAtDesc(Long uploaderId, Pageable pageable);
    
    /**
     * 复杂条件搜索文件
     * @param fileName 文件名（模糊匹配）
     * @param categoryId 分类ID
     * @param uploaderId 上传者ID
     * @param entityType 关联实体类型
     * @param entityId 关联实体ID
     * @param pageable 分页参数
     * @return 分页的文件信息
     */
    @Query("SELECT fi FROM FileInfo fi LEFT JOIN fi.category c LEFT JOIN fi.uploader u " +
           "WHERE fi.isActive = true AND " +
           "(:fileName IS NULL OR fi.originalName LIKE %:fileName%) AND " +
           "(:categoryId IS NULL OR fi.category.id = :categoryId) AND " +
           "(:uploaderId IS NULL OR fi.uploader.id = :uploaderId) AND " +
           "(:entityType IS NULL OR fi.relatedEntityType = :entityType) AND " +
           "(:entityId IS NULL OR fi.relatedEntityId = :entityId)")
    Page<FileInfo> searchFiles(@Param("fileName") String fileName,
                              @Param("categoryId") Long categoryId,
                              @Param("uploaderId") Long uploaderId,
                              @Param("entityType") String entityType,
                              @Param("entityId") Long entityId,
                              Pageable pageable);
    
    /**
     * 增加文件下载次数
     * @param id 文件ID
     */
    @Modifying
    @Query("UPDATE FileInfo fi SET fi.downloadCount = fi.downloadCount + 1 WHERE fi.id = :id")
    void incrementDownloadCount(@Param("id") Long id);
    
    /**
     * 查找指定时间范围内的文件
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 文件信息列表
     */
    @Query("SELECT fi FROM FileInfo fi WHERE fi.isActive = true AND " +
           "fi.createdAt BETWEEN :startTime AND :endTime")
    List<FileInfo> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据文件类型查找文件
     * @param contentTypes 文件类型列表
     * @param pageable 分页参数
     * @return 分页的文件信息
     */
    @Query("SELECT fi FROM FileInfo fi WHERE fi.isActive = true AND fi.contentType IN :contentTypes")
    Page<FileInfo> findByContentTypeIn(@Param("contentTypes") List<String> contentTypes, Pageable pageable);
    
    /**
     * 查找大文件（超过指定大小）
     * @param minSize 最小文件大小（字节）
     * @param pageable 分页参数
     * @return 分页的文件信息
     */
    @Query("SELECT fi FROM FileInfo fi WHERE fi.isActive = true AND fi.fileSize >= :minSize")
    Page<FileInfo> findLargeFiles(@Param("minSize") Long minSize, Pageable pageable);
    
    /**
     * 统计用户上传的文件数量
     * @param uploaderId 上传者ID
     * @return 文件数量
     */
    @Query("SELECT COUNT(fi) FROM FileInfo fi WHERE fi.uploader.id = :uploaderId AND fi.isActive = true")
    long countByUploaderId(@Param("uploaderId") Long uploaderId);
    
    /**
     * 统计用户上传的文件总大小
     * @param uploaderId 上传者ID
     * @return 文件总大小（字节）
     */
    @Query("SELECT COALESCE(SUM(fi.fileSize), 0) FROM FileInfo fi WHERE fi.uploader.id = :uploaderId AND fi.isActive = true")
    long sumFileSizeByUploaderId(@Param("uploaderId") Long uploaderId);
    
    /**
     * 查找热门下载文件（按下载次数排序）
     * @param limit 限制数量
     * @return 文件信息列表
     */
    @Query("SELECT fi FROM FileInfo fi WHERE fi.isActive = true ORDER BY fi.downloadCount DESC LIMIT :limit")
    List<FileInfo> findTopDownloadedFiles(@Param("limit") int limit);
    
    /**
     * 逻辑删除文件（设置is_active为false）
     * @param id 文件ID
     */
    @Modifying
    @Query("UPDATE FileInfo fi SET fi.isActive = false WHERE fi.id = :id")
    void softDeleteById(@Param("id") Long id);
    
    /**
     * 批量逻辑删除文件
     * @param ids 文件ID列表
     */
    @Modifying
    @Query("UPDATE FileInfo fi SET fi.isActive = false WHERE fi.id IN :ids")
    void softDeleteByIds(@Param("ids") List<Long> ids);
    
    /**
     * 统计指定分类下的活跃文件数量
     */
    @Query("SELECT COUNT(fi) FROM FileInfo fi WHERE fi.category.id = :categoryId AND fi.isActive = true")
    long countByCategoryIdAndIsActiveTrue(@Param("categoryId") Long categoryId);
}