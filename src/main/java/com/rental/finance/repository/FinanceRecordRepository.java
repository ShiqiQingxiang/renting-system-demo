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

    /**
     * 根据记录编号查找财务记录
     */
    Optional<FinanceRecord> findByRecordNo(String recordNo);

    /**
     * 检查记录编号是否存在
     */
    boolean existsByRecordNo(String recordNo);

    /**
     * 根据财务类型查找记录
     */
    Page<FinanceRecord> findByType(FinanceRecord.FinanceType type, Pageable pageable);

    /**
     * 根据财务分类查找记录（模糊匹配）
     */
    Page<FinanceRecord> findByCategoryContaining(String category, Pageable pageable);

    /**
     * 根据财务分类查找记录（精确匹配）
     */
    Page<FinanceRecord> findByCategory(String category, Pageable pageable);

    /**
     * 根据类型和分类查找记录
     */
    Page<FinanceRecord> findByTypeAndCategoryContaining(FinanceRecord.FinanceType type, String category, Pageable pageable);

    /**
     * 根据类型和时间范围查找记录
     */
    Page<FinanceRecord> findByTypeAndCreatedAtBetween(FinanceRecord.FinanceType type,
                                                     LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 根据分类和时间范围查找记录
     */
    Page<FinanceRecord> findByCategoryContainingAndCreatedAtBetween(String category,
                                                                   LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 根据类型、分类和时间范围查找记录
     */
    Page<FinanceRecord> findByTypeAndCategoryContainingAndCreatedAtBetween(FinanceRecord.FinanceType type, String category,
                                                                          LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 根据订单ID查找财务记录
     */
    List<FinanceRecord> findByOrderId(Long orderId);

    /**
     * 根据支付ID查找财务记录
     */
    List<FinanceRecord> findByPaymentId(Long paymentId);

    /**
     * 根据时间范围查找财务记录
     */
    List<FinanceRecord> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据时间范围分页查找财务记录
     */
    Page<FinanceRecord> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 计算指定时间范围内的总收入
     */
    @Query("SELECT COALESCE(SUM(fr.amount), 0) FROM FinanceRecord fr WHERE fr.type = 'INCOME' AND fr.createdAt BETWEEN :startTime AND :endTime")
    BigDecimal calculateTotalIncomeByPeriod(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 计算指定时间范围内的总支出
     */
    @Query("SELECT COALESCE(SUM(fr.amount), 0) FROM FinanceRecord fr WHERE fr.type = 'EXPENSE' AND fr.createdAt BETWEEN :startTime AND :endTime")
    BigDecimal calculateTotalExpenseByPeriod(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 计算指定时间范围内的总退款
     */
    @Query("SELECT COALESCE(SUM(fr.amount), 0) FROM FinanceRecord fr WHERE fr.type = 'REFUND' AND fr.createdAt BETWEEN :startTime AND :endTime")
    BigDecimal calculateTotalRefundByPeriod(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 按分类统计金额
     */
    @Query("SELECT fr.category, SUM(fr.amount) FROM FinanceRecord fr WHERE fr.type = :type AND fr.createdAt BETWEEN :startTime AND :endTime GROUP BY fr.category")
    List<Object[]> calculateAmountByCategory(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime,
                                           @Param("type") FinanceRecord.FinanceType type);
}
