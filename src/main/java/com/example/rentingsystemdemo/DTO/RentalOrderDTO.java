package com.example.rentingsystemdemo.DTO;

import com.example.rentingsystemdemo.model.RentalOrder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class RentalOrderDTO {
    private Long id;
    private Long itemId;
    private String itemName;
    private Long renterId;
    private String renterUsername;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalPrice;
    private RentalOrder.OrderStatus status;

    public RentalOrderDTO(RentalOrder order) {
        this.id = order.getId();
        this.itemId = order.getItem().getId();
        this.itemName = order.getItem().getName();
        this.renterId = order.getRenter().getId();
        this.renterUsername = order.getRenter().getUsername();
        this.startDate = order.getStartDate();
        this.endDate = order.getEndDate();
        this.totalPrice = order.getTotalPrice();
        this.status = order.getStatus();
    }
}