package com.rental.finance.service;

import com.rental.common.exception.BusinessException;
import com.rental.finance.DTO.FinanceRecordCreateRequest;
import com.rental.finance.DTO.FinanceRecordDto;
import com.rental.finance.DTO.FinanceStatisticsDto;
import com.rental.finance.model.FinanceRecord;
import com.rental.finance.repository.FinanceRecordRepository;
import com.rental.order.model.Order;
import com.rental.order.repository.OrderRepository;
import com.rental.payment.model.Payment;
import com.rental.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 财务记录服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FinanceRecordService {

    private final FinanceRecordRepository financeRecordRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    /**
     * 创建财务记录
     */
    public FinanceRecordDto createFinanceRecord(FinanceRecordCreateRequest request) {
        log.info("创建财务记录: {}", request);

        FinanceRecord record = new FinanceRecord();
        record.setRecordNo(generateRecordNo());
        record.setType(request.getType());
        record.setCategory(request.getCategory());
        record.setAmount(request.getAmount());
        record.setDescription(request.getDescription());

        // 设置订单关联
        if (request.getOrderId() != null) {
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new BusinessException("订单不存在"));
            record.setOrder(order);
        }

        // 设置支付关联
        if (request.getPaymentId() != null) {
            Payment payment = paymentRepository.findById(request.getPaymentId())
                    .orElseThrow(() -> new BusinessException("支付记录不存在"));
            record.setPayment(payment);
        }

        FinanceRecord saved = financeRecordRepository.save(record);
        log.info("财务记录创建成功，ID: {}", saved.getId());

        return FinanceRecordDto.fromEntity(saved);
    }

    /**
     * 根据订单创建财务记录
     */
    public FinanceRecordDto createRecordForOrder(Order order, FinanceRecord.FinanceType type, String category, BigDecimal amount, String description) {
        FinanceRecord record = new FinanceRecord();
        record.setRecordNo(generateRecordNo());
        record.setOrder(order);
        record.setType(type);
        record.setCategory(category);
        record.setAmount(amount);
        record.setDescription(description);

        FinanceRecord saved = financeRecordRepository.save(record);
        log.info("为订单 {} 创建财务记录成功", order.getOrderNo());

        return FinanceRecordDto.fromEntity(saved);
    }

    /**
     * 根据支付创建财务记录
     */
    public FinanceRecordDto createRecordForPayment(Payment payment, FinanceRecord.FinanceType type, String category, String description) {
        FinanceRecord record = new FinanceRecord();
        record.setRecordNo(generateRecordNo());
        record.setPayment(payment);
        record.setOrder(payment.getOrder());
        record.setType(type);
        record.setCategory(category);
        record.setAmount(payment.getAmount());
        record.setDescription(description);

        FinanceRecord saved = financeRecordRepository.save(record);
        log.info("为支付 {} 创建财务记录成功", payment.getPaymentNo());

        return FinanceRecordDto.fromEntity(saved);
    }

    /**
     * 获取财务记录详情
     */
    @Transactional(readOnly = true)
    public FinanceRecordDto getFinanceRecord(Long id) {
        FinanceRecord record = financeRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("财务记录不存在"));
        return FinanceRecordDto.fromEntity(record);
    }

    /**
     * 根据记录编号获取财务记录
     */
    @Transactional(readOnly = true)
    public FinanceRecordDto getFinanceRecordByNo(String recordNo) {
        FinanceRecord record = financeRecordRepository.findByRecordNo(recordNo)
                .orElseThrow(() -> new BusinessException("财务记录不存在"));
        return FinanceRecordDto.fromEntity(record);
    }

    /**
     * 分页查询财务记录
     */
    @Transactional(readOnly = true)
    public Page<FinanceRecordDto> getFinanceRecords(Pageable pageable) {
        return financeRecordRepository.findAll(pageable)
                .map(FinanceRecordDto::fromEntity);
    }

    /**
     * 根据类型分页查询财务记录
     */
    @Transactional(readOnly = true)
    public Page<FinanceRecordDto> getFinanceRecordsByType(FinanceRecord.FinanceType type, Pageable pageable) {
        return financeRecordRepository.findByType(type, pageable)
                .map(FinanceRecordDto::fromEntity);
    }

    /**
     * 根据分类分页查询财务记录
     */
    @Transactional(readOnly = true)
    public Page<FinanceRecordDto> getFinanceRecordsByCategory(String category, Pageable pageable) {
        return financeRecordRepository.findByCategory(category, pageable)
                .map(FinanceRecordDto::fromEntity);
    }

    /**
     * 获取订单相关的财务记录
     */
    @Transactional(readOnly = true)
    public List<FinanceRecordDto> getFinanceRecordsByOrder(Long orderId) {
        return financeRecordRepository.findByOrderId(orderId).stream()
                .map(FinanceRecordDto::fromEntity)
                .toList();
    }

    /**
     * 获取支付相关的财务记录
     */
    @Transactional(readOnly = true)
    public List<FinanceRecordDto> getFinanceRecordsByPayment(Long paymentId) {
        return financeRecordRepository.findByPaymentId(paymentId).stream()
                .map(FinanceRecordDto::fromEntity)
                .toList();
    }

    /**
     * 获取指定时间范围的财务统计
     */
    @Transactional(readOnly = true)
    public FinanceStatisticsDto getFinanceStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("获取财务统计数据: {} - {}", startTime, endTime);

        BigDecimal totalIncome = financeRecordRepository.calculateTotalIncomeByPeriod(startTime, endTime);
        BigDecimal totalExpense = financeRecordRepository.calculateTotalExpenseByPeriod(startTime, endTime);
        BigDecimal totalRefund = financeRecordRepository.calculateTotalRefundByPeriod(startTime, endTime);

        // 获取分类统计
        Map<String, BigDecimal> incomeByCategory = getAmountByCategory(startTime, endTime, FinanceRecord.FinanceType.INCOME);
        Map<String, BigDecimal> expenseByCategory = getAmountByCategory(startTime, endTime, FinanceRecord.FinanceType.EXPENSE);
        Map<String, BigDecimal> refundByCategory = getAmountByCategory(startTime, endTime, FinanceRecord.FinanceType.REFUND);

        // 获取交易总数
        List<FinanceRecord> records = financeRecordRepository.findByCreatedAtBetween(startTime, endTime);
        int totalTransactions = records.size();

        FinanceStatisticsDto statistics = new FinanceStatisticsDto();
        statistics.setTotalIncome(totalIncome);
        statistics.setTotalExpense(totalExpense);
        statistics.setTotalRefund(totalRefund);
        statistics.setIncomeByCategory(incomeByCategory);
        statistics.setExpenseByCategory(expenseByCategory);
        statistics.setRefundByCategory(refundByCategory);
        statistics.setTotalTransactions(totalTransactions);
        statistics.setPeriod(startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                          " 至 " + endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        statistics.calculateNetProfit();

        return statistics;
    }

    /**
     * 删除财务记录
     */
    public void deleteFinanceRecord(Long id) {
        if (!financeRecordRepository.existsById(id)) {
            throw new BusinessException("财务记录不存在");
        }
        financeRecordRepository.deleteById(id);
        log.info("删除财务记录成功，ID: {}", id);
    }

    /**
     * 生成财务记录编号
     */
    private String generateRecordNo() {
        String prefix = "FIN";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        String recordNo = prefix + timestamp + random;

        // 确保编号唯一
        while (financeRecordRepository.existsByRecordNo(recordNo)) {
            random = String.format("%04d", (int) (Math.random() * 10000));
            recordNo = prefix + timestamp + random;
        }

        return recordNo;
    }

    /**
     * 获取分类统计数据
     */
    private Map<String, BigDecimal> getAmountByCategory(LocalDateTime startTime, LocalDateTime endTime, FinanceRecord.FinanceType type) {
        List<Object[]> results = financeRecordRepository.calculateAmountByCategory(startTime, endTime, type);
        Map<String, BigDecimal> categoryMap = new HashMap<>();

        for (Object[] result : results) {
            String category = (String) result[0];
            BigDecimal amount = (BigDecimal) result[1];
            categoryMap.put(category, amount);
        }

        return categoryMap;
    }

    /**
     * 分页查询财务记录（支持多条件筛选）
     */
    @Transactional(readOnly = true)
    public Page<FinanceRecordDto> getFinanceRecords(FinanceRecord.FinanceType type, String category,
                                                   LocalDate startDate, LocalDate endDate, Pageable pageable) {
        // 转换日期为时间范围
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        // 如果没有任何筛选条件，返回所有记录
        if (type == null && category == null && startDateTime == null && endDateTime == null) {
            return financeRecordRepository.findAll(pageable).map(FinanceRecordDto::fromEntity);
        }

        // 根据条件组合查询
        Page<FinanceRecord> recordPage;

        if (type != null && category != null && startDateTime != null && endDateTime != null) {
            // 全部条件都有
            recordPage = financeRecordRepository.findByTypeAndCategoryContainingAndCreatedAtBetween(
                type, category, startDateTime, endDateTime, pageable);
        } else if (type != null && category != null) {
            // 类型和分类
            recordPage = financeRecordRepository.findByTypeAndCategoryContaining(type, category, pageable);
        } else if (type != null && startDateTime != null && endDateTime != null) {
            // 类型和时间范围
            recordPage = financeRecordRepository.findByTypeAndCreatedAtBetween(type, startDateTime, endDateTime, pageable);
        } else if (category != null && startDateTime != null && endDateTime != null) {
            // 分类和时间范围
            recordPage = financeRecordRepository.findByCategoryContainingAndCreatedAtBetween(
                category, startDateTime, endDateTime, pageable);
        } else if (type != null) {
            // 仅类型
            recordPage = financeRecordRepository.findByType(type, pageable);
        } else if (category != null) {
            // 仅分类
            recordPage = financeRecordRepository.findByCategoryContaining(category, pageable);
        } else if (startDateTime != null && endDateTime != null) {
            // 仅时间范围
            recordPage = financeRecordRepository.findByCreatedAtBetween(startDateTime, endDateTime, pageable);
        } else {
            // 其他情况，返回所有记录
            recordPage = financeRecordRepository.findAll(pageable);
        }

        return recordPage.map(FinanceRecordDto::fromEntity);
    }
}
