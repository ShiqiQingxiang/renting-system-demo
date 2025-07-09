package com.rental.security.config;

import com.rental.auth.filter.JwtAuthenticationFilter;
import com.rental.auth.handler.JwtAccessDeniedHandler;
import com.rental.auth.handler.JwtAuthenticationEntryPoint;
import com.rental.user.model.User;
import com.rental.security.userdetails.CustomUserDetails;
import com.rental.security.userdetails.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Optional;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // 公开访问的端点 - 注册和登录必须在最前面，使用更宽松的匹配
                .requestMatchers(
                        "/api/auth/register",
                        "/api/auth/login",
                        "/api/auth/refresh"
                ).permitAll()
                // 其他公开端点
                .requestMatchers(
                        "/api/items/public/**",
                        "/api/categories/public/**",
                        "/items/public/**",
                        "/categories/public/**",
                        // Swagger相关
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api-docs/**",
                        "/v3/api-docs/**",
                        // 静态资源
                        "/static/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        // 错误页面
                        "/error",
                        // H2控制台
                        "/h2-console/**"
                ).permitAll()
                // 需要认证的端点
                .requestMatchers("/api/auth/logout", "/api/auth/validate", "/api/auth/me").authenticated()
                .requestMatchers("/auth/logout", "/auth/validate", "/auth/me").authenticated()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/finance/**").hasAnyRole("ADMIN", "FINANCE")
                .requestMatchers("/api/management/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/api/owner/**").hasAnyRole("ADMIN", "OWNER")
                .requestMatchers("/api/renter/**").hasAnyRole("ADMIN", "RENTER")
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            .headers(headers -> headers.frameOptions().disable()) // 允许H2控制台
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
                return Optional.empty();
            }

            if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
                return Optional.of(userDetails.getUserId());
            }

            return Optional.empty();
        };
    }
}