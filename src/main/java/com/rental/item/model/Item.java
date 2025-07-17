package com.rental.item.model;

import com.rental.user.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "items", indexes = {
    @Index(name = "idx_name", columnList = "name"),
    @Index(name = "idx_category_id", columnList = "category_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_price", columnList = "price_per_day"),
    @Index(name = "idx_owner_id", columnList = "owner_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    @NotBlank(message = "物品名称不能为空")
    @Size(max = 200, message = "物品名称长度不能超过200")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ItemCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @NotNull(message = "物品所有者不能为空")
    private User owner;

    @Column(name = "price_per_day", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "每日租金不能为空")
    @DecimalMin(value = "0.01", message = "每日租金必须大于0")
    private BigDecimal pricePerDay;

    @Column(precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "押金不能为负数")
    private BigDecimal deposit = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status = ItemStatus.PENDING;

    @Column(length = 255)
    private String location;

    // 将图片存储为逗号分隔的字符串，避免JSON字段兼容性问题
    @Column(columnDefinition = "TEXT")
    private String images;

    // 将规格信息改为单独的字段，避免@Embeddable和JSON的组合问题
    @Column(length = 100)
    private String brand;      // 品牌

    @Column(length = 100)
    private String model;      // 型号

    @Column(length = 50)
    private String color;      // 颜色

    @Column(length = 100)
    private String size;       // 尺寸

    @Column(length = 50)
    private String weight;     // 重量

    @Column(length = 100)
    private String material;   // 材质

    @Column(name = "item_condition", length = 100)
    private String itemCondition;  // 成色

    @Column(columnDefinition = "TEXT")
    private String features;   // 特性描述

    @Column(name = "approval_status")
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "approval_comment")
    private String approvalComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 枚举定义
    public enum ItemStatus {
        PENDING("待审核"),
        AVAILABLE("可租赁"),
        RENTED("已租出"),
        MAINTENANCE("维护中"),
        UNAVAILABLE("不可用"),
        OFFLINE("已下架");

        private final String description;

        ItemStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum ApprovalStatus {
        PENDING("待审核"),
        APPROVED("已通过"),
        REJECTED("已拒绝");

        private final String description;

        ApprovalStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 业务方法
    public boolean isAvailable() {
        return status == ItemStatus.AVAILABLE && approvalStatus == ApprovalStatus.APPROVED;
    }

    public boolean canBeRented() {
        return isAvailable();
    }

    public void approve(User approver, String comment) {
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
        this.approvalComment = comment;
        if (this.status == ItemStatus.PENDING) {
            this.status = ItemStatus.AVAILABLE;
        }
    }

    public void reject(User approver, String comment) {
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
        this.approvalComment = comment;
        this.status = ItemStatus.UNAVAILABLE;
    }

    // 辅助方法：获取图片列表
    public java.util.List<String> getImageList() {
        if (images == null || images.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return java.util.Arrays.asList(images.split(","));
    }

    // 辅助方法：设置图片列表
    public void setImageList(java.util.List<String> imageList) {
        if (imageList == null || imageList.isEmpty()) {
            this.images = null;
        } else {
            this.images = String.join(",", imageList);
        }
    }

    // 辅助方法：获取规格信息对象
    public ItemSpecification getSpecifications() {
        ItemSpecification spec = new ItemSpecification();
        spec.setBrand(this.brand);
        spec.setModel(this.model);
        spec.setColor(this.color);
        spec.setSize(this.size);
        spec.setWeight(this.weight);
        spec.setMaterial(this.material);
        spec.setCondition(this.itemCondition);
        spec.setFeatures(this.features);
        return spec;
    }

    // 辅助方法：设置规格信息
    public void setSpecifications(ItemSpecification specifications) {
        if (specifications != null) {
            this.brand = specifications.getBrand();
            this.model = specifications.getModel();
            this.color = specifications.getColor();
            this.size = specifications.getSize();
            this.weight = specifications.getWeight();
            this.material = specifications.getMaterial();
            this.itemCondition = specifications.getCondition();
            this.features = specifications.getFeatures();
        }
    }

    // 规格信息类（不再使用@Embeddable）
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemSpecification {
        private String brand;      // 品牌
        private String model;      // 型号
        private String color;      // 颜色
        private String size;       // 尺寸
        private String weight;     // 重量
        private String material;   // 材质
        private String condition;  // 成色
        private String features;   // 特性描述
    }
}