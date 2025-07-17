package com.rental.contract.controller;

import com.rental.contract.DTO.ContractTemplateDTO;
import com.rental.contract.service.ContractTemplateService;
import com.rental.common.response.ApiResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contract-templates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "合同模板管理", description = "合同模板管理相关接口，包括模板的增删改查、启用禁用等功能")
@SecurityRequirement(name = "bearerAuth")
public class ContractTemplateController {

    private final ContractTemplateService contractTemplateService;

    @GetMapping
    @Operation(
        summary = "分页获取合同模板",
        description = "分页获取所有合同模板列表"
    )
    @RequirePermission("CONTRACT_TEMPLATE_MANAGE")
    public ApiResponse<Page<ContractTemplateDTO>> getTemplates(
            @Parameter(description = "页码（从0开始）", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向（asc/desc）", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = Sort.by(
            "desc".equalsIgnoreCase(sortDir) ?
            Sort.Direction.DESC : Sort.Direction.ASC,
            sortBy
        );
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ContractTemplateDTO> templates = contractTemplateService.getTemplates(pageable);
        return ApiResponse.success(templates);
    }

    @GetMapping("/active")
    @Operation(
        summary = "获取活跃的合同模板",
        description = "获取所有活跃状态的合同模板"
    )
    @RequirePermission("CONTRACT_VIEW")
    public ApiResponse<List<ContractTemplateDTO>> getActiveTemplates() {
        List<ContractTemplateDTO> templates = contractTemplateService.getActiveTemplates();
        return ApiResponse.success(templates);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取合同模板详情", description = "根据模板ID获取合同模板的详细信息")
    @RequirePermission("CONTRACT_TEMPLATE_MANAGE")
    public ApiResponse<ContractTemplateDTO> getTemplateById(
            @Parameter(description = "模板ID", example = "1") @PathVariable Long id) {
        ContractTemplateDTO template = contractTemplateService.getTemplateById(id);
        return ApiResponse.success(template);
    }

    @PostMapping
    @Operation(
        summary = "创建合同模板",
        description = "创建新的合同模板"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "模板创建成功",
        content = @Content(
            examples = @ExampleObject(
                name = "创建模板成功示例",
                value = """
                {
                    "code": 200,
                    "message": "合同模板创建成功",
                    "data": {
                        "id": 1,
                        "name": "标准租赁合同模板",
                        "version": "1.0",
                        "isActive": true
                    }
                }
                """
            )
        )
    )
    @RequirePermission("CONTRACT_TEMPLATE_MANAGE")
    public ApiResponse<ContractTemplateDTO> createTemplate(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "合同模板创建请求",
                content = @Content(
                    examples = @ExampleObject(
                        name = "创建模板请求示例",
                        value = """
                        {
                            "name": "标准租赁合同模板",
                            "content": "租赁合同模板内容...",
                            "version": "1.0",
                            "isActive": true
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody ContractTemplateDTO templateDTO) {
        log.info("创建合同模板，名称: {}", templateDTO.getName());
        ContractTemplateDTO template = contractTemplateService.createTemplate(templateDTO);
        return ApiResponse.success("合同模板创建成功", template);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新合同模板", description = "更新指定ID的合同模板信息")
    @RequirePermission("CONTRACT_TEMPLATE_MANAGE")
    public ApiResponse<ContractTemplateDTO> updateTemplate(
            @Parameter(description = "模板ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody ContractTemplateDTO templateDTO) {
        log.info("更新合同模板，ID: {}", id);
        ContractTemplateDTO template = contractTemplateService.updateTemplate(id, templateDTO);
        return ApiResponse.success("合同模板更新成功", template);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除合同模板", description = "删除指定ID的合同模板")
    @RequirePermission("CONTRACT_TEMPLATE_MANAGE")
    public ApiResponse<Void> deleteTemplate(
            @Parameter(description = "模板ID", example = "1") @PathVariable Long id) {
        log.info("删除合同模板，ID: {}", id);
        contractTemplateService.deleteTemplate(id);
        return ApiResponse.success("合同模板删除成功", null);
    }

    @PostMapping("/{id}/toggle-status")
    @Operation(summary = "切换模板状态", description = "启用或禁用合同模板")
    @RequirePermission("CONTRACT_TEMPLATE_MANAGE")
    public ApiResponse<Void> toggleTemplateStatus(
            @Parameter(description = "模板ID", example = "1") @PathVariable Long id) {
        log.info("切换合同模板状态，ID: {}", id);
        contractTemplateService.toggleTemplateStatus(id);
        return ApiResponse.success("模板状态切换成功", null);
    }
}
