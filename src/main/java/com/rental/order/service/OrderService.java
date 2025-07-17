package com.rental.order.service;

import com.rental.order.DTO.*;
import com.rental.order.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface OrderService {

    /**
     * 创建订单
     */
    OrderDto createOrder(OrderCreateRequest request, Long userId);

    /**
     * 更新订单
     */
    OrderDto updateOrder(Long orderId, OrderUpdateRequest request, Long currentUserId);

    /**
     * 根据ID获取订单
     */
    OrderDto getOrderById(Long orderId);

    /**
     * 根据订单号获取订单
     */
    OrderDto getOrderByOrderNo(String orderNo);

    /**
     * 删除订单
     */
    void deleteOrder(Long orderId, Long currentUserId);

    /**
     * 搜索订单
     */
    Page<OrderDto> searchOrders(OrderSearchRequest request, Pageable pageable);

    /**
     * 获取用户的订单
     */
    Page<OrderDto> getUserOrders(Long userId, Pageable pageable);

    /**
     * 获取我的订单
     */
    Page<OrderDto> getMyOrders(Long currentUserId, Pageable pageable);

    /**
     * 确认订单
     */
    OrderDto confirmOrder(Long orderId, Long currentUserId);

    /**
     * 取消订单
     */
    OrderDto cancelOrder(Long orderId, String reason, Long currentUserId);

    /**
     * 审核订单
     */
    OrderDto auditOrder(Long orderId, OrderAuditRequest request, Long auditorId);

    /**
     * 开始使用订单（将订单状态从已支付改为使用中）
     */
    OrderDto startUsingOrder(Long orderId, Long currentUserId);

    /**
     * 处理订单归还
     */
    OrderDto returnOrder(Long orderId, OrderReturnRequest request, Long currentUserId);

    /**
     * 获取待审核订单
     */
    Page<OrderDto> getPendingAuditOrders(Pageable pageable);

    /**
     * 获取即将到期的订单
     */
    List<OrderDto> getOrdersExpiringToday();

    /**
     * 获取超期订单
     */
    List<OrderDto> getOverdueOrders();

    /**
     * 检查物品在指定时间段是否可租赁
     */
    boolean isItemAvailableForRent(Long itemId, LocalDate startDate, LocalDate endDate);

    /**
     * 统计订单数量
     */
    long countOrdersByStatus(Order.OrderStatus status);

    /**
     * 统计用户订单数量
     */
    long countUserOrders(Long userId);

    /**
     * 计算用户在指定时间范围内的订单总金额
     */
    BigDecimal calculateUserOrderTotal(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 检查用户是否可以修改订单
     */
    boolean canUserModifyOrder(Long orderId, Long userId);

    /**
     * 检查用户是否可以取消订单
     */
    boolean canUserCancelOrder(Long orderId, Long userId);

    /**
     * 生成订单号
     */
    String generateOrderNo();
}
