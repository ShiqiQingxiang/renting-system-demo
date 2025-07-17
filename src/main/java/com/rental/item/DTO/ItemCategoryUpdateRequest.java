package com.rental.item.DTO;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemCategoryUpdateRequest {

    @Size(max = 100, message = "分类名称长度不能超过100")
    private String name;

    private String description;

    private Long parentId;

    private Integer sortOrder;
}
