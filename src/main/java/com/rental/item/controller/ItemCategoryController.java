package com.rental.item.controller;

import com.rental.common.response.ApiResponse;
import com.rental.item.DTO.ItemCategoryCreateRequest;
import com.rental.item.DTO.ItemCategoryDto;
import com.rental.item.DTO.ItemCategoryUpdateRequest;
import com.rental.item.service.ItemCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/item-categories")
@RequiredArgsConstructor
@Tag(name = "物品分类管理", description = "物品分类管理相关接口，支持分类的增删改查、树形结构管理等功能")
@SecurityRequirement(name = "bearerAuth")
public class ItemCategoryController {

    private final ItemCategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('CATEGORY_CREATE')")
    @Operation(
        summary = "创建物品分类",
        description = "创建新的物品分类，支持父子分类关系"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "201",
        description = "分类创建成功",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "创建分类成功示例",
                value = """
                {
                    "success": true,
                    "message": "分类创建成功",
                    "data": {
                        "id": 1,
                        "name": "电子设备",
                        "description": "各类电子设备租赁",
                        "parentId": null,
                        "sortOrder": 1,
                        "children": []
                    }
                }
                """
            )
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数错误")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "权限不足")
    public ResponseEntity<ApiResponse<ItemCategoryDto>> createCategory(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "分类创建请求信息",
                content = @Content(
                    schema = @Schema(implementation = ItemCategoryCreateRequest.class),
                    examples = @ExampleObject(
                        name = "创建分类请求示例",
                        value = """
                        {
                            "name": "电子设备",
                            "description": "各类电子设备租赁",
                            "parentId": null,
                            "sortOrder": 1
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody ItemCategoryCreateRequest request) {

        ItemCategoryDto result = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("分类创建成功", result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CATEGORY_UPDATE')")
    @Operation(summary = "更新物品分类", description = "更新物品分类信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "分类更新成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数错误")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "权限不足")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "分类不存在")
    public ResponseEntity<ApiResponse<ItemCategoryDto>> updateCategory(
            @Parameter(description = "分类ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody ItemCategoryUpdateRequest request) {

        ItemCategoryDto result = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("分类更新成功", result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取分类详情", description = "根据ID获取分类详细信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "分类不存在")
    public ResponseEntity<ApiResponse<ItemCategoryDto>> getCategory(
            @Parameter(description = "分类ID", example = "1") @PathVariable Long id) {
        ItemCategoryDto result = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CATEGORY_DELETE')")
    @Operation(summary = "删除物品分类", description = "删除指定的物品分类")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "删除成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "权限不足")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "分类不存在")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "分类下有物品，无法删除")
    public ResponseEntity<ApiResponse<String>> deleteCategory(
            @Parameter(description = "分类ID", example = "1") @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("分类删除成功"));
    }

    @GetMapping
    @Operation(summary = "获取所有分类", description = "获取所有物品分类列表")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    public ResponseEntity<ApiResponse<List<ItemCategoryDto>>> getAllCategories() {
        List<ItemCategoryDto> result = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/roots")
    @Operation(summary = "获取根分类", description = "获取所有根级分类")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    public ResponseEntity<ApiResponse<List<ItemCategoryDto>>> getRootCategories() {
        List<ItemCategoryDto> result = categoryService.getRootCategories();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{parentId}/children")
    @Operation(summary = "获取子分类", description = "获取指定分类的所有子分类")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "父分类不存在")
    public ResponseEntity<ApiResponse<List<ItemCategoryDto>>> getSubCategories(
            @Parameter(description = "父分类ID", example = "1") @PathVariable Long parentId) {

        List<ItemCategoryDto> result = categoryService.getSubCategories(parentId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/tree")
    @Operation(summary = "获取分类树", description = "获取完整的分类树形结构")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "获取成功",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "分类树示例",
                value = """
                {
                    "success": true,
                    "message": "操作成功",
                    "data": [
                        {
                            "id": 1,
                            "name": "电子设备",
                            "description": "各类电子设备租赁",
                            "parentId": null,
                            "sortOrder": 1,
                            "children": [
                                {
                                    "id": 2,
                                    "name": "手机数码",
                                    "description": "手机、平板等数码产品",
                                    "parentId": 1,
                                    "sortOrder": 1,
                                    "children": []
                                }
                            ]
                        }
                    ]
                }
                """
            )
        )
    )
    public ResponseEntity<ApiResponse<List<ItemCategoryDto>>> getCategoryTree() {
        List<ItemCategoryDto> result = categoryService.getCategoryTree();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/with-items")
    @Operation(
        summary = "获取有物品的分类",
        description = "获取所有包含物品的分类"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    public ResponseEntity<ApiResponse<List<ItemCategoryDto>>> getCategoriesWithItems() {
        List<ItemCategoryDto> result = categoryService.getCategoriesWithItems();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/{id}/sort")
    @PreAuthorize("hasAuthority('CATEGORY_UPDATE')")
    @Operation(
        summary = "更新分类排序",
        description = "更新分类的排序顺序"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "排序更新成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "权限不足")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "分类不存在")
    public ResponseEntity<ApiResponse<String>> updateCategorySort(
            @PathVariable Long id,
            @Parameter(description = "排序值") @RequestParam Integer sortOrder) {

        categoryService.updateCategorySort(id, sortOrder);
        return ResponseEntity.ok(ApiResponse.success("排序更新成功"));
    }

    @GetMapping("/{id}/can-delete")
    @PreAuthorize("hasAuthority('CATEGORY_DELETE')")
    @Operation(
        summary = "检查是否可删除",
        description = "检查分类是否可以被删除"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "检查完成")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "权限不足")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "分类不存在")
    public ResponseEntity<ApiResponse<Boolean>> canDeleteCategory(@PathVariable Long id) {
        boolean canDelete = categoryService.canDeleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success(canDelete));
    }

    @GetMapping("/{id}/item-count")
    @Operation(
        summary = "获取分类物品数量",
        description = "获取指定分类下的物品数量"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "分类不存在")
    public ResponseEntity<ApiResponse<Long>> getItemCountByCategory(@PathVariable Long id) {
        long count = categoryService.getItemCountByCategory(id);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/check-name")
    @Operation(summary = "检查分类名称", description = "检查分类名称是否已存在")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "检查完成")
    public ResponseEntity<ApiResponse<Boolean>> checkCategoryName(
            @Parameter(description = "分类名称") @RequestParam String name,
            @Parameter(description = "排除的分类ID") @RequestParam(required = false) Long excludeId) {

        boolean exists = excludeId != null ?
                categoryService.isCategoryNameExists(name, excludeId) :
                categoryService.isCategoryNameExists(name);

        return ResponseEntity.ok(ApiResponse.success(exists));
    }
}
