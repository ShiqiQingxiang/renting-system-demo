package com.rental.review.repository;

import com.rental.review.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 评价数据访问层
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 根据评价编号查找评价
     */
    Optional<Review> findByReviewNo(String reviewNo);

    /**
     * 根据物品ID分页查询评价
     */
    Page<Review> findByItemIdAndStatus(Long itemId, Review.ReviewStatus status, Pageable pageable);

    /**
     * 根据用户ID分页查询评价（评价者）
     */
    Page<Review> findByReviewerId(Long reviewerId, Pageable pageable);

    /**
     * 根据物品拥有者ID分页查询评价
     */
    Page<Review> findByOwnerId(Long ownerId, Pageable pageable);

    /**
     * 根据订单ID查询评价
     */
    Optional<Review> findByOrderId(Long orderId);

    /**
     * 根据状态分页查询评价
     */
    Page<Review> findByStatus(Review.ReviewStatus status, Pageable pageable);

    /**
     * 查询物品的平均评分
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.itemId = :itemId AND r.status = 'APPROVED'")
    Double getAverageRatingByItemId(@Param("itemId") Long itemId);

    /**
     * 查询物品的评价数量
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.itemId = :itemId AND r.status = 'APPROVED'")
    Long getReviewCountByItemId(@Param("itemId") Long itemId);

    /**
     * 查询用户是否已经评价过某个订单
     */
    boolean existsByOrderIdAndReviewerId(Long orderId, Long reviewerId);

    /**
     * 根据评分范围查询评价
     */
    Page<Review> findByItemIdAndRatingBetweenAndStatus(
            Long itemId, Integer minRating, Integer maxRating,
            Review.ReviewStatus status, Pageable pageable);

    /**
     * 搜索评价（标题和内容）
     */
    @Query("SELECT r FROM Review r WHERE r.status = 'APPROVED' AND " +
           "(r.title LIKE %:keyword% OR r.content LIKE %:keyword%)")
    Page<Review> searchReviews(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查询最新的评价
     */
    List<Review> findTop10ByStatusOrderByCreatedAtDesc(Review.ReviewStatus status);
}
