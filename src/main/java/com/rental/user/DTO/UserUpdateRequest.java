package com.rental.user.DTO;

import com.rental.user.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱地址", example = "updated@example.com")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号码", example = "13900139000")
    private String phone;

    @Schema(description = "用户状态", example = "ACTIVE")
    private User.UserStatus status;

    @Schema(description = "角色ID列表", example = "[1, 2]")
    private Long[] roleIds;
}
