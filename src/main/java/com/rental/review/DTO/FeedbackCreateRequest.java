package com.rental.review.DTO;

import com.rental.review.model.Feedback;
import lombok.Data;

import jakarta.validation.constraints.*;

/**
 * 反馈创建请求DTO
 */
@Data
public class FeedbackCreateRequest {

    @NotNull(message = "反馈类型不能为空")
    private Feedback.FeedbackType type;

    @Size(max = 100, message = "反馈分类不能超过100字符")
    private String category;

    @NotBlank(message = "反馈标题不能为空")
    @Size(max = 200, message = "反馈标题不能超过200字符")
    private String title;

    @NotBlank(message = "反馈内容不能为空")
    @Size(max = 2000, message = "反馈内容不能超过2000字符")
    private String content;

    private String attachments;

    private Feedback.FeedbackPriority priority = Feedback.FeedbackPriority.MEDIUM;
}
