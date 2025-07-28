package com.rental.payment.integration.alipay;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Data
@Configuration
@ConfigurationProperties(prefix = "payment.alipay")
@Slf4j
public class AlipayConfig {

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 商户私钥
     */
    private String privateKey;

    /**
     * 支付宝公钥
     */
    private String alipayPublicKey;

    /**
     * 签名方式
     */
    private String signType;

    /**
     * 字符编码格式
     */
    private String charset;

    /**
     * 支付宝网关地址
     */
    private String gatewayUrl;

    /**
     * 异步通知地址
     */
    private String notifyUrl;

    /**
     * 同步跳转地址
     */
    private String returnUrl;

    /**
     * 日志记录目录
     */
    private String logPath;

    /**
     * 沙箱环境标识
     */
    private boolean sandbox;

    /**
     * 配置验证
     */
    @PostConstruct
    public void validateConfig() {
        if (!StringUtils.hasText(appId)) {
            throw new IllegalArgumentException("支付宝AppId不能为空");
        }
        if (!StringUtils.hasText(privateKey)) {
            throw new IllegalArgumentException("支付宝商户私钥不能为空");
        }
        if (!StringUtils.hasText(alipayPublicKey)) {
            throw new IllegalArgumentException("支付宝公钥不能为空");
        }
        if (!StringUtils.hasText(gatewayUrl)) {
            throw new IllegalArgumentException("支付宝网关地址不能为空");
        }
        
        log.info("支付宝配置验证成功，AppId: {}, 沙箱模式: {}", appId, sandbox);
    }

    /**
     * 创建支付宝客户端
     */
    @Bean
    public AlipayClient alipayClient() {
        log.info("初始化支付宝客户端，网关地址：{}", gatewayUrl);
        
        return new DefaultAlipayClient(
            gatewayUrl,
            appId, 
            privateKey,
            "json",
            charset,
            alipayPublicKey,
            signType
        );
    }
}
