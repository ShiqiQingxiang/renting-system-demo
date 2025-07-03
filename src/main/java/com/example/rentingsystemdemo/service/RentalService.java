package com.example.rentingsystemdemo.service;

import com.example.rentingsystemdemo.DTO.ItemDTO;
import com.example.rentingsystemdemo.DTO.RentalOrderDTO;
import com.example.rentingsystemdemo.model.Item;
import com.example.rentingsystemdemo.model.RentalOrder;
import com.example.rentingsystemdemo.model.User;
import com.example.rentingsystemdemo.repository.ItemRepository;
import com.example.rentingsystemdemo.repository.RentalOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RentalService {
    private final ItemRepository itemRepository;
    private final RentalOrderRepository rentalOrderRepository;

    public RentalService(ItemRepository itemRepository, RentalOrderRepository rentalOrderRepository) {
        this.itemRepository = itemRepository;
        this.rentalOrderRepository = rentalOrderRepository;
    }

    public List<ItemDTO> getAllAvailableItems() {
        List<Item> items = itemRepository.findByAvailableTrue();
        return items.stream()
                .map(ItemDTO::new)
                .collect(Collectors.toList());
    }

    public List<ItemDTO> searchItems(String keyword) {
        List<Item> items = itemRepository.findByNameContainingIgnoreCase(keyword);
        return items.stream()
                .map(ItemDTO::new)
                .collect(Collectors.toList());
    }

    public List<ItemDTO> getItems(Long id) {
        List<Item> items = itemRepository.findByOwnerId(id);
        return items.stream()
                .map(ItemDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public RentalOrderDTO createOrder(Item item, User renter, LocalDate startDate, LocalDate endDate) {
        // 验证日期
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }

        // 计算租赁天数
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days < 1) {
            throw new IllegalArgumentException("租赁天数至少为1天");
        }

        // 计算总价
        BigDecimal totalPrice = item.getDailyPrice().multiply(BigDecimal.valueOf(days));

        // 创建订单
        RentalOrder order = new RentalOrder();
        order.setItem(item);
        order.setRenter(renter);
        order.setStartDate(startDate);
        order.setEndDate(endDate);
        order.setTotalPrice(totalPrice);

        // 更新物品状态
        item.setAvailable(false);
        itemRepository.save(item);

        RentalOrder savedOrder = rentalOrderRepository.save(order);
        return new RentalOrderDTO(savedOrder);
    }

    public List<RentalOrder> getUserOrders(User user) {
        return rentalOrderRepository.findByRenter(user);
    }

    public List<RentalOrder> getOwnerOrders(User owner) {
        return rentalOrderRepository.findByItemOwner(owner);
    }

    @Transactional
    public RentalOrder completeOrder(Long orderId) {
        RentalOrder order = rentalOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        if (order.getStatus() != RentalOrder.OrderStatus.ACTIVE) {
            throw new IllegalStateException("只有活动中的订单才能完成");
        }

        order.setStatus(RentalOrder.OrderStatus.COMPLETED);

        // 释放物品
        Item item = order.getItem();
        item.setAvailable(true);

        rentalOrderRepository.save(order);
        itemRepository.save(item);
        return order;
    }

    @Transactional
    public RentalOrder cancelOrder(Long orderId) {
        RentalOrder order = rentalOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        if (order.getStatus() != RentalOrder.OrderStatus.PENDING) {
            throw new IllegalStateException("只有待处理的订单才能取消");
        }

        order.setStatus(RentalOrder.OrderStatus.CANCELLED);

        // 释放物品
        Item item = order.getItem();
        item.setAvailable(true);

        rentalOrderRepository.save(order);
        itemRepository.save(item);
        return order;
    }
}