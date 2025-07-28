package com.rental.payment.service.impl;

import com.rental.common.exception.BusinessException;
import com.rental.common.exception.ResourceNotFoundException;
import com.rental.order.model.Order;
import com.rental.order.repository.OrderRepository;
import com.rental.payment.DTO.*;
import com.rental.payment.integration.alipay.AlipayService;
import com.rental.payment.model.Payment;
import com.rental.payment.model.PaymentRecord;
import com.rental.payment.repository.PaymentRepository;
import com.rental.payment.repository.PaymentRecordRepository;
import com.rental.payment.service.PaymentService;
import com.rental.user.model.User;
import com.rental.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AlipayService alipayService;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentCreateRequest request, Long userId) {
        log.info("创建支付，用户ID：{}, 订单ID：{}", userId, request.getOrderId());

        // 验证用户
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 验证订单
        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));

        // 验证订单归属
        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException("无权限支付此订单");
        }

        // 验证订单状态
        if (order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new BusinessException("只有已确认状态的订单才能支付");
        }

        // 验证支付金额
        validatePaymentAmount(order, request);

        // 检查是否已有待支付的支付记录
        List<Payment> existingPayments = paymentRepository.findByOrderAndStatus(order, Payment.PaymentStatus.PENDING);
        if (!existingPayments.isEmpty()) {
            throw new BusinessException("该订单已有待支付的支付记录");
        }

        // 创建支付记录
        Payment payment = new Payment();
        payment.setPaymentNo(generatePaymentNo());
        payment.setOrder(order);
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentType(request.getPaymentType());
        payment.setStatus(Payment.PaymentStatus.PENDING);

        Payment savedPayment = paymentRepository.save(payment);

        // 创建支付记录
        createPaymentRecord(savedPayment, Payment.PaymentStatus.PENDING, "支付创建", null);

        // 调用第三方支付
        PaymentResponse response = processThirdPartyPayment(savedPayment, request);
        response.setPaymentId(savedPayment.getId());
        response.setPaymentNo(savedPayment.getPaymentNo());

        log.info("支付创建成功，支付单号：{}", savedPayment.getPaymentNo());
        return response;
    }

    @Override
    @Transactional
    public void handlePaymentCallback(PaymentCallbackRequest request) {
        log.info("处理支付回调，支付单号：{}", request.getPaymentNo());

        Payment payment = paymentRepository.findByPaymentNo(request.getPaymentNo())
            .orElseThrow(() -> new ResourceNotFoundException("支付记录不存在"));

        // 验证回调签名（根据实际第三方支付平台实现）
        if (!validateCallbackSign(request)) {
            log.warn("支付回调签名验证失败，支付单号：{}", request.getPaymentNo());
            return;
        }

        Payment.PaymentStatus newStatus = parsePaymentStatus(request.getStatus());
        Payment.PaymentStatus oldStatus = payment.getStatus();

        // 更新支付状态
        payment.setStatus(newStatus);
        payment.setThirdPartyTransactionId(request.getThirdPartyTransactionId());
        paymentRepository.save(payment);

        // 创建支付记录
        String responseData = convertToJson(request.getRawData());
        createPaymentRecord(payment, newStatus, responseData, request.getErrorMessage());

        // 如果支付成功，更新订单状态
        if (newStatus == Payment.PaymentStatus.SUCCESS && oldStatus != Payment.PaymentStatus.SUCCESS) {
            updateOrderAfterPayment(payment);
        }

        log.info("支付回调处理完成，支付单号：{}, 状态：{}", request.getPaymentNo(), newStatus);
    }

    @Override
    @Transactional
    public void handleAlipayCallback(PaymentCallbackRequest request) {
        log.info("处理支付宝回调，支付单号：{}", request.getPaymentNo());

        // 验证支付宝签名
        if (!verifyAlipaySign(request)) {
            log.warn("支付宝回调签名验证失败，支付单号：{}", request.getPaymentNo());
            return;
        }

        // 直接处理业务逻辑，跳过重复的签名验证
        handlePaymentCallbackWithoutSignVerification(request);
    }

    /**
     * 处理支付回调
     */
    @Transactional
    private void handlePaymentCallbackWithoutSignVerification(PaymentCallbackRequest request) {
        log.info("处理支付回调业务逻辑，支付单号：{}", request.getPaymentNo());

        Payment payment = paymentRepository.findByPaymentNo(request.getPaymentNo())
            .orElseThrow(() -> new ResourceNotFoundException("支付记录不存在"));

        Payment.PaymentStatus newStatus = parsePaymentStatus(request.getStatus());
        Payment.PaymentStatus oldStatus = payment.getStatus();

        // 防止重复处理
        if (oldStatus == newStatus && newStatus == Payment.PaymentStatus.SUCCESS) {
            log.info("支付状态未变化且已成功，跳过处理，支付单号：{}", request.getPaymentNo());
            return;
        }

        // 更新支付状态
        payment.setStatus(newStatus);
        payment.setThirdPartyTransactionId(request.getThirdPartyTransactionId());
        paymentRepository.save(payment);

        // 创建支付记录
        String responseData = convertToJson(request.getRawData());
        createPaymentRecord(payment, newStatus, responseData, request.getErrorMessage());

        // 如果支付成功，更新订单状态
        if (newStatus == Payment.PaymentStatus.SUCCESS && oldStatus != Payment.PaymentStatus.SUCCESS) {
            updateOrderAfterPayment(payment);
        }

        log.info("支付回调处理完成，支付单号：{}, 状态：{} -> {}", request.getPaymentNo(), oldStatus, newStatus);
    }

    @Override
    @Transactional
    public PaymentDto processRefund(RefundRequest request, Long operatorId) {
        log.info("处理退款，支付ID：{}, 操作员ID：{}", request.getPaymentId(), operatorId);

        Payment originalPayment = paymentRepository.findById(request.getPaymentId())
            .orElseThrow(() -> new ResourceNotFoundException("支付记录不存在"));

        // 验证支付状态
        if (originalPayment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new BusinessException("只有支付成功的记录才能退款");
        }

        // 验证退款金额
        if (request.getRefundAmount().compareTo(originalPayment.getAmount()) > 0) {
            throw new BusinessException("退款金额不能超过原支付金额");
        }

        // 创建退款记录
        Payment refundPayment = new Payment();
        refundPayment.setPaymentNo(generatePaymentNo());
        refundPayment.setOrder(originalPayment.getOrder());
        refundPayment.setAmount(request.getRefundAmount());
        refundPayment.setPaymentMethod(originalPayment.getPaymentMethod());
        refundPayment.setPaymentType(Payment.PaymentType.REFUND);
        refundPayment.setStatus(Payment.PaymentStatus.PENDING);

        Payment savedRefund = paymentRepository.save(refundPayment);

        // 调用第三方退款接口
        boolean refundResult = processThirdPartyRefund(originalPayment, savedRefund, request);

        if (refundResult) {
            savedRefund.setStatus(Payment.PaymentStatus.SUCCESS);
            createPaymentRecord(savedRefund, Payment.PaymentStatus.SUCCESS, "退款成功", null);
        } else {
            savedRefund.setStatus(Payment.PaymentStatus.FAILED);
            createPaymentRecord(savedRefund, Payment.PaymentStatus.FAILED, "退款失败", "第三方退款接口调用失败");
        }

        paymentRepository.save(savedRefund);

        log.info("退款处理完成，退款单号：{}, 结果：{}", savedRefund.getPaymentNo(), refundResult ? "成功" : "失败");
        return convertToDto(savedRefund);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDto getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("支付记录不存在"));
        return convertToDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDto getPaymentByPaymentNo(String paymentNo) {
        Payment payment = paymentRepository.findByPaymentNo(paymentNo)
            .orElseThrow(() -> new ResourceNotFoundException("支付记录不存在"));
        return convertToDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDto> searchPayments(PaymentSearchRequest request, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findBySearchCriteria(
            request.getPaymentNo(),
            request.getOrderId(),
            request.getOrderNo(),
            request.getUserId(),
            request.getUsername(),
            request.getPaymentMethod(),
            request.getPaymentType(),
            request.getStatus(),
            request.getMinAmount(),
            request.getMaxAmount(),
            request.getCreatedDateFrom() != null ? request.getCreatedDateFrom().toString() : null,
            request.getCreatedDateTo() != null ? request.getCreatedDateTo().toString() : null,
            request.getThirdPartyTransactionId(),
            pageable
        );

        return payments.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDto> getUserPayments(Long userId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByOrderUserIdOrderByCreatedAtDesc(userId, pageable);
        return payments.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDto> getOrderPayments(Long orderId) {
        List<Payment> payments = paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
        return payments.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentDto queryPaymentStatus(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("支付记录不存在"));

        // 如果是待支付状态，查询第三方支付状态
        if (payment.getStatus() == Payment.PaymentStatus.PENDING) {
            Payment.PaymentStatus thirdPartyStatus = queryThirdPartyPaymentStatus(payment);
            if (thirdPartyStatus != payment.getStatus()) {
                payment.setStatus(thirdPartyStatus);
                paymentRepository.save(payment);
                createPaymentRecord(payment, thirdPartyStatus, "状态查询更新", null);

                if (thirdPartyStatus == Payment.PaymentStatus.SUCCESS) {
                    updateOrderAfterPayment(payment);
                }
            }
        }

        return convertToDto(payment);
    }

    @Override
    @Transactional
    public PaymentDto cancelPayment(Long paymentId, Long operatorId) {
        log.info("取消支付，支付ID：{}, 操作员ID：{}", paymentId, operatorId);

        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("支付记录不存在"));

        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            throw new BusinessException("只有待支付状态的记录才能取消");
        }

        payment.setStatus(Payment.PaymentStatus.CANCELLED);
        Payment savedPayment = paymentRepository.save(payment);

        createPaymentRecord(payment, Payment.PaymentStatus.CANCELLED, "支付取消", null);

        log.info("支付取消成功，支付单号：{}", payment.getPaymentNo());
        return convertToDto(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentRecordDto> getPaymentRecords(Long paymentId) {
        List<PaymentRecord> records = paymentRecordRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId);
        return records.stream().map(this::convertRecordToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentStatistics getPaymentStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        long totalPayments = paymentRepository.countByCreatedAtBetween(startDateTime, endDateTime);
        long successPayments = paymentRepository.countByStatusAndCreatedAtBetween(
            Payment.PaymentStatus.SUCCESS, startDateTime, endDateTime);
        long failedPayments = paymentRepository.countByStatusAndCreatedAtBetween(
            Payment.PaymentStatus.FAILED, startDateTime, endDateTime);
        long pendingPayments = paymentRepository.countByStatusAndCreatedAtBetween(
            Payment.PaymentStatus.PENDING, startDateTime, endDateTime);

        BigDecimal totalAmount = paymentRepository.sumAmountByCreatedAtBetween(startDateTime, endDateTime);
        BigDecimal successAmount = paymentRepository.sumAmountByStatusAndCreatedAtBetween(
            Payment.PaymentStatus.SUCCESS, startDateTime, endDateTime);

        long refundCount = paymentRepository.countByPaymentTypeAndCreatedAtBetween(
            Payment.PaymentType.REFUND, startDateTime, endDateTime);
        BigDecimal refundAmount = paymentRepository.sumAmountByPaymentTypeAndCreatedAtBetween(
            Payment.PaymentType.REFUND, startDateTime, endDateTime);

        double successRate = totalPayments > 0 ?
            (double) successPayments / totalPayments * 100 : 0.0;

        return new PaymentStatistics(
            totalPayments, successPayments, failedPayments, pendingPayments,
            totalAmount != null ? totalAmount : BigDecimal.ZERO,
            successAmount != null ? successAmount : BigDecimal.ZERO,
            refundCount,
            refundAmount != null ? refundAmount : BigDecimal.ZERO,
            BigDecimal.valueOf(successRate).setScale(2, RoundingMode.HALF_UP).doubleValue()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateUserPaymentTotal(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        BigDecimal total = paymentRepository.sumAmountByUserIdAndCreatedAtBetween(userId, startDateTime, endDateTime);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public long countPaymentsByStatus(Payment.PaymentStatus status) {
        if (status == null) {
            return paymentRepository.count();
        }
        return paymentRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean paymentExists(String paymentNo) {
        return paymentRepository.existsByPaymentNo(paymentNo);
    }

    @Override
    public String generatePaymentNo() {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 使用时间戳和随机数确保唯一性
        long timestamp = System.currentTimeMillis();
        int random = (int)(Math.random() * 1000);

        return String.format("PAY%s%d%03d", dateStr, timestamp % 100000, random);
    }

    // 私有辅助方法

    private void validatePaymentAmount(Order order, PaymentCreateRequest request) {
        BigDecimal expectedAmount = BigDecimal.ZERO;

        if (request.getPaymentType() == Payment.PaymentType.RENTAL) {
            expectedAmount = order.getTotalAmount();
        } else if (request.getPaymentType() == Payment.PaymentType.DEPOSIT) {
            expectedAmount = order.getDepositAmount();
        }

        if (request.getAmount().compareTo(expectedAmount) != 0) {
            throw new BusinessException("支付金额与订单金额不匹配");
        }
    }

    private PaymentResponse processThirdPartyPayment(Payment payment, PaymentCreateRequest request) {
        // 只支持支付宝支付，删除其他模拟的支付方式
        if (payment.getPaymentMethod() != Payment.PaymentMethod.ALIPAY) {
            throw new BusinessException("目前只支持支付宝支付");
        }

        // 使用真正的支付宝服务
        return alipayService.createPayment(payment, request);
    }

    private boolean validateCallbackSign(PaymentCallbackRequest request) {
        // 根据支付方式验证签名，目前只支持支付宝
        if (request.getRawData() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rawParams = (Map<String, Object>) request.getRawData();
            Map<String, String> params = new HashMap<>();
            for (Map.Entry<String, Object> entry : rawParams.entrySet()) {
                params.put(entry.getKey(), entry.getValue() == null ? null : entry.getValue().toString());
            }
            return alipayService.verifyNotifySign(params);
        }
        log.warn("回调数据格式错误，无法验证签名");
        return false;
    }

    private Payment.PaymentStatus parsePaymentStatus(String status) {
        switch (status.toUpperCase()) {
            case "SUCCESS":
            case "TRADE_SUCCESS":
            case "PAID":
                return Payment.PaymentStatus.SUCCESS;
            case "FAILED":
            case "TRADE_CLOSED":
                return Payment.PaymentStatus.FAILED;
            case "CANCELLED":
            case "TRADE_CANCELLED":
                return Payment.PaymentStatus.CANCELLED;
            default:
                return Payment.PaymentStatus.PENDING;
        }
    }

    private void updateOrderAfterPayment(Payment payment) {
        Order order = payment.getOrder();

        if (payment.getPaymentType() == Payment.PaymentType.RENTAL) {
            order.setStatus(Order.OrderStatus.PAID);
            orderRepository.save(order);
            log.info("订单支付成功，订单号：{}", order.getOrderNo());
        }
    }

    private boolean processThirdPartyRefund(Payment originalPayment, Payment refundPayment, RefundRequest request) {
        // 只支持支付宝退款，删除其他模拟的退款方式
        if (originalPayment.getPaymentMethod() != Payment.PaymentMethod.ALIPAY) {
            throw new BusinessException("目前只支持支付宝退款");
        }

        // 使用真正的支付宝退款服务
        return alipayService.refund(originalPayment, refundPayment,
                                  request.getRefundAmount(), request.getRefundReason());
    }

    private Payment.PaymentStatus queryThirdPartyPaymentStatus(Payment payment) {
        // 只支持支付宝状态查询
        if (payment.getPaymentMethod() != Payment.PaymentMethod.ALIPAY) {
            throw new BusinessException("目前只支持支付宝支付状态查询");
        }

        // 调用支付宝真实的状态查询 API
        String statusStr = alipayService.queryPaymentStatus(payment);
        return parsePaymentStatus(statusStr);
    }

    private void createPaymentRecord(Payment payment, Payment.PaymentStatus status, String responseData, String errorMessage) {
        PaymentRecord record = new PaymentRecord();
        record.setPayment(payment);
        record.setStatus(status);

        // 确保responseData是有效的JSON格式，如果不是则转换为JSON
        if (responseData != null && !responseData.trim().isEmpty()) {
            // 检查是否已经是JSON格式
            if (isValidJson(responseData)) {
                record.setResponseData(responseData);
            } else {
                // 如果不是JSON格式，将其包装为JSON对象
                record.setResponseData("{\"message\":\"" + escapeJsonString(responseData) + "\"}");
            }
        } else {
            // 如果为空，设置为null（数据库中JSON字段可以为null）
            record.setResponseData(null);
        }

        record.setErrorMessage(errorMessage);
        paymentRecordRepository.save(record);
    }

    private boolean isValidJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return false;
        }

        String trimmed = jsonString.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
               (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }

    private String escapeJsonString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }

    private String convertToJson(Object data) {
        if (data == null) {
            return null; // 返回null而不是字符串"null"
        }

        // 如果是Map或集合类型，转换为实际的JSON字符串
        if (data instanceof java.util.Map || data instanceof java.util.Collection) {
            try {
                // 使用Jackson或类似的JSON库转换
                // 这里简单处理，实际项目中应使用ObjectMapper
                return data.toString();
            } catch (Exception e) {
                log.warn("转换JSON失败", e);
                return "{}";
            }
        }

        return data.toString();
    }

    private PaymentDto convertToDto(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setPaymentNo(payment.getPaymentNo());
        dto.setOrderId(payment.getOrder().getId());
        dto.setOrderNo(payment.getOrder().getOrderNo());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaymentMethodDesc(payment.getPaymentMethod().getDescription());
        dto.setPaymentType(payment.getPaymentType());
        dto.setPaymentTypeDesc(payment.getPaymentType().getDescription());
        dto.setStatus(payment.getStatus());
        dto.setStatusDesc(payment.getStatus().getDescription());
        dto.setThirdPartyTransactionId(payment.getThirdPartyTransactionId());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());

        if (payment.getOrder().getUser() != null) {
            dto.setUserId(payment.getOrder().getUser().getId());
            dto.setUsername(payment.getOrder().getUser().getUsername());
        }

        return dto;
    }

    private PaymentRecordDto convertRecordToDto(PaymentRecord record) {
        PaymentRecordDto dto = new PaymentRecordDto();
        dto.setId(record.getId());
        dto.setPaymentId(record.getPayment().getId());
        dto.setPaymentNo(record.getPayment().getPaymentNo());
        dto.setStatus(record.getStatus());
        dto.setStatusDesc(record.getStatus().getDescription());
        dto.setResponseData(record.getResponseData());
        dto.setErrorMessage(record.getErrorMessage());
        dto.setCreatedAt(record.getCreatedAt());
        return dto;
    }

    private boolean verifyAlipaySign(PaymentCallbackRequest request) {
        try {
            // 使用支付宝服务验证签名
            if (request.getRawData() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> rawParams = (Map<String, Object>) request.getRawData();
                Map<String, String> params = new HashMap<>();
                for (Map.Entry<String, Object> entry : rawParams.entrySet()) {
                    params.put(entry.getKey(), entry.getValue() == null ? null : entry.getValue().toString());
                }
                return alipayService.verifyNotifySign(params);
            }
            log.warn("支付宝回调数据格式错误，无法验证签名");
            return false;
        } catch (Exception e) {
            log.error("支付宝签名验证异常", e);
            return false;
        }
    }
}
