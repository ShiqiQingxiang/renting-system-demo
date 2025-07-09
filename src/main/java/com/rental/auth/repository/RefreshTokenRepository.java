package com.rental.auth.repository;

import com.rental.auth.model.RefreshToken;
import com.rental.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 根据token查找
    Optional<RefreshToken> findByToken(String token);

    // 根据用户查找未撤销的令牌
    List<RefreshToken> findByUserAndRevokedFalse(User user);

    // 根据用户ID查询刷新令牌 - 修复：使用正确的属性名
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId")
    List<RefreshToken> findByUserId(@Param("userId") Long userId);

    // 查询未撤销的令牌 - 修复：使用正确的属性名
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revoked = :revoked")
    List<RefreshToken> findByUserIdAndRevoked(@Param("userId") Long userId, @Param("revoked") Boolean revoked);

    // 查询过期的令牌
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiryDate < :now")
    List<RefreshToken> findExpiredTokens(@Param("now") Instant now);

    // 查询有效的令牌（未过期且未撤销）
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revoked = false AND rt.expiryDate > :now")
    List<RefreshToken> findValidTokensByUserId(@Param("userId") Long userId, @Param("now") Instant now);

    // 撤销用户的所有令牌
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId")
    int revokeAllUserTokens(@Param("userId") Long userId);

    // 撤销特定令牌
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.token = :token")
    int revokeToken(@Param("token") String token);

    // 清理过期令牌
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    int deleteExpiredTokens(@Param("now") Instant now);

    // 清理撤销的令牌
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true AND rt.createdAt < :cutoffDate")
    int deleteRevokedTokens(@Param("cutoffDate") LocalDateTime cutoffDate);
}
