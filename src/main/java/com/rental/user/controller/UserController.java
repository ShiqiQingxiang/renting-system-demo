package com.rental.user.controller;

import com.rental.user.service.UserService;
import com.rental.user.DTO.*;
import com.rental.user.model.User;
import com.rental.common.response.ApiResponse;
import com.rental.common.response.PageResponse;
import com.rental.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户管理", description = "用户管理相关接口，包括用户的增删改查、角色分配、状态管理等功能")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(
        summary = "创建用户",
        description = "创建新用户，需要管理员权限。系统会自动对密码进行BCrypt加密处理。"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "用户创建成功",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "创建用户成功示例",
                value = """
                {
                    "code": 200,
                    "message": "用户创建成功",
                    "data": {
                        "id": 4,
                        "username": "testuser",
                        "email": "test@example.com",
                        "phone": "13800138000",
                        "status": "ACTIVE",
                        "roleNames": ["CUSTOMER"],
                        "createdAt": "2025-07-14T10:00:00"
                    }
                }
                """
            )
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数错误或用户已存在")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "权限不足")
    @RequirePermission("USER_CREATE")
    public ApiResponse<UserDTO> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "用户创建请求",
                content = @Content(
                    schema = @Schema(implementation = UserCreateRequest.class),
                    examples = @ExampleObject(
                        name = "创建用户请求示例",
                        value = """
                        {
                            "username": "testuser",
                            "password": "123456",
                            "email": "test@example.com",
                            "phone": "13800138000",
                            "roleIds": [6]
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody UserCreateRequest request) {
        log.info("Creating user with username: {}", request.getUsername());
        UserDTO user = userService.createUser(request);
        return ApiResponse.success("用户创建成功", user);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "更新用户信息", description = "更新指定用户的基本信息，如邮箱、手机号、状态等")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "用户更新成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "用户不存在")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "权限不足")
    @RequirePermission("USER_UPDATE")
    public ApiResponse<UserDTO> updateUser(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("Updating user with ID: {}", userId);
        UserDTO user = userService.updateUser(userId, request);
        return ApiResponse.success("用户更新成功", user);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户的详细信息，包括基本信息、角色、资料等")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "获取成功",
        content = @Content(
            examples = @ExampleObject(
                name = "获取用户详情成功示例",
                value = """
                {
                    "code": 200,
                    "message": "操作成功",
                    "data": {
                        "id": 1,
                        "username": "admin",
                        "email": "admin@rental.com",
                        "phone": "13800138000",
                        "status": "ACTIVE",
                        "roleNames": ["ADMIN"],
                        "profile": {
                            "realName": "系统管理员",
                            "address": "北京市朝阳区"
                        },
                        "createdAt": "2025-07-14T10:00:00"
                    }
                }
                """
            )
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "用户不存在")
    @RequirePermission("USER_VIEW")
    public ApiResponse<UserDTO> getUserById(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long userId) {
        UserDTO user = userService.getUserById(userId);
        return ApiResponse.success(user);
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "根据用户名获取用户", description = "根据用户名获取用户信息，常用于用户查询和验证")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "用户不存在")
    @RequirePermission("USER_VIEW")
    public ApiResponse<UserDTO> getUserByUsername(
            @Parameter(description = "用户名", example = "admin") @PathVariable String username) {
        UserDTO user = userService.getUserByUsername(username);
        return ApiResponse.success(user);
    }

    @GetMapping
    @Operation(
        summary = "分页查询用户列表",
        description = "根据条件分页查询用户列表，支持关键词搜索、状态筛选、角色筛选等多种筛选条件"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "查询成功",
        content = @Content(
            examples = @ExampleObject(
                name = "分页查询用户成功示例",
                value = """
                {
                    "code": 200,
                    "message": "操作成功",
                    "data": {
                        "content": [
                            {
                                "id": 1,
                                "username": "admin",
                                "email": "admin@rental.com",
                                "status": "ACTIVE",
                                "roleNames": ["ADMIN"]
                            }
                        ],
                        "currentPage": 0,
                        "pageSize": 10,
                        "totalElements": 3,
                        "totalPages": 1,
                        "hasNext": false,
                        "hasPrevious": false
                    }
                }
                """
            )
        )
    )
    @RequirePermission("USER_VIEW")
    public ApiResponse<PageResponse<UserDTO>> queryUsers(
            @Parameter(description = "搜索关键词（用户名或邮箱）", example = "admin")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "用户状态", example = "ACTIVE")
            @RequestParam(required = false) User.UserStatus status,
            @Parameter(description = "角色名称", example = "ADMIN")
            @RequestParam(required = false) String roleName,
            @Parameter(description = "页码（从0开始）", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向（asc/desc）", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {

        UserQueryRequest request = new UserQueryRequest();
        request.setKeyword(keyword);
        request.setStatus(status);
        request.setRoleName(roleName);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setSortDir(sortDir);

        PageResponse<UserDTO> result = userService.queryUsers(request);
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "删除用户", description = "根据用户ID删除用户，此操作不可逆，请谨慎操作")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "删除成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "用户不存在")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "权限不足")
    @RequirePermission("USER_DELETE")
    public ApiResponse<String> deleteUser(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long userId) {
        log.info("Deleting user with ID: {}", userId);
        userService.deleteUser(userId);
        return ApiResponse.success("用户删除成功", null);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除用户", description = "批量删除多个用户，此操作不可逆，请谨慎操作")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "批量删除成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数错误")
    @RequirePermission("USER_DELETE")
    public ApiResponse<String> deleteUsers(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "要删除的用户ID列表",
                content = @Content(
                    examples = @ExampleObject(value = "[1, 2, 3]")
                )
            )
            @RequestBody List<Long> userIds) {
        log.info("Batch deleting users with IDs: {}", userIds);
        userService.deleteUsers(userIds);
        return ApiResponse.success("用户批量删除成功", null);
    }

    @PutMapping("/{userId}/status")
    @Operation(summary = "更新用户状态", description = "更新用户状态：启用(ACTIVE)、禁用(INACTIVE)、锁定(LOCKED)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "状态更新成功")
    @RequirePermission("USER_UPDATE")
    public ApiResponse<String> updateUserStatus(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long userId,
            @Parameter(description = "用户状态", example = "ACTIVE") @RequestParam User.UserStatus status) {
        log.info("Updating user status for ID: {} to {}", userId, status);
        userService.updateUserStatus(userId, status);
        return ApiResponse.success("用户状态更新成功", null);
    }

    @PutMapping("/batch/status")
    @Operation(summary = "批量更新用户状态", description = "批量更新多个用户的状态")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "批量更新成功")
    @RequirePermission("USER_UPDATE")
    public ApiResponse<String> batchUpdateUserStatus(@Valid @RequestBody UserBatchOperationRequest request) {
        log.info("Batch updating user status for IDs: {} to {}",
                request.getUserIds(), request.getStatus());
        userService.batchUpdateUserStatus(request);
        return ApiResponse.success("用户状态批量更新成功", null);
    }

    @PutMapping("/{userId}/roles")
    @Operation(summary = "为用户分配角色", description = "为指定用户分配一个或多个角色")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "角色分配成功")
    @RequirePermission("USER_UPDATE")
    public ApiResponse<String> assignRoles(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "角色ID数组",
                content = @Content(examples = @ExampleObject(value = "[1, 2]"))
            )
            @RequestBody Long[] roleIds) {
        log.info("Assigning roles to user ID: {}", userId);
        userService.assignRoles(userId, roleIds);
        return ApiResponse.success("角色分配成功", null);
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    @Operation(summary = "移除用户角色", description = "移除用户的指定角色")
    @RequirePermission("USER_UPDATE")
    public ApiResponse<String> removeRoleFromUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "角色ID") @PathVariable Long roleId) {
        log.info("Removing role {} from user {}", roleId, userId);
        userService.removeRoleFromUser(userId, roleId);
        return ApiResponse.success("角色移除成功", null);
    }

    @DeleteMapping("/{userId}/roles")
    @Operation(summary = "清空用户角色", description = "移除用户的所有角色")
    @RequirePermission("USER_UPDATE")
    public ApiResponse<String> clearUserRoles(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        log.info("Clearing all roles for user {}", userId);
        userService.clearUserRoles(userId);
        return ApiResponse.success("用户角色清空成功", null);
    }

    @GetMapping("/{userId}/roles")
    @Operation(summary = "获取用户角色", description = "获取指定用户的所有角色")
    @RequirePermission("USER_VIEW")
    public ApiResponse<Set<String>> getUserRoles(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        Set<String> roles = userService.getUserRoleNames(userId);
        return ApiResponse.success(roles);
    }

    @GetMapping("/check/username/{username}")
    @Operation(
        summary = "检查用户名可用性",
        description = "检查指定用户名是否已被使用，用于注册时的实时验证，此接口无需认证"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "检查完成",
        content = @Content(
            examples = {
                @ExampleObject(
                    name = "用户名可用",
                    value = """
                    {
                        "code": 200,
                        "message": "用户名可用",
                        "data": true
                    }
                    """
                ),
                @ExampleObject(
                    name = "用户名已存在",
                    value = """
                    {
                        "code": 200,
                        "message": "用户名已存在",
                        "data": false
                    }
                    """
                )
            }
        )
    )
    public ApiResponse<Boolean> checkUsername(
            @Parameter(description = "用户名", example = "newuser") @PathVariable String username) {
        boolean exists = userService.existsByUsername(username);
        return ApiResponse.success(exists ? "用户名已存在" : "用户名可用", !exists);
    }

    @GetMapping("/check/email/{email}")
    @Operation(summary = "检查邮箱可用性", description = "检查指定邮箱是否已被注册，此接口无需认证")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "检查完成")
    public ApiResponse<Boolean> checkEmail(
            @Parameter(description = "邮箱地址", example = "test@example.com") @PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        return ApiResponse.success(exists ? "邮箱已存在" : "邮箱可用", !exists);
    }

    @GetMapping("/check/phone/{phone}")
    @Operation(summary = "检查手机号可用性", description = "检查指定手机号是否已被注册，此接口无需认证")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "检查完成")
    public ApiResponse<Boolean> checkPhone(
            @Parameter(description = "手机号", example = "13800138000") @PathVariable String phone) {
        boolean exists = userService.existsByPhone(phone);
        return ApiResponse.success(exists ? "手机号已存在" : "手机号可用", !exists);
    }

    @PutMapping("/{userId}/password/reset")
    @Operation(summary = "重置用户密码", description = "管理员重置指定用户的密码，新密码会自动进行BCrypt加密")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "密码重置成功")
    @RequirePermission("USER_UPDATE")
    public ApiResponse<String> resetPassword(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long userId,
            @Parameter(description = "新密码", example = "newpassword123") @RequestParam String newPassword) {
        log.info("Resetting password for user ID: {}", userId);
        userService.resetPassword(userId, newPassword);
        return ApiResponse.success("密码重置成功", null);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取用户统计信息", description = "获取用户相关的统计数据，包括总用户数、各状态用户数等")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "获取成功",
        content = @Content(
            examples = @ExampleObject(
                name = "用户统计信息示例",
                value = """
                {
                    "code": 200,
                    "message": "操作成功",
                    "data": {
                        "totalUsers": 100,
                        "activeUsers": 85,
                        "usersByStatus": {
                            "ACTIVE": 85,
                            "INACTIVE": 10,
                            "LOCKED": 5
                        }
                    }
                }
                """
            )
        )
    )
    @RequirePermission("USER_VIEW")
    public ApiResponse<Map<String, Object>> getUserStatistics() {
        Map<String, Object> statistics = userService.getUserStatistics();
        return ApiResponse.success(statistics);
    }
}
