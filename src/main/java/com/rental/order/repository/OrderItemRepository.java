package com.rental.order.repository;

import com.rental.order.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * 根据订单ID查找订单项
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * 根据物品ID查找订单项
     */
    List<OrderItem> findByItemId(Long itemId);

    /**
     * 根据订单ID和物品ID查找订单项
     */
    List<OrderItem> findByOrderIdAndItemId(Long orderId, Long itemId);

    /**
     * 统计物品的租赁次数
     */
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.item.id = :itemId AND oi.order.status IN ('PAID', 'IN_USE', 'RETURNED')")
    long countRentalsByItemId(@Param("itemId") Long itemId);

    /**
     * 统计物品的总收入
     */
    @Query("SELECT COALESCE(SUM(oi.totalAmount), 0) FROM OrderItem oi WHERE oi.item.id = :itemId AND oi.order.status IN ('PAID', 'IN_USE', 'RETURNED')")
    BigDecimal sumRevenueByItemId(@Param("itemId") Long itemId);

    /**
     * 查找用户租赁过的物品
     */
    @Query("SELECT DISTINCT oi FROM OrderItem oi WHERE oi.order.user.id = :userId AND oi.order.status IN ('PAID', 'IN_USE', 'RETURNED')")
    List<OrderItem> findRentedItemsByUserId(@Param("userId") Long userId);

    /**
     * 统计指定时间范围内的订单项总金额
     */
    @Query("SELECT COALESCE(SUM(oi.totalAmount), 0) FROM OrderItem oi " +
           "WHERE oi.order.createdAt >= :startDate AND oi.order.createdAt <= :endDate " +
           "AND oi.order.status IN ('PAID', 'IN_USE', 'RETURNED')")
    BigDecimal sumTotalAmountInDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 删除订单的所有订单项
     */
    void deleteByOrderId(Long orderId);
}
