package com.rental.payment.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Schema(description = "支付回调请求")
public class PaymentCallbackRequest {

    @Schema(description = "支付单号", example = "PAY20250716000001")
    private String paymentNo;

    @Schema(description = "第三方交易ID")
    private String thirdPartyTransactionId;

    @Schema(description = "支付状态", example = "SUCCESS")
    private String status;

    @Schema(description = "支付金额", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "支付时间", example = "20250716123000")
    private String paymentTime;

    @Schema(description = "原始回调数据")
    private Map<String, Object> rawData;

    @Schema(description = "签名")
    private String sign;

    @Schema(description = "错误码")
    private String errorCode;

    @Schema(description = "错误信息")
    private String errorMessage;
}
