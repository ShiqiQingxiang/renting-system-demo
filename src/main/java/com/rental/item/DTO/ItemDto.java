package com.rental.item.DTO;

import com.rental.item.model.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {

    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Long ownerId;
    private String ownerUsername;
    private BigDecimal pricePerDay;
    private BigDecimal deposit;
    private Item.ItemStatus status;
    private String location;
    private String images; // 改为字符串类型
    private Item.ItemSpecification specifications;
    private Item.ApprovalStatus approvalStatus;
    private String approvalComment;
    private Long approvedById;
    private String approvedByUsername;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 扩展信息
    private boolean available;
    private boolean canBeRented;

    // 辅助方法：获取图片列表
    public List<String> getImageList() {
        if (images == null || images.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return java.util.Arrays.asList(images.split(","));
    }

    // 辅助方法：设置图片列表
    public void setImageList(List<String> imageList) {
        if (imageList == null || imageList.isEmpty()) {
            this.images = null;
        } else {
            this.images = String.join(",", imageList);
        }
    }
}
