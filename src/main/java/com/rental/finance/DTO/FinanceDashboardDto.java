package com.rental.finance.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 财务仪表板数据DTO
 */
@Data
public class FinanceDashboardDto {

    // 今日数据
    private BigDecimal todayIncome;
    private BigDecimal todayExpense;
    private BigDecimal todayProfit;
    private Integer todayTransactions;

    // 本月数据
    private BigDecimal monthIncome;
    private BigDecimal monthExpense;
    private BigDecimal monthProfit;
    private Integer monthTransactions;

    // 同比数据（与上月比较）
    private BigDecimal incomeGrowthRate;
    private BigDecimal expenseGrowthRate;
    private BigDecimal profitGrowthRate;

    // 最近7天趋势数据
    private List<DailyTrendData> weeklyTrend;

    // 分类收入分布
    private Map<String, BigDecimal> incomeByCategory;

    // 分类支出分布
    private Map<String, BigDecimal> expenseByCategory;

    // 最新财务记录
    private List<FinanceRecordDto> recentRecords;

    // 统计日期
    private LocalDate statisticsDate;

    /**
     * 每日趋势数据
     */
    @Data
    public static class DailyTrendData {
        private LocalDate date;
        private BigDecimal income;
        private BigDecimal expense;
        private BigDecimal profit;
        private Integer transactions;
    }
}
