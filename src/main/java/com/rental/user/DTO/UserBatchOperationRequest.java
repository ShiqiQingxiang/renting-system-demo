package com.rental.user.DTO;

import com.rental.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 用户批量操作请求DTO
 */
@Data
@Schema(description = "用户批量操作请求")
public class UserBatchOperationRequest {

    @NotEmpty(message = "用户ID列表不能为空")
    @Schema(description = "用户ID列表", example = "[1, 2, 3]")
    private List<Long> userIds;

    @NotNull(message = "状态不能为空")
    @Schema(description = "要设置的用户状态", example = "ACTIVE")
    private User.UserStatus status;
}
