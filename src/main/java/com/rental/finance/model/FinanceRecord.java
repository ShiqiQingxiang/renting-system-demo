package com.rental.finance.model;

import com.rental.order.model.Order;
import com.rental.payment.model.Payment;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "finance_records", indexes = {
    @Index(name = "idx_record_no", columnList = "record_no"),
    @Index(name = "idx_type", columnList = "type"),
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FinanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_no", nullable = false, unique = true, length = 64)
    @NotBlank(message = "记录编号不能为空")
    private String recordNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "财务类型不能为空")
    private FinanceType type;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "财务分类不能为空")
    private String category;

    @Column(nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "金额必须大于等于0")
    private BigDecimal amount;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 财务类型枚举
     */
    public enum FinanceType {
        INCOME("收入"),
        EXPENSE("支出"),
        REFUND("退款");

        private final String description;

        FinanceType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
