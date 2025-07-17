package com.rental.permission.controller;

import com.rental.permission.DTO.PermissionDTO;
import com.rental.permission.model.Permission;
import com.rental.permission.service.PermissionService;
import com.rental.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Tag(name = "权限管理", description = "权限管理相关接口")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @Operation(summary = "获取所有权限", description = "获取系统中所有权限列表")
    @PreAuthorize("hasAuthority('PERMISSION_VIEW')")
    public ResponseEntity<ApiResponse<List<PermissionDTO>>> getAllPermissions() {
        List<PermissionDTO> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    @GetMapping("/tree")
    @Operation(summary = "获取权限树", description = "获取层级结构的权限树")
    @PreAuthorize("hasAuthority('PERMISSION_VIEW')")
    public ResponseEntity<ApiResponse<List<PermissionDTO>>> getPermissionTree() {
        List<PermissionDTO> permissionTree = permissionService.getPermissionTree();
        return ResponseEntity.ok(ApiResponse.success(permissionTree));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取权限", description = "根据权限ID获取权限详情")
    @PreAuthorize("hasAuthority('PERMISSION_VIEW')")
    public ResponseEntity<ApiResponse<PermissionDTO>> getPermissionById(
            @Parameter(description = "权限ID") @PathVariable Long id) {
        PermissionDTO permission = permissionService.getPermissionById(id);
        return ResponseEntity.ok(ApiResponse.success(permission));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "根据类型获取权限", description = "根据权限类型获取权限列表")
    @PreAuthorize("hasAuthority('PERMISSION_VIEW')")
    public ResponseEntity<ApiResponse<List<PermissionDTO>>> getPermissionsByType(
            @Parameter(description = "权限类型") @PathVariable Permission.PermissionType type) {
        List<PermissionDTO> permissions = permissionService.getPermissionsByType(type);
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户权限", description = "获取指定用户的所有权限")
    @PreAuthorize("hasAuthority('PERMISSION_VIEW')")
    public ResponseEntity<ApiResponse<List<PermissionDTO>>> getUserPermissions(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        List<PermissionDTO> permissions = permissionService.getUserPermissions(userId);
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    @PostMapping
    @Operation(summary = "创建权限", description = "创建新的权限")
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ResponseEntity<ApiResponse<PermissionDTO>> createPermission(
            @Valid @RequestBody PermissionDTO permissionDTO) {
        PermissionDTO createdPermission = permissionService.createPermission(permissionDTO);
        return ResponseEntity.ok(ApiResponse.success("权限创建成功", createdPermission));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新权限", description = "更新指定ID的权限信息")
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ResponseEntity<ApiResponse<PermissionDTO>> updatePermission(
            @Parameter(description = "权限ID") @PathVariable Long id,
            @Valid @RequestBody PermissionDTO permissionDTO) {
        PermissionDTO updatedPermission = permissionService.updatePermission(id, permissionDTO);
        return ResponseEntity.ok(ApiResponse.success("权限更新成功", updatedPermission));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除权限", description = "删除指定ID的权限")
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> deletePermission(
            @Parameter(description = "权限ID") @PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok(ApiResponse.success("权限删除成功", null));
    }
}
