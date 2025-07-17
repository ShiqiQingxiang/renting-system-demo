package com.rental.auth.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新令牌请求DTO
 */
@Data
@Schema(description = "刷新令牌请求")
public class RefreshTokenRequest {

    @NotBlank(message = "刷新令牌不能为空")
    @Schema(description = "刷新令牌", example = "refresh_token_here")
    private String refreshToken;
}
