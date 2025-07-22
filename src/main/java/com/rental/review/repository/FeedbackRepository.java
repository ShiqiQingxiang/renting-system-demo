package com.rental.review.repository;

import com.rental.review.model.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 反馈数据访问层
 */
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    /**
     * 根据反馈编号查找反馈
     */
    Optional<Feedback> findByFeedbackNo(String feedbackNo);

    /**
     * 根据用户ID分页查询反馈
     */
    Page<Feedback> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据反馈类型分页查询
     */
    Page<Feedback> findByType(Feedback.FeedbackType type, Pageable pageable);

    /**
     * 根据状态分页查询反馈
     */
    Page<Feedback> findByStatus(Feedback.FeedbackStatus status, Pageable pageable);

    /**
     * 根据优先级分页查询反馈
     */
    Page<Feedback> findByPriority(Feedback.FeedbackPriority priority, Pageable pageable);

    /**
     * 根据分配的处理人查询反馈
     */
    Page<Feedback> findByAssignedTo(Long assignedTo, Pageable pageable);

    /**
     * 根据分类查询反馈
     */
    Page<Feedback> findByCategory(String category, Pageable pageable);

    /**
     * 搜索反馈（标题和内容）
     */
    @Query("SELECT f FROM Feedback f WHERE " +
           "f.title LIKE %:keyword% OR f.content LIKE %:keyword%")
    Page<Feedback> searchFeedbacks(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查询指定时间范围内的反馈
     */
    Page<Feedback> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * 统计各状态的反馈数量
     */
    @Query("SELECT f.status, COUNT(f) FROM Feedback f GROUP BY f.status")
    List<Object[]> countByStatus();

    /**
     * 统计各类型的反馈数量
     */
    @Query("SELECT f.type, COUNT(f) FROM Feedback f GROUP BY f.type")
    List<Object[]> countByType();

    /**
     * 查询待处理的反馈（开放和处理中状态）
     */
    Page<Feedback> findByStatusIn(List<Feedback.FeedbackStatus> statuses, Pageable pageable);

    /**
     * 查询超时未处理的反馈
     */
    @Query("SELECT f FROM Feedback f WHERE f.status = 'OPEN' AND f.createdAt < :deadline")
    List<Feedback> findOverdueFeedbacks(@Param("deadline") LocalDateTime deadline);
}
