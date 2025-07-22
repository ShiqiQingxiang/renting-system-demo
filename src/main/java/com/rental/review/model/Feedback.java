package com.rental.review.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 反馈实体类
 */
@Data
@Entity
@Table(name = "feedbacks")
@EqualsAndHashCode(callSuper = false)
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "feedback_no", unique = true, nullable = false, length = 64)
    private String feedbackNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedbackType type;

    @Column(length = 100)
    private String category;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String attachments;

    @Enumerated(EnumType.STRING)
    private FeedbackPriority priority = FeedbackPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    private FeedbackStatus status = FeedbackStatus.OPEN;

    @Column(name = "assigned_to")
    private Long assignedTo;

    @Column(columnDefinition = "TEXT")
    private String resolution;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 反馈类型枚举
     */
    public enum FeedbackType {
        COMPLAINT,        // 投诉
        SUGGESTION,       // 建议
        BUG_REPORT,       // Bug报告
        FEATURE_REQUEST   // 功能请求
    }

    /**
     * 反馈优先级枚举
     */
    public enum FeedbackPriority {
        LOW,      // 低
        MEDIUM,   // 中
        HIGH,     // 高
        URGENT    // 紧急
    }

    /**
     * 反馈状态枚举
     */
    public enum FeedbackStatus {
        OPEN,         // 开放
        IN_PROGRESS,  // 处理中
        RESOLVED,     // 已解决
        CLOSED        // 已关闭
    }
}
