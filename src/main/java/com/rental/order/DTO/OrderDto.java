package com.rental.order.DTO;

import com.rental.order.model.Order;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private Long id;
    private String orderNo;
    private Long userId;
    private String username;
    private String userRealName;
    private BigDecimal totalAmount;
    private BigDecimal depositAmount;
    private Order.OrderStatus status;
    private String statusDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate actualReturnDate;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 订单项信息
    private List<OrderItemDto> orderItems;

    // 租赁天数
    private Integer rentalDays;

    // 订单状态相关
    private Boolean canCancel;
    private Boolean canConfirm;
    private Boolean canPay;
    private Boolean canReturn;

    // 支付状态
    private Boolean isPaid;
    private BigDecimal paidAmount;

    // 合同状态
    private Boolean hasContract;
    private String contractNo;

    // 辅助方法
    public String getStatusDescription() {
        return status != null ? status.getDescription() : null;
    }

    public Boolean getCanCancel() {
        return status == Order.OrderStatus.PENDING || status == Order.OrderStatus.CONFIRMED;
    }

    public Boolean getCanConfirm() {
        return status == Order.OrderStatus.PENDING;
    }

    public Boolean getCanPay() {
        return status == Order.OrderStatus.CONFIRMED && !getIsPaid();
    }

    public Boolean getCanReturn() {
        return status == Order.OrderStatus.IN_USE;
    }
}
