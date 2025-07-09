package com.rental.finance.repository;

import com.rental.finance.model.FinanceReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinanceReportRepository extends JpaRepository<FinanceReport, Long> {

    // 按报表类型查询
    List<FinanceReport> findByReportType(FinanceReport.ReportType reportType);

    Page<FinanceReport> findByReportType(FinanceReport.ReportType reportType, Pageable pageable);

    // 按时间范围查询
    List<FinanceReport> findByPeriodStartBetween(LocalDate startDate, LocalDate endDate);

    List<FinanceReport> findByPeriodEndBetween(LocalDate startDate, LocalDate endDate);

    // 查询指定时间段的报表
    @Query("SELECT fr FROM FinanceReport fr WHERE " +
           "fr.periodStart >= :startDate AND fr.periodEnd <= :endDate")
    List<FinanceReport> findReportsInPeriod(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    // 查询特定时间段和类型的报表
    Optional<FinanceReport> findByReportTypeAndPeriodStartAndPeriodEnd(
        FinanceReport.ReportType reportType,
        LocalDate periodStart,
        LocalDate periodEnd
    );

    // 查询最新的报表
    @Query("SELECT fr FROM FinanceReport fr WHERE fr.reportType = :reportType ORDER BY fr.periodEnd DESC")
    List<FinanceReport> findLatestReportsByType(@Param("reportType") FinanceReport.ReportType reportType,
                                               Pageable pageable);

    // 按类型和时间排序查询
    List<FinanceReport> findByReportTypeOrderByPeriodEndDesc(FinanceReport.ReportType reportType);

    // 检查报表是否存在
    boolean existsByReportTypeAndPeriodStartAndPeriodEnd(
        FinanceReport.ReportType reportType,
        LocalDate periodStart,
        LocalDate periodEnd
    );
}
