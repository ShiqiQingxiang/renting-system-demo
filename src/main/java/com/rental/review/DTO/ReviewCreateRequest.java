package com.rental.review.DTO;

import com.rental.review.model.Review;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评价创建请求DTO
 */
@Data
public class ReviewCreateRequest {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "物品ID不能为空")
    private Long itemId;

    @NotNull(message = "物品拥有者ID不能为空")
    private Long ownerId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分不能低于1分")
    @Max(value = 5, message = "评分不能超过5分")
    private Integer rating;

    @Min(value = 1, message = "质量评分不能低于1分")
    @Max(value = 5, message = "质量评分不能超过5分")
    private Integer qualityRating;

    @Min(value = 1, message = "服务评分不能低于1分")
    @Max(value = 5, message = "服务评分不能超过5分")
    private Integer serviceRating;

    @Min(value = 1, message = "配送评分不能低于1分")
    @Max(value = 5, message = "配送评分不能超过5分")
    private Integer deliveryRating;

    @Size(max = 200, message = "评价标题不能超过200字符")
    private String title;

    @Size(max = 1000, message = "评价内容不能超过1000字符")
    private String content;

    private String images;

    private Review.ReviewType reviewType = Review.ReviewType.ORDER;

    private Boolean isAnonymous = false;
}
