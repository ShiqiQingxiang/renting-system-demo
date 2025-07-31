package com.rental.file.controller;

import com.rental.common.response.ApiResponse;
import com.rental.file.DTO.FileCategoryDTO;
import com.rental.file.service.FileCategoryService;
import com.rental.security.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件分类控制器
 */
@RestController
@RequestMapping("/api/files/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "文件分类管理", description = "文件分类管理相关接口")
@SecurityRequirement(name = "bearerAuth")
public class FileCategoryController {
    
    private final FileCategoryService categoryService;
    
    @GetMapping
    @Operation(
        summary = "获取所有活跃的文件分类",
        description = "获取系统中所有启用状态的文件分类列表"
    )
    public ApiResponse<List<FileCategoryDTO>> getAllCategories() {
        List<FileCategoryDTO> categories = categoryService.getAllActiveCategories();
        return ApiResponse.success("获取成功", categories);
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "根据ID获取文件分类",
        description = "获取指定ID的文件分类详细信息"
    )
    public ApiResponse<FileCategoryDTO> getCategoryById(
            @Parameter(description = "分类ID", required = true)
            @PathVariable Long id) {
        FileCategoryDTO category = categoryService.getCategoryById(id);
        return ApiResponse.success("获取成功", category);
    }
    
    @GetMapping("/code/{code}")
    @Operation(
        summary = "根据代码获取文件分类",
        description = "获取指定代码的文件分类详细信息"
    )
    public ApiResponse<FileCategoryDTO> getCategoryByCode(
            @Parameter(description = "分类代码", required = true)
            @PathVariable String code) {
        FileCategoryDTO category = categoryService.getCategoryByCode(code);
        return ApiResponse.success("获取成功", category);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('FILE_MANAGE')")
    @Operation(
        summary = "创建文件分类",
        description = "创建新的文件分类"
    )
    public ApiResponse<FileCategoryDTO> createCategory(
            @Parameter(description = "分类信息", required = true)
            @Valid @RequestBody FileCategoryDTO categoryDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        FileCategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
        
        log.info("用户 {} 创建文件分类: {} ({})", userDetails.getUsername(), 
            createdCategory.getName(), createdCategory.getCode());
        
        return ApiResponse.success("创建成功", createdCategory);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('FILE_MANAGE')")
    @Operation(
        summary = "更新文件分类",
        description = "更新指定ID的文件分类信息"
    )
    public ApiResponse<FileCategoryDTO> updateCategory(
            @Parameter(description = "分类ID", required = true)
            @PathVariable Long id,
            
            @Parameter(description = "更新信息", required = true)
            @Valid @RequestBody FileCategoryDTO categoryDTO,
            
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        FileCategoryDTO updatedCategory = categoryService.updateCategory(id, categoryDTO);
        
        log.info("用户 {} 更新文件分类: {} ({})", userDetails.getUsername(),
            updatedCategory.getName(), updatedCategory.getCode());
        
        return ApiResponse.success("更新成功", updatedCategory);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('FILE_MANAGE')")
    @Operation(
        summary = "删除文件分类",
        description = "删除指定ID的文件分类（逻辑删除）"
    )
    public ApiResponse<Void> deleteCategory(
            @Parameter(description = "分类ID", required = true)
            @PathVariable Long id,
            
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        FileCategoryDTO category = categoryService.getCategoryById(id);
        categoryService.deleteCategory(id);
        
        log.info("用户 {} 删除文件分类: {} ({})", userDetails.getUsername(),
            category.getName(), category.getCode());
        
        return ApiResponse.success();
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('FILE_MANAGE')")
    @Operation(
        summary = "切换分类状态",
        description = "启用或禁用指定的文件分类"
    )
    public ApiResponse<Void> toggleCategoryStatus(
            @Parameter(description = "分类ID", required = true)
            @PathVariable Long id,
            
            @Parameter(description = "是否启用", required = true)
            @RequestParam boolean isActive,
            
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        FileCategoryDTO category = categoryService.getCategoryById(id);
        categoryService.toggleCategoryStatus(id, isActive);
        
        log.info("用户 {} {}文件分类: {} ({})", userDetails.getUsername(),
            isActive ? "启用" : "禁用", category.getName(), category.getCode());
        
        return ApiResponse.success();
    }
    
    @GetMapping("/{id}/files/count")
    @Operation(
        summary = "获取分类文件数量",
        description = "获取指定分类下的文件数量统计"
    )
    public ApiResponse<Long> getFileCountByCategory(
            @Parameter(description = "分类ID", required = true)
            @PathVariable Long id) {
        
        long fileCount = categoryService.getFileCountByCategory(id);
        return ApiResponse.success("获取成功", fileCount);
    }
    
    @GetMapping("/code/{code}/exists")
    @PreAuthorize("hasAuthority('FILE_MANAGE')")
    @Operation(
        summary = "检查分类代码是否存在",
        description = "检查指定的分类代码是否已经存在"
    )
    public ApiResponse<Boolean> checkCategoryCodeExists(
            @Parameter(description = "分类代码", required = true)
            @PathVariable String code,
            
            @Parameter(description = "排除的ID（更新时使用）")
            @RequestParam(required = false) Long excludeId) {
        
        boolean exists = categoryService.isCategoryCodeExists(code, excludeId);
        return ApiResponse.success("检查完成", exists);
    }
}
