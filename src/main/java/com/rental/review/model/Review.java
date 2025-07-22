package com.rental.review.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评价实体类
 */
@Data
@Entity
@Table(name = "reviews")
@EqualsAndHashCode(callSuper = false)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_no", unique = true, nullable = false, length = 64)
    private String reviewNo;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private Integer rating;

    @Column(name = "quality_rating")
    private Integer qualityRating;

    @Column(name = "service_rating")
    private Integer serviceRating;

    @Column(name = "delivery_rating")
    private Integer deliveryRating;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String images;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type")
    private ReviewType reviewType = ReviewType.ORDER;

    @Enumerated(EnumType.STRING)
    private ReviewStatus status = ReviewStatus.PENDING;

    @Column(name = "is_anonymous")
    private Boolean isAnonymous = false;

    @Column(name = "helpful_count")
    private Integer helpfulCount = 0;

    @Column(name = "reply_count")
    private Integer replyCount = 0;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 关联评价回复（一对多）
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewReply> replies;

    /**
     * 评价类型枚举
     */
    public enum ReviewType {
        ORDER,    // 订单评价
        ITEM,     // 物品评价
        SERVICE   // 服务评价
    }

    /**
     * 评价状态枚举
     */
    public enum ReviewStatus {
        PENDING,   // 待审核
        APPROVED,  // 已通过
        REJECTED,  // 已拒绝
        HIDDEN     // 已隐藏
    }
}
