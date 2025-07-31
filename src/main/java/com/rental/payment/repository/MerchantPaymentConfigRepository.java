package com.rental.payment.repository;

import com.rental.payment.model.MerchantPaymentConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantPaymentConfigRepository extends JpaRepository<MerchantPaymentConfig, Long> {

    /**
     * 根据商家ID查找配置
     */
    Optional<MerchantPaymentConfig> findByMerchantId(Long merchantId);

    /**
     * 检查商家是否已有配置
     */
    boolean existsByMerchantId(Long merchantId);

    /**
     * 根据支付宝应用ID查找配置
     */
    Optional<MerchantPaymentConfig> findByAlipayAppId(String alipayAppId);

    /**
     * 查找所有启用状态的配置
     */
    List<MerchantPaymentConfig> findByStatus(MerchantPaymentConfig.ConfigStatus status);

    /**
     * 查找指定商家的启用配置
     */
    @Query("SELECT c FROM MerchantPaymentConfig c WHERE c.merchantId = :merchantId AND c.status = 'ACTIVE'")
    Optional<MerchantPaymentConfig> findActiveMerchantConfig(@Param("merchantId") Long merchantId);

    /**
     * 统计启用状态的配置数量
     */
    long countByStatus(MerchantPaymentConfig.ConfigStatus status);

    /**
     * 根据商家ID列表查找配置
     */
    @Query("SELECT c FROM MerchantPaymentConfig c WHERE c.merchantId IN :merchantIds")
    List<MerchantPaymentConfig> findByMerchantIds(@Param("merchantIds") List<Long> merchantIds);

    /**
     * 删除指定商家的配置
     */
    void deleteByMerchantId(Long merchantId);

    /**
     * 根据状态分页查找配置
     */
    Page<MerchantPaymentConfig> findByStatus(MerchantPaymentConfig.ConfigStatus status, Pageable pageable);
}