package com.rental.payment.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "商家支付配置请求对象")
public class MerchantPaymentConfigRequest {

    @Schema(description = "支付宝应用ID", example = "2021001234567890", required = true)
    @NotBlank(message = "支付宝应用ID不能为空")
    @Pattern(regexp = "^\\d{16}$", message = "支付宝应用ID格式不正确，应为16位数字")
    private String alipayAppId;

    @Schema(description = "商家私钥", example = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC...", required = true)
    @NotBlank(message = "商家私钥不能为空")
    private String alipayPrivateKey;

    @Schema(description = "支付宝公钥", example = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMI...", required = true)
    @NotBlank(message = "支付宝公钥不能为空")
    private String alipayPublicKey;

    @Schema(description = "支付宝收款账户（邮箱或手机号）", example = "merchant@example.com", required = true)
    @NotBlank(message = "支付宝收款账户不能为空")
    private String alipayAccount;

    @Schema(description = "异步通知地址", example = "https://example.com/api/payment/notify/{merchantId}", required = true)
    @NotBlank(message = "异步通知地址不能为空")
    @Pattern(regexp = "^https://.*", message = "异步通知地址必须使用HTTPS协议")
    private String notifyUrl;

    @Schema(description = "同步返回地址", example = "https://example.com/payment/result", required = true)
    @NotBlank(message = "同步返回地址不能为空")
    @Pattern(regexp = "^https://.*", message = "同步返回地址必须使用HTTPS协议")
    private String returnUrl;

    @Schema(description = "备注信息", example = "商家支付配置")
    private String remark;

    /**
     * 验证私钥格式
     * 采用更宽松的验证策略，支持多种常见格式
     */
    public boolean isValidPrivateKey() {
        if (alipayPrivateKey == null || alipayPrivateKey.trim().isEmpty()) {
            return false;
        }

        String key = alipayPrivateKey.trim();

        // 1. 检查PEM格式
        if (key.contains("-----BEGIN PRIVATE KEY-----") ||
            key.contains("-----BEGIN RSA PRIVATE KEY-----")) {
            return true;
        }

        // 2. 检查纯Base64格式 - 更宽松的验证
        String cleanKey = key.replaceAll("\\s+", ""); // 移除所有空白字符

        // 支付宝私钥通常以 MII 开头，长度在 1000-3000 字符之间
        if (cleanKey.startsWith("MII") && cleanKey.length() > 500) {
            return isValidBase64Loose(cleanKey);
        }

        // 3. 其他可能的Base64格式
        if (cleanKey.length() > 500 && isValidBase64Loose(cleanKey)) {
            return true;
        }

        return false;
    }

    /**
     * 验证公钥格式
     * 采用更宽松的验证策略
     */
    public boolean isValidPublicKey() {
        if (alipayPublicKey == null || alipayPublicKey.trim().isEmpty()) {
            return false;
        }

        String key = alipayPublicKey.trim();

        // 1. 检查PEM格式
        if (key.contains("-----BEGIN PUBLIC KEY-----") ||
            key.contains("-----BEGIN RSA PUBLIC KEY-----")) {
            return true;
        }

        // 2. 检查纯Base64格式
        String cleanKey = key.replaceAll("\\s+", "");

        // 支付宝公钥通常以 MII 开头，长度在 200-1000 字符之间
        if (cleanKey.startsWith("MII") && cleanKey.length() > 100) {
            return isValidBase64Loose(cleanKey);
        }

        // 3. 其他可能的Base64格式
        if (cleanKey.length() > 100 && isValidBase64Loose(cleanKey)) {
            return true;
        }

        return false;
    }

    /**
     * 更宽松的Base64验证
     * 主要检查字符集，对长度要求更宽松
     */
    private boolean isValidBase64Loose(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        try {
            // 简单检查字符集是否符合Base64
            for (char c : str.toCharArray()) {
                if (!Character.isLetterOrDigit(c) && c != '+' && c != '/' && c != '=') {
                    return false;
                }
            }

            // 基本长度检查 - 更宽松
            return str.length() >= 100;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 清理私钥信息（用于日志记录等场景）
     */
    public MerchantPaymentConfigRequest sanitizeForLogging() {
        MerchantPaymentConfigRequest sanitized = new MerchantPaymentConfigRequest();
        sanitized.setAlipayAppId(this.alipayAppId);
        sanitized.setAlipayPrivateKey("***REDACTED***");
        sanitized.setAlipayPublicKey(this.alipayPublicKey != null ? "***EXISTS***" : null);
        sanitized.setAlipayAccount(this.alipayAccount);
        sanitized.setNotifyUrl(this.notifyUrl);
        sanitized.setReturnUrl(this.returnUrl);
        sanitized.setRemark(this.remark);
        return sanitized;
    }
}