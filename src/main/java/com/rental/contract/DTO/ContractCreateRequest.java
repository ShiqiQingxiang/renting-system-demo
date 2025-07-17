package com.rental.contract.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractCreateRequest {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    private Long templateId;

    private String customContent;
}
