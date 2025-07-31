package com.rental.payment.service;

import com.rental.payment.DTO.MerchantPaymentConfigDTO;
import com.rental.payment.DTO.MerchantPaymentConfigRequest;
import com.rental.payment.model.MerchantPaymentConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MerchantPaymentConfigService {

    /**
     * 创建或更新商家支付配置
     *
     * @param merchantId 商家ID
     * @param request 配置请求
     * @return 配置信息
     */
    MerchantPaymentConfigDTO saveOrUpdateConfig(Long merchantId, MerchantPaymentConfigRequest request);

    /**
     * 获取商家支付配置
     *
     * @param merchantId 商家ID
     * @return 配置信息
     */
    MerchantPaymentConfigDTO getMerchantConfig(Long merchantId);

    /**
     * 获取商家的有效支付配置
     *
     * @param merchantId 商家ID
     * @return 配置信息，如果不存在或未启用则返回null
     */
    MerchantPaymentConfig getActiveMerchantConfig(Long merchantId);

    /**
     * 删除商家支付配置
     *
     * @param merchantId 商家ID
     */
    void deleteConfig(Long merchantId);

    /**
     * 启用商家支付配置
     *
     * @param merchantId 商家ID
     * @return 更新后的配置信息
     */
    MerchantPaymentConfigDTO enableConfig(Long merchantId);

    /**
     * 禁用商家支付配置
     *
     * @param merchantId 商家ID
     * @return 更新后的配置信息
     */
    MerchantPaymentConfigDTO disableConfig(Long merchantId);

    /**
     * 验证支付宝配置的有效性
     *
     * @param request 配置请求
     * @return 验证结果
     */
    boolean validateAlipayConfig(MerchantPaymentConfigRequest request);

    /**
     * 检查商家是否已配置支付信息
     *
     * @param merchantId 商家ID
     * @return 是否已配置
     */
    boolean hasConfig(Long merchantId);

    /**
     * 获取所有商家配置（管理员功能）
     *
     * @param pageable 分页参数
     * @return 配置列表
     */
    Page<MerchantPaymentConfigDTO> getAllConfigs(Pageable pageable);

    /**
     * 根据状态查询配置
     *
     * @param status 配置状态
     * @param pageable 分页参数
     * @return 配置列表
     */
    Page<MerchantPaymentConfigDTO> getConfigsByStatus(MerchantPaymentConfig.ConfigStatus status, Pageable pageable);

    /**
     * 批量获取商家配置
     *
     * @param merchantIds 商家ID列表
     * @return 配置列表
     */
    List<MerchantPaymentConfig> getBatchMerchantConfigs(List<Long> merchantIds);

    /**
     * 测试商家支付配置连通性
     *
     * @param merchantId 商家ID
     * @return 测试结果
     */
    boolean testConfig(Long merchantId);

    /**
     * 获取配置统计信息
     *
     * @return 统计信息
     */
    ConfigStatistics getConfigStatistics();

    /**
     * 获取商家原始配置对象（用于内部处理）
     */
    MerchantPaymentConfig getMerchantConfigRaw(Long merchantId);

    /**
     * 配置统计信息
     */
    class ConfigStatistics {
        private long totalConfigs;
        private long activeConfigs;
        private long inactiveConfigs;
        private long pendingConfigs;

        // Getters and Setters
        public long getTotalConfigs() { return totalConfigs; }
        public void setTotalConfigs(long totalConfigs) { this.totalConfigs = totalConfigs; }
        public long getActiveConfigs() { return activeConfigs; }
        public void setActiveConfigs(long activeConfigs) { this.activeConfigs = activeConfigs; }
        public long getInactiveConfigs() { return inactiveConfigs; }
        public void setInactiveConfigs(long inactiveConfigs) { this.inactiveConfigs = inactiveConfigs; }
        public long getPendingConfigs() { return pendingConfigs; }
        public void setPendingConfigs(long pendingConfigs) { this.pendingConfigs = pendingConfigs; }
    }
}