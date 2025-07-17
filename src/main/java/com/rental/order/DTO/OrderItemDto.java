package com.rental.order.DTO;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {

    private Long id;
    private Long orderId;
    private Long itemId;
    private String itemName;
    private String itemDescription;
    private String itemLocation;
    private String itemImages;
    private Integer quantity;
    private BigDecimal pricePerDay;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;

    // 物品所有者信息
    private Long ownerId;
    private String ownerUsername;

    // 物品分类信息
    private Long categoryId;
    private String categoryName;

    // 物品状态
    private String itemStatus;
}
