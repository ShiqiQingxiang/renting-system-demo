package com.rental.finance.service;

import com.rental.common.exception.BusinessException;
import com.rental.finance.DTO.FinanceReportDto;
import com.rental.finance.DTO.FinanceStatisticsDto;
import com.rental.finance.model.FinanceReport;
import com.rental.finance.repository.FinanceRecordRepository;
import com.rental.finance.repository.FinanceReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

/**
 * 财务报表服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FinanceReportService {

    private final FinanceReportRepository financeReportRepository;
    private final FinanceRecordRepository financeRecordRepository;

    /**
     * 生成财务报表
     */
    public FinanceReportDto generateReport(FinanceReport.ReportType reportType, LocalDate startDate, LocalDate endDate) {
        log.info("生成财务报表: 类型={}, 开始日期={}, 结束日期={}", reportType, startDate, endDate);

        // 检查是否已存在相同时间段的报表
        Optional<FinanceReport> existingReport = financeReportRepository
                .findByReportTypeAndPeriodStartAndPeriodEnd(reportType, startDate, endDate);

        if (existingReport.isPresent()) {
            log.info("报表已存在，返回现有报表");
            return FinanceReportDto.fromEntity(existingReport.get());
        }

        // 计算时间范围
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 计算财务数据
        BigDecimal totalIncome = financeRecordRepository.calculateTotalIncomeByPeriod(startDateTime, endDateTime);
        BigDecimal totalExpense = financeRecordRepository.calculateTotalExpenseByPeriod(startDateTime, endDateTime);
        BigDecimal totalRefund = financeRecordRepository.calculateTotalRefundByPeriod(startDateTime, endDateTime);

        // 创建报表
        FinanceReport report = new FinanceReport();
        report.setReportType(reportType);
        report.setPeriodStart(startDate);
        report.setPeriodEnd(endDate);
        report.setTotalIncome(totalIncome);
        report.setTotalExpense(totalExpense);
        report.calculateNetProfit();

        // 生成详细报表数据
        String reportData = generateReportData(startDateTime, endDateTime);
        report.setReportData(reportData);

        FinanceReport saved = financeReportRepository.save(report);
        log.info("财务报表生成成功，ID: {}", saved.getId());

        return FinanceReportDto.fromEntity(saved);
    }

    /**
     * 生成日报表
     */
    public FinanceReportDto generateDailyReport(LocalDate date) {
        return generateReport(FinanceReport.ReportType.DAILY, date, date);
    }

    /**
     * 生成周报表
     */
    public FinanceReportDto generateWeeklyReport(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        return generateReport(FinanceReport.ReportType.WEEKLY, weekStart, weekEnd);
    }

    /**
     * 生成月报表
     */
    public FinanceReportDto generateMonthlyReport(LocalDate monthStart) {
        LocalDate monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth());
        return generateReport(FinanceReport.ReportType.MONTHLY, monthStart, monthEnd);
    }

    /**
     * 生成年报表
     */
    public FinanceReportDto generateYearlyReport(LocalDate yearStart) {
        LocalDate yearEnd = yearStart.with(TemporalAdjusters.lastDayOfYear());
        return generateReport(FinanceReport.ReportType.YEARLY, yearStart, yearEnd);
    }

    /**
     * 获取报表详情
     */
    @Transactional(readOnly = true)
    public FinanceReportDto getReport(Long id) {
        FinanceReport report = financeReportRepository.findById(id)
                .orElseThrow(() -> new BusinessException("财务报表不存在"));
        return FinanceReportDto.fromEntity(report);
    }

    /**
     * 分页查询财务报表（支持报表类型筛选）
     */
    @Transactional(readOnly = true)
    public Page<FinanceReportDto> getReports(FinanceReport.ReportType reportType, Pageable pageable) {
        if (reportType != null) {
            return financeReportRepository.findByReportType(reportType, pageable)
                    .map(FinanceReportDto::fromEntity);
        }
        return financeReportRepository.findAll(pageable)
                .map(FinanceReportDto::fromEntity);
    }

    /**
     * 分页查询财务报表（原有方法保持兼容）
     */
    @Transactional(readOnly = true)
    public Page<FinanceReportDto> getReports(Pageable pageable) {
        return financeReportRepository.findAll(pageable)
                .map(FinanceReportDto::fromEntity);
    }

    /**
     * 获取财务统计数据
     */
    @Transactional(readOnly = true)
    public FinanceStatisticsDto getFinanceStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 计算基本统计数据
        BigDecimal totalIncome = financeRecordRepository.calculateTotalIncomeByPeriod(startDateTime, endDateTime);
        BigDecimal totalExpense = financeRecordRepository.calculateTotalExpenseByPeriod(startDateTime, endDateTime);
        BigDecimal totalRefund = financeRecordRepository.calculateTotalRefundByPeriod(startDateTime, endDateTime);

        FinanceStatisticsDto statistics = new FinanceStatisticsDto();
        statistics.setTotalIncome(totalIncome);
        statistics.setTotalExpense(totalExpense);
        statistics.setTotalRefund(totalRefund);
        statistics.calculateNetProfit();
        statistics.setPeriod(startDate + " 至 " + endDate);

        // 获取交易总数
        int totalTransactions = financeRecordRepository.findByCreatedAtBetween(startDateTime, endDateTime).size();
        statistics.setTotalTransactions(totalTransactions);

        return statistics;
    }

    /**
     * 生成报表详细数据
     */
    private String generateReportData(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // 这里可以生成更详细的JSON格式报表数据
        // 包括分类统计、趋势分析等
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"period\": \"").append(startDateTime.toLocalDate()).append(" 至 ").append(endDateTime.toLocalDate()).append("\",");
        sb.append("\"generated_at\": \"").append(LocalDateTime.now()).append("\"");
        sb.append("}");
        return sb.toString();
    }
}
