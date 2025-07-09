package com.rental.order.repository;

import com.rental.order.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 基础查询
    Optional<Order> findByOrderNo(String orderNo);

    boolean existsByOrderNo(String orderNo);

    // 用户订单查询
    List<Order> findByUserId(Long userId);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    // 状态查询
    List<Order> findByStatus(Order.OrderStatus status);

    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);

    Page<Order> findByUserIdAndStatus(Long userId, Order.OrderStatus status, Pageable pageable);

    // 日期范围查��
    List<Order> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

    List<Order> findByEndDateBetween(LocalDate startDate, LocalDate endDate);

    List<Order> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    // 金额范围查询
    List<Order> findByTotalAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

    // 复合条件查询
    @Query("SELECT o FROM Order o WHERE " +
           "(:userId IS NULL OR o.user.id = :userId) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:startDate IS NULL OR o.startDate >= :startDate) AND " +
           "(:endDate IS NULL OR o.endDate <= :endDate) AND " +
           "(:minAmount IS NULL OR o.totalAmount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR o.totalAmount <= :maxAmount)")
    Page<Order> findOrdersByConditions(
        @Param("userId") Long userId,
        @Param("status") Order.OrderStatus status,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("minAmount") BigDecimal minAmount,
        @Param("maxAmount") BigDecimal maxAmount,
        Pageable pageable
    );

    // 逾期订单查询
    @Query("SELECT o FROM Order o WHERE o.status = 'IN_USE' AND o.endDate < :currentDate")
    List<Order> findOverdueOrders(@Param("currentDate") LocalDate currentDate);

    // 即将到期订单查询
    @Query("SELECT o FROM Order o WHERE o.status = 'IN_USE' AND o.endDate BETWEEN :startDate AND :endDate")
    List<Order> findOrdersExpiringSoon(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 包含特定物品的订单
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE oi.item.id = :itemId")
    List<Order> findOrdersByItemId(@Param("itemId") Long itemId);

    // 统计查询
    long countByStatus(Order.OrderStatus status);

    long countByUserId(Long userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :date")
    long countNewOrdersAfter(@Param("date") LocalDateTime date);

    // 收入统计
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status IN ('PAID', 'IN_USE', 'RETURNED') AND o.createdAt BETWEEN :startTime AND :endTime")
    BigDecimal getTotalRevenueByDateRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status IN ('PAID', 'IN_USE', 'RETURNED')")
    BigDecimal getTotalRevenue();

    // 月度统计
    @Query("SELECT YEAR(o.createdAt), MONTH(o.createdAt), COUNT(o), SUM(o.totalAmount) " +
           "FROM Order o WHERE o.status IN ('PAID', 'IN_USE', 'RETURNED') " +
           "GROUP BY YEAR(o.createdAt), MONTH(o.createdAt) " +
           "ORDER BY YEAR(o.createdAt) DESC, MONTH(o.createdAt) DESC")
    List<Object[]> getMonthlyOrderStatistics();
}
