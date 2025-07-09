package com.rental.finance.repository;

import com.rental.finance.model.FinanceRecord;
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
public interface FinanceRecordRepository extends JpaRepository<FinanceRecord, Long> {

    // 基础查询
    Optional<FinanceRecord> findByRecordNo(String recordNo);

    boolean existsByRecordNo(String recordNo);

    // 类型查询
    List<FinanceRecord> findByType(FinanceRecord.FinanceType type);

    Page<FinanceRecord> findByType(FinanceRecord.FinanceType type, Pageable pageable);

    // 分类查询
    List<FinanceRecord> findByCategory(String category);

    Page<FinanceRecord> findByCategory(String category, Pageable pageable);

    // 订单相关查询
    List<FinanceRecord> findByOrderId(Long orderId);

    // 支付相关查询
    List<FinanceRecord> findByPaymentId(Long paymentId);

    // 时间范围查询
    List<FinanceRecord> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    // 金额范围查询
    List<FinanceRecord> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

    // 复合条件查询
    @Query("SELECT fr FROM FinanceRecord fr WHERE " +
           "(:type IS NULL OR fr.type = :type) AND " +
           "(:category IS NULL OR fr.category = :category) AND " +
           "(:minAmount IS NULL OR fr.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR fr.amount <= :maxAmount) AND " +
           "(:startTime IS NULL OR fr.createdAt >= :startTime) AND " +
           "(:endTime IS NULL OR fr.createdAt <= :endTime)")
    Page<FinanceRecord> findRecordsByConditions(
        @Param("type") FinanceRecord.FinanceType type,
        @Param("category") String category,
        @Param("minAmount") BigDecimal minAmount,
        @Param("maxAmount") BigDecimal maxAmount,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        Pageable pageable
    );

    // 统计查询
    @Query("SELECT SUM(fr.amount) FROM FinanceRecord fr WHERE fr.type = :type")
    BigDecimal getTotalAmountByType(@Param("type") FinanceRecord.FinanceType type);

    @Query("SELECT SUM(fr.amount) FROM FinanceRecord fr WHERE fr.type = :type AND fr.createdAt BETWEEN :startTime AND :endTime")
    BigDecimal getTotalAmountByTypeAndDateRange(@Param("type") FinanceRecord.FinanceType type,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    @Query("SELECT SUM(fr.amount) FROM FinanceRecord fr WHERE fr.category = :category AND fr.createdAt BETWEEN :startTime AND :endTime")
    BigDecimal getTotalAmountByCategoryAndDateRange(@Param("category") String category,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    // 分类统计
    @Query("SELECT fr.category, fr.type, COUNT(fr), SUM(fr.amount) " +
           "FROM FinanceRecord fr WHERE fr.createdAt BETWEEN :startTime AND :endTime " +
           "GROUP BY fr.category, fr.type")
    List<Object[]> getCategoryStatistics(@Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    // 日统计
    @Query("SELECT DATE(fr.createdAt), fr.type, SUM(fr.amount) " +
           "FROM FinanceRecord fr WHERE fr.createdAt BETWEEN :startTime AND :endTime " +
           "GROUP BY DATE(fr.createdAt), fr.type " +
           "ORDER BY DATE(fr.createdAt)")
    List<Object[]> getDailyStatistics(@Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    // 月统计
    @Query("SELECT YEAR(fr.createdAt), MONTH(fr.createdAt), fr.type, SUM(fr.amount) " +
           "FROM FinanceRecord fr WHERE fr.createdAt BETWEEN :startTime AND :endTime " +
           "GROUP BY YEAR(fr.createdAt), MONTH(fr.createdAt), fr.type " +
           "ORDER BY YEAR(fr.createdAt), MONTH(fr.createdAt)")
    List<Object[]> getMonthlyStatistics(@Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);
}
