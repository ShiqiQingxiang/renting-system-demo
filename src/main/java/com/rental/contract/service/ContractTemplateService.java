package com.rental.contract.service;

import com.rental.contract.DTO.ContractTemplateDTO;
import com.rental.contract.model.ContractTemplate;
import com.rental.contract.repository.ContractTemplateRepository;
import com.rental.common.exception.BusinessException;
import com.rental.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ContractTemplateService {

    private final ContractTemplateRepository contractTemplateRepository;

    /**
     * 获取所有活跃的合同模板
     */
    public List<ContractTemplateDTO> getActiveTemplates() {
        List<ContractTemplate> templates = contractTemplateRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        return templates.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 分页获取合同模板
     */
    public Page<ContractTemplateDTO> getTemplates(Pageable pageable) {
        Page<ContractTemplate> templates = contractTemplateRepository.findAll(pageable);
        return templates.map(this::convertToDTO);
    }

    /**
     * 根据ID获取合同模板
     */
    public ContractTemplateDTO getTemplateById(Long id) {
        ContractTemplate template = contractTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("合同模板不存在，ID: " + id));
        return convertToDTO(template);
    }

    /**
     * 创建合同模板
     */
    @Transactional
    public ContractTemplateDTO createTemplate(ContractTemplateDTO templateDTO) {
        // 检查模板名称是否已存在
        if (contractTemplateRepository.existsByName(templateDTO.getName())) {
            throw new BusinessException("模板名称已存在: " + templateDTO.getName());
        }

        ContractTemplate template = convertToEntity(templateDTO);
        template = contractTemplateRepository.save(template);

        log.info("创建合同模板成功，ID: {}, 名称: {}", template.getId(), template.getName());
        return convertToDTO(template);
    }

    /**
     * 更新合同模板
     */
    @Transactional
    public ContractTemplateDTO updateTemplate(Long id, ContractTemplateDTO templateDTO) {
        ContractTemplate existingTemplate = contractTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("合同模板不存在，ID: " + id));

        // 检查名称是否被其他模板使用
        if (contractTemplateRepository.existsByNameAndIdNot(templateDTO.getName(), id)) {
            throw new BusinessException("模板名称已存在: " + templateDTO.getName());
        }

        existingTemplate.setName(templateDTO.getName());
        existingTemplate.setContent(templateDTO.getContent());
        existingTemplate.setVersion(templateDTO.getVersion());
        if (templateDTO.getIsActive() != null) {
            existingTemplate.setIsActive(templateDTO.getIsActive());
        }

        existingTemplate = contractTemplateRepository.save(existingTemplate);
        log.info("更新合同模板成功，ID: {}, 名称: {}", existingTemplate.getId(), existingTemplate.getName());

        return convertToDTO(existingTemplate);
    }

    /**
     * 删除合同模板
     */
    @Transactional
    public void deleteTemplate(Long id) {
        ContractTemplate template = contractTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("合同模板不存在，ID: " + id));

        // 检查是否有合同在使用该模板
        if (contractTemplateRepository.countContractsByTemplateId(id) > 0) {
            throw new BusinessException("该模板正在被使用，无法删除");
        }

        contractTemplateRepository.delete(template);
        log.info("删除合同模板成功，ID: {}, 名称: {}", template.getId(), template.getName());
    }

    /**
     * 激活/停用模板
     */
    @Transactional
    public void toggleTemplateStatus(Long id) {
        ContractTemplate template = contractTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("合同模板不存在，ID: " + id));

        template.setIsActive(!template.getIsActive());
        contractTemplateRepository.save(template);

        log.info("切换合同模板状态成功，ID: {}, 新状态: {}", id, template.getIsActive());
    }

    /**
     * 转换为DTO
     */
    private ContractTemplateDTO convertToDTO(ContractTemplate template) {
        ContractTemplateDTO dto = new ContractTemplateDTO();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setContent(template.getContent());
        dto.setVersion(template.getVersion());
        dto.setIsActive(template.getIsActive());
        dto.setCreatedAt(template.getCreatedAt());
        dto.setUpdatedAt(template.getUpdatedAt());
        return dto;
    }

    /**
     * 转换为实体
     */
    private ContractTemplate convertToEntity(ContractTemplateDTO dto) {
        ContractTemplate template = new ContractTemplate();
        template.setName(dto.getName());
        template.setContent(dto.getContent());
        template.setVersion(dto.getVersion() != null ? dto.getVersion() : "1.0");
        template.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        return template;
    }
}
