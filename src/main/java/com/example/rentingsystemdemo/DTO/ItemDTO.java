package com.example.rentingsystemdemo.DTO;

import com.example.rentingsystemdemo.model.Item;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal dailyPrice;
    private boolean available;
    private Long ownerId; // 只包含用户名

    public ItemDTO(Item item) {
        this.id = item.getId();
        this.name = item.getName();
        this.description = item.getDescription();
        this.dailyPrice = item.getDailyPrice();
        this.available = item.isAvailable();
        this.ownerId = item.getOwner().getId();
    }

}
