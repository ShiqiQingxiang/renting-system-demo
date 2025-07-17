package com.rental.contract.repository;

import com.rental.contract.model.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    /**
     * 根据合同编号查找
     */
    Optional<Contract> findByContractNo(String contractNo);

    /**
     * 根据订单ID查找合同
     */
    Optional<Contract> findByOrderId(Long orderId);

    /**
     * 检查订单是否已有合同
     */
    boolean existsByOrderId(Long orderId);

    /**
     * 根据状态查找合同
     */
    List<Contract> findByStatus(Contract.ContractStatus status);

    /**
     * 根据用户ID查找合同（通过订单关联）
     */
    @Query("SELECT c FROM Contract c WHERE c.order.user.id = :userId ORDER BY c.createdAt DESC")
    List<Contract> findByOrderUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * 复杂条件查询合同
     */
    @Query("""
        SELECT c FROM Contract c 
        LEFT JOIN c.order o 
        LEFT JOIN o.user u 
        WHERE (:keyword IS NULL OR c.contractNo LIKE %:keyword% OR u.username LIKE %:keyword% OR 
               (u.profile IS NOT NULL AND u.profile.realName LIKE %:keyword%))
        AND (:status IS NULL OR c.status = :status)
        AND (:orderId IS NULL OR c.order.id = :orderId)
        AND (:userId IS NULL OR o.user.id = :userId)
        AND (:signedDateStart IS NULL OR DATE(c.signedAt) >= :signedDateStart)
        AND (:signedDateEnd IS NULL OR DATE(c.signedAt) <= :signedDateEnd)
        AND (:expiryDateStart IS NULL OR DATE(c.expiresAt) >= :expiryDateStart)
        AND (:expiryDateEnd IS NULL OR DATE(c.expiresAt) <= :expiryDateEnd)
        """)
    Page<Contract> findContractsWithFilters(
        @Param("keyword") String keyword,
        @Param("status") Contract.ContractStatus status,
        @Param("orderId") Long orderId,
        @Param("userId") Long userId,
        @Param("signedDateStart") LocalDate signedDateStart,
        @Param("signedDateEnd") LocalDate signedDateEnd,
        @Param("expiryDateStart") LocalDate expiryDateStart,
        @Param("expiryDateEnd") LocalDate expiryDateEnd,
        Pageable pageable
    );

    /**
     * 查找即将过期的合同
     */
    @Query("SELECT c FROM Contract c WHERE c.status = 'SIGNED' AND c.expiresAt <= :expiryDate")
    List<Contract> findExpiringContracts(@Param("expiryDate") LocalDate expiryDate);

    /**
     * 统计各状态合同数量
     */
    @Query("SELECT c.status, COUNT(c) FROM Contract c GROUP BY c.status")
    List<Object[]> countContractsByStatus();

    /**
     * 根据模板ID统计合同数量
     */
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.template.id = :templateId")
    long countByTemplateId(@Param("templateId") Long templateId);

    /**
     * 查找用户的已签署合同
     */
    @Query("SELECT c FROM Contract c WHERE c.order.user.id = :userId AND c.status = 'SIGNED'")
    List<Contract> findSignedContractsByUserId(@Param("userId") Long userId);
}
