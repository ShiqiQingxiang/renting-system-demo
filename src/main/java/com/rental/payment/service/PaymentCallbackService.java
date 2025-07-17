package com.rental.payment.service;

import com.rental.payment.DTO.PaymentCallbackRequest;

/**
 * 支付回调服务接口
 */
public interface PaymentCallbackService {

    /**
     * 处理支付宝回调
     */
    void handleAlipayCallback(PaymentCallbackRequest request);

    /**
     * 处理微信支付回调
     */
    void handleWechatCallback(PaymentCallbackRequest request);

    /**
     * 验证回调签名
     */
    boolean verifyCallbackSign(PaymentCallbackRequest request, String paymentMethod);

    /**
     * 解析回调数据
     */
    PaymentCallbackRequest parseCallbackData(String callbackData, String paymentMethod);
}
