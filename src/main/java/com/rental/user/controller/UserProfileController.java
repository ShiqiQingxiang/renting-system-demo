package com.rental.user.controller;

import com.rental.user.service.UserProfileService;
import com.rental.user.DTO.UserProfileDTO;
import com.rental.user.DTO.UserProfileUpdateRequest;
import com.rental.common.response.ApiResponse;
import com.rental.security.annotation.RequirePermission;
import com.rental.security.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户资料管理", description = "用户个人资料管理相关接口")
@SecurityRequirement(name = "bearerAuth")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/{userId}/profile")
    @Operation(summary = "获取用户资料", description = "根据用户ID获取用户资料信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    @PreAuthorize("hasAuthority('USER_VIEW') or #userId == authentication.principal.id")
    public ApiResponse<UserProfileDTO> getProfile(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        UserProfileDTO profile = userProfileService.getProfileByUserId(userId);
        return ApiResponse.success(profile);
    }

    @GetMapping("/profile/me")
    @Operation(summary = "获取当前用户资料", description = "获取当前登录用户的个人资料信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    public ApiResponse<UserProfileDTO> getMyProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = getCurrentUserId(auth);
        UserProfileDTO profile = userProfileService.getProfileByUserId(userId);
        return ApiResponse.success(profile);
    }

    @PutMapping("/{userId}/profile")
    @Operation(summary = "更新用户资料", description = "创建或更新指定用户的个人资料信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "更新成功")
    @PreAuthorize("hasAuthority('USER_UPDATE') or #userId == authentication.principal.id")
    public ApiResponse<UserProfileDTO> updateProfile(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        log.info("Updating profile for user ID: {}", userId);
        UserProfileDTO profile = userProfileService.createOrUpdateProfile(userId, request);
        return ApiResponse.success("用户资料更新成功", profile);
    }

    @PutMapping("/profile/me")
    @Operation(summary = "更新当前用户资料", description = "更新当前登录用户的个人资料信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "更新成功")
    public ApiResponse<UserProfileDTO> updateMyProfile(
            @Valid @RequestBody UserProfileUpdateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = getCurrentUserId(auth);
        log.info("Updating profile for current user ID: {}", userId);
        UserProfileDTO profile = userProfileService.createOrUpdateProfile(userId, request);
        return ApiResponse.success("用户资料更新成功", profile);
    }

    @DeleteMapping("/{userId}/profile")
    @Operation(summary = "删除用户资料", description = "删除指定用户的个人资料信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "删除成功")
    @RequirePermission("USER_DELETE")
    public ApiResponse<String> deleteProfile(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        log.info("Deleting profile for user ID: {}", userId);
        userProfileService.deleteProfile(userId);
        return ApiResponse.success("用户资料删除成功", null);
    }

    @GetMapping("/check/idcard/{idCard}")
    @Operation(summary = "检查身份证号", description = "检查指定身份证号是否已被使用")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "检查完成")
    @RequirePermission("USER_VIEW")
    public ApiResponse<Boolean> checkIdCard(
            @Parameter(description = "身份证号") @PathVariable String idCard) {
        boolean exists = userProfileService.existsByIdCard(idCard);
        return ApiResponse.success(exists ? "身份证号已存在" : "身份证号可用", !exists);
    }

    @PutMapping("/{userId}/avatar")
    @Operation(summary = "更新用户头像", description = "更新指定用户的头像URL")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "头像更新成功")
    @PreAuthorize("hasAuthority('USER_UPDATE') or #userId == authentication.principal.id")
    public ApiResponse<UserProfileDTO> updateAvatar(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "头像URL") @RequestParam String avatarUrl) {
        log.info("Updating avatar for user ID: {}", userId);
        UserProfileDTO profile = userProfileService.updateAvatar(userId, avatarUrl);
        return ApiResponse.success("头像更新成功", profile);
    }

    @PutMapping("/avatar/me")
    @Operation(summary = "更新当前用户头像", description = "更新当前登录用户的头像URL")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "头像更新成功")
    public ApiResponse<UserProfileDTO> updateMyAvatar(
            @Parameter(description = "头像URL") @RequestParam String avatarUrl) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = getCurrentUserId(auth);
        log.info("Updating avatar for current user ID: {}", userId);
        UserProfileDTO profile = userProfileService.updateAvatar(userId, avatarUrl);
        return ApiResponse.success("头像更新成功", profile);
    }

    /**
     * 从认证对象中获取当前用户ID
     */
    private Long getCurrentUserId(Authentication auth) {
        if (auth != null && auth.getPrincipal() != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof com.rental.security.userdetails.CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                return userDetails.getUserId();
            }
        }
        throw new RuntimeException("无法获取当前用户信息");
    }
}
