package com.rental.finance.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rental.finance.model.FinanceReport;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 财务报表数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinanceReportDto {

    private Long id;

    private FinanceReport.ReportType reportType;

    private String reportTypeDescription;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodStart;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodEnd;

    private BigDecimal totalIncome;

    private BigDecimal totalExpense;

    private BigDecimal netProfit;

    private String reportData;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 从实体转换为DTO
     */
    public static FinanceReportDto fromEntity(FinanceReport report) {
        FinanceReportDto dto = new FinanceReportDto();
        dto.setId(report.getId());
        dto.setReportType(report.getReportType());
        dto.setReportTypeDescription(report.getReportType().getDescription());
        dto.setPeriodStart(report.getPeriodStart());
        dto.setPeriodEnd(report.getPeriodEnd());
        dto.setTotalIncome(report.getTotalIncome());
        dto.setTotalExpense(report.getTotalExpense());
        dto.setNetProfit(report.getNetProfit());
        dto.setReportData(report.getReportData());
        dto.setCreatedAt(report.getCreatedAt());
        return dto;
    }
}
