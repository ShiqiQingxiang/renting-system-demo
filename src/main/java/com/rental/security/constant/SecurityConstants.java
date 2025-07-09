package com.rental.security.constant;

/**
 * 权限常量类
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // 常量类，不允许实例化
    }

    // JWT相关常量
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String TOKEN_TYPE = "JWT";
    public static final long ACCESS_TOKEN_EXPIRATION_TIME = 24 * 60 * 60 * 1000L; // 24小时
    public static final long REFRESH_TOKEN_EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000L; // 7天

    // 角色常量
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_OWNER = "OWNER";
    public static final String ROLE_RENTER = "RENTER";
    public static final String ROLE_FINANCE = "FINANCE";
    public static final String ROLE_MANAGER = "MANAGER";

    // 权限表达式
    public static final String HAS_ROLE_ADMIN = "hasRole('" + ROLE_ADMIN + "')";
    public static final String HAS_ROLE_OWNER = "hasRole('" + ROLE_OWNER + "')";
    public static final String HAS_ROLE_RENTER = "hasRole('" + ROLE_RENTER + "')";
    public static final String HAS_ROLE_FINANCE = "hasRole('" + ROLE_FINANCE + "')";
    public static final String HAS_ROLE_MANAGER = "hasRole('" + ROLE_MANAGER + "')";

    // 复合权限表达式
    public static final String IS_ADMIN_OR_OWNER = HAS_ROLE_ADMIN + " or " + HAS_ROLE_OWNER;
    public static final String IS_ADMIN_OR_RENTER = HAS_ROLE_ADMIN + " or " + HAS_ROLE_RENTER;
    public static final String IS_ADMIN_OR_FINANCE = HAS_ROLE_ADMIN + " or " + HAS_ROLE_FINANCE;
    public static final String IS_ADMIN_OR_MANAGER = HAS_ROLE_ADMIN + " or " + HAS_ROLE_MANAGER;

    // 资源所有权检查表达式
    public static final String IS_OWNER_OR_ADMIN = HAS_ROLE_ADMIN + " or @securityService.isOwnerOrAdmin(#resourceId)";
    public static final String CAN_ACCESS_USER_DATA = HAS_ROLE_ADMIN + " or @securityService.canAccessUserData(#userId)";

    // 公开访问路径
    public static final String[] PUBLIC_URLS = {
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/items/public/**",
            "/api/categories/public/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/static/**",
            "/css/**",
            "/js/**",
            "/images/**"
    };
}
