package com.rental.notification.repository;

import com.rental.notification.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 用户通知查询
    List<Notification> findByUserId(Long userId);

    Page<Notification> findByUserId(Long userId, Pageable pageable);

    // 按类型查询
    List<Notification> findByType(Notification.NotificationType type);

    Page<Notification> findByType(Notification.NotificationType type, Pageable pageable);

    // 按状态查询
    List<Notification> findByStatus(Notification.NotificationStatus status);

    Page<Notification> findByStatus(Notification.NotificationStatus status, Pageable pageable);

    // 用户未读通知
    List<Notification> findByUserIdAndStatus(Long userId, Notification.NotificationStatus status);

    Page<Notification> findByUserIdAndStatus(Long userId, Notification.NotificationStatus status, Pageable pageable);

    // 用户指定类型的通知
    List<Notification> findByUserIdAndType(Long userId, Notification.NotificationType type);

    // 复合条件查询
    @Query("SELECT n FROM Notification n WHERE " +
           "(:userId IS NULL OR n.user.id = :userId) AND " +
           "(:type IS NULL OR n.type = :type) AND " +
           "(:status IS NULL OR n.status = :status)")
    Page<Notification> findNotificationsByConditions(
        @Param("userId") Long userId,
        @Param("type") Notification.NotificationType type,
        @Param("status") Notification.NotificationStatus status,
        Pageable pageable
    );

    // 时间范围查询
    List<Notification> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    List<Notification> findBySentAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    // 查询待发送通知
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING'")
    List<Notification> findPendingNotifications();

    // 查询发送失败的通知
    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED'")
    List<Notification> findFailedNotifications();

    // 批量标记为已读
    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = :readTime WHERE n.user.id = :userId AND n.status = 'SENT'")
    int markAllAsReadByUserId(@Param("userId") Long userId, @Param("readTime") LocalDateTime readTime);

    // 标记单个通知为已读
    @Modifying
    @Query("UPDATE Notification n SET n.status = 'read', n.readAt = :readTime WHERE n.id = :notificationId")
    int markAsRead(@Param("notificationId") Long notificationId, @Param("readTime") LocalDateTime readTime);

    // 清理旧通知
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    int deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    // 统计查询
    long countByUserIdAndStatus(Long userId, Notification.NotificationStatus status);

    long countByStatus(Notification.NotificationStatus status);

    long countByType(Notification.NotificationType type);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.createdAt >= :date")
    long countNewNotificationsAfter(@Param("date") LocalDateTime date);

    // 统计用户未读通知数量
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.status = 'SENT'")
    long countUnreadByUserId(@Param("userId") Long userId);
}
