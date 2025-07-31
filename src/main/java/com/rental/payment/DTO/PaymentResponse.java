package com.rental.payment.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "支付响应")
public class PaymentResponse {

    @Schema(description = "支付ID", example = "1")
    private Long paymentId;

    @Schema(description = "支付单号", example = "PAY20250716000001")
    private String paymentNo;

    @Schema(description = "支付URL或支付码")
    private String paymentUrl;

    @Schema(description = "支付二维码（Base64编码）")
    private String qrCode;

    @Schema(description = "第三方支付订单号")
    private String thirdPartyOrderNo;

    @Schema(description = "商家ID")
    private Long merchantId;

    @Schema(description = "支付表单HTML（用于跳转支付）")
    private String paymentForm;

    @Schema(description = "支付状态", example = "PENDING")
    private String status;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "是否需要跳转", example = "true")
    private Boolean needRedirect = false;

    @Schema(description = "过期时间（秒）", example = "900")
    private Integer expireTime;
}
