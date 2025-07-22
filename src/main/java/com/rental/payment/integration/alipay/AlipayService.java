package com.rental.payment.integration.alipay;

import com.rental.payment.DTO.PaymentCreateRequest;
import com.rental.payment.DTO.PaymentResponse;
import com.rental.payment.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlipayService {

    private final AlipayConfig alipayConfig;

    /**
     * 创建支付宝支付
     */
    public PaymentResponse createPayment(Payment payment, PaymentCreateRequest request) {
        log.info("创建支付宝支付，支付单号：{}", payment.getPaymentNo());

        PaymentResponse response = new PaymentResponse();

        try {
            // 构建支付参数
            String orderInfo = buildOrderInfo(payment, request);

            // 生成支付URL
            String paymentUrl = generatePaymentUrl(orderInfo);

            response.setPaymentUrl(paymentUrl);
            response.setNeedRedirect(true);
            response.setStatus("PENDING");
            response.setExpireTime(900); // 15分钟过期

            log.info("支付宝支付创建成功，支付单号：{}", payment.getPaymentNo());

        } catch (Exception e) {
            log.error("创建支付宝支付失败，支付单号：{}", payment.getPaymentNo(), e);
            response.setStatus("FAILED");
            response.setErrorMessage("创建支付失败：" + e.getMessage());
        }

        return response;
    }

    /**
     * 支付宝退款
     */
    public boolean refund(Payment originalPayment, Payment refundPayment, BigDecimal refundAmount, String refundReason) {
        log.info("处理支付宝退款，原支付单号：{}，退款单号：{}",
                originalPayment.getPaymentNo(), refundPayment.getPaymentNo());

        try {
            // 这里应该调用支付宝退款API
            // 实际实现需要使用支付宝SDK

            // 模拟退款成功
            log.info("支付宝退款处理成功");
            return true;

        } catch (Exception e) {
            log.error("支付宝退款失败", e);
            return false;
        }
    }

    /**
     * 查询支付状态
     */
    public String queryPaymentStatus(Payment payment) {
        log.info("查询支付宝支付状态，支付单号：{}", payment.getPaymentNo());

        try {
            // 这里应该调用支付宝查询API
            // 实际实现需要使用支付宝SDK
            /*
            AlipayTradeQueryRequest queryRequest = new AlipayTradeQueryRequest();
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(payment.getPaymentNo());
            queryRequest.setBizModel(model);

            AlipayTradeQueryResponse response = alipayClient.execute(queryRequest);
            if (response.isSuccess()) {
                return response.getTradeStatus();
            }
            */

            // 模拟返回当前状态
            return payment.getStatus().name();

        } catch (Exception e) {
            log.error("查询支付宝支付状态失败", e);
            return "UNKNOWN";
        }
    }

    private String buildOrderInfo(Payment payment, PaymentCreateRequest request) {
        // 构建支付参数
        StringBuilder orderInfo = new StringBuilder();
        orderInfo.append("app_id=").append(alipayConfig.getAppId());
        orderInfo.append("&method=alipay.trade.app.pay");
        orderInfo.append("&charset=").append(alipayConfig.getCharset());
        orderInfo.append("&sign_type=").append(alipayConfig.getSignType());
        orderInfo.append("&timestamp=").append(System.currentTimeMillis());
        orderInfo.append("&version=1.0");
        orderInfo.append("&notify_url=").append(alipayConfig.getNotifyUrl());

        // 业务参数
        String bizContent = String.format(
            "{\"out_trade_no\":\"%s\",\"total_amount\":\"%s\",\"subject\":\"%s\",\"body\":\"%s\"}",
            payment.getPaymentNo(),
            payment.getAmount().toString(),
            "租赁订单支付",
            "订单号：" + payment.getOrder().getOrderNo()
        );
        orderInfo.append("&biz_content=").append(bizContent);

        return orderInfo.toString();
    }

    private String generatePaymentUrl(String orderInfo) {
        // 生成支付URL
        // 实际实现需要对参数进行签名
        return alipayConfig.getGatewayUrl() + "?" + orderInfo + "&sign=MOCK_SIGNATURE";
    }
}
