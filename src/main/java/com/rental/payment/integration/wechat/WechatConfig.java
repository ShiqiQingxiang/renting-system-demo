package com.rental.payment.integration.wechat;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "payment.wechat")
public class WechatConfig {

    /**
     * 应用ID
     */
    private String appId = "wx1234567890123456";

    /**
     * 商户号
     */
    private String mchId = "1234567890";

    /**
     * API密钥
     */
    private String apiKey = "your_api_key_here_32_characters";

    /**
     * 商户证书路径
     */
    private String certPath = "/path/to/apiclient_cert.p12";

    /**
     * 异步通知地址
     */
    private String notifyUrl = "http://localhost:8080/api/payments/callback/wechat";

    /**
     * 统一下单接口地址
     */
    private String unifiedOrderUrl = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    /**
     * 查询订单接口地址
     */
    private String orderQueryUrl = "https://api.mch.weixin.qq.com/pay/orderquery";

    /**
     * 退款接口地址
     */
    private String refundUrl = "https://api.mch.weixin.qq.com/secapi/pay/refund";

    /**
     * 沙箱环境标识
     */
    private boolean sandbox = true;

    /**
     * 签名类型
     */
    private String signType = "MD5";
}
