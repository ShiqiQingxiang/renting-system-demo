package com.rental.item.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemCategoryDto {

    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private String parentName;
    private List<ItemCategoryDto> children;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 扩展信息
    private boolean isRoot;
    private boolean hasChildren;
    private int itemCount; // 该分类下的物品数量
}
