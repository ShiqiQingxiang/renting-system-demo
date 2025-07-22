package com.rental.review.DTO;

import com.rental.review.model.Review;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评价响应DTO
 */
@Data
public class ReviewDTO {

    private Long id;
    private String reviewNo;
    private Long orderId;
    private Long itemId;
    private Long reviewerId;
    private String reviewerName;
    private String reviewerAvatar;
    private Long ownerId;
    private String ownerName;
    private Integer rating;
    private Integer qualityRating;
    private Integer serviceRating;
    private Integer deliveryRating;
    private String title;
    private String content;
    private String images;
    private Review.ReviewType reviewType;
    private Review.ReviewStatus status;
    private Boolean isAnonymous;
    private Integer helpfulCount;
    private Integer replyCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 物品信息
    private String itemName;
    private String itemImage;

    // 评价回复列表
    private List<ReviewReplyDTO> replies;
}
