package com.rental.review.DTO;

import com.rental.review.model.Feedback;
import lombok.Data;

import jakarta.validation.constraints.Size;

/**
 * 反馈处理请求DTO
 */
@Data
public class FeedbackProcessRequest {

    private Feedback.FeedbackStatus status;

    private Feedback.FeedbackPriority priority;

    private Long assignedTo;

    @Size(max = 1000, message = "处理结果不能超过1000字符")
    private String resolution;
}
