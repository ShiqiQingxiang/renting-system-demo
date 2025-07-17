package com.rental.contract.controller;

import com.rental.contract.DTO.*;
import com.rental.contract.service.ContractService;
import com.rental.common.response.ApiResponse;
import com.rental.common.response.PageResponse;
import com.rental.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "合同管理", description = "合同管理相关接口，包括合同创建、签署、查询、下载等功能")
@SecurityRequirement(name = "bearerAuth")
public class ContractController {

    private final ContractService contractService;

    @GetMapping
    @Operation(
        summary = "分页查询合同",
        description = "根据条件分页查询合同列表，支持多种筛选条件"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "查询成功",
        content = @Content(
            examples = @ExampleObject(
                name = "分页查询合同成功示例",
                value = """
                {
                    "code": 200,
                    "message": "操作成功",
                    "data": {
                        "content": [
                            {
                                "id": 1,
                                "contractNo": "CONTRACT202501170001",
                                "orderId": 1,
                                "orderNo": "ORDER202501170001",
                                "status": "SIGNED",
                                "statusDescription": "已签署",
                                "userName": "张三",
                                "itemNames": "MacBook Pro",
                                "signedAt": "2025-01-17T10:00:00"
                            }
                        ],
                        "currentPage": 0,
                        "pageSize": 10,
                        "totalElements": 1,
                        "totalPages": 1
                    }
                }
                """
            )
        )
    )
    @RequirePermission("CONTRACT_VIEW")
    public ApiResponse<PageResponse<ContractDTO>> queryContracts(
            @Parameter(description = "搜索关键词（合同编号或用户名）", example = "CONTRACT")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "合同状态", example = "SIGNED")
            @RequestParam(required = false) String status,
            @Parameter(description = "订单ID", example = "1")
            @RequestParam(required = false) Long orderId,
            @Parameter(description = "用户ID", example = "1")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "页码（从0开始）", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向（asc/desc）", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {

        ContractQueryRequest request = new ContractQueryRequest();
        request.setKeyword(keyword);
        if (status != null) {
            try {
                request.setStatus(com.rental.contract.model.Contract.ContractStatus.valueOf(status));
            } catch (IllegalArgumentException e) {
                // 忽略无效状态
            }
        }
        request.setOrderId(orderId);
        request.setUserId(userId);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setSortDir(sortDir);

        PageResponse<ContractDTO> result = contractService.queryContracts(request);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取合同详情", description = "根据合同ID获取合同的详细信息")
    @RequirePermission("CONTRACT_VIEW")
    public ApiResponse<ContractDTO> getContractById(
            @Parameter(description = "合同ID", example = "1") @PathVariable Long id) {
        ContractDTO contract = contractService.getContractById(id);
        return ApiResponse.success(contract);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "根据订单获取合同", description = "根据订单ID获取对应的合同信息")
    @RequirePermission("CONTRACT_VIEW")
    public ApiResponse<ContractDTO> getContractByOrderId(
            @Parameter(description = "订单ID", example = "1") @PathVariable Long orderId) {
        ContractDTO contract = contractService.getContractByOrderId(orderId);
        return ApiResponse.success(contract);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户合同列表", description = "获取指定用户的所有合同")
    @RequirePermission("CONTRACT_VIEW")
    public ApiResponse<List<ContractDTO>> getUserContracts(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long userId) {
        List<ContractDTO> contracts = contractService.getUserContracts(userId);
        return ApiResponse.success(contracts);
    }

    @PostMapping
    @Operation(
        summary = "创建合同",
        description = "根据订单创建合同，可以使用合同模板或自定义内容"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "合同创建成功",
        content = @Content(
            examples = @ExampleObject(
                name = "创建合同成功示例",
                value = """
                {
                    "code": 200,
                    "message": "合同创建成功",
                    "data": {
                        "id": 1,
                        "contractNo": "CONTRACT202501170001",
                        "orderId": 1,
                        "status": "DRAFT",
                        "content": "租赁合同内容..."
                    }
                }
                """
            )
        )
    )
    @RequirePermission("CONTRACT_CREATE")
    public ApiResponse<ContractDTO> createContract(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "合同创建请求",
                content = @Content(
                    examples = @ExampleObject(
                        name = "创建合同请求示例",
                        value = """
                        {
                            "orderId": 1,
                            "templateId": 1,
                            "customContent": "自定义合同内容（可选）"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody ContractCreateRequest request) {
        log.info("创建合同，订单ID: {}", request.getOrderId());
        ContractDTO contract = contractService.createContract(request);
        return ApiResponse.success("合同创建成功", contract);
    }

    @PostMapping("/{id}/sign")
    @Operation(summary = "签署合同", description = "签署指定的合同")
    @RequirePermission("CONTRACT_SIGN")
    public ApiResponse<ContractDTO> signContract(
            @Parameter(description = "合同ID", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "合同签署请求",
                content = @Content(
                    examples = @ExampleObject(
                        name = "签署合同请求示例",
                        value = """
                        {
                            "contractId": 1,
                            "signatureData": "签名数据",
                            "remarks": "签署备注"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody ContractSignRequest request) {
        log.info("签署合同，合同ID: {}", id);
        request.setContractId(id);
        ContractDTO contract = contractService.signContract(request);
        return ApiResponse.success("合同签署成功", contract);
    }

    @PostMapping("/{id}/terminate")
    @Operation(summary = "终止合同", description = "终止指定的合同")
    @RequirePermission("CONTRACT_MANAGE")
    public ApiResponse<ContractDTO> terminateContract(
            @Parameter(description = "合同ID", example = "1") @PathVariable Long id,
            @Parameter(description = "终止原因", example = "用户要求终止")
            @RequestParam String reason) {
        log.info("终止合同，合同ID: {}, 原因: {}", id, reason);
        ContractDTO contract = contractService.terminateContract(id, reason);
        return ApiResponse.success("合同终止成功", contract);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "下载合同", description = "下载合同文件")
    @RequirePermission("CONTRACT_VIEW")
    public ResponseEntity<byte[]> downloadContract(
            @Parameter(description = "合同ID", example = "1") @PathVariable Long id) {
        log.info("下载合同，合同ID: {}", id);

        byte[] contractData = contractService.downloadContract(id);
        ContractDTO contract = contractService.getContractById(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + contract.getContractNo() + ".txt\"")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                .body(contractData);
    }
}
