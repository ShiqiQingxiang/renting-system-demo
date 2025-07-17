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
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 根据订单号查找订单
     */
    Optional<Order> findByOrderNo(String orderNo);

    /**
     * 根据用户ID查找订单
     */
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 根据用户ID分页查找订单
     */
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 根据状态查找订单
     */
    List<Order> findByStatusOrderByCreatedAtDesc(Order.OrderStatus status);

    /**
     * 根据状态分页查找订单
     */
    Page<Order> findByStatusOrderByCreatedAtDesc(Order.OrderStatus status, Pageable pageable);

    /**
     * 根据用户ID和状态查找订单
     */
    List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Order.OrderStatus status);

    /**
     * 统计用户订单数量
     */
    long countByUserId(Long userId);

    /**
     * 统计状态订单数量
     */
    long countByStatus(Order.OrderStatus status);

    /**
     * 查找指定日期范围内的订单
     */
    @Query("SELECT o FROM Order o WHERE o.startDate >= :startDate AND o.endDate <= :endDate")
    List<Order> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 复杂条件搜索订单
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN o.user u " +
           "LEFT JOIN o.orderItems oi " +
           "LEFT JOIN oi.item i " +
           "LEFT JOIN i.category c " +
           "WHERE (:orderNo IS NULL OR o.orderNo LIKE %:orderNo%) " +
           "AND (:userId IS NULL OR o.user.id = :userId) " +
           "AND (:username IS NULL OR u.username LIKE %:username%) " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:startDateFrom IS NULL OR o.startDate >= :startDateFrom) " +
           "AND (:startDateTo IS NULL OR o.startDate <= :startDateTo) " +
           "AND (:endDateFrom IS NULL OR o.endDate >= :endDateFrom) " +
           "AND (:endDateTo IS NULL OR o.endDate <= :endDateTo) " +
           "AND (:minAmount IS NULL OR o.totalAmount >= :minAmount) " +
           "AND (:maxAmount IS NULL OR o.totalAmount <= :maxAmount) " +
           "AND (:itemId IS NULL OR i.id = :itemId) " +
           "AND (:itemName IS NULL OR i.name LIKE %:itemName%) " +
           "AND (:categoryId IS NULL OR c.id = :categoryId)")
    Page<Order> findBySearchCriteria(@Param("orderNo") String orderNo,
                                   @Param("userId") Long userId,
                                   @Param("username") String username,
                                   @Param("status") Order.OrderStatus status,
                                   @Param("startDateFrom") LocalDate startDateFrom,
                                   @Param("startDateTo") LocalDate startDateTo,
                                   @Param("endDateFrom") LocalDate endDateFrom,
                                   @Param("endDateTo") LocalDate endDateTo,
                                   @Param("minAmount") BigDecimal minAmount,
                                   @Param("maxAmount") BigDecimal maxAmount,
                                   @Param("itemId") Long itemId,
                                   @Param("itemName") String itemName,
                                   @Param("categoryId") Long categoryId,
                                   Pageable pageable);

    /**
     * 查找需要归还的订单（超期）
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'IN_USE' AND o.endDate < :currentDate")
    List<Order> findOverdueOrders(@Param("currentDate") LocalDate currentDate);

    /**
     * 查找即将到期的订单
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'IN_USE' AND o.endDate = :expiryDate")
    List<Order> findOrdersExpiringOn(@Param("expiryDate") LocalDate expiryDate);

    /**
     * 统计用户在指定时间范围内的订单总金额
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
           "WHERE o.user.id = :userId " +
           "AND o.status IN ('PAID', 'IN_USE', 'RETURNED') " +
           "AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    BigDecimal sumUserOrderAmountInDateRange(@Param("userId") Long userId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /**
     * 检查物品在指定时间段是否有冲突的订单
     */
    @Query("SELECT COUNT(o) > 0 FROM Order o " +
           "JOIN o.orderItems oi " +
           "WHERE oi.item.id = :itemId " +
           "AND o.status IN ('CONFIRMED', 'PAID', 'IN_USE') " +
           "AND ((:startDate >= o.startDate AND :startDate <= o.endDate) " +
           "OR (:endDate >= o.startDate AND :endDate <= o.endDate) " +
           "OR (:startDate <= o.startDate AND :endDate >= o.endDate))")
    boolean existsConflictingOrder(@Param("itemId") Long itemId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);
}
