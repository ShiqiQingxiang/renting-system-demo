package com.rental.permission.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

    private Long id;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50")
    private String name;

    @Size(max = 255, message = "角色描述长度不能超过255")
    private String description;

    private Boolean isSystem;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Set<Long> permissionIds;

    private List<PermissionDTO> permissions;
}
