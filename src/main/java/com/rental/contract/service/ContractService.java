package com.rental.contract.service;

import com.rental.contract.DTO.*;
import com.rental.contract.model.Contract;
import com.rental.contract.model.ContractTemplate;
import com.rental.contract.repository.ContractRepository;
import com.rental.contract.repository.ContractTemplateRepository;
import com.rental.order.model.Order;
import com.rental.order.repository.OrderRepository;
import com.rental.user.model.User;
import com.rental.common.response.PageResponse;
import com.rental.common.exception.BusinessException;
import com.rental.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ContractService {

    private final ContractRepository contractRepository;
    private final ContractTemplateRepository contractTemplateRepository;
    private final OrderRepository orderRepository;
    private final AtomicLong contractCounter = new AtomicLong(1);

    /**
     * 分页查询合同
     */
    public PageResponse<ContractDTO> queryContracts(ContractQueryRequest request) {
        Sort sort = Sort.by(
            "desc".equalsIgnoreCase(request.getSortDir()) ?
            Sort.Direction.DESC : Sort.Direction.ASC,
            request.getSortBy()
        );

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<Contract> contractPage = contractRepository.findContractsWithFilters(
            request.getKeyword(),
            request.getStatus(),
            request.getOrderId(),
            request.getUserId(),
            request.getSignedDateStart(),
            request.getSignedDateEnd(),
            request.getExpiryDateStart(),
            request.getExpiryDateEnd(),
            pageable
        );

        List<ContractDTO> contractDTOs = contractPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.<ContractDTO>builder()
                .content(contractDTOs)
                .totalElements(contractPage.getTotalElements())
                .totalPages(contractPage.getTotalPages())
                .currentPage(contractPage.getNumber())
                .pageSize(contractPage.getSize())
                .hasNext(contractPage.hasNext())
                .hasPrevious(contractPage.hasPrevious())
                .build();
    }

    /**
     * 根据ID获取合同
     */
    public ContractDTO getContractById(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("合同不存在，ID: " + id));
        return convertToDTO(contract);
    }

    /**
     * 根据订单ID获取合同
     */
    public ContractDTO getContractByOrderId(Long orderId) {
        Contract contract = contractRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单对应的合同不存在，订单ID: " + orderId));
        return convertToDTO(contract);
    }

    /**
     * 创建合同
     */
    @Transactional
    public ContractDTO createContract(ContractCreateRequest request) {
        // 验证订单是否存在
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在，ID: " + request.getOrderId()));

        // 检查订单是否已有合同
        if (contractRepository.existsByOrderId(request.getOrderId())) {
            throw new BusinessException("该订单已存在合同");
        }

        // 获取合同模板
        ContractTemplate template = null;
        String content = request.getCustomContent();

        if (request.getTemplateId() != null) {
            template = contractTemplateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new ResourceNotFoundException("合同模板不存在，ID: " + request.getTemplateId()));

            // 使用模板内容并替换占位符
            content = generateContractContent(template.getContent(), order);
        } else if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("必须提供合同模板或自定义内容");
        }

        // 创建合同
        Contract contract = new Contract();
        contract.setContractNo(generateContractNo());
        contract.setOrder(order);
        contract.setTemplate(template);
        contract.setContent(content);
        contract.setStatus(Contract.ContractStatus.DRAFT);

        // 设置合同过期时间（订单结束日期后30天）
        contract.setExpiresAt(order.getEndDate().atStartOfDay().plusDays(30));

        contract = contractRepository.save(contract);
        log.info("创建合同成功，ID: {}, 合同编号: {}, 订单ID: {}",
                contract.getId(), contract.getContractNo(), order.getId());

        return convertToDTO(contract);
    }

    /**
     * 签署合同
     */
    @Transactional
    public ContractDTO signContract(ContractSignRequest request) {
        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new ResourceNotFoundException("合同不存在，ID: " + request.getContractId()));

        if (contract.getStatus() != Contract.ContractStatus.DRAFT) {
            throw new BusinessException("只有草稿状态的合同才能签署");
        }

        contract.setStatus(Contract.ContractStatus.SIGNED);
        contract.setSignedAt(LocalDateTime.now());

        contract = contractRepository.save(contract);
        log.info("签署合同成功，合同ID: {}, 合同编号: {}", contract.getId(), contract.getContractNo());

        return convertToDTO(contract);
    }

    /**
     * 终止合同
     */
    @Transactional
    public ContractDTO terminateContract(Long id, String reason) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("合同不存在，ID: " + id));

        if (contract.getStatus() != Contract.ContractStatus.SIGNED) {
            throw new BusinessException("只有已签署的合同才能终止");
        }

        contract.setStatus(Contract.ContractStatus.TERMINATED);
        contract = contractRepository.save(contract);

        log.info("终止合同成功，合同ID: {}, 合同编号: {}, 原因: {}",
                contract.getId(), contract.getContractNo(), reason);

        return convertToDTO(contract);
    }

    /**
     * 获取用户的合同列表
     */
    public List<ContractDTO> getUserContracts(Long userId) {
        List<Contract> contracts = contractRepository.findByOrderUserIdOrderByCreatedAtDesc(userId);
        return contracts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 下载合同
     */
    public byte[] downloadContract(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("合同不存在，ID: " + id));

        // TODO: 实现PDF生成逻辑
        return contract.getContent().getBytes();
    }

    /**
     * 生成合同编号
     */
    private String generateContractNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long counter = contractCounter.getAndIncrement();
        return "CONTRACT" + timestamp + String.format("%06d", counter);
    }

    /**
     * 生成合同内容（替换模板占位符）
     */
    private String generateContractContent(String template, Order order) {
        User user = order.getUser();
        String itemNames = order.getOrderItems().stream()
                .map(item -> item.getItem().getName())
                .collect(Collectors.joining(", "));

        return template
                .replace("{{用户姓名}}", user.getProfile() != null ? user.getProfile().getRealName() : user.getUsername())
                .replace("{{物品名称}}", itemNames)
                .replace("{{物品描述}}", order.getOrderItems().get(0).getItem().getDescription())
                .replace("{{租赁数量}}", String.valueOf(order.getOrderItems().stream().mapToInt(item -> item.getQuantity()).sum()))
                .replace("{{开始日期}}", order.getStartDate().toString())
                .replace("{{结束日期}}", order.getEndDate().toString())
                .replace("{{租赁天数}}", String.valueOf(order.getStartDate().until(order.getEndDate()).getDays()))
                .replace("{{日租金}}", order.getOrderItems().get(0).getPricePerDay().toString())
                .replace("{{总租金}}", order.getTotalAmount().toString())
                .replace("{{押金}}", order.getDepositAmount().toString())
                .replace("{{签约日期}}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
    }

    /**
     * 转换为DTO
     */
    private ContractDTO convertToDTO(Contract contract) {
        ContractDTO dto = new ContractDTO();
        dto.setId(contract.getId());
        dto.setContractNo(contract.getContractNo());
        dto.setOrderId(contract.getOrder().getId());
        dto.setOrderNo(contract.getOrder().getOrderNo());
        dto.setContent(contract.getContent());
        dto.setStatus(contract.getStatus());
        dto.setStatusDescription(contract.getStatus().getDescription());
        dto.setSignedAt(contract.getSignedAt());
        dto.setExpiresAt(contract.getExpiresAt());
        dto.setCreatedAt(contract.getCreatedAt());
        dto.setUpdatedAt(contract.getUpdatedAt());

        if (contract.getTemplate() != null) {
            dto.setTemplateId(contract.getTemplate().getId());
            dto.setTemplateName(contract.getTemplate().getName());
        }

        // 设置订单相关信息
        Order order = contract.getOrder();
        User user = order.getUser();
        dto.setUserName(user.getProfile() != null ? user.getProfile().getRealName() : user.getUsername());
        dto.setUserEmail(user.getEmail());

        String itemNames = order.getOrderItems().stream()
                .map(item -> item.getItem().getName())
                .collect(Collectors.joining(", "));
        dto.setItemNames(itemNames);
        dto.setRentalPeriod(order.getStartDate() + " 至 " + order.getEndDate());

        return dto;
    }
}
