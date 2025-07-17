package com.rental.item.DTO;

import com.rental.item.model.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemSearchRequest {

    private String name;
    private Long categoryId;
    private Item.ItemStatus status;
    private Item.ApprovalStatus approvalStatus;
    private Long ownerId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String location;
    private String sortBy = "createdAt"; // 排序字段
    private String sortDir = "desc"; // 排序方向
    private int page = 0;
    private int size = 20;
}
