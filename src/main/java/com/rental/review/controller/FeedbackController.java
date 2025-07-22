package com.rental.review.controller;

import com.rental.review.DTO.*;
import com.rental.review.model.Feedback;
import com.rental.review.service.FeedbackService;
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
 * 反馈管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
@Validated
@Tag(name = "反馈管理", description = "反馈管理相关接口")
public class FeedbackController {

    private final FeedbackService feedbackService;

    /**
     * 创建反馈
     */
    @PostMapping
    @Operation(summary = "创建反馈", description = "用户提交反馈")
    @PreAuthorize("hasAuthority('FEEDBACK_CREATE')")
    public ApiResponse<FeedbackDTO> createFeedback(
            @Valid @RequestBody FeedbackCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("User {} creating feedback with type {}", userDetails.getUserId(), request.getType());

        FeedbackDTO feedback = feedbackService.createFeedback(request, userDetails.getUserId());
        return ApiResponse.success("反馈提交成功", feedback);
    }

    /**
     * 获取反馈列表
     */
    @GetMapping
    @Operation(summary = "获取反馈列表", description = "分页获取反馈列表")
    @PreAuthorize("hasAuthority('FEEDBACK_VIEW')")
    public ApiResponse<PageResponse<FeedbackDTO>> getFeedbacks(
            @PageableDefault Pageable pageable,
            @Parameter(description = "反馈类型") @RequestParam(required = false) Feedback.FeedbackType type,
            @Parameter(description = "反馈状态") @RequestParam(required = false) Feedback.FeedbackStatus status,
            @Parameter(description = "优先级") @RequestParam(required = false) Feedback.FeedbackPriority priority) {

        log.info("Getting feedbacks with type: {}, status: {}, priority: {}", type, status, priority);

        PageResponse<FeedbackDTO> feedbacks = feedbackService.getFeedbacks(pageable, type, status, priority);
        return ApiResponse.success("获取反馈列表成功", feedbacks);
    }

    /**
     * 获取反馈详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取反馈详情", description = "根据ID获取反馈详情")
    @PreAuthorize("hasAuthority('FEEDBACK_VIEW')")
    public ApiResponse<FeedbackDTO> getFeedbackById(
            @Parameter(description = "反馈ID") @PathVariable Long id) {

        log.info("Getting feedback by ID: {}", id);

        FeedbackDTO feedback = feedbackService.getFeedbackById(id);
        return ApiResponse.success("获取反馈详情成功", feedback);
    }

    /**
     * 处理反馈
     */
    @PutMapping("/{id}/process")
    @Operation(summary = "处理反馈", description = "客服处理反馈")
    @PreAuthorize("hasAuthority('FEEDBACK_PROCESS')")
    public ApiResponse<FeedbackDTO> processFeedback(
            @Parameter(description = "反馈ID") @PathVariable Long id,
            @Valid @RequestBody FeedbackProcessRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("User {} processing feedback {}", userDetails.getUserId(), id);

        FeedbackDTO feedback = feedbackService.processFeedback(id, request, userDetails.getUserId());
        return ApiResponse.success("反馈处理成功", feedback);
    }

    /**
     * 分配反馈
     */
    @PutMapping("/{id}/assign")
    @Operation(summary = "分配反馈", description = "将反馈分配给客服人员")
    @PreAuthorize("hasAuthority('FEEDBACK_ASSIGN')")
    public ApiResponse<FeedbackDTO> assignFeedback(
            @Parameter(description = "反馈ID") @PathVariable Long id,
            @Parameter(description = "被分配人ID") @RequestParam Long assigneeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("User {} assigning feedback {} to user {}", userDetails.getUserId(), id, assigneeId);

        FeedbackDTO feedback = feedbackService.assignFeedback(id, assigneeId, userDetails.getUserId());
        return ApiResponse.success("反馈分配成功", feedback);
    }

    /**
     * 获取用户反馈
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户反馈", description = "获取指定用户的反馈列表")
    @PreAuthorize("hasAuthority('FEEDBACK_VIEW')")
    public ApiResponse<PageResponse<FeedbackDTO>> getUserFeedbacks(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @PageableDefault Pageable pageable) {

        log.info("Getting feedbacks for user: {}", userId);

        PageResponse<FeedbackDTO> feedbacks = feedbackService.getUserFeedbacks(userId, pageable);
        return ApiResponse.success("获取用户反馈成功", feedbacks);
    }

    /**
     * 获取我的反馈
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的反馈", description = "获取当前用户的反馈列表")
    @PreAuthorize("hasAuthority('FEEDBACK_VIEW')")
    public ApiResponse<PageResponse<FeedbackDTO>> getMyFeedbacks(
            @PageableDefault Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Getting feedbacks for current user: {}", userDetails.getUserId());

        PageResponse<FeedbackDTO> feedbacks = feedbackService.getUserFeedbacks(userDetails.getUserId(), pageable);
        return ApiResponse.success("获取我的反馈成功", feedbacks);
    }

    /**
     * 获取分配给我的反馈
     */
    @GetMapping("/assigned")
    @Operation(summary = "获取分配给我的反馈", description = "获取分配给当前用户处理的反馈列表")
    @PreAuthorize("hasAuthority('FEEDBACK_PROCESS')")
    public ApiResponse<PageResponse<FeedbackDTO>> getAssignedFeedbacks(
            @PageableDefault Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Getting assigned feedbacks for user: {}", userDetails.getUserId());

        PageResponse<FeedbackDTO> feedbacks = feedbackService.getAssignedFeedbacks(userDetails.getUserId(), pageable);
        return ApiResponse.success("获取分配给我的反馈成功", feedbacks);
    }

    /**
     * 搜索反馈
     */
    @GetMapping("/search")
    @Operation(summary = "搜索反馈", description = "根据关键词搜索反馈")
    @PreAuthorize("hasAuthority('FEEDBACK_VIEW')")
    public ApiResponse<PageResponse<FeedbackDTO>> searchFeedbacks(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @PageableDefault Pageable pageable) {

        log.info("Searching feedbacks with keyword: {}", keyword);

        PageResponse<FeedbackDTO> feedbacks = feedbackService.searchFeedbacks(keyword, pageable);
        return ApiResponse.success("搜索反馈成功", feedbacks);
    }
}
