package com.rental.permission.DTO;

import com.rental.permission.model.Permission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO {

    private Long id;

    @NotBlank(message = "权限名称不能为空")
    @Size(max = 100, message = "权限名称长度不能超过100")
    private String name;

    @Size(max = 255, message = "权限描述长度不能超过255")
    private String description;

    @NotNull(message = "权限类型不能为空")
    private Permission.PermissionType type;

    @Size(max = 255, message = "资源路径长度不能超过255")
    private String resource;

    private Long parentId;

    private String parentName;

    private Integer sortOrder;

    private LocalDateTime createdAt;

    private List<PermissionDTO> children;
}
