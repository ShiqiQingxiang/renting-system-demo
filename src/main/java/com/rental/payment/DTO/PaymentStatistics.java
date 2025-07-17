package com.rental.payment.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "支付统计信息")
public class PaymentStatistics {

    @Schema(description = "总支付笔数", example = "100")
    private long totalPayments;

    @Schema(description = "成功支付笔数", example = "95")
    private long successPayments;

    @Schema(description = "失败支付笔数", example = "3")
    private long failedPayments;

    @Schema(description = "待支付笔数", example = "2")
    private long pendingPayments;

    @Schema(description = "总支付金额", example = "50000.00")
    private BigDecimal totalAmount;

    @Schema(description = "成功支付金额", example = "47500.00")
    private BigDecimal successAmount;

    @Schema(description = "退款笔数", example = "5")
    private long refundCount;

    @Schema(description = "退款金额", example = "2500.00")
    private BigDecimal refundAmount;

    @Schema(description = "支付成功率", example = "95.0")
    private Double successRate;
}
