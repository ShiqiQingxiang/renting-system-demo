package com.rental.payment.integration.alipay;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "payment.alipay")
public class AlipayConfig {

    /**
     * 应用ID
     */
    private String appId = "2021000122671234";

    /**
     * 商户私钥
     */
    private String privateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC...";

    /**
     * 支付宝公钥
     */
    private String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...";

    /**
     * 签名方式
     */
    private String signType = "RSA2";

    /**
     * 字符编码格式
     */
    private String charset = "UTF-8";

    /**
     * 支付宝网关地址
     */
    private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    /**
     * 异步通知地址
     */
    private String notifyUrl = "http://localhost:8080/api/payments/callback/alipay";

    /**
     * 同步跳转地址
     */
    private String returnUrl = "http://localhost:3000/payment/success";

    /**
     * 日志记录目录
     */
    private String logPath = "/logs/alipay";

    /**
     * 沙箱环境标识
     */
    private boolean sandbox = true;
}
