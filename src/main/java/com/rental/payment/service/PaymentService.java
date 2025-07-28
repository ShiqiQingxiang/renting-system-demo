package com.rental.payment.service;

import com.rental.payment.DTO.*;
import com.rental.payment.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PaymentService {

    /**
     * 创建支付
     */
    PaymentResponse createPayment(PaymentCreateRequest request, Long userId);

    /**
     * 处理支付回调
     */
    void handlePaymentCallback(PaymentCallbackRequest request);

    /**
     * 处理支付宝回调
     */
    void handleAlipayCallback(PaymentCallbackRequest request);

    /**
     * 处理退款
     */
    PaymentDto processRefund(RefundRequest request, Long operatorId);

    /**
     * 根据ID获取支付信息
     */
    PaymentDto getPaymentById(Long paymentId);

    /**
     * 根据支付单号获取支付信息
     */
    PaymentDto getPaymentByPaymentNo(String paymentNo);

    /**
     * 搜索支付记录
     */
    Page<PaymentDto> searchPayments(PaymentSearchRequest request, Pageable pageable);

    /**
     * 获取用户的支付记录
     */
    Page<PaymentDto> getUserPayments(Long userId, Pageable pageable);

    /**
     * 获取订单的支付记录
     */
    List<PaymentDto> getOrderPayments(Long orderId);

    /**
     * 查询支付状态
     */
    PaymentDto queryPaymentStatus(Long paymentId);

    /**
     * 取消支付
     */
    PaymentDto cancelPayment(Long paymentId, Long operatorId);

    /**
     * 获取支付记录
     */
    List<PaymentRecordDto> getPaymentRecords(Long paymentId);

    /**
     * 获取支付统计信息
     */
    PaymentStatistics getPaymentStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 计算用户支付总额
     */
    BigDecimal calculateUserPaymentTotal(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 统计支付数量
     */
    long countPaymentsByStatus(Payment.PaymentStatus status);

    /**
     * 检查支付是否存在
     */
    boolean paymentExists(String paymentNo);

    /**
     * 生成支付单号
     */
    String generatePaymentNo();
}
