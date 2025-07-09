package com.rental.payment.repository;

import com.rental.payment.model.PaymentRecord;
import com.rental.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {

    // 根据支付ID查询记录
    List<PaymentRecord> findByPaymentId(Long paymentId);

    // 按状态查询
    List<PaymentRecord> findByStatus(Payment.PaymentStatus status);

    // 查询指定支付的最新记录
    @Query("SELECT pr FROM PaymentRecord pr WHERE pr.payment.id = :paymentId ORDER BY pr.createdAt DESC")
    List<PaymentRecord> findLatestByPaymentId(@Param("paymentId") Long paymentId);

    // 查询失败的支付记录
    @Query("SELECT pr FROM PaymentRecord pr WHERE pr.status = 'FAILED' AND pr.errorMessage IS NOT NULL")
    List<PaymentRecord> findFailedRecords();

    // 时间范围查询
    List<PaymentRecord> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    // 清理旧记录
    @Query("SELECT pr FROM PaymentRecord pr WHERE pr.createdAt < :cutoffDate")
    List<PaymentRecord> findOldRecords(@Param("cutoffDate") LocalDateTime cutoffDate);
}
