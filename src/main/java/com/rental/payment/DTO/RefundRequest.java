package com.rental.payment.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "退款请求")
public class RefundRequest {

    @Schema(description = "支付ID", example = "1", required = true)
    @NotNull(message = "支付ID不能为空")
    private Long paymentId;

    @Schema(description = "退款金额", example = "50.00", required = true)
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    private BigDecimal refundAmount;

    @Schema(description = "退款原因", example = "用户申请退款", required = true)
    @NotBlank(message = "退款原因不能为空")
    private String refundReason;

    @Schema(description = "退款备注", example = "提前归还物品")
    private String remark;
}
