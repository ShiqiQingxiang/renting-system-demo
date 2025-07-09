package com.rental.auth.service;

import com.rental.auth.model.UserSession;
import com.rental.auth.repository.UserSessionRepository;
import com.rental.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户会话管理服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserSessionService {

    private final UserSessionRepository userSessionRepository;

    /**
     * 创建用户会话
     */
    @Transactional
    public UserSession createSession(User user, String deviceInfo, String ipAddress) {
        UserSession session = new UserSession();
        session.setUser(user);
        session.setDeviceInfo(deviceInfo);
        session.setIpAddress(ipAddress);
        session.setActive(true);
        session.setLoginTime(LocalDateTime.now());
        session.setLastAccessTime(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusDays(30)); // 30天过期

        return userSessionRepository.save(session);
    }

    /**
     * 更新会话最后访问时间
     */
    @Async
    @Transactional
    public void updateLastAccessTime(String sessionId) {
        userSessionRepository.findBySessionId(sessionId)
            .ifPresent(session -> {
                session.setLastAccessTime(LocalDateTime.now());
                userSessionRepository.save(session);
            });
    }

    /**
     * 终止会话
     */
    @Transactional
    public void terminateSession(String sessionId) {
        userSessionRepository.findBySessionId(sessionId)
            .ifPresent(session -> {
                session.setActive(false);
                userSessionRepository.save(session);
            });
    }

    /**
     * 终止用户的所有会话
     */
    @Transactional
    public void terminateAllUserSessions(User user) {
        List<UserSession> sessions = userSessionRepository.findByUserAndActiveTrue(user);
        sessions.forEach(session -> {
            session.setActive(false);
            userSessionRepository.save(session);
        });
    }

    /**
     * 获取用户的活跃会话列表
     */
    public List<UserSession> getActiveUserSessions(User user) {
        return userSessionRepository.findByUserAndActiveTrue(user);
    }

    /**
     * 检查会话是否有效
     */
    public boolean isSessionValid(String sessionId) {
        Optional<UserSession> session = userSessionRepository.findBySessionId(sessionId);
        return session.map(s -> s.isActive() &&
                               s.getExpiresAt() != null &&
                               s.getExpiresAt().isAfter(LocalDateTime.now()))
                     .orElse(false);
    }

    /**
     * 清理过期会话（定时任务）
     */
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    @Transactional
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();

        // 清理过期的会话
        List<UserSession> expiredSessions = userSessionRepository.findExpiredSessions(now);
        expiredSessions.forEach(session -> {
            session.setActive(false);
            userSessionRepository.save(session);
        });

        // 删除旧的非活跃会话（保留30天）
        LocalDateTime cutoffDate = now.minusDays(30);
        userSessionRepository.deleteInactiveSessions(cutoffDate);

        if (!expiredSessions.isEmpty()) {
            log.info("清理了 {} 个过期会话", expiredSessions.size());
        }
    }
}
