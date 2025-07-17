package com.rental.payment.DTO;

import com.rental.payment.model.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "支付创建请求")
public class PaymentCreateRequest {

    @Schema(description = "订单ID", example = "1", required = true)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @Schema(description = "支付金额", example = "100.00", required = true)
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    private BigDecimal amount;

    @Schema(description = "支付方式", example = "ALIPAY", required = true)
    @NotNull(message = "支付方式不能为空")
    private Payment.PaymentMethod paymentMethod;

    @Schema(description = "支付类型", example = "RENTAL", required = true)
    @NotNull(message = "支付类型不能为空")
    private Payment.PaymentType paymentType;

    @Schema(description = "客户端IP地址", example = "192.168.1.1")
    private String clientIp;

    @Schema(description = "回调地址", example = "https://example.com/callback")
    private String notifyUrl;

    @Schema(description = "返回地址", example = "https://example.com/return")
    private String returnUrl;

    @Schema(description = "备注信息", example = "租赁订单支付")
    private String remark;
}
