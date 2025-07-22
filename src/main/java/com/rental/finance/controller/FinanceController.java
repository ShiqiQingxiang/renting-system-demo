package com.rental.finance.controller;

import com.rental.common.response.ApiResponse;
import com.rental.finance.DTO.*;
import com.rental.finance.model.FinanceRecord;
import com.rental.finance.model.FinanceReport;
import com.rental.finance.service.FinanceRecordService;
import com.rental.finance.service.FinanceReportService;
import com.rental.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 财务管理控制器 - 简化版
 * 只包含核心必要的财务管理API
 */
@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "财务管理", description = "财务记录和报表管理相关接口")
public class FinanceController {

    private final FinanceRecordService financeRecordService;
    private final FinanceReportService financeReportService;

    // ================================
    // 财务记录核心接口
    // ================================

    @GetMapping("/records")
    @Operation(summary = "查询财务记录", description = "分页查询财务记录，支持类型和分类筛选")
    @RequirePermission("FINANCE_VIEW")
    public ResponseEntity<ApiResponse<Page<FinanceRecordDto>>> getFinanceRecords(
            @Parameter(description = "财务类型") @RequestParam(required = false) FinanceRecord.FinanceType type,
            @Parameter(description = "财务分类") @RequestParam(required = false) String category,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<FinanceRecordDto> result = financeRecordService.getFinanceRecords(type, category, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success("查询成功", result));
    }

    @GetMapping("/records/{id}")
    @Operation(summary = "获取财务记录详情", description = "根据ID获取财务记录详情")
    @RequirePermission("FINANCE_VIEW")
    public ResponseEntity<ApiResponse<FinanceRecordDto>> getFinanceRecord(
            @Parameter(description = "财务记录ID") @PathVariable Long id) {
        FinanceRecordDto result = financeRecordService.getFinanceRecord(id);
        return ResponseEntity.ok(ApiResponse.success("获取成功", result));
    }

    @PostMapping("/records")
    @Operation(summary = "创建财务记录", description = "手动创建财务记录")
    @RequirePermission("FINANCE_RECORD_MANAGE")
    public ResponseEntity<ApiResponse<FinanceRecordDto>> createFinanceRecord(
            @Valid @RequestBody FinanceRecordCreateRequest request) {
        log.info("创建财务记录请求: {}", request);
        FinanceRecordDto result = financeRecordService.createFinanceRecord(request);
        return ResponseEntity.ok(ApiResponse.success("财务记录创建成功", result));
    }

    @GetMapping("/records/order/{orderId}")
    @Operation(summary = "获取订单财务记录", description = "获取指定订单的所有财务记录")
    @RequirePermission("FINANCE_VIEW")
    public ResponseEntity<ApiResponse<List<FinanceRecordDto>>> getFinanceRecordsByOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        List<FinanceRecordDto> result = financeRecordService.getFinanceRecordsByOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("查询成功", result));
    }

    // ================================
    // 财务报表核心接口
    // ================================

    @PostMapping("/reports/generate")
    @Operation(summary = "生成财务报表", description = "生成指定类型和时间范围的财务报表")
    @RequirePermission("FINANCE_REPORT_GENERATE")
    public ResponseEntity<ApiResponse<FinanceReportDto>> generateReport(
            @Parameter(description = "报表类型") @RequestParam FinanceReport.ReportType reportType,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("生成财务报表请求: 类型={}, 开始={}, 结束={}", reportType, startDate, endDate);
        FinanceReportDto result = financeReportService.generateReport(reportType, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("报表生成成功", result));
    }

    @GetMapping("/reports")
    @Operation(summary = "查询财务报表", description = "分页查询财务报表列表")
    @RequirePermission("FINANCE_REPORT_VIEW")
    public ResponseEntity<ApiResponse<Page<FinanceReportDto>>> getReports(
            @Parameter(description = "报表类型") @RequestParam(required = false) FinanceReport.ReportType reportType,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<FinanceReportDto> result;
        if (reportType != null) {
            result = financeReportService.getReports(reportType, pageable);
        } else {
            result = financeReportService.getReports(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success("查询成功", result));
    }

    @GetMapping("/reports/{id}")
    @Operation(summary = "获取报表详情", description = "根据ID获取财务报表详情")
    @RequirePermission("FINANCE_REPORT_VIEW")
    public ResponseEntity<ApiResponse<FinanceReportDto>> getReport(
            @Parameter(description = "报表ID") @PathVariable Long id) {
        FinanceReportDto result = financeReportService.getReport(id);
        return ResponseEntity.ok(ApiResponse.success("获取成功", result));
    }

    // ================================
    // 财务统计接口
    // ================================

    @GetMapping("/statistics")
    @Operation(summary = "获取财务统计", description = "获取指定时间范围的财务统计数据")
    @RequirePermission("FINANCE_VIEW")
    public ResponseEntity<ApiResponse<FinanceStatisticsDto>> getFinanceStatistics(
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        FinanceStatisticsDto result = financeReportService.getFinanceStatistics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("统计查询成功", result));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "获取财务仪表板", description = "获取财务管理仪表板数据")
    @RequirePermission("FINANCE_VIEW")
    public ResponseEntity<ApiResponse<FinanceDashboardDto>> getDashboard() {
        // 创建一个简单的仪表板数据，实际应该从Service获取
        FinanceDashboardDto dashboard = new FinanceDashboardDto();
        dashboard.setStatisticsDate(LocalDate.now());

        return ResponseEntity.ok(ApiResponse.success("获取成功", dashboard));
    }
}
