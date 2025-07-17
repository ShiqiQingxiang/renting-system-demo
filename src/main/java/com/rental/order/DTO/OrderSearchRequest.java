package com.rental.order.DTO;

import com.rental.order.model.Order;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderSearchRequest {

    private String orderNo;
    private Long userId;
    private String username;
    private Order.OrderStatus status;
    private LocalDate startDateFrom;
    private LocalDate startDateTo;
    private LocalDate endDateFrom;
    private LocalDate endDateTo;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Long itemId;
    private String itemName;
    private Long categoryId;
}
