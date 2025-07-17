package com.rental.order.DTO;

import jakarta.validation.constraints.NotNull;
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
public class OrderReturnRequest {

    @NotNull(message = "归还日期不能为空")
    private LocalDate returnDate;

    @Size(max = 1000, message = "归还备注长度不能超过1000")
    private String returnRemark;

    // 物品状态检查
    private Boolean allItemsReturned = true;

    // 是否有损坏
    private Boolean hasDamage = false;

    @Size(max = 500, message = "损坏描述长度不能超过500")
    private String damageDescription;
}
