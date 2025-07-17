package com.rental.item.service.impl;

import com.rental.common.exception.BusinessException;
import com.rental.common.exception.ResourceNotFoundException;
import com.rental.item.DTO.*;
import com.rental.item.model.Item;
import com.rental.item.model.ItemCategory;
import com.rental.item.repository.ItemRepository;
import com.rental.item.repository.ItemCategoryRepository;
import com.rental.item.service.ItemService;
import com.rental.user.model.User;
import com.rental.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemCategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemDto createItem(ItemCreateRequest request, Long ownerId) {
        log.info("创建物品，所有者ID：{}, 物品名称：{}", ownerId, request.getName());

        User owner = userRepository.findById(ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Item item = new Item();
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setOwner(owner);
        item.setPricePerDay(request.getPricePerDay());
        item.setDeposit(request.getDeposit());
        item.setLocation(request.getLocation());
        item.setImages(request.getImagesAsString()); // 使用辅助方法转换为字符串
        item.setSpecifications(request.getSpecifications());

        // 设置分类
        if (request.getCategoryId() != null) {
            ItemCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在"));
            item.setCategory(category);
        }

        Item savedItem = itemRepository.save(item);
        log.info("物品创建成功，ID：{}", savedItem.getId());

        return convertToDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long id, ItemUpdateRequest request, Long currentUserId) {
        log.info("更新物品，ID：{}, 用户ID：{}", id, currentUserId);

        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("物品不存在"));

        // 权限检查
        if (!canUserModifyItem(id, currentUserId)) {
            throw new BusinessException("无权限修改此物品");
        }

        // 更新字段
        if (request.getName() != null) {
            item.setName(request.getName());
        }
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
        }
        if (request.getCategoryId() != null) {
            ItemCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在"));
            item.setCategory(category);
        }
        if (request.getPricePerDay() != null) {
            item.setPricePerDay(request.getPricePerDay());
        }
        if (request.getDeposit() != null) {
            item.setDeposit(request.getDeposit());
        }
        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
        }
        if (request.getLocation() != null) {
            item.setLocation(request.getLocation());
        }
        if (request.getImages() != null) {
        }
        if (request.getSpecifications() != null) {
            item.setSpecifications(request.getSpecifications());
        }

        Item savedItem = itemRepository.save(item);
        log.info("物品更新成功，ID：{}", savedItem.getId());

        return convertToDto(savedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getItemById(Long id) {
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("物品不存在"));
        return convertToDto(item);
    }

    @Override
    @Transactional
    public void deleteItem(Long id, Long currentUserId) {
        log.info("删除物品，ID：{}, 用户ID：{}", id, currentUserId);

        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("物品不存在"));

        // 权限检查
        if (!canUserModifyItem(id, currentUserId)) {
            throw new BusinessException("无权限删除此物品");
        }

        // 检查物品状态
        if (item.getStatus() == Item.ItemStatus.RENTED) {
            throw new BusinessException("物品正在租赁中，无法删除");
        }

        itemRepository.delete(item);
        log.info("物品删除成功，ID：{}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemDto> searchItems(ItemSearchRequest request, Pageable pageable) {
        Page<Item> items = itemRepository.findBySearchCriteria(
            request.getName(),
            request.getCategoryId(),
            request.getStatus(),
            request.getApprovalStatus(),
            request.getOwnerId(),
            request.getMinPrice(),
            request.getMaxPrice(),
            request.getLocation(),
            pageable
        );

        return items.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemDto> getAvailableItems(Pageable pageable) {
        Page<Item> items = itemRepository.findAvailableItems(pageable);
        return items.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getItemsByOwner(Long ownerId) {
        List<Item> items = itemRepository.findByOwnerId(ownerId);
        return items.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getItemsByCategory(Long categoryId) {
        List<Item> items = itemRepository.findByCategoryId(categoryId);
        return items.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemDto approveItem(Long id, ItemApprovalRequest request, Long approverId) {
        log.info("审核物品，ID：{}, 审核员ID：{}, 结果：{}", id, approverId, request.getApproved());

        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("物品不存在"));

        User approver = userRepository.findById(approverId)
            .orElseThrow(() -> new ResourceNotFoundException("审核员不存在"));

        if (request.getApproved()) {
            item.approve(approver, request.getComment());
        } else {
            item.reject(approver, request.getComment());
        }

        Item savedItem = itemRepository.save(item);
        log.info("物品审核完成，ID：{}, 状态：{}", id, savedItem.getApprovalStatus());

        return convertToDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto rejectItem(Long id, ItemApprovalRequest request, Long approverId) {
        return approveItem(id, new ItemApprovalRequest(false, request.getComment()), approverId);
    }

    @Override
    @Transactional
    public ItemDto updateItemStatus(Long id, Item.ItemStatus status, Long currentUserId) {
        log.info("更新物品状态，ID：{}, 新状态：{}, 用户ID：{}", id, status, currentUserId);

        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("物品不存在"));

        // 权限检查
        if (!canUserModifyItem(id, currentUserId)) {
            throw new BusinessException("无权限修改此物品状态");
        }

        item.setStatus(status);
        Item savedItem = itemRepository.save(item);

        return convertToDto(savedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemDto> getPendingApprovalItems(Pageable pageable) {
        Page<Item> items = itemRepository.findPendingApprovalItems(pageable);
        return items.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(Item.ItemStatus status) {
        return itemRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByApprovalStatus(Item.ApprovalStatus approvalStatus) {
        return itemRepository.countByApprovalStatus(approvalStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByOwner(Long ownerId) {
        return itemRepository.countByOwnerId(ownerId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserModifyItem(Long itemId, Long userId) {
        if (userId == null) {
            return false;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        // 检查用户是否有管理员权限
        boolean isAdmin = hasAdminRole(userId);

        if (isAdmin) {
            return true;
        }

        // 物品所有者可以修改自己的物品
        Item item = itemRepository.findById(itemId).orElse(null);
        return item != null && item.getOwner().getId().equals(userId);
    }

    /**
     * 检查用户是否具有管理员角色
     * 使用Spring Security上下文和用户名判断
     */
    private boolean hasAdminRole(Long userId) {
        if (userId == null) {
            return false;
        }

        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return false;
            }

            // 使用Spring Security的权限检查
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getAuthorities() != null) {
                // 检查是否有管理员权限
                boolean hasAdminAuthority = authentication.getAuthorities().stream()
                    .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN") ||
                        authority.getAuthority().equals("ROLE_SUPER_ADMIN") ||
                        authority.getAuthority().equals("ITEM_APPROVE") ||
                        authority.getAuthority().equals("ITEM_MANAGE_ALL"));

                if (hasAdminAuthority) {
                    return true;
                }
            }

            // 备用方案：通过用户名判断（临时解决方案）
            String username = user.getUsername().toLowerCase();
            return username.equals("admin") ||
                   username.contains("admin") ||
                   username.equals("superadmin");

        } catch (Exception e) {
            log.warn("检查用户管理员权限时出错，用户ID: {}, 错误: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * 检查用户是否具有物品审核权限
     */
    private boolean hasItemApprovalPermission(Long userId) {
        if (userId == null) {
            return false;
        }

        try {
            // 检查是否有管理员权限
            if (hasAdminRole(userId)) {
                return true;
            }

            // 使用Spring Security权限检查
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getAuthorities() != null) {
                return authentication.getAuthorities().stream()
                    .anyMatch(authority ->
                        authority.getAuthority().equals("ITEM_APPROVE") ||
                        authority.getAuthority().equals("ROLE_OPERATOR"));
            }

            return false;
        } catch (Exception e) {
            log.warn("检查用户审核权限时出错，用户ID: {}, 错误: {}", userId, e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isItemAvailable(Long itemId) {
        Item item = itemRepository.findById(itemId).orElse(null);
        return item != null && item.isAvailable();
    }

    private ItemDto convertToDto(Item item) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setPricePerDay(item.getPricePerDay());
        dto.setDeposit(item.getDeposit());
        dto.setStatus(item.getStatus());
        dto.setLocation(item.getLocation());
        dto.setImages(item.getImages());
        dto.setSpecifications(item.getSpecifications());
        dto.setApprovalStatus(item.getApprovalStatus());
        dto.setApprovalComment(item.getApprovalComment());
        dto.setApprovedAt(item.getApprovedAt());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        dto.setAvailable(item.isAvailable());
        dto.setCanBeRented(item.canBeRented());

        if (item.getCategory() != null) {
            dto.setCategoryId(item.getCategory().getId());
            dto.setCategoryName(item.getCategory().getName());
        }

        if (item.getOwner() != null) {
            dto.setOwnerId(item.getOwner().getId());
            dto.setOwnerUsername(item.getOwner().getUsername());
        }

        if (item.getApprovedBy() != null) {
            dto.setApprovedById(item.getApprovedBy().getId());
            dto.setApprovedByUsername(item.getApprovedBy().getUsername());
        }

        return dto;
    }
}
