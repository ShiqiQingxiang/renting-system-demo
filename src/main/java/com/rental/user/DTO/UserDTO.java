package com.rental.user.DTO;

import com.rental.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息DTO")
public class UserDTO {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "邮箱地址", example = "admin@rental.com")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "用户状态", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "LOCKED"})
    private User.UserStatus status;

    @Schema(description = "创建时间", example = "2025-07-14T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2025-07-14T11:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "用户拥有的角色名称列表", example = "[\"ADMIN\", \"USER\"]")
    private Set<String> roleNames;

    @Schema(description = "用户详细资料信息")
    private UserProfileDTO profile;
}
