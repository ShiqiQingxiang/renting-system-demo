package com.rental.contract.repository;

import com.rental.contract.model.ContractTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractTemplateRepository extends JpaRepository<ContractTemplate, Long> {

    // 基础查询
    Optional<ContractTemplate> findByName(String name);

    boolean existsByName(String name);

    List<ContractTemplate> findByNameContaining(String name);

    // 激活状态查询
    List<ContractTemplate> findByIsActive(Boolean isActive);

    Page<ContractTemplate> findByIsActive(Boolean isActive, Pageable pageable);

    // 版本查询
    List<ContractTemplate> findByVersion(String version);

    // 查询最新版本的模板
    @Query("SELECT ct FROM ContractTemplate ct WHERE ct.isActive = true ORDER BY ct.version DESC")
    List<ContractTemplate> findActiveTemplatesOrderByVersionDesc();

    // 查询默认模板（最新的激活模板）
    @Query("SELECT ct FROM ContractTemplate ct WHERE ct.isActive = true ORDER BY ct.createdAt DESC")
    Optional<ContractTemplate> findLatestActiveTemplate();

    // 统计使用次数
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.template.id = :templateId")
    long countUsageByTemplateId(@Param("templateId") Long templateId);
}
