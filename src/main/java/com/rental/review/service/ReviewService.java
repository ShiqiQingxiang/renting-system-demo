package com.rental.review.service;

import com.rental.review.DTO.*;
import com.rental.review.model.Review;
import com.rental.review.model.ReviewReply;
import com.rental.review.repository.ReviewRepository;
import com.rental.review.repository.ReviewReplyRepository;
import com.rental.common.response.PageResponse;
import com.rental.common.exception.BusinessException;
import com.rental.common.exception.ResourceNotFoundException;
import com.rental.user.repository.UserRepository;
import com.rental.item.repository.ItemRepository;
import com.rental.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评价服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;

    /**
     * 创建评价
     */
    @Transactional
    public ReviewDTO createReview(ReviewCreateRequest request, Long reviewerId) {
        log.info("Creating review for order: {}, reviewer: {}", request.getOrderId(), reviewerId);

        // 验证订单是否存在
        var order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));

        // 验证用户是否有权限评价此订单
        if (!order.getUser().getId().equals(reviewerId)) {
            throw new BusinessException("您只能评价自己的订单");
        }

        // 验证是否已经评价过
        if (reviewRepository.existsByOrderIdAndReviewerId(request.getOrderId(), reviewerId)) {
            throw new BusinessException("该订单已经评价过了");
        }

        // 验证物品是否存在
        itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("物品不存在"));

        // 验证用户是否存在
        userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 创建评价实体
        Review review = new Review();
        review.setReviewNo(generateReviewNo());
        review.setOrderId(request.getOrderId());
        review.setItemId(request.getItemId());
        review.setReviewerId(reviewerId);
        review.setOwnerId(request.getOwnerId());
        review.setRating(request.getRating());
        review.setQualityRating(request.getQualityRating());
        review.setServiceRating(request.getServiceRating());
        review.setDeliveryRating(request.getDeliveryRating());
        review.setTitle(request.getTitle());
        review.setContent(request.getContent());
        review.setImages(request.getImages());
        review.setReviewType(request.getReviewType());
        review.setIsAnonymous(request.getIsAnonymous());
        review.setStatus(Review.ReviewStatus.PENDING);

        review = reviewRepository.save(review);

        log.info("Review created successfully with ID: {}", review.getId());
        return convertToDTO(review);
    }

    /**
     * 获取评价列表
     */
    public PageResponse<ReviewDTO> getReviews(Pageable pageable, Review.ReviewStatus status) {
        log.info("Getting reviews with status: {}, page: {}", status, pageable.getPageNumber());

        Page<Review> reviewPage;
        if (status != null) {
            reviewPage = reviewRepository.findByStatus(status, pageable);
        } else {
            reviewPage = reviewRepository.findAll(pageable);
        }

        List<ReviewDTO> reviewDTOs = reviewPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.of(
                reviewDTOs,
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalElements()
        );
    }

    /**
     * 获取评价详情
     */
    public ReviewDTO getReviewById(Long id) {
        log.info("Getting review by ID: {}", id);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("评价不存在"));

        ReviewDTO reviewDTO = convertToDTO(review);

        // 获取评价回复
        List<ReviewReply> replies = reviewReplyRepository.findByReviewIdOrderByCreatedAtAsc(id);
        List<ReviewReplyDTO> replyDTOs = replies.stream()
                .map(this::convertReplyToDTO)
                .collect(Collectors.toList());
        reviewDTO.setReplies(replyDTOs);

        return reviewDTO;
    }

    /**
     * 更新评价
     */
    @Transactional
    public ReviewDTO updateReview(Long id, ReviewUpdateRequest request, Long userId) {
        log.info("Updating review ID: {} by user: {}", id, userId);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("评价不存在"));

        // 验证权限：只有评价者本人可以修改评价
        if (!review.getReviewerId().equals(userId)) {
            throw new BusinessException("您只能修改自己的评价");
        }

        // 验证评价状态：只有待审核状态的评价可以修改
        if (review.getStatus() != Review.ReviewStatus.PENDING) {
            throw new BusinessException("只有待审核的评价可以修改");
        }

        // 更新评价信息
        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getQualityRating() != null) {
            review.setQualityRating(request.getQualityRating());
        }
        if (request.getServiceRating() != null) {
            review.setServiceRating(request.getServiceRating());
        }
        if (request.getDeliveryRating() != null) {
            review.setDeliveryRating(request.getDeliveryRating());
        }
        if (request.getTitle() != null) {
            review.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            review.setContent(request.getContent());
        }
        if (request.getImages() != null) {
            review.setImages(request.getImages());
        }
        if (request.getReviewType() != null) {
            review.setReviewType(request.getReviewType());
        }
        if (request.getIsAnonymous() != null) {
            review.setIsAnonymous(request.getIsAnonymous());
        }

        review = reviewRepository.save(review);

        log.info("Review updated successfully: {}", id);
        return convertToDTO(review);
    }

    /**
     * 删除评价
     */
    @Transactional
    public void deleteReview(Long id, Long userId) {
        log.info("Deleting review ID: {} by user: {}", id, userId);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("评价不存在"));

        // 验证权限：只有评价者本人可以删除评价
        if (!review.getReviewerId().equals(userId)) {
            throw new BusinessException("您只能删除自己的评价");
        }

        // 验证评价状态：只有待审核状态的评价可以删除
        if (review.getStatus() != Review.ReviewStatus.PENDING) {
            throw new BusinessException("只有待审核的评价可以删除");
        }

        reviewRepository.delete(review);
        log.info("Review deleted successfully: {}", id);
    }

    /**
     * 获取物品评价
     */
    public PageResponse<ReviewDTO> getItemReviews(Long itemId, Pageable pageable) {
        log.info("Getting reviews for item: {}", itemId);

        // 验证物品是否存在
        itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("物品不存在"));

        Page<Review> reviewPage = reviewRepository.findByItemIdAndStatus(
                itemId, Review.ReviewStatus.APPROVED, pageable);

        List<ReviewDTO> reviewDTOs = reviewPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.of(
                reviewDTOs,
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalElements()
        );
    }

    /**
     * 获取用户评价
     */
    public PageResponse<ReviewDTO> getUserReviews(Long userId, Pageable pageable) {
        log.info("Getting reviews for user: {}", userId);

        // 验证用户是否存在
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Page<Review> reviewPage = reviewRepository.findByReviewerId(userId, pageable);

        List<ReviewDTO> reviewDTOs = reviewPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.of(
                reviewDTOs,
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalElements()
        );
    }

    /**
     * 回复评价
     */
    @Transactional
    public ReviewReplyDTO replyToReview(Long reviewId, ReviewReplyRequest request, Long replierId) {
        log.info("Replying to review: {} by user: {}", reviewId, replierId);

        // 验证评价是否存在
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("评价不存在"));

        // 验证用户是否存在
        userRepository.findById(replierId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 创建回复
        ReviewReply reply = new ReviewReply();
        reply.setReviewId(reviewId);
        reply.setReplierId(replierId);
        reply.setContent(request.getContent());

        // 确定回复类型
        if (review.getOwnerId().equals(replierId)) {
            reply.setReplyType(ReviewReply.ReplyType.OWNER);
        } else {
            // 根据用户角色确定回复类型，这里简化处理
            reply.setReplyType(ReviewReply.ReplyType.CUSTOMER_SERVICE);
        }

        reply = reviewReplyRepository.save(reply);

        // 更新评价的回复数量
        long replyCount = reviewReplyRepository.countByReviewId(reviewId);
        review.setReplyCount((int) replyCount);
        reviewRepository.save(review);

        log.info("Reply created successfully for review: {}", reviewId);
        return convertReplyToDTO(reply);
    }

    /**
     * 生成评价编号
     */
    private String generateReviewNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "REV" + timestamp + String.format("%03d", (int) (Math.random() * 1000));
    }

    /**
     * 转换为DTO
     */
    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setReviewNo(review.getReviewNo());
        dto.setOrderId(review.getOrderId());
        dto.setItemId(review.getItemId());
        dto.setReviewerId(review.getReviewerId());
        dto.setOwnerId(review.getOwnerId());
        dto.setRating(review.getRating());
        dto.setQualityRating(review.getQualityRating());
        dto.setServiceRating(review.getServiceRating());
        dto.setDeliveryRating(review.getDeliveryRating());
        dto.setTitle(review.getTitle());
        dto.setContent(review.getContent());
        dto.setImages(review.getImages());
        dto.setReviewType(review.getReviewType());
        dto.setStatus(review.getStatus());
        dto.setIsAnonymous(review.getIsAnonymous());
        dto.setHelpfulCount(review.getHelpfulCount());
        dto.setReplyCount(review.getReplyCount());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());

        // 获取用户信息
        userRepository.findById(review.getReviewerId()).ifPresent(user -> {
            if (!review.getIsAnonymous()) {
                dto.setReviewerName(user.getUsername());
            } else {
                dto.setReviewerName("匿名用户");
            }
        });

        // 获取物品信息
        itemRepository.findById(review.getItemId()).ifPresent(item -> {
            dto.setItemName(item.getName());
        });

        return dto;
    }

    /**
     * 转换回复为DTO
     */
    private ReviewReplyDTO convertReplyToDTO(ReviewReply reply) {
        ReviewReplyDTO dto = new ReviewReplyDTO();
        dto.setId(reply.getId());
        dto.setReviewId(reply.getReviewId());
        dto.setReplierId(reply.getReplierId());
        dto.setContent(reply.getContent());
        dto.setReplyType(reply.getReplyType());
        dto.setCreatedAt(reply.getCreatedAt());

        // 获取回复者信息
        userRepository.findById(reply.getReplierId()).ifPresent(user -> {
            dto.setReplierName(user.getUsername());
        });

        return dto;
    }
}
