package com.rental.order.service.impl;

import com.rental.common.exception.BusinessException;
import com.rental.common.exception.ResourceNotFoundException;
import com.rental.item.model.Item;
import com.rental.item.repository.ItemRepository;
import com.rental.order.DTO.*;
import com.rental.order.model.Order;
import com.rental.order.model.OrderItem;
import com.rental.order.repository.OrderRepository;
import com.rental.order.repository.OrderItemRepository;
import com.rental.order.service.OrderService;
import com.rental.user.model.User;
import com.rental.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OrderDto createOrder(OrderCreateRequest request, Long userId) {
        log.info("创建订单，用户ID：{}", userId);

        // 验证用户
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 验证日期
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("结束日期不能早于开始日期");
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new BusinessException("开始日期不能早于今天");
        }

        // 验证订单项
        if (request.getOrderItems().isEmpty()) {
            throw new BusinessException("订单项不能为空");
        }

        // 检查物品可用性
        for (OrderCreateRequest.OrderItemCreateRequest itemRequest : request.getOrderItems()) {
            if (!isItemAvailableForRent(itemRequest.getItemId(), request.getStartDate(), request.getEndDate())) {
                Item item = itemRepository.findById(itemRequest.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("物品不存在"));
                throw new BusinessException("物品《" + item.getName() + "》在指定时间段不可用");
            }
        }

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUser(user);
        order.setStartDate(request.getStartDate());
        order.setEndDate(request.getEndDate());
        order.setRemark(request.getRemark());
        order.setStatus(Order.OrderStatus.PENDING);

        // 计算总金额和押金
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal depositAmount = BigDecimal.ZERO;
        long rentalDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        for (OrderCreateRequest.OrderItemCreateRequest itemRequest : request.getOrderItems()) {
            Item item = itemRepository.findById(itemRequest.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("物品不存在"));

            BigDecimal itemTotal = item.getPricePerDay()
                .multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
                .multiply(BigDecimal.valueOf(rentalDays));

            totalAmount = totalAmount.add(itemTotal);
            depositAmount = depositAmount.add(item.getDeposit().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        order.setTotalAmount(totalAmount);
        order.setDepositAmount(depositAmount);

        Order savedOrder = orderRepository.save(order);

        // 创建订单项
        for (OrderCreateRequest.OrderItemCreateRequest itemRequest : request.getOrderItems()) {
            Item item = itemRepository.findById(itemRequest.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("物品不存在"));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setItem(item);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPricePerDay(item.getPricePerDay());
            orderItem.setTotalAmount(item.getPricePerDay()
                .multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
                .multiply(BigDecimal.valueOf(rentalDays)));

            orderItemRepository.save(orderItem);
        }

        log.info("订单创建成功，订单号：{}", savedOrder.getOrderNo());
        return convertToDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto updateOrder(Long orderId, OrderUpdateRequest request, Long currentUserId) {
        log.info("更新订单，订单ID：{}, 用户ID：{}", orderId, currentUserId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));

        // 权限检查
        if (!canUserModifyOrder(orderId, currentUserId)) {
            throw new BusinessException("无权限修改此订单");
        }

        // 状态检查
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new BusinessException("只有待确认状态的订单才能修改");
        }

        // 更新字段
        if (request.getStartDate() != null) {
            if (request.getStartDate().isBefore(LocalDate.now())) {
                throw new BusinessException("开始日期不能早于今天");
            }
            order.setStartDate(request.getStartDate());
        }

        if (request.getEndDate() != null) {
            if (request.getEndDate().isBefore(order.getStartDate())) {
                throw new BusinessException("结束日期不能早于开始日期");
            }
            order.setEndDate(request.getEndDate());
        }

        if (request.getStatus() != null) {
            order.setStatus(request.getStatus());
        }

        if (request.getRemark() != null) {
            order.setRemark(request.getRemark());
        }

        Order savedOrder = orderRepository.save(order);
        log.info("订单更新成功，订单ID：{}", orderId);

        return convertToDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));
        return convertToDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderByOrderNo(String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo)
            .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));
        return convertToDto(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId, Long currentUserId) {
        log.info("删除订单，订单ID：{}, 用户ID：{}", orderId, currentUserId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));

        // 权限检查
        if (!canUserModifyOrder(orderId, currentUserId)) {
            throw new BusinessException("无权限删除此订单");
        }

        // 状态检查
        if (order.getStatus() != Order.OrderStatus.PENDING && order.getStatus() != Order.OrderStatus.CANCELLED) {
            throw new BusinessException("只有待确认或已取消状态的订单才能删除");
        }

        orderRepository.delete(order);
        log.info("订单删除成功，订单ID：{}", orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> searchOrders(OrderSearchRequest request, Pageable pageable) {
        Page<Order> orders = orderRepository.findBySearchCriteria(
            request.getOrderNo(),
            request.getUserId(),
            request.getUsername(),
            request.getStatus(),
            request.getStartDateFrom(),
            request.getStartDateTo(),
            request.getEndDateFrom(),
            request.getEndDateTo(),
            request.getMinAmount(),
            request.getMaxAmount(),
            request.getItemId(),
            request.getItemName(),
            request.getCategoryId(),
            pageable
        );

        return orders.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getUserOrders(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return orders.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getMyOrders(Long currentUserId, Pageable pageable) {
        return getUserOrders(currentUserId, pageable);
    }

    @Override
    @Transactional
    public OrderDto confirmOrder(Long orderId, Long currentUserId) {
        log.info("确认订单，订单ID：{}, 用户ID：{}", orderId, currentUserId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new BusinessException("只有待确认状态的订单才能确认");
        }

        // 检查物品是否仍然可用
        for (OrderItem orderItem : order.getOrderItems()) {
            if (!isItemAvailableForRent(orderItem.getItem().getId(), order.getStartDate(), order.getEndDate())) {
                throw new BusinessException("物品《" + orderItem.getItem().getName() + "》已不可用");
            }
        }

        order.setStatus(Order.OrderStatus.CONFIRMED);
        Order savedOrder = orderRepository.save(order);

        log.info("订单确认成功，订单号：{}", order.getOrderNo());
        return convertToDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto cancelOrder(Long orderId, String reason, Long currentUserId) {
        log.info("取消订单，订单ID：{}, 用户ID：{}, 原因：{}", orderId, currentUserId, reason);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));

        if (!canUserCancelOrder(orderId, currentUserId)) {
            throw new BusinessException("无权限取消此订单");
        }

        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new BusinessException("订单已经取消");
        }

        if (order.getStatus() == Order.OrderStatus.IN_USE || order.getStatus() == Order.OrderStatus.RETURNED) {
            throw new BusinessException("订单已在使用中或已归还，无法取消");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        if (reason != null && !reason.trim().isEmpty()) {
            order.setRemark(order.getRemark() != null ? order.getRemark() + "\n取消原因：" + reason : "取消原因：" + reason);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("订单取消成功，订单号：{}", order.getOrderNo());

        return convertToDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto auditOrder(Long orderId, OrderAuditRequest request, Long auditorId) {
        log.info("审核订单，订单ID：{}, 审核员ID：{}, 审核结果：{}", orderId, auditorId, request.getApproved());

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));

        if (order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new BusinessException("只有已确认状态的订单才能审核");
        }

        if (request.getApproved()) {
            order.setStatus(Order.OrderStatus.PAID); // 审核通过后设为已支付状态
        } else {
            order.setStatus(Order.OrderStatus.CANCELLED);
        }

        if (request.getComment() != null && !request.getComment().trim().isEmpty()) {
            order.setRemark(order.getRemark() != null ?
                order.getRemark() + "\n审核意见：" + request.getComment() :
                "审核意见：" + request.getComment());
        }

        Order savedOrder = orderRepository.save(order);
        log.info("订单审核完成，订单号：{}, 状态：{}", order.getOrderNo(), order.getStatus());

        return convertToDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto startUsingOrder(Long orderId, Long currentUserId) {
        log.info("开始使用订单，订单ID：{}, 用户ID：{}", orderId, currentUserId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));

        if (order.getStatus() != Order.OrderStatus.PAID) {
            throw new BusinessException("只有已支付状态的订单才能开始使用");
        }

        // 检查订单开始日期
        LocalDate today = LocalDate.now();
        if (order.getStartDate().isAfter(today)) {
            throw new BusinessException("订单尚未到开始使用日期");
        }

        order.setStatus(Order.OrderStatus.IN_USE);
        Order savedOrder = orderRepository.save(order);

        log.info("订单开始使用成功，订单号：{}", order.getOrderNo());
        return convertToDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto returnOrder(Long orderId, OrderReturnRequest request, Long currentUserId) {
        log.info("处理订单归还，订单ID：{}, 用户ID：{}", orderId, currentUserId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));

        // 修改状态检查逻辑，允许已支付和使用中的订单归还
        if (order.getStatus() != Order.OrderStatus.IN_USE && order.getStatus() != Order.OrderStatus.PAID) {
            throw new BusinessException("只有已支付或使用中的订单才能归还");
        }

        order.setStatus(Order.OrderStatus.RETURNED);
        order.setActualReturnDate(request.getReturnDate());

        if (request.getReturnRemark() != null && !request.getReturnRemark().trim().isEmpty()) {
            order.setRemark(order.getRemark() != null ?
                order.getRemark() + "\n归还备注：" + request.getReturnRemark() :
                "归还备注：" + request.getReturnRemark());
        }

        // 如果有损坏情况，记录损坏信息
        if (request.getHasDamage() && request.getDamageDescription() != null) {
            order.setRemark(order.getRemark() + "\n损坏情况：" + request.getDamageDescription());
        }

        Order savedOrder = orderRepository.save(order);
        log.info("订单归还处理完成，订单号：{}", order.getOrderNo());

        return convertToDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getPendingAuditOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findByStatusOrderByCreatedAtDesc(Order.OrderStatus.CONFIRMED, pageable);
        return orders.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersExpiringToday() {
        LocalDate today = LocalDate.now();
        List<Order> orders = orderRepository.findOrdersExpiringOn(today);
        return orders.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getOverdueOrders() {
        LocalDate today = LocalDate.now();
        List<Order> orders = orderRepository.findOverdueOrders(today);
        return orders.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isItemAvailableForRent(Long itemId, LocalDate startDate, LocalDate endDate) {
        // 检查物品状态
        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null || item.getStatus() != Item.ItemStatus.AVAILABLE) {
            return false;
        }

        // 检查是否有冲突的订单
        return !orderRepository.existsConflictingOrder(itemId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public long countOrdersByStatus(Order.OrderStatus status) {
        if (status == null) {
            return orderRepository.count(); // 返回总数
        }
        return orderRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUserOrders(Long userId) {
        return orderRepository.countByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateUserOrderTotal(Long userId, LocalDate startDate, LocalDate endDate) {
        return orderRepository.sumUserOrderAmountInDateRange(userId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserModifyOrder(Long orderId, Long userId) {
        if (userId == null) {
            return false;
        }

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }

        // 检查是否是订单创建者
        if (order.getUser().getId().equals(userId)) {
            return true;
        }

        // 检查是否有管理员权限
        return hasAdminPermission(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserCancelOrder(Long orderId, Long userId) {
        if (userId == null) {
            return false;
        }

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }

        // 检查订单状态
        if (order.getStatus() == Order.OrderStatus.IN_USE || order.getStatus() == Order.OrderStatus.RETURNED) {
            return false;
        }

        // 检查是否是订单创建者或管理员
        return order.getUser().getId().equals(userId) || hasAdminPermission(userId);
    }

    @Override
    public String generateOrderNo() {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = orderRepository.count() + 1;
        return "ORD" + dateStr + String.format("%06d", count);
    }

    /**
     * 检查用户是否有管理员权限
     */
    private boolean hasAdminPermission(Long userId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getAuthorities() != null) {
                return authentication.getAuthorities().stream()
                    .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN") ||
                        authority.getAuthority().equals("ORDER_AUDIT") ||
                        authority.getAuthority().equals("ORDER_UPDATE"));
            }
            return false;
        } catch (Exception e) {
            log.warn("检查管理员权限时出错，用户ID: {}, 错误: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * 转换为DTO
     */
    private OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setDepositAmount(order.getDepositAmount());
        dto.setStatus(order.getStatus());
        dto.setStartDate(order.getStartDate());
        dto.setEndDate(order.getEndDate());
        dto.setActualReturnDate(order.getActualReturnDate());
        dto.setRemark(order.getRemark());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        // 计算租赁天数
        long rentalDays = ChronoUnit.DAYS.between(order.getStartDate(), order.getEndDate()) + 1;
        dto.setRentalDays((int) rentalDays);

        // 设置用户信息
        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getId());
            dto.setUsername(order.getUser().getUsername());
            // 如果有用户资料，设置真实姓名
            if (order.getUser().getProfile() != null) {
                dto.setUserRealName(order.getUser().getProfile().getRealName());
            }
        }

        // 设置订单项信息
        if (order.getOrderItems() != null) {
            List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
                .map(this::convertOrderItemToDto)
                .collect(Collectors.toList());
            dto.setOrderItems(orderItemDtos);
        }

        // 设置支付状态
        dto.setIsPaid(order.getStatus() == Order.OrderStatus.PAID ||
                     order.getStatus() == Order.OrderStatus.IN_USE ||
                     order.getStatus() == Order.OrderStatus.RETURNED);

        // 设置合同状态
        dto.setHasContract(order.getContract() != null);
        if (order.getContract() != null) {
            dto.setContractNo(order.getContract().getContractNo());
        }

        return dto;
    }

    /**
     * 转换订单项为DTO
     */
    private OrderItemDto convertOrderItemToDto(OrderItem orderItem) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(orderItem.getId());
        dto.setOrderId(orderItem.getOrder().getId());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPricePerDay(orderItem.getPricePerDay());
        dto.setTotalAmount(orderItem.getTotalAmount());
        dto.setCreatedAt(orderItem.getCreatedAt());

        // 设置物品信息
        if (orderItem.getItem() != null) {
            Item item = orderItem.getItem();
            dto.setItemId(item.getId());
            dto.setItemName(item.getName());
            dto.setItemDescription(item.getDescription());
            dto.setItemLocation(item.getLocation());
            dto.setItemImages(item.getImages());
            dto.setItemStatus(item.getStatus().name());

            // 设置物品所有者信息
            if (item.getOwner() != null) {
                dto.setOwnerId(item.getOwner().getId());
                dto.setOwnerUsername(item.getOwner().getUsername());
            }

            // 设置分类信息
            if (item.getCategory() != null) {
                dto.setCategoryId(item.getCategory().getId());
                dto.setCategoryName(item.getCategory().getName());
            }
        }

        return dto;
    }
}
