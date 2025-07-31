package com.rental.file.service.impl;

import com.rental.common.exception.BusinessException;
import com.rental.common.exception.ResourceNotFoundException;
import com.rental.file.DTO.FileCategoryDTO;
import com.rental.file.model.FileCategory;
import com.rental.file.repository.FileCategoryRepository;
import com.rental.file.repository.FileInfoRepository;
import com.rental.file.service.FileCategoryService;
import com.rental.file.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件分类服务实现类
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class FileCategoryServiceImpl implements FileCategoryService {
    
    private final FileCategoryRepository categoryRepository;
    private final FileInfoRepository fileRepository;
    
    @Override
    @Transactional(readOnly = true)
    public List<FileCategoryDTO> getAllActiveCategories() {
        List<FileCategory> categories = categoryRepository.findByIsActiveTrueOrderBySortOrder();
        return categories.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public FileCategoryDTO getCategoryById(Long id) {
        FileCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("文件分类不存在: " + id));
        return convertToDTO(category);
    }
    
    @Override
    @Transactional(readOnly = true)
    public FileCategoryDTO getCategoryByCode(String code) {
        FileCategory category = categoryRepository.findByCode(code)
            .orElseThrow(() -> new ResourceNotFoundException("文件分类不存在: " + code));
        return convertToDTO(category);
    }
    
    @Override
    public FileCategoryDTO createCategory(FileCategoryDTO categoryDTO) {
        // 验证分类代码是否已存在
        if (isCategoryCodeExists(categoryDTO.getCode(), null)) {
            throw new BusinessException("分类代码已存在: " + categoryDTO.getCode());
        }
        
        FileCategory category = new FileCategory();
        category.setName(categoryDTO.getName());
        category.setCode(categoryDTO.getCode().toUpperCase());
        category.setDescription(categoryDTO.getDescription());
        category.setAllowedExtensions(categoryDTO.getAllowedExtensions());
        category.setMaxFileSize(categoryDTO.getMaxFileSize());
        category.setIsActive(true);
        category.setSortOrder(categoryDTO.getSortOrder() != null ? categoryDTO.getSortOrder() : 0);
        
        FileCategory savedCategory = categoryRepository.save(category);
        log.info("创建文件分类: {} ({})", savedCategory.getName(), savedCategory.getCode());
        
        return convertToDTO(savedCategory);
    }
    
    @Override
    public FileCategoryDTO updateCategory(Long id, FileCategoryDTO categoryDTO) {
        FileCategory existingCategory = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("文件分类不存在: " + id));
        
        // 验证分类代码是否已存在（排除当前记录）
        if (isCategoryCodeExists(categoryDTO.getCode(), id)) {
            throw new BusinessException("分类代码已存在: " + categoryDTO.getCode());
        }
        
        existingCategory.setName(categoryDTO.getName());
        existingCategory.setCode(categoryDTO.getCode().toUpperCase());
        existingCategory.setDescription(categoryDTO.getDescription());
        existingCategory.setAllowedExtensions(categoryDTO.getAllowedExtensions());
        existingCategory.setMaxFileSize(categoryDTO.getMaxFileSize());
        existingCategory.setSortOrder(categoryDTO.getSortOrder() != null ? categoryDTO.getSortOrder() : 0);
        
        FileCategory updatedCategory = categoryRepository.save(existingCategory);
        log.info("更新文件分类: {} ({})", updatedCategory.getName(), updatedCategory.getCode());
        
        return convertToDTO(updatedCategory);
    }
    
    @Override
    public void deleteCategory(Long id) {
        FileCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("文件分类不存在: " + id));
        
        // 检查是否有文件正在使用此分类
        long fileCount = getFileCountByCategory(id);
        if (fileCount > 0) {
            throw new BusinessException("无法删除分类，还有 " + fileCount + " 个文件正在使用此分类");
        }
        
        category.setIsActive(false);
        categoryRepository.save(category);
        
        log.info("删除文件分类: {} ({})", category.getName(), category.getCode());
    }
    
    @Override
    public void toggleCategoryStatus(Long id, boolean isActive) {
        FileCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("文件分类不存在: " + id));
        
        category.setIsActive(isActive);
        categoryRepository.save(category);
        
        log.info("{}文件分类: {} ({})", isActive ? "启用" : "禁用", category.getName(), category.getCode());
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getFileCountByCategory(Long categoryId) {
        return fileRepository.countByCategoryIdAndIsActiveTrue(categoryId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isCategoryCodeExists(String code, Long excludeId) {
        return categoryRepository.existsByCodeAndIdNot(code.toUpperCase(), excludeId);
    }
    
    /**
     * 转换为DTO
     */
    private FileCategoryDTO convertToDTO(FileCategory category) {
        long fileCount = getFileCountByCategory(category.getId());
        
        return FileCategoryDTO.builder()
            .id(category.getId())
            .name(category.getName())
            .code(category.getCode())
            .description(category.getDescription())
            .allowedExtensions(category.getAllowedExtensions())
            .allowedExtensionsArray(category.getAllowedExtensionsArray())
            .maxFileSize(category.getMaxFileSize())
            .maxFileSizeDisplay(FileUtil.formatFileSize(category.getMaxFileSize()))
            .isActive(category.getIsActive())
            .sortOrder(category.getSortOrder())
            .fileCount(fileCount)
            .createdAt(category.getCreatedAt())
            .updatedAt(category.getUpdatedAt())
            .build();
    }
} 