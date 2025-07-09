package com.rental.auth.filter;

import com.rental.auth.util.JwtUtil;
import com.rental.security.userdetails.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 对于公开端点，跳过JWT验证
        if (isPublicEndpoint(requestURI)) {
            log.debug("跳过JWT验证的公开端点: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        // JWT Token在"Bearer token"的形式。去掉Bearer单词获取token
        if (StringUtils.hasText(requestTokenHeader) && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                log.error("无法获取JWT Token", e);
            } catch (ExpiredJwtException e) {
                log.error("JWT Token已过期", e);
            }
        } else {
            log.debug("JWT Token不以Bearer开头");
        }

        // 验证token
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 如果token有效配置Spring Security手动设置认证
            if (jwtUtil.validateToken(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 设置当前用户的安全上下文
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 检查是否为公开端点
     */
    private boolean isPublicEndpoint(String requestURI) {
        return requestURI.equals("/api/auth/register") ||
               requestURI.equals("/api/auth/login") ||
               requestURI.equals("/api/auth/refresh") ||
               requestURI.equals("/auth/register") ||
               requestURI.equals("/auth/login") ||
               requestURI.equals("/auth/refresh") ||
               requestURI.startsWith("/api/items/public/") ||
               requestURI.startsWith("/api/categories/public/") ||
               requestURI.startsWith("/items/public/") ||
               requestURI.startsWith("/categories/public/") ||
               requestURI.startsWith("/swagger-ui/") ||
               requestURI.startsWith("/v3/api-docs/") ||
               requestURI.startsWith("/api-docs/") ||
               requestURI.startsWith("/h2-console/") ||
               requestURI.equals("/error");
    }
}
