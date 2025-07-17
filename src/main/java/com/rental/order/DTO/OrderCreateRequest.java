package com.rental.order.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    @NotEmpty(message = "订单项不能为空")
    @Valid
    private List<OrderItemCreateRequest> orderItems;

    @Size(max = 1000, message = "备注长度不能超过1000")
    private String remark;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemCreateRequest {

        @NotNull(message = "物品ID不能为空")
        private Long itemId;

        @NotNull(message = "数量不能为空")
        private Integer quantity = 1;
    }
}
