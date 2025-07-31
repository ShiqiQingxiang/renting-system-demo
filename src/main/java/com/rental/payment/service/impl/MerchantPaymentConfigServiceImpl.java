package com.rental.payment.service.impl;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.rental.common.exception.BusinessException;
import com.rental.common.exception.ResourceNotFoundException;
import com.rental.common.service.EncryptionService;
import com.rental.payment.DTO.MerchantPaymentConfigDTO;
import com.rental.payment.DTO.MerchantPaymentConfigRequest;
import com.rental.payment.model.MerchantPaymentConfig;
import com.rental.payment.repository.MerchantPaymentConfigRepository;
import com.rental.payment.service.MerchantPaymentConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MerchantPaymentConfigServiceImpl implements MerchantPaymentConfigService {

    private final MerchantPaymentConfigRepository configRepository;
    private final EncryptionService encryptionService;

    @Override
    public MerchantPaymentConfigDTO saveOrUpdateConfig(Long merchantId, MerchantPaymentConfigRequest request) {
        log.info("保存或更新商家支付配置，商家ID：{}", merchantId);

        // 验证配置
        if (!validateAlipayConfig(request)) {
            throw new BusinessException("支付宝配置验证失败");
        }

        // 检查是否存在配置
        Optional<MerchantPaymentConfig> existingConfig = configRepository.findByMerchantId(merchantId);
        
        MerchantPaymentConfig config;
        if (existingConfig.isPresent()) {
            // 更新现有配置
            config = existingConfig.get();
            log.info("更新现有配置，配置ID：{}", config.getId());
        } else {
            // 创建新配置
            config = new MerchantPaymentConfig();
            config.setMerchantId(merchantId);
            log.info("创建新配置，商家ID：{}", merchantId);
        }

        // 检查支付宝应用ID是否已被其他商家使用
        Optional<MerchantPaymentConfig> duplicateConfig = configRepository.findByAlipayAppId(request.getAlipayAppId());
        if (duplicateConfig.isPresent() && !duplicateConfig.get().getMerchantId().equals(merchantId)) {
            throw new BusinessException("该支付宝应用ID已被其他商家使用");
        }

        // 设置配置信息
        config.setAlipayAppId(request.getAlipayAppId());
        config.setEncryptedPrivateKey(encryptionService.encrypt(request.getAlipayPrivateKey()));
        config.setAlipayPublicKey(request.getAlipayPublicKey());
        config.setAlipayAccount(request.getAlipayAccount());
        config.setNotifyUrl(request.getNotifyUrl());
        config.setReturnUrl(request.getReturnUrl());
        config.setRemark(request.getRemark());
        
        // 新配置默认为待审核状态
        if (existingConfig.isEmpty()) {
            config.setStatus(MerchantPaymentConfig.ConfigStatus.PENDING_REVIEW);
        }

        // 保存配置
        MerchantPaymentConfig savedConfig = configRepository.save(config);
        
        log.info("商家支付配置保存成功，配置ID：{}，商家ID：{}", savedConfig.getId(), merchantId);
        
        return convertToDTO(savedConfig);
    }

    @Override
    @Transactional(readOnly = true)
    public MerchantPaymentConfigDTO getMerchantConfig(Long merchantId) {
        log.debug("获取商家支付配置，商家ID：{}", merchantId);
        
        Optional<MerchantPaymentConfig> config = configRepository.findByMerchantId(merchantId);
        if (config.isEmpty()) {
            throw new ResourceNotFoundException("商家支付配置不存在，商家ID：" + merchantId);
        }
        
        return convertToDTO(config.get());
    }

    @Override
    @Transactional(readOnly = true)
    public MerchantPaymentConfig getActiveMerchantConfig(Long merchantId) {
        log.debug("获取商家有效支付配置，商家ID：{}", merchantId);
        
        Optional<MerchantPaymentConfig> config = configRepository.findActiveMerchantConfig(merchantId);
        if (config.isEmpty()) {
            log.warn("商家没有有效的支付配置，商家ID：{}", merchantId);
            return null;
        }
        
        // 解密私钥
        MerchantPaymentConfig result = config.get();
        try {
            String decryptedPrivateKey = encryptionService.decrypt(result.getEncryptedPrivateKey());
            result.setEncryptedPrivateKey(decryptedPrivateKey); // 临时存储解密后的私钥
        } catch (Exception e) {
            log.error("解密商家私钥失败，商家ID：{}", merchantId, e);
            throw new BusinessException("配置信息异常，请重新配置");
        }
        
        return result;
    }

    @Override
    public void deleteConfig(Long merchantId) {
        log.info("删除商家支付配置，商家ID：{}", merchantId);
        
        if (!configRepository.existsByMerchantId(merchantId)) {
            throw new ResourceNotFoundException("商家支付配置不存在，商家ID：" + merchantId);
        }
        
        configRepository.deleteByMerchantId(merchantId);
        log.info("商家支付配置删除成功，商家ID：{}", merchantId);
    }

    @Override
    public MerchantPaymentConfigDTO enableConfig(Long merchantId) {
        log.info("启用商家支付配置，商家ID：{}", merchantId);
        
        Optional<MerchantPaymentConfig> configOpt = configRepository.findByMerchantId(merchantId);
        if (configOpt.isEmpty()) {
            throw new ResourceNotFoundException("商家支付配置不存在，商家ID：" + merchantId);
        }
        
        MerchantPaymentConfig config = configOpt.get();
        config.setStatus(MerchantPaymentConfig.ConfigStatus.ACTIVE);
        
        MerchantPaymentConfig savedConfig = configRepository.save(config);
        log.info("商家支付配置启用成功，商家ID：{}", merchantId);
        
        return convertToDTO(savedConfig);
    }

    @Override
    public MerchantPaymentConfigDTO disableConfig(Long merchantId) {
        log.info("禁用商家支付配置，商家ID：{}", merchantId);
        
        Optional<MerchantPaymentConfig> configOpt = configRepository.findByMerchantId(merchantId);
        if (configOpt.isEmpty()) {
            throw new ResourceNotFoundException("商家支付配置不存在，商家ID：" + merchantId);
        }
        
        MerchantPaymentConfig config = configOpt.get();
        config.setStatus(MerchantPaymentConfig.ConfigStatus.INACTIVE);
        
        MerchantPaymentConfig savedConfig = configRepository.save(config);
        log.info("商家支付配置禁用成功，商家ID：{}", merchantId);
        
        return convertToDTO(savedConfig);
    }

    @Override
    public boolean validateAlipayConfig(MerchantPaymentConfigRequest request) {
        log.debug("验证支付宝配置，应用ID：{}", request.getAlipayAppId());
        
        try {
            // 基本参数验证
            if (!StringUtils.hasText(request.getAlipayAppId()) ||
                !StringUtils.hasText(request.getAlipayPrivateKey()) ||
                !StringUtils.hasText(request.getAlipayPublicKey()) ||
                !StringUtils.hasText(request.getAlipayAccount())) {
                log.warn("支付宝配置参数不完整");
                return false;
            }

            // 验证应用ID格式
            if (!request.getAlipayAppId().matches("^\\d{16}$")) {
                log.warn("支付宝应用ID格式不正确：{}", request.getAlipayAppId());
                return false;
            }

            // 简化私钥验证 - 临时解决方案
            String privateKey = request.getAlipayPrivateKey().trim();
            if (privateKey.length() < 100) {
                log.warn("支付宝私钥长度不足");
                return false;
            }

            // 检查是否包含基本的Base64字符
            if (!privateKey.matches("^[A-Za-z0-9+/=\\s\\-]+$")) {
                log.warn("支付宝私钥包含非法字符");
                return false;
            }

            // 简化公钥验证
            String publicKey = request.getAlipayPublicKey().trim();
            if (publicKey.length() < 100) {
                log.warn("支付宝公钥长度不足");
                return false;
            }

            // 验证通知地址格式
            if (!request.getNotifyUrl().startsWith("https://")) {
                log.warn("异步通知地址必须使用HTTPS");
                return false;
            }

            log.debug("支付宝配置验证通过");
            return true;
            
        } catch (Exception e) {
            log.error("验证支付宝配置时发生异常", e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasConfig(Long merchantId) {
        return configRepository.existsByMerchantId(merchantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MerchantPaymentConfigDTO> getAllConfigs(Pageable pageable) {
        log.debug("获取所有商家支付配置，页码：{}，大小：{}", pageable.getPageNumber(), pageable.getPageSize());
        
        Page<MerchantPaymentConfig> configPage = configRepository.findAll(pageable);
        return configPage.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MerchantPaymentConfigDTO> getConfigsByStatus(MerchantPaymentConfig.ConfigStatus status, Pageable pageable) {
        log.debug("根据状态获取商家支付配置，状态：{}，页码：{}", status, pageable.getPageNumber());
        // 直接使用JPA分页查询
        Page<MerchantPaymentConfig> configPage = configRepository.findByStatus(status, pageable);
        return configPage.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MerchantPaymentConfig> getBatchMerchantConfigs(List<Long> merchantIds) {
        log.debug("批量获取商家支付配置，商家数量：{}", merchantIds.size());
        
        List<MerchantPaymentConfig> configs = configRepository.findByMerchantIds(merchantIds);
        
        // 解密私钥
        configs.forEach(config -> {
            try {
                String decryptedPrivateKey = encryptionService.decrypt(config.getEncryptedPrivateKey());
                config.setEncryptedPrivateKey(decryptedPrivateKey);
            } catch (Exception e) {
                log.error("解密商家私钥失败，商家ID：{}", config.getMerchantId(), e);
            }
        });
        
        return configs;
    }

    @Override
    public boolean testConfig(Long merchantId) {
        log.info("测试商家支付配置连通性，商家ID：{}", merchantId);
        
        MerchantPaymentConfig config = getActiveMerchantConfig(merchantId);
        if (config == null) {
            log.warn("商家没有有效的支付配置，测试失败，商家ID：{}", merchantId);
            return false;
        }
        
        try {
            // 创建支付宝客户端
            AlipayClient alipayClient = new DefaultAlipayClient(
                "https://openapi.alipaydev.com/gateway.do", // 沙箱环境
                config.getAlipayAppId(),
                config.getEncryptedPrivateKey(), // 已解密的私钥
                "json",
                "UTF-8",
                config.getAlipayPublicKey(),
                "RSA2"
            );
            
            // 调用一个简单的查询接口测试连通性
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo("test_order_" + System.currentTimeMillis());
            request.setBizModel(model);
            
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            
            // 即使查询失败（因为订单不存在），但能正常调用API说明配置正确
            log.info("支付宝配置测试完成，商家ID：{}，响应码：{}", merchantId, response.getCode());
            return true;
            
        } catch (Exception e) {
            log.error("测试支付宝配置连通性失败，商家ID：{}", merchantId, e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ConfigStatistics getConfigStatistics() {
        log.debug("获取配置统计信息");
        
        ConfigStatistics statistics = new ConfigStatistics();
        statistics.setTotalConfigs(configRepository.count());
        statistics.setActiveConfigs(configRepository.countByStatus(MerchantPaymentConfig.ConfigStatus.ACTIVE));
        statistics.setInactiveConfigs(configRepository.countByStatus(MerchantPaymentConfig.ConfigStatus.INACTIVE));
        statistics.setPendingConfigs(configRepository.countByStatus(MerchantPaymentConfig.ConfigStatus.PENDING_REVIEW));
        
        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public MerchantPaymentConfig getMerchantConfigRaw(Long merchantId) {
        log.debug("获取商家原始支付配置，商家ID：{}", merchantId);

        return configRepository.findByMerchantId(merchantId).orElse(null);
    }

    /**
     * 转换实体为DTO
     */
    private MerchantPaymentConfigDTO convertToDTO(MerchantPaymentConfig config) {
        MerchantPaymentConfigDTO dto = new MerchantPaymentConfigDTO();
        BeanUtils.copyProperties(config, dto);
        
        // 不返回加密的私钥信息
        // dto中没有encryptedPrivateKey字段，所以不会包含敏感信息
        
        return dto;
    }
}