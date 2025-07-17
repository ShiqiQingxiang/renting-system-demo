package com.rental.item.service;

import com.rental.item.DTO.ItemCategoryCreateRequest;
import com.rental.item.DTO.ItemCategoryDto;
import com.rental.item.DTO.ItemCategoryUpdateRequest;

import java.util.List;

public interface ItemCategoryService {

    // 基本CRUD操作
    ItemCategoryDto createCategory(ItemCategoryCreateRequest request);
    ItemCategoryDto updateCategory(Long id, ItemCategoryUpdateRequest request);
    ItemCategoryDto getCategoryById(Long id);
    void deleteCategory(Long id);

    // 查询操作
    List<ItemCategoryDto> getAllCategories();
    List<ItemCategoryDto> getRootCategories();
    List<ItemCategoryDto> getSubCategories(Long parentId);
    List<ItemCategoryDto> getCategoryTree();
    List<ItemCategoryDto> getCategoriesWithItems();

    // 业务操作
    void updateCategorySort(Long id, Integer sortOrder);
    boolean canDeleteCategory(Long id);
    long getItemCountByCategory(Long categoryId);

    // 验证方法
    boolean isCategoryNameExists(String name);
    boolean isCategoryNameExists(String name, Long excludeId);
}
