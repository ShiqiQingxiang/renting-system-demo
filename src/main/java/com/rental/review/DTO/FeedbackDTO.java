package com.rental.review.DTO;

import com.rental.review.model.Feedback;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 反馈响应DTO
 */
@Data
public class FeedbackDTO {

    private Long id;
    private String feedbackNo;
    private Long userId;
    private String userName;
    private Feedback.FeedbackType type;
    private String category;
    private String title;
    private String content;
    private String attachments;
    private Feedback.FeedbackPriority priority;
    private Feedback.FeedbackStatus status;
    private Long assignedTo;
    private String assignedToName;
    private String resolution;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
