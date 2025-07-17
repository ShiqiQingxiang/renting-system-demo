package com.rental.payment.DTO;

import com.rental.payment.model.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "支付搜索请求")
public class PaymentSearchRequest {

    @Schema(description = "支付单号", example = "PAY20250716000001")
    private String paymentNo;

    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    @Schema(description = "订单号", example = "ORD20250716000001")
    private String orderNo;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "testuser")
    private String username;

    @Schema(description = "支付方式", example = "ALIPAY")
    private Payment.PaymentMethod paymentMethod;

    @Schema(description = "支付类型", example = "RENTAL")
    private Payment.PaymentType paymentType;

    @Schema(description = "支付状态", example = "SUCCESS")
    private Payment.PaymentStatus status;

    @Schema(description = "最小金额", example = "0.01")
    private BigDecimal minAmount;

    @Schema(description = "最大金额", example = "1000.00")
    private BigDecimal maxAmount;

    @Schema(description = "创建日期起", example = "2025-07-01")
    private LocalDate createdDateFrom;

    @Schema(description = "创建日期止", example = "2025-07-31")
    private LocalDate createdDateTo;

    @Schema(description = "第三方交易ID")
    private String thirdPartyTransactionId;
}
