package com.rental.payment.controller;

import com.rental.common.response.ApiResponse;
import com.rental.payment.DTO.*;
import com.rental.payment.model.Payment;
import com.rental.payment.service.PaymentService;
import com.rental.security.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "支付管理", description = "支付管理相关接口，包括支付的创建、查询、回调处理、退款等功能")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAuthority('PAYMENT_CREATE')")
    @Operation(
        summary = "创建支付",
        description = "为订单创建支付记录并调用第三方支付接口"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "201",
        description = "支付创建成功",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "创建支付成功示例",
                value = """
                {
                    "success": true,
                    "message": "支付创建成功",
                    "data": {
                        "paymentId": 1,
                        "paymentNo": "PAY20250716000001",
                        "paymentUrl": "https://qr.alipay.com/bax03055hjkbzgw0qnhz008a",
                        "needRedirect": true,
                        "status": "PENDING",
                        "expireTime": 900
                    }
                }
                """
            )
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数错误")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "权限不足")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "支付创建请求信息",
                content = @Content(
                    schema = @Schema(implementation = PaymentCreateRequest.class),
                    examples = @ExampleObject(
                        name = "创建支付请求示例",
                        value = """
                        {
                            "orderId": 1,
                            "amount": 100.00,
                            "paymentMethod": "ALIPAY",
                            "paymentType": "RENTAL",
                            "clientIp": "192.168.1.1",
                            "notifyUrl": "https://example.com/callback",
                            "returnUrl": "https://example.com/return",
                            "remark": "租赁订单支付"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody PaymentCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        PaymentResponse result = paymentService.createPayment(request, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("支付创建成功", result));
    }

    @PostMapping("/callback")
    @Operation(summary = "支付回调", description = "第三方支付平台回调接口")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "回调处理成功")
    public ResponseEntity<String> paymentCallback(@RequestBody PaymentCallbackRequest request) {
        paymentService.handlePaymentCallback(request);
        return ResponseEntity.ok("success");
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAuthority('PAYMENT_REFUND')")
    @Operation(summary = "处理退款", description = "为支付记录处理退款")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "退款处理完成")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "退款条件不满足")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "支付记录不存在")
    public ResponseEntity<ApiResponse<PaymentDto>> processRefund(
            @Parameter(description = "支付ID", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "退款请求信息",
                content = @Content(
                    schema = @Schema(implementation = RefundRequest.class),
                    examples = @ExampleObject(
                        value = """
                        {
                            "paymentId": 1,
                            "refundAmount": 50.00,
                            "refundReason": "用户申请退款",
                            "remark": "提前归还物品"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody RefundRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        request.setPaymentId(id); // 确保路径参数和请求体中的ID一致
        PaymentDto result = paymentService.processRefund(request, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("退款处理完成", result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PAYMENT_VIEW')")
    @Operation(summary = "获取支付详情", description = "根据ID获取支付详细信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "支付记录不存在")
    public ResponseEntity<ApiResponse<PaymentDto>> getPayment(
            @Parameter(description = "支付ID", example = "1") @PathVariable Long id) {
        PaymentDto result = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/payment-no/{paymentNo}")
    @PreAuthorize("hasAuthority('PAYMENT_VIEW')")
    @Operation(summary = "根据支付单号获取支付", description = "根据支付单号获取支付详细信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "支付记录不存在")
    public ResponseEntity<ApiResponse<PaymentDto>> getPaymentByPaymentNo(
            @Parameter(description = "支付单号", example = "PAY20250716000001") @PathVariable String paymentNo) {
        PaymentDto result = paymentService.getPaymentByPaymentNo(paymentNo);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PAYMENT_VIEW')")
    @Operation(summary = "搜索支付记录", description = "根据条件搜索支付记录")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "搜索成功")
    public ResponseEntity<ApiResponse<Page<PaymentDto>>> searchPayments(
            @Parameter(description = "支付单号") @RequestParam(required = false) String paymentNo,
            @Parameter(description = "订单ID") @RequestParam(required = false) Long orderId,
            @Parameter(description = "订单号") @RequestParam(required = false) String orderNo,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "支付方式") @RequestParam(required = false) Payment.PaymentMethod paymentMethod,
            @Parameter(description = "支付类型") @RequestParam(required = false) Payment.PaymentType paymentType,
            @Parameter(description = "支付状态") @RequestParam(required = false) Payment.PaymentStatus status,
            @Parameter(description = "最小金额") @RequestParam(required = false) BigDecimal minAmount,
            @Parameter(description = "最大金额") @RequestParam(required = false) BigDecimal maxAmount,
            @Parameter(description = "创建日期起") @RequestParam(required = false) LocalDate createdDateFrom,
            @Parameter(description = "创建日期止") @RequestParam(required = false) LocalDate createdDateTo,
            @Parameter(description = "第三方交易ID") @RequestParam(required = false) String thirdPartyTransactionId,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        PaymentSearchRequest request = new PaymentSearchRequest();
        request.setPaymentNo(paymentNo);
        request.setOrderId(orderId);
        request.setOrderNo(orderNo);
        request.setUserId(userId);
        request.setUsername(username);
        request.setPaymentMethod(paymentMethod);
        request.setPaymentType(paymentType);
        request.setStatus(status);
        request.setMinAmount(minAmount);
        request.setMaxAmount(maxAmount);
        request.setCreatedDateFrom(createdDateFrom);
        request.setCreatedDateTo(createdDateTo);
        request.setThirdPartyTransactionId(thirdPartyTransactionId);

        Page<PaymentDto> result = paymentService.searchPayments(request, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/my-payments")
    @PreAuthorize("hasAuthority('PAYMENT_VIEW')")
    @Operation(summary = "获取我的支付记录", description = "获取当前用户的所有支付记录")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    public ResponseEntity<ApiResponse<Page<PaymentDto>>> getMyPayments(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PaymentDto> result = paymentService.getUserPayments(userDetails.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAuthority('PAYMENT_VIEW')")
    @Operation(summary = "获取订单支付记录", description = "获取指定订单的所有支付记录")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    public ResponseEntity<ApiResponse<List<PaymentDto>>> getOrderPayments(
            @Parameter(description = "订单ID", example = "1") @PathVariable Long orderId) {
        List<PaymentDto> result = paymentService.getOrderPayments(orderId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{id}/query-status")
    @PreAuthorize("hasAuthority('PAYMENT_VIEW')")
    @Operation(summary = "查询支付状态", description = "主动查询第三方支付平台的支付状态")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功")
    public ResponseEntity<ApiResponse<PaymentDto>> queryPaymentStatus(
            @Parameter(description = "支付ID", example = "1") @PathVariable Long id) {
        PaymentDto result = paymentService.queryPaymentStatus(id);
        return ResponseEntity.ok(ApiResponse.success("支付状态查询完成", result));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('PAYMENT_PROCESS')")
    @Operation(summary = "取消支付", description = "取消待支付状态的支付记录")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "取消成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "支付状态不允许取消")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "支付记录不存在")
    public ResponseEntity<ApiResponse<PaymentDto>> cancelPayment(
            @Parameter(description = "支付ID", example = "1") @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PaymentDto result = paymentService.cancelPayment(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("支付取消成功", result));
    }

    @GetMapping("/{id}/records")
    @PreAuthorize("hasAuthority('PAYMENT_VIEW')")
    @Operation(summary = "获取支付记录", description = "获取支付的详细操作记录")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    public ResponseEntity<ApiResponse<List<PaymentRecordDto>>> getPaymentRecords(
            @Parameter(description = "支付ID", example = "1") @PathVariable Long id) {
        List<PaymentRecordDto> result = paymentService.getPaymentRecords(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('PAYMENT_VIEW')")
    @Operation(summary = "获取支付统计信息", description = "获取支付的各种统计数据")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    public ResponseEntity<ApiResponse<PaymentStatistics>> getPaymentStatistics(
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDate endDate) {

        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1); // 当月第一天
        }
        if (endDate == null) {
            endDate = LocalDate.now(); // 今天
        }

        PaymentStatistics statistics = paymentService.getPaymentStatistics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @GetMapping("/check-exists")
    @PreAuthorize("hasAuthority('PAYMENT_VIEW')")
    @Operation(summary = "检查支付是否存在", description = "根据支付单号检查支付记录是否存在")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "检查完成")
    public ResponseEntity<ApiResponse<Boolean>> checkPaymentExists(
            @Parameter(description = "支付单号") @RequestParam String paymentNo) {
        boolean exists = paymentService.paymentExists(paymentNo);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    @PostMapping("/callback/alipay")
    @Operation(summary = "支付宝支付回调（统一配置）", description = "处理支付宝的异步通知回调（使用统一配置）")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "处理成功")
    public ResponseEntity<String> handleAlipayCallback(
            HttpServletRequest request) {
        
        try {
            // 获取支付宝POST过来的所有参数
            Map<String, String> params = new HashMap<>();
            Enumeration<String> parameterNames = request.getParameterNames();
            
            while (parameterNames.hasMoreElements()) {
                String paramName = parameterNames.nextElement();
                String paramValue = request.getParameter(paramName);
                params.put(paramName, paramValue);
                log.debug("支付宝回调参数：{} = {}", paramName, paramValue);
            }

            // 直接调用 PaymentService 处理回调
            paymentService.handleAlipayCallback(parseAlipayCallback(params));

            // 返回success告诉支付宝处理成功
            return ResponseEntity.ok("success");
            
        } catch (Exception e) {
            log.error("处理支付宝回调失败", e);
            // 返回failure告诉支付宝处理失败，支付宝会重试
            return ResponseEntity.ok("failure");
        }
    }

    @PostMapping("/callback/alipay/{merchantId}")
    @Operation(summary = "商家支付宝支付回调", description = "处理特定商家的支付宝异步通知回调")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "处理成功")
    public ResponseEntity<String> handleMerchantAlipayCallback(
            @Parameter(description = "商家ID", example = "123") @PathVariable Long merchantId,
            HttpServletRequest request) {
        
        try {
            log.info("处理商家支付宝回调，商家ID：{}", merchantId);
            
            // 获取支付宝POST过来的所有参数
            Map<String, String> params = new HashMap<>();
            Enumeration<String> parameterNames = request.getParameterNames();
            
            while (parameterNames.hasMoreElements()) {
                String paramName = parameterNames.nextElement();
                String paramValue = request.getParameter(paramName);
                params.put(paramName, paramValue);
                log.debug("商家支付宝回调参数，商家ID：{}，参数：{} = {}", merchantId, paramName, paramValue);
            }

            // 验证商家ID与订单的匹配性
            String outTradeNo = params.get("out_trade_no");
            if (StringUtils.hasText(outTradeNo)) {
                validateMerchantCallback(merchantId, outTradeNo);
            }

            // 调用 PaymentService 处理回调
            paymentService.handleAlipayCallback(parseAlipayCallback(params));

            // 返回success告诉支付宝处理成功
            log.info("商家支付宝回调处理成功，商家ID：{}，订单号：{}", merchantId, outTradeNo);
            return ResponseEntity.ok("success");
            
        } catch (Exception e) {
            log.error("处理商家支付宝回调失败，商家ID：{}", merchantId, e);
            // 返回failure告诉支付宝处理失败，支付宝会重试
            return ResponseEntity.ok("failure");
        }
    }

    /**
     * 验证商家回调的合法性
     */
    private void validateMerchantCallback(Long merchantId, String paymentNo) {
        // 这里可以添加额外的验证逻辑，比如验证该支付单号确实属于该商家
        // 当前的验证逻辑在PaymentService的签名验证中已经处理
        log.debug("验证商家回调合法性，商家ID：{}，支付单号：{}", merchantId, paymentNo);
    }

    /**
     * 解析支付宝回调参数
     */
    private PaymentCallbackRequest parseAlipayCallback(Map<String, String> params) {
        PaymentCallbackRequest request = new PaymentCallbackRequest();
        request.setPaymentNo(params.get("out_trade_no"));
        request.setThirdPartyTransactionId(params.get("trade_no"));
        request.setStatus(convertAlipayTradeStatus(params.get("trade_status")));
        Map<String, Object> rawData = new java.util.HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            rawData.put(entry.getKey(), entry.getValue());
        }
        request.setRawData(rawData);
        return request;
    }

    /**
     * 转换支付宝交易状态
     */
    private String convertAlipayTradeStatus(String tradeStatus) {
        if (tradeStatus == null) {
            return "PENDING";
        }
        
        switch (tradeStatus.toUpperCase()) {
            case "TRADE_SUCCESS":
            case "TRADE_FINISHED":
                return "SUCCESS";
            case "TRADE_CLOSED":
                return "FAILED";
            case "WAIT_BUYER_PAY":
                return "PENDING";
            default:
                return "PENDING";
        }
    }
}
