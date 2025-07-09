package com.rental.auth.controller;

import com.rental.auth.DTO.LoginRequest;
import com.rental.auth.DTO.RegisterRequest;
import com.rental.auth.DTO.RefreshTokenRequest;
import com.rental.auth.DTO.LogoutRequest;
import com.rental.auth.service.AuthService;
import com.rental.auth.service.UserRegistrationService;
import com.rental.common.response.ApiResponse;
import com.rental.security.userdetails.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserRegistrationService userRegistrationService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthService.LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String deviceInfo = getDeviceInfo(httpRequest);
        String ipAddress = getClientIpAddress(httpRequest);

        AuthService.LoginResponse response = authService.login(
            request.getUsername(),
            request.getPassword(),
            deviceInfo,
            ipAddress
        );

        return ResponseEntity.ok(ApiResponse.success("登录成功", response));
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        userRegistrationService.register(request);
        return ResponseEntity.ok(ApiResponse.success("注册成功", "注册成功"));
    }

    /**
     * 刷新访问令牌
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthService.TokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthService.TokenResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("令牌刷新成功", response));
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestBody(required = false) LogoutRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            String username = userDetails.getUsername();
            String sessionId = request != null ? request.getSessionId() : null;

            authService.logout(username, sessionId);
            return ResponseEntity.ok(ApiResponse.success("登出成功", "登出成功"));
        }

        return ResponseEntity.ok(ApiResponse.success("登出成功", "登出成功"));
    }

    /**
     * 验证令牌
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<AuthService.UserInfo>> validateToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            AuthService.UserInfo userInfo = AuthService.UserInfo.fromUser(userDetails.getUser());
            return ResponseEntity.ok(ApiResponse.success("令牌有效", userInfo));
        }

        return ResponseEntity.status(401).body(ApiResponse.error("令牌无效"));
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthService.UserInfo>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            AuthService.UserInfo userInfo = AuthService.UserInfo.fromUser(userDetails.getUser());
            return ResponseEntity.ok(ApiResponse.success(userInfo));
        }

        return ResponseEntity.status(401).body(ApiResponse.error("未登录"));
    }

    /**
     * 获取设备信息
     */
    private String getDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "Unknown Device";
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
