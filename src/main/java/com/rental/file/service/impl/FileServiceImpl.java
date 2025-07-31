package com.rental.file.service.impl;

import com.rental.common.exception.BusinessException;
import com.rental.common.exception.ResourceNotFoundException;
import com.rental.common.response.PageResponse;
import com.rental.file.DTO.*;
import com.rental.file.config.FileConfig;
import com.rental.file.model.FileCategory;
import com.rental.file.model.FileInfo;
import com.rental.file.repository.FileCategoryRepository;
import com.rental.file.repository.FileInfoRepository;
import com.rental.file.service.FileService;
import com.rental.file.util.FileUtil;
import com.rental.file.util.FileValidationUtil;
import com.rental.user.model.User;
import com.rental.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 文件服务实现类
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    
    private final FileInfoRepository fileRepository;
    private final FileCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final FileConfig fileConfig;
    
    @Override
    public List<FileUploadResponse> uploadFiles(MultipartFile[] files, Long categoryId, 
                                               String entityType, Long entityId, Long uploaderId) {
        
        // 验证批量上传
        FileValidationUtil.validateBatchUpload(files, 10, 100 * 1024 * 1024); // 最多10个文件，总大小100MB
        
        return Arrays.stream(files)
            .map(file -> uploadSingleFile(file, categoryId, entityType, entityId, uploaderId))
            .collect(Collectors.toList());
    }
    
    @Override
    public FileUploadResponse uploadSingleFile(MultipartFile file, Long categoryId, 
                                              String entityType, Long entityId, Long uploaderId) {
        try {
            // 1. 获取文件分类
            FileCategory category = null;
            if (categoryId != null) {
                category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("文件分类不存在: " + categoryId));
            }
            
            // 2. 验证文件
            FileValidationUtil.validateFile(file, category);
            
            // 3. 计算文件哈希值（用于去重）
            String fileHash = null;
            if (fileConfig.isEnableHashCheck()) {
                fileHash = FileUtil.calculateMD5(file);
                
                // 检查是否已存在相同文件
                Optional<FileInfo> existingFile = fileRepository.findByFileHash(fileHash);
                if (existingFile.isPresent()) {
                    log.info("发现重复文件，返回现有文件: {}", existingFile.get().getId());
                    return buildFileUploadResponse(existingFile.get());
                }
            }
            
            // 4. 生成存储信息
            String categoryCode = category != null ? category.getCode() : "DOCUMENT";
            String storagePath = FileUtil.generateStoragePath(categoryCode);
            String storedName = FileUtil.generateStoredName(file.getOriginalFilename());
            
            // 5. 保存文件到磁盘
            File savedFile = saveFileToDisk(file, storagePath, storedName);
            
            // 6. 保存文件信息到数据库
            FileInfo fileInfo = createAndSaveFileInfo(file, storagePath + storedName, storedName, 
                fileHash, category, entityType, entityId, uploaderId);
            
            log.info("文件上传成功: {} -> {}", file.getOriginalFilename(), savedFile.getAbsolutePath());
            return buildFileUploadResponse(fileInfo);
            
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Resource downloadFile(Long fileId, Long currentUserId) {
        FileInfo fileInfo = getFileWithPermissionCheck(fileId, currentUserId, false);
        
        // 检查物理文件是否存在
        Path filePath = Paths.get(fileConfig.getBasePath(), fileInfo.getFilePath());
        File physicalFile = filePath.toFile();
        
        if (!physicalFile.exists()) {
            log.error("物理文件不存在: {}", filePath);
            throw new ResourceNotFoundException("文件不存在");
        }
        
        // 增加下载计数
        fileRepository.incrementDownloadCount(fileId);
        
        log.info("用户 {} 下载文件: {} ({})", currentUserId, fileInfo.getOriginalName(), fileId);
        return new FileSystemResource(physicalFile);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Resource previewFile(Long fileId, Long currentUserId) {
        FileInfo fileInfo = getFileWithPermissionCheck(fileId, currentUserId, true);
        
        // 只允许预览图片文件
        if (!fileInfo.isImageFile()) {
            throw new BusinessException("该文件类型不支持在线预览");
        }
        
        Path filePath = Paths.get(fileConfig.getBasePath(), fileInfo.getFilePath());
        File physicalFile = filePath.toFile();
        
        if (!physicalFile.exists()) {
            throw new ResourceNotFoundException("文件不存在");
        }
        
        return new FileSystemResource(physicalFile);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<FileInfoDTO> searchFiles(FileSearchRequest request, Pageable pageable, Long currentUserId) {
        // 如果不是管理员，只能搜索自己的文件
        if (!hasFileViewAllPermission(currentUserId)) {
            request.setUploaderId(currentUserId);
        }
        
        // 修复方法调用，确保参数类型正确
        Page<FileInfo> page = fileRepository.searchFiles(
            request.getFileName(),
            request.getCategoryId(),
            request.getUploaderId(),
            request.getRelatedEntityType(),
            request.getRelatedEntityId(),
            pageable
        );
        
        List<FileInfoDTO> dtoList = page.getContent().stream()
            .map(this::convertToFileInfoDTO)
            .collect(Collectors.toList());
        
        return PageResponse.of(
            dtoList,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements()
        );
    }
    
    @Override
    public void deleteFile(Long fileId, Long currentUserId) {
        FileInfo fileInfo = getFileWithPermissionCheck(fileId, currentUserId, false);
        
        // 执行逻辑删除
        fileRepository.softDeleteById(fileId);
        
        // 可选：删除物理文件（这里先不删除，保留数据）
        // deletePhysicalFile(fileInfo);
        
        log.info("用户 {} 删除文件: {} ({})", currentUserId, fileInfo.getOriginalName(), fileId);
    }
    
    @Override
    public void deleteFiles(List<Long> fileIds, Long currentUserId) {
        for (Long fileId : fileIds) {
            deleteFile(fileId, currentUserId);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<FileInfoDTO> getFilesByEntity(String entityType, Long entityId, Long currentUserId) {
        List<FileInfo> files = fileRepository.findByRelatedEntityTypeAndRelatedEntityIdAndIsActiveTrue(entityType, entityId);
        
        return files.stream()
            .filter(file -> hasFileAccessPermission(file, currentUserId))
            .map(this::convertToFileInfoDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public FileInfoDTO getFileInfo(Long fileId, Long currentUserId) {
        FileInfo fileInfo = getFileWithPermissionCheck(fileId, currentUserId, true);
        return convertToFileInfoDTO(fileInfo);
    }
    
    @Override
    public void updateAccessLevel(Long fileId, String accessLevel, Long currentUserId) {
        FileInfo fileInfo = getFileWithPermissionCheck(fileId, currentUserId, false);
        
        if (!Arrays.asList("PUBLIC", "PRIVATE").contains(accessLevel)) {
            throw new BusinessException("无效的访问级别: " + accessLevel);
        }
        
        fileInfo.setAccessLevel(accessLevel);
        fileRepository.save(fileInfo);
        
        log.info("用户 {} 更新文件访问级别: {} -> {}", currentUserId, fileId, accessLevel);
    }
    
    @Override
    public void moveFileToCategory(Long fileId, Long newCategoryId, Long currentUserId) {
        FileInfo fileInfo = getFileWithPermissionCheck(fileId, currentUserId, false);
        
        FileCategory newCategory = categoryRepository.findById(newCategoryId)
            .orElseThrow(() -> new ResourceNotFoundException("文件分类不存在: " + newCategoryId));
        
        fileInfo.setCategory(newCategory);
        fileRepository.save(fileInfo);
        
        log.info("用户 {} 移动文件到新分类: {} -> {}", currentUserId, fileId, newCategoryId);
    }
    
    @Override
    public void batchOperateFiles(FileBatchOperationRequest request, Long currentUserId) {
        switch (request.getOperation().toUpperCase()) {
            case "DELETE" -> deleteFiles(request.getFileIds(), currentUserId);
            case "UPDATE_ACCESS_LEVEL" -> {
                for (Long fileId : request.getFileIds()) {
                    updateAccessLevel(fileId, request.getNewAccessLevel(), currentUserId);
                }
            }
            case "MOVE" -> {
                for (Long fileId : request.getFileIds()) {
                    moveFileToCategory(fileId, request.getTargetCategoryId(), currentUserId);
                }
            }
            default -> throw new BusinessException("不支持的批量操作类型: " + request.getOperation());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public FileStatisticsDTO getUserFileStatistics(Long userId) {
        long totalFiles = fileRepository.countByUploaderId(userId);
        long totalSize = fileRepository.sumFileSizeByUploaderId(userId);
        
        // 这里可以添加更多统计逻辑
        return FileStatisticsDTO.builder()
            .totalFiles(totalFiles)
            .totalSize(totalSize)
            .totalSizeDisplay(FileUtil.formatFileSize(totalSize))
            .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<FileInfoDTO> getPopularFiles(int limit) {
        List<FileInfo> popularFiles = fileRepository.findTopDownloadedFiles(limit);
        return popularFiles.stream()
            .map(this::convertToFileInfoDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public int cleanupInvalidFiles() {
        // 实现清理逻辑
        return 0;
    }
    
    // === 私有辅助方法 ===
    
    /**
     * 保存文件到磁盘
     */
    private File saveFileToDisk(MultipartFile file, String storagePath, String storedName) throws IOException {
        Path uploadDir = Paths.get(fileConfig.getBasePath(), storagePath);
        
        // 确保目录存在
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        File targetFile = uploadDir.resolve(storedName).toFile();
        file.transferTo(targetFile);
        
        return targetFile;
    }
    
    /**
     * 创建并保存文件信息到数据库
     */
    private FileInfo createAndSaveFileInfo(MultipartFile file, String fullPath, String storedName,
                                          String fileHash, FileCategory category, String entityType,
                                          Long entityId, Long uploaderId) {
        
        User uploader = userRepository.findById(uploaderId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + uploaderId));
        
        FileInfo fileInfo = new FileInfo();
        fileInfo.setOriginalName(file.getOriginalFilename());
        fileInfo.setStoredName(storedName);
        fileInfo.setFilePath(fullPath);
        fileInfo.setFileSize(file.getSize());
        fileInfo.setContentType(file.getContentType());
        fileInfo.setFileExtension(FileUtil.getFileExtension(file.getOriginalFilename()));
        fileInfo.setFileHash(fileHash);
        fileInfo.setCategory(category);
        fileInfo.setUploader(uploader);
        fileInfo.setRelatedEntityType(entityType);
        fileInfo.setRelatedEntityId(entityId);
        fileInfo.setAccessLevel("PRIVATE");
        fileInfo.setIsActive(true);
        fileInfo.setDownloadCount(0);
        
        return fileRepository.save(fileInfo);
    }
    
    /**
     * 获取文件并检查权限
     */
    private FileInfo getFileWithPermissionCheck(Long fileId, Long currentUserId, boolean isPreview) {
        FileInfo fileInfo = fileRepository.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("文件不存在: " + fileId));
        
        if (!fileInfo.getIsActive()) {
            throw new ResourceNotFoundException("文件已被删除");
        }
        
        if (!hasFileAccessPermission(fileInfo, currentUserId)) {
            throw new BusinessException("没有访问权限");
        }
        
        return fileInfo;
    }
    
    /**
     * 检查文件访问权限
     */
    private boolean hasFileAccessPermission(FileInfo fileInfo, Long currentUserId) {
        // 公开文件所有人都可以访问
        if ("PUBLIC".equals(fileInfo.getAccessLevel())) {
            return true;
        }
        
        // 管理员可以访问所有文件
        if (hasFileViewAllPermission(currentUserId)) {
            return true;
        }
        
        // 文件所有者可以访问
        return fileInfo.getUploader().getId().equals(currentUserId);
    }
    
    /**
     * 检查是否有查看所有文件的权限（简化实现）
     */
    private boolean hasFileViewAllPermission(Long userId) {
        // 这里应该检查用户是否有FILE_VIEW_ALL权限
        // 简化实现，实际应该查询用户权限
        return false; // 暂时返回false，实际项目中需要实现权限检查
    }
    
    /**
     * 构建文件上传响应
     */
    private FileUploadResponse buildFileUploadResponse(FileInfo fileInfo) {
        return FileUploadResponse.builder()
            .id(fileInfo.getId())
            .originalName(fileInfo.getOriginalName())
            .storedName(fileInfo.getStoredName())
            .fileSize(fileInfo.getFileSize())
            .fileSizeDisplay(fileInfo.getFileSizeDisplay())
            .contentType(fileInfo.getContentType())
            .fileExtension(fileInfo.getFileExtension())
            .categoryName(fileInfo.getCategory() != null ? fileInfo.getCategory().getName() : null)
            .accessLevel(fileInfo.getAccessLevel())
            .downloadUrl(FileUtil.generateDownloadUrl(fileInfo.getId()))
            .previewUrl(FileUtil.generatePreviewUrl(fileInfo.getId()))
            .isImageFile(fileInfo.isImageFile())
            .createdAt(fileInfo.getCreatedAt())
            .build();
    }
    
    /**
     * 转换为文件信息DTO
     */
    private FileInfoDTO convertToFileInfoDTO(FileInfo fileInfo) {
        return FileInfoDTO.builder()
            .id(fileInfo.getId())
            .originalName(fileInfo.getOriginalName())
            .storedName(fileInfo.getStoredName())
            .fileSize(fileInfo.getFileSize())
            .fileSizeDisplay(fileInfo.getFileSizeDisplay())
            .contentType(fileInfo.getContentType())
            .fileExtension(fileInfo.getFileExtension())
            .fileHash(fileInfo.getFileHash())
            .categoryId(fileInfo.getCategory() != null ? fileInfo.getCategory().getId() : null)
            .categoryName(fileInfo.getCategory() != null ? fileInfo.getCategory().getName() : null)
            .categoryCode(fileInfo.getCategory() != null ? fileInfo.getCategory().getCode() : null)
            .uploaderId(fileInfo.getUploader().getId())
            .uploaderUsername(fileInfo.getUploader().getUsername())
            .relatedEntityType(fileInfo.getRelatedEntityType())
            .relatedEntityId(fileInfo.getRelatedEntityId())
            .accessLevel(fileInfo.getAccessLevel())
            .downloadCount(fileInfo.getDownloadCount())
            .isImageFile(fileInfo.isImageFile())
            .isDocumentFile(fileInfo.isDocumentFile())
            .downloadUrl(FileUtil.generateDownloadUrl(fileInfo.getId()))
            .previewUrl(FileUtil.generatePreviewUrl(fileInfo.getId()))
            .createdAt(fileInfo.getCreatedAt())
            .updatedAt(fileInfo.getUpdatedAt())
            .build();
    }
}

