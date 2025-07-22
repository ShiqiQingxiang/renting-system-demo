package com.rental.finance.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.rental.finance.model.FinanceRecord;

import java.math.BigDecimal;

/**
 * 创建财务记录请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinanceRecordCreateRequest {

    private Long orderId;

    private Long paymentId;

    @NotNull(message = "财务类型不能为空")
    private FinanceRecord.FinanceType type;

    @NotBlank(message = "财务分类不能为空")
    private String category;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.0", message = "金额必须大于等于0")
    private BigDecimal amount;

    private String description;
}
