package com.rental.review.repository;

import com.rental.review.model.ReviewReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 评价回复数据访问层
 */
@Repository
public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Long> {

    /**
     * 根据评价ID查询所有回复
     */
    List<ReviewReply> findByReviewIdOrderByCreatedAtAsc(Long reviewId);

    /**
     * 根据评价ID分页查询回复
     */
    Page<ReviewReply> findByReviewId(Long reviewId, Pageable pageable);

    /**
     * 根据回复者ID查询回复
     */
    Page<ReviewReply> findByReplierId(Long replierId, Pageable pageable);

    /**
     * 统计评价的回复数量
     */
    long countByReviewId(Long reviewId);

    /**
     * 根据回复类型查询回复
     */
    List<ReviewReply> findByReviewIdAndReplyType(Long reviewId, ReviewReply.ReplyType replyType);
}
