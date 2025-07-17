package com.rental.payment.DTO;

import com.rental.payment.model.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "支付信息响应")
public class PaymentDto {

    @Schema(description = "支付ID", example = "1")
    private Long id;

    @Schema(description = "支付单号", example = "PAY20250716000001")
    private String paymentNo;

    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    @Schema(description = "订单号", example = "ORD20250716000001")
    private String orderNo;

    @Schema(description = "支付金额", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "支付方式", example = "ALIPAY")
    private Payment.PaymentMethod paymentMethod;

    @Schema(description = "支付方式描述", example = "支付宝")
    private String paymentMethodDesc;

    @Schema(description = "支付类型", example = "RENTAL")
    private Payment.PaymentType paymentType;

    @Schema(description = "支付类型描述", example = "租金")
    private String paymentTypeDesc;

    @Schema(description = "支付状态", example = "SUCCESS")
    private Payment.PaymentStatus status;

    @Schema(description = "支付状态描述", example = "支付成功")
    private String statusDesc;

    @Schema(description = "第三方交易ID")
    private String thirdPartyTransactionId;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "testuser")
    private String username;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
