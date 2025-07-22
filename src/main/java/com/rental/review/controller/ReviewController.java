package com.rental.review.controller;

import com.rental.review.DTO.*;
import com.rental.review.model.Review;
import com.rental.review.service.ReviewService;
import com.rental.common.response.ApiResponse;
import com.rental.common.response.PageResponse;
import com.rental.security.userdetails.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 评价管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Validated
@Tag(name = "评价管理", description = "评价管理相关接口")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 创建评价
     */
    @PostMapping
    @Operation(summary = "创建评价", description = "用户对订单进行评价")
    @PreAuthorize("hasAuthority('REVIEW_CREATE')")
    public ApiResponse<ReviewDTO> createReview(
            @Valid @RequestBody ReviewCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("User {} creating review for order {}", userDetails.getUserId(), request.getOrderId());

        ReviewDTO review = reviewService.createReview(request, userDetails.getUserId());
        return ApiResponse.success("评价创建成功", review);
    }

    /**
     * 获取评价列表
     */
    @GetMapping
    @Operation(summary = "获取评价列表", description = "分页获取评价列表")
    @PreAuthorize("hasAuthority('REVIEW_VIEW')")
    public ApiResponse<PageResponse<ReviewDTO>> getReviews(
            @PageableDefault Pageable pageable,
            @Parameter(description = "评价状态") @RequestParam(required = false) Review.ReviewStatus status) {

        log.info("Getting reviews with status: {}, page: {}", status, pageable.getPageNumber());

        PageResponse<ReviewDTO> reviews = reviewService.getReviews(pageable, status);
        return ApiResponse.success("获取评价列表成功", reviews);
    }

    /**
     * 获取评价详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取评价详情", description = "根据ID获取评价详情")
    @PreAuthorize("hasAuthority('REVIEW_VIEW')")
    public ApiResponse<ReviewDTO> getReviewById(
            @Parameter(description = "评价ID") @PathVariable Long id) {

        log.info("Getting review by ID: {}", id);

        ReviewDTO review = reviewService.getReviewById(id);
        return ApiResponse.success("获取评价详情成功", review);
    }

    /**
     * 更新评价
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新评价", description = "用户更新自己的评价")
    @PreAuthorize("hasAuthority('REVIEW_CREATE')")
    public ApiResponse<ReviewDTO> updateReview(
            @Parameter(description = "评价ID") @PathVariable Long id,
            @Valid @RequestBody ReviewUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("User {} updating review {}", userDetails.getUserId(), id);

        ReviewDTO review = reviewService.updateReview(id, request, userDetails.getUserId());
        return ApiResponse.success("评价更新成功", review);
    }

    /**
     * 删除评价
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除评价", description = "用户删除自己的评价")
    @PreAuthorize("hasAuthority('REVIEW_DELETE')")
    public ApiResponse<String> deleteReview(
            @Parameter(description = "评价ID") @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("User {} deleting review {}", userDetails.getUserId(), id);

        reviewService.deleteReview(id, userDetails.getUserId());
        return ApiResponse.success("评价删除成功", "删除成功");
    }

    /**
     * 获取物品评价
     */
    @GetMapping("/item/{itemId}")
    @Operation(summary = "获取物品评价", description = "获取指定物品的评价列表")
    public ApiResponse<PageResponse<ReviewDTO>> getItemReviews(
            @Parameter(description = "物品ID") @PathVariable Long itemId,
            @PageableDefault Pageable pageable) {

        log.info("Getting reviews for item: {}", itemId);

        PageResponse<ReviewDTO> reviews = reviewService.getItemReviews(itemId, pageable);
        return ApiResponse.success("获取物品评价成功", reviews);
    }

    /**
     * 获取用户评价
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户评价", description = "获取指定用户的评价列表")
    @PreAuthorize("hasAuthority('REVIEW_VIEW')")
    public ApiResponse<PageResponse<ReviewDTO>> getUserReviews(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @PageableDefault Pageable pageable) {

        log.info("Getting reviews for user: {}", userId);

        PageResponse<ReviewDTO> reviews = reviewService.getUserReviews(userId, pageable);
        return ApiResponse.success("获取用户评价成功", reviews);
    }

    /**
     * 回复评价
     */
    @PostMapping("/{id}/reply")
    @Operation(summary = "回复评价", description = "商家或客服回复评价")
    @PreAuthorize("hasAuthority('REVIEW_REPLY')")
    public ApiResponse<ReviewReplyDTO> replyToReview(
            @Parameter(description = "评价ID") @PathVariable Long id,
            @Valid @RequestBody ReviewReplyRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("User {} replying to review {}", userDetails.getUserId(), id);

        ReviewReplyDTO reply = reviewService.replyToReview(id, request, userDetails.getUserId());
        return ApiResponse.success("回复评价成功", reply);
    }

    /**
     * 审核评价（管理员功能）
     */
    @PutMapping("/{id}/moderate")
    @Operation(summary = "审核评价", description = "管理员审核评价")
    @PreAuthorize("hasAuthority('REVIEW_MODERATE')")
    public ApiResponse<ReviewDTO> moderateReview(
            @Parameter(description = "评价ID") @PathVariable Long id,
            @Parameter(description = "审核状态") @RequestParam Review.ReviewStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Admin {} moderating review {} to status {}", userDetails.getUserId(), id, status);

        // 这里可以添加审核逻辑
        ReviewDTO review = reviewService.getReviewById(id);
        return ApiResponse.success("评价审核成功", review);
    }

    /**
     * 获取我的评价
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的评价", description = "获取当前用户的评价列表")
    @PreAuthorize("hasAuthority('REVIEW_VIEW')")
    public ApiResponse<PageResponse<ReviewDTO>> getMyReviews(
            @PageableDefault Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Getting reviews for current user: {}", userDetails.getUserId());

        PageResponse<ReviewDTO> reviews = reviewService.getUserReviews(userDetails.getUserId(), pageable);
        return ApiResponse.success("获取我的评价成功", reviews);
    }
}
