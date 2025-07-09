package com.rental.security.service;

import com.rental.security.userdetails.CustomUserDetails;
import com.rental.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("securityService")
@RequiredArgsConstructor
@Slf4j
public class SecurityService {

    /**
     * 获取当前登录用户
     */
    public CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
            authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails;
        }

        return null;
    }

    /**
     * 获取当前用户ID
     */
    public Long getCurrentUserId() {
        CustomUserDetails userDetails = getCurrentUser();
        return userDetails != null ? userDetails.getUserId() : null;
    }

    /**
     * 检查当前用户是否为指定角色
     */
    public boolean hasRole(String role) {
        CustomUserDetails userDetails = getCurrentUser();
        if (userDetails == null) {
            return false;
        }

        return userDetails.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    /**
     * 检查当前用户是否为管理员
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * 检查当前用户是否为资源所有者
     */
    public boolean isOwner(Long userId, Long resourceOwnerId) {
        if (userId == null || resourceOwnerId == null) {
            return false;
        }
        return userId.equals(resourceOwnerId);
    }

    /**
     * 检查当前用户是否为资源所有者或管理员
     */
    public boolean isOwnerOrAdmin(Long resourceOwnerId) {
        return isAdmin() || isOwner(getCurrentUserId(), resourceOwnerId);
    }

    /**
     * 检查当前用户是否可以访问指定用户的数据
     */
    public boolean canAccessUserData(Long targetUserId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }

        // 管理员可以访问所有用户数据
        if (isAdmin()) {
            return true;
        }

        // 用户只能访问自己的数据
        return currentUserId.equals(targetUserId);
    }
}
