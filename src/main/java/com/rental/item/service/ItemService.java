package com.rental.item.service;

import com.rental.item.DTO.*;
import com.rental.item.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ItemService {

    // 基本CRUD操作
    ItemDto createItem(ItemCreateRequest request, Long ownerId);
    ItemDto updateItem(Long id, ItemUpdateRequest request, Long currentUserId);
    ItemDto getItemById(Long id);
    void deleteItem(Long id, Long currentUserId);

    // 查询操作
    Page<ItemDto> searchItems(ItemSearchRequest request, Pageable pageable);
    Page<ItemDto> getAvailableItems(Pageable pageable);
    List<ItemDto> getItemsByOwner(Long ownerId);
    List<ItemDto> getItemsByCategory(Long categoryId);

    // 管理操作
    ItemDto approveItem(Long id, ItemApprovalRequest request, Long approverId);
    ItemDto rejectItem(Long id, ItemApprovalRequest request, Long approverId);
    ItemDto updateItemStatus(Long id, Item.ItemStatus status, Long currentUserId);

    // 审核相关
    Page<ItemDto> getPendingApprovalItems(Pageable pageable);

    // 统计信息
    long countByStatus(Item.ItemStatus status);
    long countByApprovalStatus(Item.ApprovalStatus approvalStatus);
    long countByOwner(Long ownerId);

    // 辅助方法
    boolean canUserModifyItem(Long itemId, Long userId);
    boolean isItemAvailable(Long itemId);
}
