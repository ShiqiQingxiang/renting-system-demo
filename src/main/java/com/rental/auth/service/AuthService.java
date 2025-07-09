package com.rental.auth.service;

import com.rental.auth.config.JwtProperties;
import com.rental.auth.model.RefreshToken;
import com.rental.auth.model.UserSession;
import com.rental.auth.repository.RefreshTokenRepository;
import com.rental.auth.repository.UserSessionRepository;
import com.rental.auth.util.JwtTokenUtil;
import com.rental.common.exception.BusinessException;
import com.rental.security.userdetails.CustomUserDetailsService;
import com.rental.user.model.User;
import com.rental.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserSessionRepository userSessionRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;

    /**
     * 用户登录
     */
    @Transactional
    public LoginResponse login(String username, String password, String deviceInfo, String ipAddress) {
        try {
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

            // 检查用户状态
            if (user.getStatus() != User.UserStatus.ACTIVE) {
                throw new BusinessException("用户账号已被禁用");
            }

            // 生成令牌
            String accessToken = jwtTokenUtil.generateAccessToken(userDetails);
            String refreshToken = jwtTokenUtil.generateRefreshToken(username);

            // 保存刷新令牌
            saveRefreshToken(user, refreshToken);

            // 创建用户会话
            createUserSession(user, deviceInfo, ipAddress);

            log.info("用户 {} 登录成功", username);

            return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getExpiration() / 1000) // 转换为秒
                .user(UserInfo.fromUser(user))
                .build();

        } catch (Exception e) {
            log.error("用户 {} 登录失败: {}", username, e.getMessage());
            throw new BusinessException("登录失败: " + e.getMessage());
        }
    }

    /**
     * 刷新令牌
     */
    @Transactional
    public TokenResponse refreshToken(String refreshToken) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(() -> new BusinessException("刷新令牌无效"));

        if (storedToken.isRevoked()) {
            throw new BusinessException("刷新令牌已被撤销");
        }

        if (storedToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new BusinessException("刷新令牌已过期");
        }

        User user = storedToken.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        String newAccessToken = jwtTokenUtil.generateAccessToken(userDetails);
        String newRefreshToken = jwtTokenUtil.generateRefreshToken(user.getUsername());

        // 撤销旧的刷新令牌
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        // 保存新的刷新令牌
        saveRefreshToken(user, newRefreshToken);

        return TokenResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtProperties.getExpiration() / 1000)
            .build();
    }

    /**
     * 用户登出
     */
    @Transactional
    public void logout(String username, String sessionId) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException("用户不存在"));

        // 撤销所有刷新令牌
        refreshTokenRepository.findByUserAndRevokedFalse(user)
            .forEach(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });

        // 终止用户会话
        if (sessionId != null) {
            userSessionRepository.findBySessionId(sessionId)
                .ifPresent(session -> {
                    session.setActive(false);
                    userSessionRepository.save(session);
                });
        }

        log.info("用户 {} 登出成功", username);
    }

    /**
     * 验证令牌
     */
    public boolean validateToken(String token) {
        return jwtTokenUtil.validateToken(token);
    }

    /**
     * 保存刷新令牌
     */
    private void saveRefreshToken(User user, String token) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(jwtProperties.getRefreshExpiration()));
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * 创建用户会话
     */
    private void createUserSession(User user, String deviceInfo, String ipAddress) {
        UserSession session = new UserSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setUser(user);
        session.setDeviceInfo(deviceInfo);
        session.setIpAddress(ipAddress);
        session.setActive(true);
        session.setLoginTime(LocalDateTime.now());
        session.setLastAccessTime(LocalDateTime.now());
        // 设置会话过期时间（比如30天）
        session.setExpiresAt(LocalDateTime.now().plusDays(30));
        userSessionRepository.save(session);
    }

    /**
     * 登录响应数据类
     */
    @lombok.Data
    @lombok.Builder
    public static class LoginResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private Long expiresIn;
        private UserInfo user;
    }

    /**
     * 令牌响应数据类
     */
    @lombok.Data
    @lombok.Builder
    public static class TokenResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private Long expiresIn;
    }

    /**
     * 用户信息数据类
     */
    @lombok.Data
    @lombok.Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String phone;
        private User.UserStatus status;
        private LocalDateTime createdAt;

        public static UserInfo fromUser(User user) {
            return UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
        }
    }
}
