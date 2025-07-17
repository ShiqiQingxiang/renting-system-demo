package com.rental.order.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderAuditRequest {

    @NotNull(message = "审核结果不能为空")
    private Boolean approved;

    @Size(max = 500, message = "审核意见长度不能超过500")
    private String comment;
}
