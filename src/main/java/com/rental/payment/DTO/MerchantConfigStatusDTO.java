package com.rental.payment.DTO;

import com.rental.payment.model.MerchantPaymentConfig;
import lombok.Data;

/**
 * 商家支付配置状态 DTO
 */
@Data
public class MerchantConfigStatusDTO {

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 是否已配置支付信息
     */
    private Boolean hasConfig;

    /**
     * 配置状态
     */
    private MerchantPaymentConfig.ConfigStatus status;

    /**
     * 配置ID（如果存在）
     */
    private Long configId;

    /**
     * 支付宝应用ID（如果存在，用于前端展示）
     */
    private String alipayAppId;
}
