package com.rental.item.DTO;

import com.rental.item.model.Item;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemUpdateRequest {

    @Size(max = 200, message = "物品名称长度不能超过200")
    private String name;

    private String description;

    private Long categoryId;

    @DecimalMin(value = "0.01", message = "每日租金必须大于0")
    private BigDecimal pricePerDay;

    @DecimalMin(value = "0.00", message = "押金不能为负数")
    private BigDecimal deposit;

    private Item.ItemStatus status;

    private String location;

    private List<String> images; // 修改为List<String>类型，直接接收图片URL数组

    private Item.ItemSpecification specifications;

    // 辅助方法：获取图片的逗号分隔字符串（用于数据库存储）
    public String getImagesAsString() {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return String.join(",", images);
    }

    // 辅助方法：从逗号分隔的字符串设置图片列表
    public void setImagesFromString(String imagesStr) {
        if (imagesStr == null || imagesStr.trim().isEmpty()) {
            this.images = new java.util.ArrayList<>();
        } else {
            this.images = java.util.Arrays.asList(imagesStr.split(","));
        }
    }
}
