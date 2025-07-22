package com.rental.review.DTO;

import com.rental.review.model.ReviewReply;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价回复DTO
 */
@Data
public class ReviewReplyDTO {

    private Long id;
    private Long reviewId;
    private Long replierId;
    private String replierName;
    private String replierAvatar;
    private String content;
    private ReviewReply.ReplyType replyType;
    private LocalDateTime createdAt;
}
