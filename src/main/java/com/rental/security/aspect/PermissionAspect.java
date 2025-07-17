package com.rental.security.aspect;

import com.rental.security.annotation.RequirePermission;
import com.rental.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 权限验证切面
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionAspect {

    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("用户未登录");
        }

        String requiredPermission = requirePermission.value();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        boolean hasPermission = authorities.stream()
            .anyMatch(authority -> authority.getAuthority().equals(requiredPermission));

        if (!hasPermission) {
            log.warn("用户 {} 尝试访问需要权限 {} 的资源，但权限不足",
                    authentication.getName(), requiredPermission);
            throw new BusinessException("权限不足，需要权限: " + requiredPermission);
        }

        log.debug("用户 {} 成功通过权限验证: {}", authentication.getName(), requiredPermission);
    }
}
