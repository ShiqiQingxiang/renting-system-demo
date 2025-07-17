package com.rental.payment.service.impl;

import com.rental.payment.DTO.PaymentCallbackRequest;
import com.rental.payment.service.PaymentCallbackService;
import com.rental.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCallbackServiceImpl implements PaymentCallbackService {

    private final PaymentService paymentService;

    @Override
    public void handleAlipayCallback(PaymentCallbackRequest request) {
        log.info("处理支付宝回调，支付单号：{}", request.getPaymentNo());

        // 验证签名
        if (!verifyCallbackSign(request, "ALIPAY")) {
            log.warn("支付宝回调签名验证失败，支付单号：{}", request.getPaymentNo());
            return;
        }

        // 委托给支付服务处理
        paymentService.handlePaymentCallback(request);
    }

    @Override
    public void handleWechatCallback(PaymentCallbackRequest request) {
        log.info("处理微信支付回调，支付单号：{}", request.getPaymentNo());

        // 验证签名
        if (!verifyCallbackSign(request, "WECHAT")) {
            log.warn("微信支付回调签名验证失败，支付单号：{}", request.getPaymentNo());
            return;
        }

        // 委托给支付服务处理
        paymentService.handlePaymentCallback(request);
    }

    @Override
    public boolean verifyCallbackSign(PaymentCallbackRequest request, String paymentMethod) {
        // 根据不同支付方式验证签名
        switch (paymentMethod.toUpperCase()) {
            case "ALIPAY":
                return verifyAlipaySign(request);
            case "WECHAT":
                return verifyWechatSign(request);
            default:
                log.warn("不支持的支付方式：{}", paymentMethod);
                return false;
        }
    }

    @Override
    public PaymentCallbackRequest parseCallbackData(String callbackData, String paymentMethod) {
        PaymentCallbackRequest request = new PaymentCallbackRequest();

        // 根据不同支付方式解析回调数据
        switch (paymentMethod.toUpperCase()) {
            case "ALIPAY":
                parseAlipayCallback(callbackData, request);
                break;
            case "WECHAT":
                parseWechatCallback(callbackData, request);
                break;
            default:
                log.warn("不支持的支付方式：{}", paymentMethod);
        }

        return request;
    }

    private boolean verifyAlipaySign(PaymentCallbackRequest request) {
        // 实际项目中需要使用支付宝SDK验证签名
        // 这里简化处理，返回true
        // 实际实现示例：
        // return AlipaySignature.rsaCheckV1(params, aliPayPublicKey, charset, signType);
        return true;
    }

    private boolean verifyWechatSign(PaymentCallbackRequest request) {
        // 实际项目中需要使用微信支付SDK验证签名
        // 这里简化处理，返回true
        return true;
    }

    private void parseAlipayCallback(String callbackData, PaymentCallbackRequest request) {
        // 实际项目中需要解析支付宝回调参数
        // 这里简化处理
        log.info("解析支付宝回调数据：{}", callbackData);
    }

    private void parseWechatCallback(String callbackData, PaymentCallbackRequest request) {
        // 实际项目中需要解析微信支付回调参数
        // 这里简化处理
        log.info("解析微信支付回调数据：{}", callbackData);
    }
}
