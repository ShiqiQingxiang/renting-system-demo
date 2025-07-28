package com.rental.payment.integration.alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.rental.common.exception.BusinessException;
import com.rental.payment.DTO.PaymentCreateRequest;
import com.rental.payment.DTO.PaymentResponse;
import com.rental.payment.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlipayService {

    private final AlipayClient alipayClient;
    private final AlipayConfig alipayConfig;

    public PaymentResponse createPayment(Payment payment, PaymentCreateRequest request) {
        log.info("创建支付宝支付，支付单号：{}", payment.getPaymentNo());

        PaymentResponse response = new PaymentResponse();

        try {
            // 创建 alipay.trade.page.pay 请求
            AlipayTradePagePayRequest pagePayRequest = new AlipayTradePagePayRequest();
            
            // 设置异步通知地址
            pagePayRequest.setNotifyUrl(alipayConfig.getNotifyUrl());
            // 设置同步跳转地址
            pagePayRequest.setReturnUrl(alipayConfig.getReturnUrl());

            // 构建和验证请求参数
            AlipayTradePagePayModel model = buildAndValidatePaymentModel(payment, request);
            pagePayRequest.setBizModel(model);

            // 调用 alipay.trade.page.pay API 接口
            log.info("调用支付宝alipay.trade.page.pay接口，商户订单号：{}", payment.getPaymentNo());
            AlipayTradePagePayResponse pagePayResponse = alipayClient.pageExecute(pagePayRequest);

            if (pagePayResponse.isSuccess()) {
                // 获取支付表单HTML
                String paymentForm = pagePayResponse.getBody();
                
                response.setPaymentId(payment.getId());
                response.setPaymentNo(payment.getPaymentNo());
                response.setPaymentForm(paymentForm);
                response.setPaymentUrl(paymentForm); // 对于页面支付，URL就是表单内容
                response.setNeedRedirect(true);
                response.setStatus("PENDING");
                response.setExpireTime(900); // 15分钟过期
                response.setThirdPartyOrderNo(payment.getPaymentNo()); // 商户订单号
                
                log.info("支付宝alipay.trade.page.pay接口调用成功，支付单号：{}", payment.getPaymentNo());
            } else {
                // API调用失败
                log.error("支付宝alipay.trade.page.pay接口调用失败，支付单号：{}，错误码：{}，错误信息：{}", 
                         payment.getPaymentNo(), pagePayResponse.getCode(), pagePayResponse.getMsg());
                         
                response.setStatus("FAILED");
                response.setErrorMessage(String.format("支付宝支付创建失败[%s]: %s", 
                                                     pagePayResponse.getCode(), pagePayResponse.getMsg()));
                throw new BusinessException(response.getErrorMessage());
            }

        } catch (AlipayApiException e) {
            log.error("支付宝alipay.trade.page.pay API异常，支付单号：{}，错误码：{}，错误信息：{}", 
                     payment.getPaymentNo(), e.getErrCode(), e.getErrMsg(), e);
            response.setStatus("FAILED");
            response.setErrorMessage(String.format("支付宝API调用异常[%s]: %s", e.getErrCode(), e.getErrMsg()));
        } catch (BusinessException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            log.error("创建支付宝支付异常，支付单号：{}", payment.getPaymentNo(), e);
            response.setStatus("FAILED");
            response.setErrorMessage("创建支付失败：" + e.getMessage());
        }

        return response;
    }

    /**
     * 支付宝退款
     */
    public boolean refund(Payment originalPayment, Payment refundPayment, BigDecimal refundAmount, String refundReason) {
        log.info("处理支付宝退款，原支付单号：{}，退款单号：{}，退款金额：{}",
                originalPayment.getPaymentNo(), refundPayment.getPaymentNo(), refundAmount);

        try {
            // 创建退款请求
            AlipayTradeRefundRequest alipayRequest = new AlipayTradeRefundRequest();
            
            // 构建业务参数
            AlipayTradeRefundModel model = new AlipayTradeRefundModel();
            model.setOutTradeNo(originalPayment.getPaymentNo());
            model.setRefundAmount(refundAmount.toString());
            model.setOutRequestNo(refundPayment.getPaymentNo());
            model.setRefundReason(StringUtils.hasText(refundReason) ? refundReason : "订单退款");
            
            alipayRequest.setBizModel(model);

            // 调用支付宝API
            AlipayTradeRefundResponse alipayResponse = alipayClient.execute(alipayRequest);
            
            if (alipayResponse.isSuccess()) {
                log.info("支付宝退款处理成功，退款单号：{}", refundPayment.getPaymentNo());
                return true;
            } else {
                log.error("支付宝退款失败，退款单号：{}，错误信息：{}", 
                         refundPayment.getPaymentNo(), alipayResponse.getSubMsg());
                return false;
            }

        } catch (AlipayApiException e) {
            log.error("支付宝退款API调用失败，退款单号：{}", refundPayment.getPaymentNo(), e);
            return false;
        } catch (Exception e) {
            log.error("支付宝退款异常，退款单号：{}", refundPayment.getPaymentNo(), e);
            return false;
        }
    }

    /**
     * 查询支付状态
     */
    public String queryPaymentStatus(Payment payment) {
        log.info("查询支付宝支付状态，支付单号：{}", payment.getPaymentNo());

        try {
            // 创建查询请求
            AlipayTradeQueryRequest alipayRequest = new AlipayTradeQueryRequest();
            
            // 构建业务参数
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(payment.getPaymentNo());
            
            alipayRequest.setBizModel(model);

            // 调用支付宝API
            AlipayTradeQueryResponse alipayResponse = alipayClient.execute(alipayRequest);
            
            if (alipayResponse.isSuccess()) {
                String tradeStatus = alipayResponse.getTradeStatus();
                log.info("支付宝支付状态查询成功，支付单号：{}，状态：{}", 
                        payment.getPaymentNo(), tradeStatus);
                return convertAlipayStatusToSystemStatus(tradeStatus);
            } else {
                log.warn("支付宝支付状态查询失败，支付单号：{}，错误信息：{}", 
                        payment.getPaymentNo(), alipayResponse.getSubMsg());
                return payment.getStatus().name();
            }

        } catch (AlipayApiException e) {
            log.error("查询支付宝支付状态API调用失败，支付单号：{}", payment.getPaymentNo(), e);
            return "UNKNOWN";
        } catch (Exception e) {
            log.error("查询支付宝支付状态异常，支付单号：{}", payment.getPaymentNo(), e);
            return "UNKNOWN";
        }
    }

    /**
     * 验证支付宝回调签名
     */
    public boolean verifyNotifySign(Map<String, String> params) {
        try {
            return AlipaySignature.rsaCheckV1(
                params, 
                alipayConfig.getAlipayPublicKey(), 
                alipayConfig.getCharset(), 
                alipayConfig.getSignType()
            );
        } catch (AlipayApiException e) {
            log.error("支付宝回调签名验证失败", e);
            return false;
        }
    }

    /**
     * 构建支付主题 - 符合alipay.trade.page.pay规范
     */
    private String buildPaymentSubject(Payment payment) {
        String subject;
        switch (payment.getPaymentType()) {
            case RENTAL:
                subject = "租赁服务费";
                break;
            case DEPOSIT:
                subject = "租赁押金";
                break;
            case REFUND:
                subject = "退款";
                break;
            default:
                subject = "在线支付";
        }
        
        // 支付宝要求：订单标题不能包含特殊字符，长度限制为256个字符
        if (subject.length() > 256) {
            subject = subject.substring(0, 256);
        }
        
        return subject;
    }

    /**
     * 构建支付描述 - 符合alipay.trade.page.pay规范
     */
    private String buildPaymentBody(Payment payment) {
        StringBuilder body = new StringBuilder();
        body.append("订单编号: ").append(payment.getOrder().getOrderNo());
        body.append(", 支付类型: ").append(buildPaymentSubject(payment));
        body.append(", 支付金额: ").append(payment.getAmount()).append("元");
        
        // 支付宝要求：商品描述长度限制为400个字符
        String result = body.toString();
        if (result.length() > 400) {
            result = result.substring(0, 400);
        }
        
        return result;
    }

    /**
     * 转换支付宝状态到系统状态
     */
    private String convertAlipayStatusToSystemStatus(String alipayStatus) {
        if (!StringUtils.hasText(alipayStatus)) {
            return "PENDING";
        }
        
        switch (alipayStatus.toUpperCase()) {
            case "WAIT_BUYER_PAY":
                return "PENDING";
            case "TRADE_SUCCESS":
            case "TRADE_FINISHED":
                return "SUCCESS";
            case "TRADE_CLOSED":
                return "FAILED";
            default:
                return "PENDING";
        }
    }

    /**
     * 构建并验证支付宝 alipay.trade.page.pay 请求参数
     */
    private AlipayTradePagePayModel buildAndValidatePaymentModel(Payment payment, PaymentCreateRequest request) {
        log.debug("构建支付宝alipay.trade.page.pay请求参数，支付单号：{}", payment.getPaymentNo());
        
        // 参数验证
        validatePaymentParams(payment, request);
        
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        
        // 必填参数设置
        model.setOutTradeNo(payment.getPaymentNo());                    // 商户订单号
        model.setProductCode("FAST_INSTANT_TRADE_PAY");                 // 销售产品码，固定值
        model.setTotalAmount(formatAmount(payment.getAmount()));        // 订单总金额
        model.setSubject(buildPaymentSubject(payment));                 // 订单标题
        
        // 可选参数设置
        String paymentBody = buildPaymentBody(payment);
        if (StringUtils.hasText(request.getRemark())) {
            // 如果有备注信息，优先使用备注作为描述
            paymentBody = sanitizeText(request.getRemark(), 400);
        }
        model.setBody(paymentBody);                                     // 订单描述
        
        model.setTimeoutExpress("15m");                                 // 该笔订单允许的最晚付款时间
        model.setPassbackParams("rental_payment");                     // 回传参数
        
        // 设置商品明细信息（可选）
        // model.setGoodsDetail(); // 如果需要详细商品信息可以设置
        
        // 设置扩展信息（可选）
        // model.setExtendParams(); // 如果需要业务扩展参数可以设置
        
        log.debug("支付宝请求参数构建完成，商户订单号：{}，金额：{}，标题：{}", 
                 model.getOutTradeNo(), model.getTotalAmount(), model.getSubject());
        
        return model;
    }

    /**
     * 验证支付参数
     */
    private void validatePaymentParams(Payment payment, PaymentCreateRequest request) {
        if (payment == null) {
            throw new IllegalArgumentException("支付对象不能为空");
        }
        
        if (!StringUtils.hasText(payment.getPaymentNo())) {
            throw new IllegalArgumentException("支付单号不能为空");
        }
        
        // 支付宝要求商户订单号长度不能超过64位
        if (payment.getPaymentNo().length() > 64) {
            throw new IllegalArgumentException("支付单号长度不能超过64位");
        }
        
        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("支付金额必须大于0");
        }
        
        // 支付宝单笔交易限额检查（这里设置为100万，可根据实际业务调整）
        if (payment.getAmount().compareTo(new BigDecimal("1000000")) > 0) {
            throw new IllegalArgumentException("支付金额超出限制");
        }
        
        if (payment.getOrder() == null) {
            throw new IllegalArgumentException("订单信息不能为空");
        }
        
        log.debug("支付参数验证通过，支付单号：{}，金额：{}", payment.getPaymentNo(), payment.getAmount());
    }

    /**
     * 格式化金额 - 支付宝要求金额格式为两位小数
     */
    private String formatAmount(BigDecimal amount) {
        return amount.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 清理文本内容，确保符合支付宝规范
     */
    private String sanitizeText(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        
        // 移除特殊字符，保留中文、英文、数字、常用标点
        String sanitized = text.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9\\s,，.。:：;；!！?？()（）\\-_]", "");
        
        // 限制长度
        if (sanitized.length() > maxLength) {
            sanitized = sanitized.substring(0, maxLength);
        }
        
        return sanitized.trim();
    }
}
