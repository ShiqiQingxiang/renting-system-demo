package com.rental.payment.controller;

import com.rental.common.response.ApiResponse;
import com.rental.payment.DTO.MerchantConfigStatusDTO;
import com.rental.payment.DTO.MerchantPaymentConfigDTO;
import com.rental.payment.DTO.MerchantPaymentConfigRequest;
import com.rental.payment.model.MerchantPaymentConfig;
import com.rental.payment.service.MerchantPaymentConfigService;
import com.rental.security.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant/payment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "商家支付配置管理", description = "商家支付配置相关接口，包括支付宝配置的增删改查等功能")
@SecurityRequirement(name = "bearerAuth")
public class MerchantPaymentController {

    private final MerchantPaymentConfigService configService;

    @PostMapping("/config")
    @PreAuthorize("hasAuthority('MERCHANT_PAYMENT_CONFIG') or hasRole('VENDOR')")
    @Operation(
        summary = "配置支付宝信息",
        description = "商家配置或更新自己的支付宝支付信息"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "201",
        description = "配置成功",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "配置成功示例",
                value = """
                {
                    "success": true,
                    "message": "支付配置保存成功",
                    "data": {
                        "id": 1,
                        "merchantId": 123,
                        "alipayAppId": "2021001234567890",
                        "alipayAccount": "merchant@example.com",
                        "notifyUrl": "https://example.com/api/payment/notify/{merchantId}",
                        "returnUrl": "https://example.com/payment/result",
                        "status": "PENDING_REVIEW",
                        "createdAt": "2023-07-16T10:30:00"
                    }
                }
                """
            )
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "配置信息验证失败")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "权限不足")
    public ResponseEntity<ApiResponse<MerchantPaymentConfigDTO>> configurePayment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "支付配置请求信息",
                content = @Content(
                    schema = @Schema(implementation = MerchantPaymentConfigRequest.class),
                    examples = @ExampleObject(
                        name = "配置请求示例",
                        value = """
                        {
                            "alipayAppId": "2021001234567890",
                            "alipayPrivateKey": "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC...",
                            "alipayPublicKey": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMI...",
                            "alipayAccount": "merchant@example.com",
                            "notifyUrl": "https://example.com/api/payment/notify/{merchantId}",
                            "returnUrl": "https://example.com/payment/result",
                            "remark": "商家支付配置"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody MerchantPaymentConfigRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 从认证信息获取商家ID，这里简化处理，实际应根据用户角色获取
        Long merchantId = getMerchantIdFromUser(userDetails);
        
        log.info("商家配置支付信息，商家ID：{}，应用ID：{}", merchantId, 
                request.sanitizeForLogging().getAlipayAppId());

        MerchantPaymentConfigDTO result = configService.saveOrUpdateConfig(merchantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("支付配置保存成功", result));
    }

    @GetMapping("/config")
    @PreAuthorize("hasAuthority('MERCHANT_PAYMENT_CONFIG') or hasRole('VENDOR')")
    @Operation(summary = "获取支付配置", description = "获取当前商家的支付配置信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    public ResponseEntity<ApiResponse<MerchantPaymentConfigDTO>> getPaymentConfig(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long merchantId = getMerchantIdFromUser(userDetails);
        log.debug("获取商家支付配置，商家ID：{}", merchantId);

        MerchantPaymentConfigDTO result = configService.getMerchantConfig(merchantId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/config")
    @PreAuthorize("hasAuthority('MERCHANT_PAYMENT_CONFIG') or hasRole('VENDOR')")
    @Operation(summary = "更新支付配置", description = "更新商家的支付配置信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "更新成功")
    public ResponseEntity<ApiResponse<MerchantPaymentConfigDTO>> updatePaymentConfig(
            @Valid @RequestBody MerchantPaymentConfigRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long merchantId = getMerchantIdFromUser(userDetails);
        log.info("更新商家支付配置，商家ID：{}", merchantId);

        MerchantPaymentConfigDTO result = configService.saveOrUpdateConfig(merchantId, request);
        return ResponseEntity.ok(ApiResponse.success("支付配置更新成功", result));
    }

    @DeleteMapping("/config")
    @PreAuthorize("hasAuthority('MERCHANT_PAYMENT_CONFIG') or hasRole('VENDOR')")
    @Operation(summary = "删除支付配置", description = "删除商家的支付配置信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "删除成功")
    public ResponseEntity<ApiResponse<Void>> deletePaymentConfig(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long merchantId = getMerchantIdFromUser(userDetails);
        log.info("删除商家支付配置，商家ID：{}", merchantId);

        configService.deleteConfig(merchantId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/config/test")
    @PreAuthorize("hasAuthority('MERCHANT_PAYMENT_CONFIG') or hasRole('VENDOR')")
    @Operation(summary = "测试支付配置", description = "测试商家支付配置的连通性")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "测试完成")
    public ResponseEntity<ApiResponse<Boolean>> testPaymentConfig(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long merchantId = getMerchantIdFromUser(userDetails);
        log.info("测试商家支付配置，商家ID：{}", merchantId);

        boolean testResult = configService.testConfig(merchantId);
        String message = testResult ? "支付配置测试成功" : "支付配置测试失败，请检查配置信息";
        
        return ResponseEntity.ok(ApiResponse.success(message, testResult));
    }

    @GetMapping("/config/status")
    @PreAuthorize("hasAuthority('MERCHANT_PAYMENT_CONFIG') or hasRole('VENDOR')")
    @Operation(summary = "检查配置状态", description = "检查商家是否已配置支付信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "检查完成")
    public ResponseEntity<ApiResponse<Boolean>> checkConfigStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long merchantId = getMerchantIdFromUser(userDetails);
        boolean hasConfig = configService.hasConfig(merchantId);

        String message = hasConfig ? "已配置支付信息" : "未配置支付信息";
        return ResponseEntity.ok(ApiResponse.success(message, hasConfig));
    }

    // ================ 管理员功能 ================

    @GetMapping("/admin/configs")
    @PreAuthorize("hasAuthority('PAYMENT_ADMIN') or hasRole('ADMIN')")
    @Operation(summary = "获取所有商家配置", description = "管理员获取所有商家的支付配置信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    public ResponseEntity<ApiResponse<Page<MerchantPaymentConfigDTO>>> getAllConfigs(
            @Parameter(description = "配置状态") @RequestParam(required = false) MerchantPaymentConfig.ConfigStatus status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<MerchantPaymentConfigDTO> result;
        if (status != null) {
            result = configService.getConfigsByStatus(status, pageable);
        } else {
            result = configService.getAllConfigs(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/admin/configs/{merchantId}/enable")
    @PreAuthorize("hasAuthority('PAYMENT_ADMIN') or hasRole('ADMIN')")
    @Operation(summary = "启用商家配置", description = "管理员启用指定商家的支付配置")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "启用成功")
    public ResponseEntity<ApiResponse<MerchantPaymentConfigDTO>> enableConfig(
            @Parameter(description = "商家ID", example = "123") @PathVariable Long merchantId) {

        log.info("管理员启用商家支付配置，商家ID：{}", merchantId);
        MerchantPaymentConfigDTO result = configService.enableConfig(merchantId);
        return ResponseEntity.ok(ApiResponse.success("支付配置启用成功", result));
    }

    @PostMapping("/admin/configs/{merchantId}/disable")
    @PreAuthorize("hasAuthority('PAYMENT_ADMIN') or hasRole('ADMIN')")
    @Operation(summary = "禁用商家配置", description = "管理员禁用指定商家的支付配置")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "禁用成功")
    public ResponseEntity<ApiResponse<MerchantPaymentConfigDTO>> disableConfig(
            @Parameter(description = "商家ID", example = "123") @PathVariable Long merchantId) {

        log.info("管理员禁用商家支付配置，商家ID：{}", merchantId);
        MerchantPaymentConfigDTO result = configService.disableConfig(merchantId);
        return ResponseEntity.ok(ApiResponse.success("支付配置禁用成功", result));
    }

    @GetMapping("/admin/configs/statistics")
    @PreAuthorize("hasAuthority('PAYMENT_ADMIN') or hasRole('ADMIN')")
    @Operation(summary = "获取配置统计", description = "获取商家支付配置的统计信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    public ResponseEntity<ApiResponse<MerchantPaymentConfigService.ConfigStatistics>> getConfigStatistics() {

        MerchantPaymentConfigService.ConfigStatistics statistics = configService.getConfigStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    /**
     * 从用户信息中获取商家ID
     * 这里简化处理，实际实现中应该根据用户角色和关联关系获取
     */
    private Long getMerchantIdFromUser(CustomUserDetails userDetails) {
        // 简化实现：假设用户ID就是商家ID
        // 实际应用中需要根据用户角色判断：
        // 1. 如果是商家角色，从商家表中查询对应的商家ID
        // 2. 如果是管理员，可能需要传递merchantId参数
        // 3. 如果是普通用户，应该拒绝访问
        
        Long userId = userDetails.getUserId();
        
        // TODO: 这里应该实现真正的商家ID获取逻辑
        // 例如：
        // if (userDetails.getAuthorities().contains("ROLE_VENDOR")) {
        //     return merchantService.getMerchantIdByUserId(userId);
        // }
        
        return userId; // 临时简化处理
    }
}
