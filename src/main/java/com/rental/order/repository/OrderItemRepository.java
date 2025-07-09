package com.rental.order.repository;

import com.rental.order.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // 根据订单ID查询订单项
    List<OrderItem> findByOrderId(Long orderId);

    // 根据物品ID查询订单项
    List<OrderItem> findByItemId(Long itemId);

    // 查询指定订单中的特定物品
    List<OrderItem> findByOrderIdAndItemId(Long orderId, Long itemId);

    // 统计物品被租用次数
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.item.id = :itemId")
    long countByItemId(@Param("itemId") Long itemId);

    // 计算物品总收入
    @Query("SELECT SUM(oi.totalAmount) FROM OrderItem oi WHERE oi.item.id = :itemId")
    BigDecimal getTotalRevenueByItemId(@Param("itemId") Long itemId);

    // 查询热门物品（按租用次数）
    @Query("SELECT oi.item.id, oi.item.name, COUNT(oi) as rentCount " +
           "FROM OrderItem oi GROUP BY oi.item.id, oi.item.name " +
           "ORDER BY COUNT(oi) DESC")
    List<Object[]> findPopularItems();

    // 查询指定时间范围内的订单项
    @Query("SELECT oi FROM OrderItem oi WHERE oi.createdAt BETWEEN :startTime AND :endTime")
    List<OrderItem> findByDateRange(@Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    // 按数量查询
    List<OrderItem> findByQuantityGreaterThan(Integer quantity);

    // 按价格范围查询
    List<OrderItem> findByPricePerDayBetween(BigDecimal minPrice, BigDecimal maxPrice);
}
