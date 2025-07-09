package com.rental.contract.repository;

import com.rental.contract.model.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    // 基础查询
    Optional<Contract> findByContractNo(String contractNo);

    boolean existsByContractNo(String contractNo);

    // 根据订单ID查询合同
    Optional<Contract> findByOrderId(Long orderId);

    // 状态查询
    List<Contract> findByStatus(Contract.ContractStatus status);

    Page<Contract> findByStatus(Contract.ContractStatus status, Pageable pageable);

    // 模板查询
    List<Contract> findByTemplateId(Long templateId);

    // 时间查询
    List<Contract> findBySignedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    List<Contract> findByExpiresAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    // 查询即将过期的合同
    @Query("SELECT c FROM Contract c WHERE c.status = 'SIGNED' AND c.expiresAt BETWEEN :startTime AND :endTime")
    List<Contract> findContractsExpiringSoon(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    // 查询已过期的合同
    @Query("SELECT c FROM Contract c WHERE c.status = 'SIGNED' AND c.expiresAt < :now")
    List<Contract> findExpiredContracts(@Param("now") LocalDateTime now);

    // 查询待签署的合同
    @Query("SELECT c FROM Contract c WHERE c.status = 'DRAFT'")
    List<Contract> findPendingContracts();

    // 复合条件查询
    @Query("SELECT c FROM Contract c WHERE " +
           "(:orderId IS NULL OR c.order.id = :orderId) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:templateId IS NULL OR c.template.id = :templateId)")
    Page<Contract> findContractsByConditions(
        @Param("orderId") Long orderId,
        @Param("status") Contract.ContractStatus status,
        @Param("templateId") Long templateId,
        Pageable pageable
    );

    // 统计查询
    long countByStatus(Contract.ContractStatus status);

    long countByTemplateId(Long templateId);

    @Query("SELECT COUNT(c) FROM Contract c WHERE c.createdAt >= :date")
    long countNewContractsAfter(@Param("date") LocalDateTime date);
}
