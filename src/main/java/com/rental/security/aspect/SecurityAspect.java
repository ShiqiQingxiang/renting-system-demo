package com.rental.security.aspect;

import com.rental.security.service.SecurityService;
import com.rental.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * 权限验证切面
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityAspect {

    private final SecurityService securityService;

    /**
     * 检查用户是否有权限访问自己的数据
     */
    @Before("@annotation(com.rental.security.annotation.RequireOwnershipOrAdmin) && args(userId,..)")
    public void checkOwnership(JoinPoint joinPoint, Long userId) {
        log.debug("检查用户权限: 方法={}, 目标用户ID={}", joinPoint.getSignature().getName(), userId);

        if (!securityService.canAccessUserData(userId)) {
            log.warn("用户权限不足: 当前用户ID={}, 目标用户ID={}",
                    securityService.getCurrentUserId(), userId);
            throw new AccessDeniedException("没有权限访问该用户的数据");
        }
    }

    /**
     * 检查管理员权限
     */
    @Before("execution(* com.rental..*Controller.*(..)) && @target(org.springframework.web.bind.annotation.RestController)")
    public void checkAdminAccess(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // 检查是否是管理员专用的方法
        if (methodName.startsWith("admin") || className.contains("Admin")) {
            if (!SecurityUtils.isAdmin()) {
                log.warn("非管理员尝试访问管理员功能: 用户ID={}, 方法={}",
                        SecurityUtils.getCurrentUserId(), methodName);
                throw new AccessDeniedException("需要管理员权限");
            }
        }
    }
}
