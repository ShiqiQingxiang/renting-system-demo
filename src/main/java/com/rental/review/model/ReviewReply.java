package com.rental.review.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 评价回复实体类
 */
@Data
@Entity
@Table(name = "review_replies")
@EqualsAndHashCode(callSuper = false)
public class ReviewReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "replier_id", nullable = false)
    private Long replierId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "reply_type")
    private ReplyType replyType = ReplyType.OWNER;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 多对一关联到评价
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", insertable = false, updatable = false)
    private Review review;

    /**
     * 回复类型枚举
     */
    public enum ReplyType {
        OWNER,            // 物品拥有者回复
        ADMIN,            // 管理员回复
        CUSTOMER_SERVICE  // 客服回复
    }
}
