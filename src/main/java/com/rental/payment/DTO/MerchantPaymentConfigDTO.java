package com.rental.payment.DTO;

import com.rental.payment.model.MerchantPaymentConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "商家支付配置数据传输对象")
public class MerchantPaymentConfigDTO {

    @Schema(description = "配置ID", example = "1")
    private Long id;

    @Schema(description = "商家ID", example = "123")
    @NotNull(message = "商家ID不能为空")
    private Long merchantId;

    @Schema(description = "支付宝应用ID", example = "2021001234567890")
    @NotBlank(message = "支付宝应用ID不能为空")
    private String alipayAppId;

    @Schema(description = "支付宝公钥", example = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMI...")
    @NotBlank(message = "支付宝公钥不能为空")
    private String alipayPublicKey;

    @Schema(description = "支付宝收款账户", example = "merchant@example.com")
    @NotBlank(message = "支付宝收款账户不能为空")
    private String alipayAccount;

    @Schema(description = "异步通知地址", example = "https://example.com/api/payment/notify/{merchantId}")
    @NotBlank(message = "异步通知地址不能为空")
    private String notifyUrl;

    @Schema(description = "同步返回地址", example = "https://example.com/payment/result")
    @NotBlank(message = "同步返回地址不能为空")
    private String returnUrl;

    @Schema(description = "配置状态", example = "ACTIVE")
    private MerchantPaymentConfig.ConfigStatus status;

    @Schema(description = "备注信息", example = "商家支付配置")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}