package com.rental.payment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "merchant_payment_configs", indexes = {
    @Index(name = "idx_merchant_id", columnList = "merchant_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantPaymentConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_id", nullable = false, unique = true)
    @NotNull(message = "商家ID不能为空")
    private Long merchantId;

    @Column(name = "alipay_app_id", nullable = false, length = 32)
    @NotBlank(message = "支付宝应用ID不能为空")
    private String alipayAppId;

    @Column(name = "encrypted_private_key", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "商家私钥不能为空")
    private String encryptedPrivateKey;

    @Column(name = "alipay_public_key", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "支付宝公钥不能为空")
    private String alipayPublicKey;

    @Column(name = "alipay_account", nullable = false, length = 100)
    @NotBlank(message = "支付宝收款账户不能为空")
    private String alipayAccount;

    @Column(name = "notify_url", nullable = false)
    @NotBlank(message = "异步通知地址不能为空")
    private String notifyUrl;

    @Column(name = "return_url", nullable = false)
    @NotBlank(message = "同步返回地址不能为空")
    private String returnUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConfigStatus status = ConfigStatus.ACTIVE;

    @Column(name = "remark", length = 500)
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ConfigStatus {
        ACTIVE("启用"),
        INACTIVE("禁用"),
        PENDING_REVIEW("待审核");

        private final String description;

        ConfigStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 检查配置是否可用
     */
    public boolean isAvailable() {
        return ConfigStatus.ACTIVE.equals(this.status);
    }

    /**
     * 获取通知地址（包含商家ID）
     */
    public String getNotifyUrlWithMerchant() {
        if (notifyUrl.contains("{merchantId}")) {
            return notifyUrl.replace("{merchantId}", merchantId.toString());
        }
        return notifyUrl + "?merchantId=" + merchantId;
    }

    /**
     * 获取返回地址（包含商家ID）
     */
    public String getReturnUrlWithMerchant() {
        if (returnUrl.contains("{merchantId}")) {
            return returnUrl.replace("{merchantId}", merchantId.toString());
        }
        return returnUrl + "?merchantId=" + merchantId;
    }
}