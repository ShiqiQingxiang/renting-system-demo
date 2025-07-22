package com.rental.permission.controller;

import com.rental.permission.DTO.RoleDTO;
import com.rental.permission.DTO.RolePermissionAssignRequest;
import com.rental.permission.service.RoleService;
import com.rental.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "角色管理相关接口")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @Operation(summary = "获取所有角色", description = "获取系统中所有角色列表")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<ApiResponse<List<RoleDTO>>> getAllRoles() {
        List<RoleDTO> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @GetMapping("/page")
    @Operation(summary = "分页获取角色", description = "分页获取角色列表")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<ApiResponse<Page<RoleDTO>>> getRoles(Pageable pageable) {
        Page<RoleDTO> roles = roleService.getRoles(pageable);
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取角色", description = "根据角色ID获取角色详情")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<ApiResponse<RoleDTO>> getRoleById(
            @Parameter(description = "角色ID") @PathVariable Long id) {
        RoleDTO role = roleService.getRoleById(id);
        return ResponseEntity.ok(ApiResponse.success(role));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户角色", description = "获取指定用户的所有角色")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<ApiResponse<List<RoleDTO>>> getUserRoles(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        List<RoleDTO> roles = roleService.getUserRoles(userId);
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @GetMapping("/system")
    @Operation(summary = "获取系统角色", description = "获取所有系统内置角色")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<ApiResponse<List<RoleDTO>>> getSystemRoles() {
        List<RoleDTO> roles = roleService.getSystemRoles();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @GetMapping("/non-system")
    @Operation(summary = "获取非系统角色", description = "获取所有自定义角色")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<ApiResponse<List<RoleDTO>>> getNonSystemRoles() {
        List<RoleDTO> roles = roleService.getNonSystemRoles();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @PostMapping
    @Operation(summary = "创建角色", description = "创建新的角色")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<ApiResponse<RoleDTO>> createRole(
            @Valid @RequestBody RoleDTO roleDTO) {
        RoleDTO createdRole = roleService.createRole(roleDTO);
        return ResponseEntity.ok(ApiResponse.success("角色创建成功", createdRole));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色", description = "更新指定ID的角色信息")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<ApiResponse<RoleDTO>> updateRole(
            @Parameter(description = "角色ID") @PathVariable Long id,
            @Valid @RequestBody RoleDTO roleDTO) {
        RoleDTO updatedRole = roleService.updateRole(id, roleDTO);
        return ResponseEntity.ok(ApiResponse.success("角色更新成功", updatedRole));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色", description = "删除指定ID的角色")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<ApiResponse<String>> deleteRole(
            @Parameter(description = "角色ID") @PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success(null, "角色删除成功"));
    }

    @PostMapping("/assign-permissions")
    @Operation(summary = "为角色分配权限", description = "为指定角色分配权限")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<ApiResponse<String>> assignPermissionsToRole(
            @Valid @RequestBody RolePermissionAssignRequest request) {
        roleService.assignPermissionsToRole(request);
        return ResponseEntity.ok(ApiResponse.success(null, "权限分配成功"));
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @Operation(summary = "移除角色权限", description = "从角色中移除指定权限")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<ApiResponse<String>> removePermissionFromRole(
            @Parameter(description = "角色ID") @PathVariable Long roleId,
            @Parameter(description = "权限ID") @PathVariable Long permissionId) {
        roleService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.ok(ApiResponse.success(null, "权限移除成功"));
    }
}
