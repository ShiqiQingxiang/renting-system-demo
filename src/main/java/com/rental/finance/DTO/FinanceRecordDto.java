package com.rental.finance.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rental.finance.model.FinanceRecord;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 财务记录数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinanceRecordDto {

    private Long id;

    private String recordNo;

    private Long orderId;

    private String orderNo;

    private Long paymentId;

    private String paymentNo;

    private FinanceRecord.FinanceType type;

    private String typeDescription;

    private String category;

    private BigDecimal amount;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 从实体转换为DTO
     */
    public static FinanceRecordDto fromEntity(FinanceRecord record) {
        FinanceRecordDto dto = new FinanceRecordDto();
        dto.setId(record.getId());
        dto.setRecordNo(record.getRecordNo());
        dto.setType(record.getType());
        dto.setTypeDescription(record.getType().getDescription());
        dto.setCategory(record.getCategory());
        dto.setAmount(record.getAmount());
        dto.setDescription(record.getDescription());
        dto.setCreatedAt(record.getCreatedAt());

        if (record.getOrder() != null) {
            dto.setOrderId(record.getOrder().getId());
            dto.setOrderNo(record.getOrder().getOrderNo());
        }

        if (record.getPayment() != null) {
            dto.setPaymentId(record.getPayment().getId());
            dto.setPaymentNo(record.getPayment().getPaymentNo());
        }

        return dto;
    }
}
