package com.rental.order.DTO;

import com.rental.order.model.Order;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdateRequest {

    private LocalDate startDate;
    private LocalDate endDate;
    private Order.OrderStatus status;

    @Size(max = 1000, message = "备注长度不能超过1000")
    private String remark;
}
