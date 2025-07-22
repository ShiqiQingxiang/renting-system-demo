package com.rental.finance.repository;

import com.rental.finance.model.FinanceReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinanceReportRepository extends JpaRepository<FinanceReport, Long> {

    /**
     * 根据报表类型查找报表
     */
    Page<FinanceReport> findByReportType(FinanceReport.ReportType reportType, Pageable pageable);

    /**
     * 根据时间范围查找报表
     */
    List<FinanceReport> findByPeriodStartBetween(LocalDate startDate, LocalDate endDate);

    List<FinanceReport> findByPeriodEndBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 查找指定时间段的报表
     */
    @Query("SELECT fr FROM FinanceReport fr WHERE " +
           "fr.periodStart >= :startDate AND fr.periodEnd <= :endDate")
    List<FinanceReport> findReportsInPeriod(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /**
     * 查找特定时间段和类型的报表
     */
    Optional<FinanceReport> findByReportTypeAndPeriodStartAndPeriodEnd(
            FinanceReport.ReportType reportType,
            LocalDate periodStart,
            LocalDate periodEnd);

    /**
     * 查找最新的报表
     */
    @Query("SELECT fr FROM FinanceReport fr WHERE fr.reportType = :reportType ORDER BY fr.periodEnd DESC")
    List<FinanceReport> findLatestByReportType(@Param("reportType") FinanceReport.ReportType reportType,
                                              Pageable pageable);

    /**
     * 查找指定年份的报表
     */
    @Query("SELECT fr FROM FinanceReport fr WHERE YEAR(fr.periodStart) = :year")
    List<FinanceReport> findByYear(@Param("year") int year);

    /**
     * 查找指定年月的报表
     */
    @Query("SELECT fr FROM FinanceReport fr WHERE YEAR(fr.periodStart) = :year AND MONTH(fr.periodStart) = :month")
    List<FinanceReport> findByYearAndMonth(@Param("year") int year, @Param("month") int month);

    /**
     * 检查指定时间段的报表是否已存在
     */
    boolean existsByReportTypeAndPeriodStartAndPeriodEnd(
            FinanceReport.ReportType reportType,
            LocalDate periodStart,
            LocalDate periodEnd);

    /**
     * 删除过期的报表（超过指定天数）
     */
    @Modifying
    @Query("DELETE FROM FinanceReport fr WHERE fr.createdAt < :cutoffDate")
    void deleteOldReports(@Param("cutoffDate") LocalDate cutoffDate);
}
