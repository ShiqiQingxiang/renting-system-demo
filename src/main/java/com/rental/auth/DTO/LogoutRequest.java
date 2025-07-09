package com.rental.auth.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登出请求DTO
 */
@Data
@Schema(description = "用户登出请求")
public class LogoutRequest {

    @Schema(description = "会话ID（可选）", example = "session-id-here")
    private String sessionId;
}
