package com.rental.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置属性
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    /**
     * JWT密钥
     */
    private String secret = "rental-system-secret-key-2024-very-long-secret-for-security";

    /**
     * 访问令牌过期时间（毫秒）
     */
    private long expiration = 86400000; // 24小时

    /**
     * 刷新令牌过期时间（毫秒）
     */
    private long refreshExpiration = 604800000; // 7天

    /**
     * 令牌前缀
     */
    private String tokenPrefix = "Bearer ";

    /**
     * 令牌头名称
     */
    private String headerName = "Authorization";
}
