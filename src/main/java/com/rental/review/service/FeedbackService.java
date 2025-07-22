package com.rental.review.service;

import com.rental.review.DTO.*;
import com.rental.review.model.Feedback;
import com.rental.review.repository.FeedbackRepository;
import com.rental.common.response.PageResponse;
import com.rental.common.exception.ResourceNotFoundException;
import com.rental.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 反馈服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    /**
     * 创建反馈
     */
    @Transactional
    public FeedbackDTO createFeedback(FeedbackCreateRequest request, Long userId) {
        log.info("Creating feedback for user: {}, type: {}", userId, request.getType());

        // 验证用户是否存在
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 创建反馈实体
        Feedback feedback = new Feedback();
        feedback.setFeedbackNo(generateFeedbackNo());
        feedback.setUserId(userId);
        feedback.setType(request.getType());
        feedback.setCategory(request.getCategory());
        feedback.setTitle(request.getTitle());
        feedback.setContent(request.getContent());
        feedback.setAttachments(request.getAttachments());
        feedback.setPriority(request.getPriority());
        feedback.setStatus(Feedback.FeedbackStatus.OPEN);

        feedback = feedbackRepository.save(feedback);

        log.info("Feedback created successfully with ID: {}", feedback.getId());
        return convertToDTO(feedback);
    }

    /**
     * 获取反馈列表
     */
    public PageResponse<FeedbackDTO> getFeedbacks(Pageable pageable,
                                                 Feedback.FeedbackType type,
                                                 Feedback.FeedbackStatus status,
                                                 Feedback.FeedbackPriority priority) {
        log.info("Getting feedbacks with type: {}, status: {}, priority: {}", type, status, priority);

        Page<Feedback> feedbackPage;

        if (type != null) {
            feedbackPage = feedbackRepository.findByType(type, pageable);
        } else if (status != null) {
            feedbackPage = feedbackRepository.findByStatus(status, pageable);
        } else if (priority != null) {
            feedbackPage = feedbackRepository.findByPriority(priority, pageable);
        } else {
            feedbackPage = feedbackRepository.findAll(pageable);
        }

        return PageResponse.of(
                feedbackPage.getContent().stream()
                        .map(this::convertToDTO)
                        .toList(),
                feedbackPage.getNumber(),
                feedbackPage.getSize(),
                feedbackPage.getTotalElements()
        );
    }

    /**
     * 获取反馈详情
     */
    public FeedbackDTO getFeedbackById(Long id) {
        log.info("Getting feedback by ID: {}", id);

        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("反馈不存在"));

        return convertToDTO(feedback);
    }

    /**
     * 处理反馈
     */
    @Transactional
    public FeedbackDTO processFeedback(Long id, FeedbackProcessRequest request, Long processorId) {
        log.info("Processing feedback ID: {} by user: {}", id, processorId);

        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("反馈不存在"));

        // 验证处理人是否存在
        userRepository.findById(processorId)
                .orElseThrow(() -> new ResourceNotFoundException("处理人不存在"));

        // 更新反馈状态
        if (request.getStatus() != null) {
            feedback.setStatus(request.getStatus());

            // 如果状态为已解决，设置解决时间
            if (request.getStatus() == Feedback.FeedbackStatus.RESOLVED) {
                feedback.setResolvedAt(LocalDateTime.now());
            }
        }

        if (request.getPriority() != null) {
            feedback.setPriority(request.getPriority());
        }

        if (request.getAssignedTo() != null) {
            // 验证被分配人是否存在
            userRepository.findById(request.getAssignedTo())
                    .orElseThrow(() -> new ResourceNotFoundException("被分配人不存在"));
            feedback.setAssignedTo(request.getAssignedTo());
        }

        if (request.getResolution() != null) {
            feedback.setResolution(request.getResolution());
        }

        feedback = feedbackRepository.save(feedback);

        log.info("Feedback processed successfully: {}", id);
        return convertToDTO(feedback);
    }

    /**
     * 分配反馈
     */
    @Transactional
    public FeedbackDTO assignFeedback(Long id, Long assigneeId, Long assignerId) {
        log.info("Assigning feedback ID: {} to user: {} by: {}", id, assigneeId, assignerId);

        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("反馈不存在"));

        // 验证分配人和被分配人是否存在
        userRepository.findById(assignerId)
                .orElseThrow(() -> new ResourceNotFoundException("分配人不存在"));

        userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("被分配人不存在"));

        feedback.setAssignedTo(assigneeId);

        // 如果反馈还是开放状态，更新为处理中
        if (feedback.getStatus() == Feedback.FeedbackStatus.OPEN) {
            feedback.setStatus(Feedback.FeedbackStatus.IN_PROGRESS);
        }

        feedback = feedbackRepository.save(feedback);

        log.info("Feedback assigned successfully: {}", id);
        return convertToDTO(feedback);
    }

    /**
     * 获取用户反馈
     */
    public PageResponse<FeedbackDTO> getUserFeedbacks(Long userId, Pageable pageable) {
        log.info("Getting feedbacks for user: {}", userId);

        // 验证用户是否存在
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Page<Feedback> feedbackPage = feedbackRepository.findByUserId(userId, pageable);

        return PageResponse.of(
                feedbackPage.getContent().stream()
                        .map(this::convertToDTO)
                        .toList(),
                feedbackPage.getNumber(),
                feedbackPage.getSize(),
                feedbackPage.getTotalElements()
        );
    }

    /**
     * 获取分配给我的反馈
     */
    public PageResponse<FeedbackDTO> getAssignedFeedbacks(Long assigneeId, Pageable pageable) {
        log.info("Getting assigned feedbacks for user: {}", assigneeId);

        Page<Feedback> feedbackPage = feedbackRepository.findByAssignedTo(assigneeId, pageable);

        return PageResponse.of(
                feedbackPage.getContent().stream()
                        .map(this::convertToDTO)
                        .toList(),
                feedbackPage.getNumber(),
                feedbackPage.getSize(),
                feedbackPage.getTotalElements()
        );
    }

    /**
     * 搜索反馈
     */
    public PageResponse<FeedbackDTO> searchFeedbacks(String keyword, Pageable pageable) {
        log.info("Searching feedbacks with keyword: {}", keyword);

        Page<Feedback> feedbackPage = feedbackRepository.searchFeedbacks(keyword, pageable);

        return PageResponse.of(
                feedbackPage.getContent().stream()
                        .map(this::convertToDTO)
                        .toList(),
                feedbackPage.getNumber(),
                feedbackPage.getSize(),
                feedbackPage.getTotalElements()
        );
    }

    /**
     * 生成反馈编号
     */
    private String generateFeedbackNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "FB" + timestamp + String.format("%03d", (int) (Math.random() * 1000));
    }

    /**
     * 转换为DTO
     */
    private FeedbackDTO convertToDTO(Feedback feedback) {
        FeedbackDTO dto = new FeedbackDTO();
        dto.setId(feedback.getId());
        dto.setFeedbackNo(feedback.getFeedbackNo());
        dto.setUserId(feedback.getUserId());
        dto.setType(feedback.getType());
        dto.setCategory(feedback.getCategory());
        dto.setTitle(feedback.getTitle());
        dto.setContent(feedback.getContent());
        dto.setAttachments(feedback.getAttachments());
        dto.setPriority(feedback.getPriority());
        dto.setStatus(feedback.getStatus());
        dto.setAssignedTo(feedback.getAssignedTo());
        dto.setResolution(feedback.getResolution());
        dto.setResolvedAt(feedback.getResolvedAt());
        dto.setCreatedAt(feedback.getCreatedAt());
        dto.setUpdatedAt(feedback.getUpdatedAt());

        // 获取用户信息
        userRepository.findById(feedback.getUserId()).ifPresent(user ->
            dto.setUserName(user.getUsername())
        );

        // 获取分配人信息
        if (feedback.getAssignedTo() != null) {
            userRepository.findById(feedback.getAssignedTo()).ifPresent(user ->
                dto.setAssignedToName(user.getUsername())
            );
        }

        return dto;
    }
}
