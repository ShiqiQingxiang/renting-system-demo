package com.rental.payment.repository;

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

    Page<Payment> findByOrderId(Long orderId, Pageable pageable);

    // 状态查询
    List<Payment> findByStatus(Payment.PaymentStatus status);

    Page<Payment> findByStatus(Payment.PaymentStatus status, Pageable pageable);

    // 支付方式查询
    List<Payment> findByPaymentMethod(Payment.PaymentMethod paymentMethod);

    Page<Payment> findByPaymentMethod(Payment.PaymentMethod paymentMethod, Pageable pageable);

    // 支付类型查询
    List<Payment> findByPaymentType(Payment.PaymentType paymentType);

    Page<Payment> findByPaymentType(Payment.PaymentType paymentType, Pageable pageable);

    // 复合条件查询
    @Query("SELECT p FROM Payment p WHERE " +
           "(:orderId IS NULL OR p.order.id = :orderId) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:paymentMethod IS NULL OR p.paymentMethod = :paymentMethod) AND " +
           "(:paymentType IS NULL OR p.paymentType = :paymentType) AND " +
           "(:minAmount IS NULL OR p.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR p.amount <= :maxAmount)")
    Page<Payment> findPaymentsByConditions(
        @Param("orderId") Long orderId,
        @Param("status") Payment.PaymentStatus status,
        @Param("paymentMethod") Payment.PaymentMethod paymentMethod,
        @Param("paymentType") Payment.PaymentType paymentType,
        @Param("minAmount") BigDecimal minAmount,
        @Param("maxAmount") BigDecimal maxAmount,
        Pageable pageable
    );

    // 时间范围查询
    List<Payment> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    // 待处理支付查询
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.createdAt < :timeout")
    List<Payment> findTimeoutPendingPayments(@Param("timeout") LocalDateTime timeout);

    // 统计查询
    long countByStatus(Payment.PaymentStatus status);

    long countByPaymentMethod(Payment.PaymentMethod paymentMethod);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.createdAt >= :date")
    long countNewPaymentsAfter(@Param("date") LocalDateTime date);

    // 金额统计
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCESS' AND p.paymentType = :paymentType")
    BigDecimal getTotalAmountByType(@Param("paymentType") Payment.PaymentType paymentType);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCESS' AND p.createdAt BETWEEN :startTime AND :endTime")
    BigDecimal getTotalSuccessAmountByDateRange(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    // 支付方式统计
    @Query("SELECT p.paymentMethod, COUNT(p), SUM(p.amount) " +
           "FROM Payment p WHERE p.status = 'SUCCESS' " +
           "GROUP BY p.paymentMethod")
    List<Object[]> getPaymentMethodStatistics();

    // 每日支付统计
    @Query("SELECT DATE(p.createdAt), COUNT(p), SUM(p.amount) " +
           "FROM Payment p WHERE p.status = 'SUCCESS' AND p.createdAt BETWEEN :startTime AND :endTime " +
           "GROUP BY DATE(p.createdAt) ORDER BY DATE(p.createdAt)")
    List<Object[]> getDailyPaymentStatistics(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);
}
