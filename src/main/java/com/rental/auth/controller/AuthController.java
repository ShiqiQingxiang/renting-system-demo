package com.rental.auth.controller;

import com.rental.auth.DTO.LoginRequest;
import com.rental.auth.DTO.RegisterRequest;
import com.rental.auth.DTO.RefreshTokenRequest;
import com.rental.auth.DTO.LogoutRequest;
import com.rental.auth.service.AuthService;
import com.rental.auth.service.UserRegistrationService;
import com.rental.common.response.ApiResponse;
import com.rental.security.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器 - 基于SpringDoc OpenAPI 3.0
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "认证管理", description = "用户认证相关接口，包括登录、注册、令牌管理等功能")
public class AuthController {

    private final AuthService authService;
    private final UserRegistrationService userRegistrationService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(
        summary = "用户登录",
        description = "用户通过用户名和密码登录系统，成功后返回JWT访问令牌和刷新令牌"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "登录成功",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ApiResponse.class),
            examples = @ExampleObject(
                name = "登录成功示例",
                value = """
                {
                    "code": 200,
                    "message": "登录成功",
                    "data": {
                        "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
                        "refreshToken": "refresh_token_here",
                        "tokenType": "Bearer",
                        "expiresIn": 86400,
                        "user": {
                            "id": 1,
                            "username": "admin",
                            "email": "admin@rental.com",
                            "roles": ["ADMIN"]
                        }
                    }
                }
                """
            )
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数错误")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "用户名或密码错误")
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
    @Operation(
        summary = "用户注册",
        description = "新用户注册账户，创建成功后可以使用用户名密码登录"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "注册成功",
        content = @Content(
            examples = @ExampleObject(
                name = "注册成功示例",
                value = """
                {
                    "code": 200,
                    "message": "注册成功",
                    "data": "注册成功"
                }
                """
            )
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数错误或用户已存在")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        userRegistrationService.register(request);
        return ResponseEntity.ok(ApiResponse.success("注册成功", "注册成功"));
    }

    /**
     * 刷新访问令牌
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "刷新令牌",
        description = "使用刷新令牌获取新的访问令牌，延长用户会话时间"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "令牌刷新成功",
        content = @Content(
            examples = @ExampleObject(
                name = "刷新成功示例",
                value = """
                {
                    "code": 200,
                    "message": "令牌刷新成功",
                    "data": {
                        "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
                        "tokenType": "Bearer",
                        "expiresIn": 86400
                    }
                }
                """
            )
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "刷新令牌无效或已过期")
    public ResponseEntity<ApiResponse<AuthService.TokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthService.TokenResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("令牌刷新成功", response));
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(
        summary = "用户登出",
        description = "用户退出登录，清除服务器端会话信息"
    )
    @SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登出成功")
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
    @Operation(
        summary = "验证令牌",
        description = "验证当前访问令牌是否有效，并返回用户信息"
    )
    @SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "令牌有效",
        content = @Content(
            examples = @ExampleObject(
                name = "验证成功示例",
                value = """
                {
                    "code": 200,
                    "message": "令牌有效",
                    "data": {
                        "id": 1,
                        "username": "admin",
                        "email": "admin@rental.com",
                        "roles": ["ADMIN"]
                    }
                }
                """
            )
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "令牌无效")
    public ResponseEntity<ApiResponse<AuthService.UserInfo>> validateToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            AuthService.UserInfo userInfo = AuthService.UserInfo.fromUser(userDetails.getUser());
            return ResponseEntity.ok(ApiResponse.success("令牌有效", userInfo));
        }

        return ResponseEntity.badRequest().body(ApiResponse.error("令牌无效"));
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    @Operation(
        summary = "获取当前用户信息",
        description = "获取当前登录用户的基本信息"
    )
    @SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录")
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
