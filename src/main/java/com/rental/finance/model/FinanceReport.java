package com.rental.finance.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "finance_reports", indexes = {
    @Index(name = "idx_report_type", columnList = "report_type"),
    @Index(name = "idx_period", columnList = "period_start, period_end")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FinanceReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    @NotNull(message = "报表类型不能为空")
    private ReportType reportType;

    @Column(name = "period_start", nullable = false)
    @NotNull(message = "期间开始日期不能为空")
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    @NotNull(message = "期间结束日期不能为空")
    private LocalDate periodEnd;

    @Column(name = "total_income", precision = 15, scale = 2)
    private BigDecimal totalIncome = BigDecimal.ZERO;

    @Column(name = "total_expense", precision = 15, scale = 2)
    private BigDecimal totalExpense = BigDecimal.ZERO;

    @Column(name = "net_profit", precision = 15, scale = 2)
    private BigDecimal netProfit = BigDecimal.ZERO;

    @Column(name = "report_data", columnDefinition = "JSON")
    private String reportData;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 报表类型枚举
     */
    public enum ReportType {
        DAILY("日报"),
        WEEKLY("周报"),
        MONTHLY("月报"),
        YEARLY("年报");

        private final String description;

        ReportType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 计算净利润
     */
    public void calculateNetProfit() {
        this.netProfit = this.totalIncome.subtract(this.totalExpense);
    }
}
