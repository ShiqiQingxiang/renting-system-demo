package com.rental.auth.repository;

import com.rental.auth.model.UserSession;
import com.rental.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {

    // 根据sessionId查询会话
    Optional<UserSession> findBySessionId(String sessionId);

    // 根据用户和活跃状态查询会话
    List<UserSession> findByUserAndActiveTrue(User user);

    // 根据用户ID查询会话 - 修复：使用正确的属性名
    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId")
    List<UserSession> findByUserId(@Param("userId") Long userId);

    // 查询活跃会话 - 修复：使用正确的属性名
    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.active = :active")
    List<UserSession> findByUserIdAndActive(@Param("userId") Long userId, @Param("active") Boolean active);

    // 根据IP地址查询会话
    List<UserSession> findByIpAddress(String ipAddress);

    // 查询过期会话
    @Query("SELECT s FROM UserSession s WHERE s.expiresAt < :now")
    List<UserSession> findExpiredSessions(@Param("now") LocalDateTime now);

    // 查询指定时间前未访问的会话
    @Query("SELECT s FROM UserSession s WHERE s.lastAccessTime < :cutoffTime")
    List<UserSession> findInactiveSessionsBefore(@Param("cutoffTime") LocalDateTime cutoffTime);

    // 删除非活跃会话
    @Modifying
    @Transactional
    @Query("DELETE FROM UserSession s WHERE s.active = false AND s.lastAccessTime < :cutoffDate")
    void deleteInactiveSessions(@Param("cutoffDate") LocalDateTime cutoffDate);

    // 删除过期会话
    @Modifying
    @Transactional
    @Query("DELETE FROM UserSession s WHERE s.expiresAt < :now")
    void deleteExpiredSessions(@Param("now") LocalDateTime now);

    // 查询用户的活跃会话数量
    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.user = :user AND s.active = true")
    long countActiveSessionsByUser(@Param("user") User user);

    // 终止用户的所有活跃会话
    @Modifying
    @Transactional
    @Query("UPDATE UserSession s SET s.active = false WHERE s.user = :user AND s.active = true")
    void deactivateAllUserSessions(@Param("user") User user);
}
