package com.rental.payment.repository;

import com.rental.order.model.Order;
import com.rental.payment.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 基础查询
    Optional<Payment> findByPaymentNo(String paymentNo);

    boolean existsByPaymentNo(String paymentNo);

    Optional<Payment> findByThirdPartyTransactionId(String thirdPartyTransactionId);

    // 订单支付查询
    List<Payment> findByOrderId(Long orderId);

    List<Payment> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    Page<Payment> findByOrderId(Long orderId, Pageable pageable);

    List<Payment> findByOrderAndStatus(Order order, Payment.PaymentStatus status);

    // 状态查询
    List<Payment> findByStatus(Payment.PaymentStatus status);

    Page<Payment> findByStatus(Payment.PaymentStatus status, Pageable pageable);

    long countByStatus(Payment.PaymentStatus status);

    // 支付方式查询
    List<Payment> findByPaymentMethod(Payment.PaymentMethod paymentMethod);

    Page<Payment> findByPaymentMethod(Payment.PaymentMethod paymentMethod, Pageable pageable);

    // 支付类型查询
    List<Payment> findByPaymentType(Payment.PaymentType paymentType);

    Page<Payment> findByPaymentType(Payment.PaymentType paymentType, Pageable pageable);

    // 用户支付查询
    Page<Payment> findByOrderUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 时间范围查询
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    long countByStatusAndCreatedAtBetween(Payment.PaymentStatus status, LocalDateTime startDate, LocalDateTime endDate);

    long countByPaymentTypeAndCreatedAtBetween(Payment.PaymentType paymentType, LocalDateTime startDate, LocalDateTime endDate);

    // 金额统计
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status AND p.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByStatusAndCreatedAtBetween(@Param("status") Payment.PaymentStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentType = :paymentType AND p.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByPaymentTypeAndCreatedAtBetween(@Param("paymentType") Payment.PaymentType paymentType, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.order.user.id = :userId AND p.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserIdAndCreatedAtBetween(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 复合条件查询
    @Query("SELECT p FROM Payment p WHERE " +
           "(:paymentNo IS NULL OR p.paymentNo LIKE %:paymentNo%) AND " +
           "(:orderId IS NULL OR p.order.id = :orderId) AND " +
           "(:orderNo IS NULL OR p.order.orderNo LIKE %:orderNo%) AND " +
           "(:userId IS NULL OR p.order.user.id = :userId) AND " +
           "(:username IS NULL OR p.order.user.username LIKE %:username%) AND " +
           "(:paymentMethod IS NULL OR p.paymentMethod = :paymentMethod) AND " +
           "(:paymentType IS NULL OR p.paymentType = :paymentType) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:minAmount IS NULL OR p.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR p.amount <= :maxAmount) AND " +
           "(:createdDateFrom IS NULL OR DATE(p.createdAt) >= :createdDateFrom) AND " +
           "(:createdDateTo IS NULL OR DATE(p.createdAt) <= :createdDateTo) AND " +
           "(:thirdPartyTransactionId IS NULL OR p.thirdPartyTransactionId LIKE %:thirdPartyTransactionId%)")
    Page<Payment> findBySearchCriteria(
        @Param("paymentNo") String paymentNo,
        @Param("orderId") Long orderId,
        @Param("orderNo") String orderNo,
        @Param("userId") Long userId,
        @Param("username") String username,
        @Param("paymentMethod") Payment.PaymentMethod paymentMethod,
        @Param("paymentType") Payment.PaymentType paymentType,
        @Param("status") Payment.PaymentStatus status,
        @Param("minAmount") BigDecimal minAmount,
        @Param("maxAmount") BigDecimal maxAmount,
        @Param("createdDateFrom") String createdDateFrom,
        @Param("createdDateTo") String createdDateTo,
        @Param("thirdPartyTransactionId") String thirdPartyTransactionId,
        Pageable pageable
    );
}
