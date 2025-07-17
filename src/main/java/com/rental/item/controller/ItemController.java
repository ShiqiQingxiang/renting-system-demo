package com.rental.item.controller;

import com.rental.common.response.ApiResponse;
import com.rental.item.DTO.*;
import com.rental.item.model.Item;
import com.rental.item.service.ItemService;
import com.rental.security.userdetails.CustomUserDetails;
import com.rental.user.DTO.UserDTO;
import com.rental.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Tag(name = "物品管理", description = "物品管理相关接口，包括物品的增删改查、审核、状态管理等功能")
@SecurityRequirement(name = "bearerAuth")
public class ItemController {

    private final ItemService itemService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('ITEM_CREATE')")
    @Operation(
        summary = "创建物品",
        description = "用户创建新的物品信息，需要物品创建权限"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "201",
        description = "物品创建成功",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "创建物品成功示例",
                value = """
                {
                    "success": true,
                    "message": "物品创建成功",
                    "data": {
                        "id": 1,
                        "name": "电钻",
                        "description": "博世电钻，功率强劲",
                        "categoryId": 3,
                        "pricePerDay": 50.00,
                        "deposit": 200.00,
                        "status": "AVAILABLE",
                        "location": "北京市朝阳区"
                    }
                }
                """
            )
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数错误")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "权限不足")
    public ResponseEntity<ApiResponse<ItemDto>> createItem(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "物品创建请求信息",
                content = @Content(
                    schema = @Schema(implementation = ItemCreateRequest.class),
                    examples = @ExampleObject(
                        name = "创建物品请求示例",
                        value = """
                        {
                            "name": "电钻",
                            "description": "博世电钻，功率强劲，适合家装使用",
                            "categoryId": 3,
                            "pricePerDay": 50.00,
                            "deposit": 200.00,
                            "location": "北京市朝阳区",
                            "images": ["image1.jpg", "image2.jpg"],
                            "specifications": {
                                "brand": "博世",
                                "model": "GSB120",
                                "power": "650W"
                            }
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody ItemCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ItemDto result = itemService.createItem(request, userDetails.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("物品创建成功", result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ITEM_UPDATE')")
    @Operation(summary = "更新物品", description = "更新物品信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "物品更新成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数错误")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "权限不足")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "物品不存在")
    public ResponseEntity<ApiResponse<ItemDto>> updateItem(
            @Parameter(description = "物品ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody ItemUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ItemDto result = itemService.updateItem(id, request, userDetails.getUserId());

        return ResponseEntity.ok(ApiResponse.success("物品更新成功", result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取物品详情", description = "根据ID获取物品详细信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "物品不存在")
    public ResponseEntity<ApiResponse<ItemDto>> getItem(
            @Parameter(description = "物品ID", example = "1") @PathVariable Long id) {
        ItemDto result = itemService.getItemById(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ITEM_DELETE')")
    @Operation(summary = "删除物品", description = "删除指定的物品")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "删除成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "权限不足")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "物品不存在")
    public ResponseEntity<ApiResponse<String>> deleteItem(
            @Parameter(description = "物品ID", example = "1") @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        itemService.deleteItem(id, userDetails.getUserId());

        return ResponseEntity.ok(ApiResponse.success("物品删除成功"));
    }

    @GetMapping
    @Operation(summary = "搜索物品", description = "根据条件搜索物品")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "搜索成功")
    public ResponseEntity<ApiResponse<Page<ItemDto>>> searchItems(
            @Parameter(description = "物品名称") @RequestParam(required = false) String name,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "物品状态") @RequestParam(required = false) Item.ItemStatus status,
            @Parameter(description = "审核状态") @RequestParam(required = false) Item.ApprovalStatus approvalStatus,
            @Parameter(description = "所有者ID") @RequestParam(required = false) Long ownerId,
            @Parameter(description = "最低价格") @RequestParam(required = false) java.math.BigDecimal minPrice,
            @Parameter(description = "最高价格") @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @Parameter(description = "位置") @RequestParam(required = false) String location,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        ItemSearchRequest request = new ItemSearchRequest();
        request.setName(name);
        request.setCategoryId(categoryId);
        request.setStatus(status);
        request.setApprovalStatus(approvalStatus);
        request.setOwnerId(ownerId);
        request.setMinPrice(minPrice);
        request.setMaxPrice(maxPrice);
        request.setLocation(location);

        Page<ItemDto> result = itemService.searchItems(request, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/available")
    @Operation(summary = "获取可租赁物品", description = "获取所有可租赁的物品列表")
    public ResponseEntity<ApiResponse<Page<ItemDto>>> getAvailableItems(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ItemDto> result = itemService.getAvailableItems(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/my-items")
    @PreAuthorize("hasAuthority('ITEM_VIEW')")
    @Operation(summary = "获取我的物品", description = "获取当前用户的所有物品")
    public ResponseEntity<ApiResponse<List<ItemDto>>> getMyItems(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<ItemDto> result = itemService.getItemsByOwner(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasAuthority('ITEM_VIEW')")
    @Operation(summary = "获取指定用户的物品", description = "获取指定用户的所有物品")
    public ResponseEntity<ApiResponse<List<ItemDto>>> getItemsByOwner(
            @PathVariable Long ownerId) {

        List<ItemDto> result = itemService.getItemsByOwner(ownerId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "获取分类下的物品", description = "获取指定分类下的所有物品")
    public ResponseEntity<ApiResponse<List<ItemDto>>> getItemsByCategory(
            @PathVariable Long categoryId) {

        List<ItemDto> result = itemService.getItemsByCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ITEM_AUDIT')")
    @Operation(summary = "审核物品", description = "审核物品通过或拒绝")
    public ResponseEntity<ApiResponse<ItemDto>> approveItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemApprovalRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ItemDto result = itemService.approveItem(id, request, userDetails.getUserId());

        return ResponseEntity.ok(ApiResponse.success("物品审核完成", result));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ITEM_STATUS_MANAGE')")
    @Operation(summary = "更新物品状态", description = "更新物品的状态")
    public ResponseEntity<ApiResponse<ItemDto>> updateItemStatus(
            @PathVariable Long id,
            @Parameter(description = "新状态") @RequestParam Item.ItemStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ItemDto result = itemService.updateItemStatus(id, status, userDetails.getUserId());

        return ResponseEntity.ok(ApiResponse.success("物品状态更新成功", result));
    }

    @GetMapping("/pending-approval")
    @PreAuthorize("hasAuthority('ITEM_AUDIT')")
    @Operation(summary = "获取待审核物品", description = "获取所有待审核的物品列表")
    public ResponseEntity<ApiResponse<Page<ItemDto>>> getPendingApprovalItems(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ItemDto> result = itemService.getPendingApprovalItems(pageable);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('ITEM_VIEW')")
    @Operation(summary = "获取物品统计信息", description = "获取物品的各种统计数据")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "权限不足")
    public ResponseEntity<ApiResponse<ItemStatistics>> getItemStatistics() {
        ItemStatistics statistics = new ItemStatistics(
            itemService.countByStatus(null),
            itemService.countByStatus(Item.ItemStatus.AVAILABLE),
            itemService.countByStatus(Item.ItemStatus.RENTED),
            itemService.countByApprovalStatus(Item.ApprovalStatus.PENDING),
            itemService.countByApprovalStatus(Item.ApprovalStatus.APPROVED),
            itemService.countByApprovalStatus(Item.ApprovalStatus.REJECTED)
        );

        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    // 内部统计类
    public static class ItemStatistics {
        public final long totalItems;
        public final long availableItems;
        public final long rentedItems;
        public final long pendingApproval;
        public final long approvedItems;
        public final long rejectedItems;

        public ItemStatistics(long totalItems, long availableItems, long rentedItems,
                            long pendingApproval, long approvedItems, long rejectedItems) {
            this.totalItems = totalItems;
            this.availableItems = availableItems;
            this.rentedItems = rentedItems;
            this.pendingApproval = pendingApproval;
            this.approvedItems = approvedItems;
            this.rejectedItems = rejectedItems;
        }
    }
}
