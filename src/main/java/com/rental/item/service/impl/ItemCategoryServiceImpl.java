package com.rental.item.service.impl;

import com.rental.common.exception.BusinessException;
import com.rental.common.exception.ResourceNotFoundException;
import com.rental.item.DTO.ItemCategoryCreateRequest;
import com.rental.item.DTO.ItemCategoryDto;
import com.rental.item.DTO.ItemCategoryUpdateRequest;
import com.rental.item.model.ItemCategory;
import com.rental.item.repository.ItemCategoryRepository;
import com.rental.item.service.ItemCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemCategoryServiceImpl implements ItemCategoryService {

    private final ItemCategoryRepository categoryRepository;

    @Override
    @Transactional
    public ItemCategoryDto createCategory(ItemCategoryCreateRequest request) {
        log.info("创建物品分类：{}", request.getName());

        // 检查分类名称是否已存在
        if (isCategoryNameExists(request.getName())) {
            throw new BusinessException("分类名称已存在");
        }

        ItemCategory category = new ItemCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        // 设置父分类
        if (request.getParentId() != null) {
            ItemCategory parent = categoryRepository.findById(request.getParentId())
                .orElseThrow(() -> new ResourceNotFoundException("父分类不存在"));
            category.setParent(parent);
        }

        ItemCategory savedCategory = categoryRepository.save(category);
        log.info("分类创建成功，ID：{}", savedCategory.getId());

        return convertToDto(savedCategory);
    }

    @Override
    @Transactional
    public ItemCategoryDto updateCategory(Long id, ItemCategoryUpdateRequest request) {
        log.info("更新物品分类，ID：{}", id);

        ItemCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("分类不存在"));

        // 检查分类名称是否已存在（排除当前分类）
        if (request.getName() != null && isCategoryNameExists(request.getName(), id)) {
            throw new BusinessException("分类名称已存在");
        }

        // 更新字段
        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        if (request.getParentId() != null) {
            // 如果parentId为0或负数，表示设置为根分类
            if (request.getParentId() <= 0) {
                category.setParent(null);
            } else {
                // 检查是否会形成循环引用
                if (wouldCreateCircularReference(id, request.getParentId())) {
                    throw new BusinessException("不能将分类设置为自己或子分类的子分类");
                }

                ItemCategory parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("父分类不存在，ID: " + request.getParentId()));
                category.setParent(parent);
            }
        }

        ItemCategory savedCategory = categoryRepository.save(category);
        log.info("分类更新成功，ID：{}", savedCategory.getId());

        return convertToDto(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemCategoryDto getCategoryById(Long id) {
        ItemCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("分类不存在"));
        return convertToDto(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        log.info("删除物品分类，ID：{}", id);

        ItemCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("分类不存在"));

        // 检查是否可以删除
        if (!canDeleteCategory(id)) {
            throw new BusinessException("分类下存在子分类或物品，无法删除");
        }

        categoryRepository.delete(category);
        log.info("分类删除成功，ID：{}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemCategoryDto> getAllCategories() {
        List<ItemCategory> categories = categoryRepository.findAll();
        return categories.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemCategoryDto> getRootCategories() {
        List<ItemCategory> categories = categoryRepository.findByParentIsNullOrderBySortOrder();
        return categories.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemCategoryDto> getSubCategories(Long parentId) {
        List<ItemCategory> categories = categoryRepository.findByParentIdOrderBySortOrder(parentId);
        return categories.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemCategoryDto> getCategoryTree() {
        List<ItemCategory> allCategories = categoryRepository.findAllInTreeOrder();
        return buildCategoryTree(allCategories);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemCategoryDto> getCategoriesWithItems() {
        List<ItemCategory> categories = categoryRepository.findCategoriesWithItems();
        return categories.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateCategorySort(Long id, Integer sortOrder) {
        log.info("更新分类排序，ID：{}, 排序：{}", id, sortOrder);

        ItemCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("分类不存在"));

        category.setSortOrder(sortOrder);
        categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDeleteCategory(Long id) {
        // 检查是否有子分类
        List<ItemCategory> subCategories = categoryRepository.findByParentIdOrderBySortOrder(id);
        if (!subCategories.isEmpty()) {
            return false;
        }

        // 检查是否有物品
        long itemCount = categoryRepository.countItemsByCategoryId(id);
        return itemCount == 0;
    }

    @Override
    @Transactional(readOnly = true)
    public long getItemCountByCategory(Long categoryId) {
        return categoryRepository.countItemsByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCategoryNameExists(String name) {
        return categoryRepository.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCategoryNameExists(String name, Long excludeId) {
        return categoryRepository.existsByNameAndIdNot(name, excludeId);
    }

    private boolean wouldCreateCircularReference(Long categoryId, Long parentId) {
        if (categoryId.equals(parentId)) {
            return true;
        }

        ItemCategory parent = categoryRepository.findById(parentId).orElse(null);
        while (parent != null) {
            if (categoryId.equals(parent.getId())) {
                return true;
            }
            parent = parent.getParent();
        }

        return false;
    }

    private List<ItemCategoryDto> buildCategoryTree(List<ItemCategory> allCategories) {
        List<ItemCategoryDto> rootCategories = new ArrayList<>();

        // 先转换所有分类为DTO
        List<ItemCategoryDto> allDtos = allCategories.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        // 构建树形结构
        for (ItemCategoryDto dto : allDtos) {
            if (dto.getParentId() == null) {
                // 根分类
                dto.setChildren(findChildren(dto.getId(), allDtos));
                rootCategories.add(dto);
            }
        }

        return rootCategories;
    }

    private List<ItemCategoryDto> findChildren(Long parentId, List<ItemCategoryDto> allDtos) {
        List<ItemCategoryDto> children = allDtos.stream()
            .filter(dto -> parentId.equals(dto.getParentId()))
            .collect(Collectors.toList());

        // 递归设置子分类的子分类
        for (ItemCategoryDto child : children) {
            child.setChildren(findChildren(child.getId(), allDtos));
        }

        return children;
    }

    private ItemCategoryDto convertToDto(ItemCategory category) {
        ItemCategoryDto dto = new ItemCategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setSortOrder(category.getSortOrder());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        dto.setRoot(category.isRoot());
        dto.setHasChildren(category.hasChildren());

        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }

        // 设置物品数量
        dto.setItemCount((int) getItemCountByCategory(category.getId()));

        return dto;
    }
}
