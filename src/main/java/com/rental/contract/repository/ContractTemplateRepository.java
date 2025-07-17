package com.rental.contract.repository;

import com.rental.contract.model.ContractTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractTemplateRepository extends JpaRepository<ContractTemplate, Long> {

    /**
     * 根据名称查找模板
     */
    boolean existsByName(String name);

    /**
     * 检查名称是否被其他模板使用
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * 获取所有活跃的模板
     */
    List<ContractTemplate> findByIsActiveTrueOrderByCreatedAtDesc();

    /**
     * 根据状态查找模板
     */
    List<ContractTemplate> findByIsActive(Boolean isActive);

    /**
     * 统计使用该模板的合同数量
     */
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.template.id = :templateId")
    long countContractsByTemplateId(@Param("templateId") Long templateId);

    /**
     * 根据版本查找模板
     */
    List<ContractTemplate> findByVersion(String version);

    /**
     * 查找最新版本的模板
     */
    @Query("SELECT t FROM ContractTemplate t WHERE t.isActive = true ORDER BY t.version DESC, t.createdAt DESC")
    List<ContractTemplate> findLatestActiveTemplates();
}
