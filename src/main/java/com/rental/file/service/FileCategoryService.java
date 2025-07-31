package com.rental.file.service;

import com.rental.file.DTO.FileCategoryDTO;
import com.rental.file.model.FileCategory;

import java.util.List;

/**
 * 文件分类服务接口
 */
public interface FileCategoryService {
    
    /**
     * 获取所有活跃的文件分类
     * 
     * @return 文件分类列表
     */
    List<FileCategoryDTO> getAllActiveCategories();
    
    /**
     * 根据ID获取文件分类
     * 
     * @param id 分类ID
     * @return 文件分类
     */
    FileCategoryDTO getCategoryById(Long id);
    
    /**
     * 根据代码获取文件分类
     * 
     * @param code 分类代码
     * @return 文件分类
     */
    FileCategoryDTO getCategoryByCode(String code);
    
    /**
     * 创建文件分类
     * 
     * @param categoryDTO 分类信息
     * @return 创建的分类
     */
    FileCategoryDTO createCategory(FileCategoryDTO categoryDTO);
    
    /**
     * 更新文件分类
     * 
     * @param id 分类ID
     * @param categoryDTO 更新信息
     * @return 更新后的分类
     */
    FileCategoryDTO updateCategory(Long id, FileCategoryDTO categoryDTO);
    
    /**
     * 删除文件分类（逻辑删除）
     * 
     * @param id 分类ID
     */
    void deleteCategory(Long id);
    
    /**
     * 启用/禁用文件分类
     * 
     * @param id 分类ID
     * @param isActive 是否启用
     */
    void toggleCategoryStatus(Long id, boolean isActive);
    
    /**
     * 获取分类的文件数量统计
     * 
     * @param categoryId 分类ID
     * @return 文件数量
     */
    long getFileCountByCategory(Long categoryId);
    
    /**
     * 检查分类代码是否已存在
     * 
     * @param code 分类代码
     * @param excludeId 排除的ID（更新时使用）
     * @return 是否存在
     */
    boolean isCategoryCodeExists(String code, Long excludeId);
} 