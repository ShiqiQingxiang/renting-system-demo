package com.rental.finance.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 财务统计数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinanceStatisticsDto {

    private BigDecimal totalIncome;

    private BigDecimal totalExpense;

    private BigDecimal totalRefund;

    private BigDecimal netProfit;

    private Map<String, BigDecimal> incomeByCategory;

    private Map<String, BigDecimal> expenseByCategory;

    private Map<String, BigDecimal> refundByCategory;

    private int totalTransactions;

    private String period;

    public void calculateNetProfit() {
        this.netProfit = this.totalIncome.subtract(this.totalExpense);
    }
}
