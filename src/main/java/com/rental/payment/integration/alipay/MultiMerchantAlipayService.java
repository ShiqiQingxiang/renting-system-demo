package com.rental.payment.integration.alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
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
import com.rental.payment.model.MerchantPaymentConfig;
import com.rental.payment.model.Payment;
import com.rental.payment.service.MerchantPaymentConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class MultiMerchantAlipayService {

    private final MerchantPaymentConfigService merchantConfigService;
    
    // 缓存支付宝客户端，避免重复创建
    private final Map<Long, AlipayClient> clientCache = new ConcurrentHashMap<>();

    /**
     * 为商家创建支付宝支付
     */
    public PaymentResponse createPayment(Long merchantId, Payment payment, PaymentCreateRequest request) {
        log.info("为商家创建支付宝支付，商家ID：{}，支付单号：{}", merchantId, payment.getPaymentNo());

        MerchantPaymentConfig config = getMerchantConfig(merchantId);
        AlipayClient alipayClient = getOrCreateAlipayClient(merchantId, config);

        PaymentResponse response = new PaymentResponse();

        try {
            // 创建支付请求
            AlipayTradePagePayRequest pagePayRequest = new AlipayTradePagePayRequest();
            
            // 设置商家专属的回调地址
            pagePayRequest.setNotifyUrl(config.getNotifyUrlWithMerchant());
            pagePayRequest.setReturnUrl(config.getReturnUrlWithMerchant());

            // 构建请求参数
            AlipayTradePagePayModel model = buildPaymentModel(payment, request, config);
            pagePayRequest.setBizModel(model);

            // 调用支付宝API
            log.info("调用支付宝支付接口，商家ID：{}，商户订单号：{}", merchantId, payment.getPaymentNo());
            AlipayTradePagePayResponse pagePayResponse = alipayClient.pageExecute(pagePayRequest);

            if (pagePayResponse.isSuccess()) {
                response.setPaymentId(payment.getId());
                response.setPaymentNo(payment.getPaymentNo());
                response.setPaymentForm(pagePayResponse.getBody());
                response.setPaymentUrl(pagePayResponse.getBody());
                response.setNeedRedirect(true);
                response.setStatus("PENDING");
                response.setExpireTime(900); // 15分钟过期
                response.setThirdPartyOrderNo(payment.getPaymentNo());
                response.setMerchantId(merchantId); // 添加商家ID
                
                log.info("商家支付宝支付创建成功，商家ID：{}，支付单号：{}", merchantId, payment.getPaymentNo());
            } else {
                log.error("商家支付宝支付创建失败，商家ID：{}，支付单号：{}，错误：{}-{}", 
                         merchantId, payment.getPaymentNo(), pagePayResponse.getCode(), pagePayResponse.getMsg());
                         
                response.setStatus("FAILED");
                response.setErrorMessage(String.format("支付创建失败[%s]: %s", 
                                                     pagePayResponse.getCode(), pagePayResponse.getMsg()));
                throw new BusinessException(response.getErrorMessage());
            }

        } catch (AlipayApiException e) {
            log.error("商家支付宝API调用异常，商家ID：{}，支付单号：{}", merchantId, payment.getPaymentNo(), e);
            response.setStatus("FAILED");
            response.setErrorMessage(String.format("支付宝API调用异常[%s]: %s", e.getErrCode(), e.getErrMsg()));
            throw new BusinessException(response.getErrorMessage());
        } catch (Exception e) {
            log.error("创建商家支付宝支付异常，商家ID：{}，支付单号：{}", merchantId, payment.getPaymentNo(), e);
            response.setStatus("FAILED");
            response.setErrorMessage("创建支付失败：" + e.getMessage());
            throw new BusinessException(response.getErrorMessage());
        }

        return response;
    }

    /**
     * 商家支付宝退款
     */
    public boolean refund(Long merchantId, Payment originalPayment, Payment refundPayment, 
                         BigDecimal refundAmount, String refundReason) {
        log.info("处理商家支付宝退款，商家ID：{}，原支付单号：{}，退款单号：{}，退款金额：{}",
                merchantId, originalPayment.getPaymentNo(), refundPayment.getPaymentNo(), refundAmount);

        MerchantPaymentConfig config = getMerchantConfig(merchantId);
        AlipayClient alipayClient = getOrCreateAlipayClient(merchantId, config);

        try {
            AlipayTradeRefundRequest alipayRequest = new AlipayTradeRefundRequest();
            
            AlipayTradeRefundModel model = new AlipayTradeRefundModel();
            model.setOutTradeNo(originalPayment.getPaymentNo());
            model.setRefundAmount(refundAmount.toString());
            model.setOutRequestNo(refundPayment.getPaymentNo());
            model.setRefundReason(StringUtils.hasText(refundReason) ? refundReason : "订单退款");
            
            alipayRequest.setBizModel(model);

            AlipayTradeRefundResponse alipayResponse = alipayClient.execute(alipayRequest);
            
            if (alipayResponse.isSuccess()) {
                log.info("商家支付宝退款成功，商家ID：{}，退款单号：{}", merchantId, refundPayment.getPaymentNo());
                return true;
            } else {
                log.error("商家支付宝退款失败，商家ID：{}，退款单号：{}，错误：{}", 
                         merchantId, refundPayment.getPaymentNo(), alipayResponse.getSubMsg());
                return false;
            }

        } catch (AlipayApiException e) {
            log.error("商家支付宝退款API调用失败，商家ID：{}，退款单号：{}", merchantId, refundPayment.getPaymentNo(), e);
            return false;
        } catch (Exception e) {
            log.error("商家支付宝退款异常，商家ID：{}，退款单号：{}", merchantId, refundPayment.getPaymentNo(), e);
            return false;
        }
    }

    /**
     * 查询商家支付状态
     */
    public String queryPaymentStatus(Long merchantId, Payment payment) {
        log.info("查询商家支付宝支付状态，商家ID：{}，支付单号：{}", merchantId, payment.getPaymentNo());

        MerchantPaymentConfig config = getMerchantConfig(merchantId);
        AlipayClient alipayClient = getOrCreateAlipayClient(merchantId, config);

        try {
            AlipayTradeQueryRequest alipayRequest = new AlipayTradeQueryRequest();
            
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(payment.getPaymentNo());
            
            alipayRequest.setBizModel(model);

            AlipayTradeQueryResponse alipayResponse = alipayClient.execute(alipayRequest);
            
            if (alipayResponse.isSuccess()) {
                String tradeStatus = alipayResponse.getTradeStatus();
                log.info("商家支付宝支付状态查询成功，商家ID：{}，支付单号：{}，状态：{}", 
                        merchantId, payment.getPaymentNo(), tradeStatus);
                return convertAlipayStatusToSystemStatus(tradeStatus);
            } else {
                log.warn("商家支付宝支付状态查询失败，商家ID：{}，支付单号：{}，错误：{}", 
                        merchantId, payment.getPaymentNo(), alipayResponse.getSubMsg());
                return payment.getStatus().name();
            }

        } catch (AlipayApiException e) {
            log.error("查询商家支付宝支付状态API调用失败，商家ID：{}，支付单号：{}", merchantId, payment.getPaymentNo(), e);
            return "UNKNOWN";
        } catch (Exception e) {
            log.error("查询商家支付宝支付状态异常，商家ID：{}，支付单号：{}", merchantId, payment.getPaymentNo(), e);
            return "UNKNOWN";
        }
    }

    /**
     * 验证商家支付宝回调签名
     */
    public boolean verifyNotifySign(Long merchantId, Map<String, String> params) {
        log.debug("验证商家支付宝回调签名，商家ID：{}", merchantId);
        
        try {
            MerchantPaymentConfig config = getMerchantConfig(merchantId);
            
            return AlipaySignature.rsaCheckV1(
                params, 
                config.getAlipayPublicKey(), 
                "UTF-8", 
                "RSA2"
            );
        } catch (AlipayApiException e) {
            log.error("商家支付宝回调签名验证失败，商家ID：{}", merchantId, e);
            return false;
        } catch (Exception e) {
            log.error("验证商家支付宝回调签名异常，商家ID：{}", merchantId, e);
            return false;
        }
    }

    /**
     * 获取或创建支付宝客户端
     */
    private AlipayClient getOrCreateAlipayClient(Long merchantId, MerchantPaymentConfig config) {
        // 先从缓存获取
        AlipayClient cachedClient = clientCache.get(merchantId);
        if (cachedClient != null) {
            return cachedClient;
        }

        // 创建新的客户端
        synchronized (this) {
            // 双重检查
            cachedClient = clientCache.get(merchantId);
            if (cachedClient != null) {
                return cachedClient;
            }

            log.info("创建商家支付宝客户端，商家ID：{}，应用ID：{}", merchantId, config.getAlipayAppId());
            
            AlipayClient newClient = new DefaultAlipayClient(
                determineGatewayUrl(config.getAlipayAppId()),
                config.getAlipayAppId(),
                config.getEncryptedPrivateKey(), // 这里应该是已解密的私钥
                "json",
                "UTF-8",
                config.getAlipayPublicKey(),
                "RSA2"
            );
            
            clientCache.put(merchantId, newClient);
            log.info("商家支付宝客户端创建成功，商家ID：{}", merchantId);
            
            return newClient;
        }
    }

    /**
     * 获取商家配置
     */
    private MerchantPaymentConfig getMerchantConfig(Long merchantId) {
        MerchantPaymentConfig config = merchantConfigService.getActiveMerchantConfig(merchantId);
        if (config == null) {
            throw new BusinessException("商家支付配置不存在或未启用，商家ID：" + merchantId);
        }
        return config;
    }

    /**
     * 构建支付宝支付请求参数
     */
    private AlipayTradePagePayModel buildPaymentModel(Payment payment, PaymentCreateRequest request, 
                                                     MerchantPaymentConfig config) {
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        
        model.setOutTradeNo(payment.getPaymentNo());
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        model.setTotalAmount(formatAmount(payment.getAmount()));
        model.setSubject(buildPaymentSubject(payment));
        model.setBody(buildPaymentBody(payment, config));
        model.setTimeoutExpress("15m");
        model.setPassbackParams("merchant_" + config.getMerchantId()); // 添加商家标识
        
        return model;
    }

    /**
     * 构建支付主题
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
        
        if (subject.length() > 256) {
            subject = subject.substring(0, 256);
        }
        
        return subject;
    }

    /**
     * 构建支付描述
     */
    private String buildPaymentBody(Payment payment, MerchantPaymentConfig config) {
        StringBuilder body = new StringBuilder();
        body.append("商家ID: ").append(config.getMerchantId());
        body.append(", 订单编号: ").append(payment.getOrder().getOrderNo());
        body.append(", 支付类型: ").append(buildPaymentSubject(payment));
        body.append(", 支付金额: ").append(payment.getAmount()).append("元");
        
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
     * 格式化金额
     */
    private String formatAmount(BigDecimal amount) {
        return amount.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 确定支付宝网关地址
     */
    private String determineGatewayUrl(String appId) {
        // 这里可以根据应用ID或配置决定使用正式环境还是沙箱环境
        // 沙箱环境的应用ID通常有特定格式，这里简化处理
        if (appId.startsWith("2016") || appId.startsWith("2021")) {
            return "https://openapi.alipay.com/gateway.do"; // 正式环境
        } else {
            return "https://openapi.alipaydev.com/gateway.do"; // 沙箱环境
        }
    }

    /**
     * 清理缓存的客户端（商家配置更新时调用）
     */
    public void clearClientCache(Long merchantId) {
        log.info("清理商家支付宝客户端缓存，商家ID：{}", merchantId);
        clientCache.remove(merchantId);
    }

    /**
     * 清理所有缓存
     */
    public void clearAllClientCache() {
        log.info("清理所有商家支付宝客户端缓存");
        clientCache.clear();
    }
}