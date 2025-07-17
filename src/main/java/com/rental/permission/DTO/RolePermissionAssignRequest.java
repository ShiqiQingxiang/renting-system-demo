package com.rental.permission.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionAssignRequest {

    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    @NotEmpty(message = "权限ID列表不能为空")
    private Set<Long> permissionIds;
}
