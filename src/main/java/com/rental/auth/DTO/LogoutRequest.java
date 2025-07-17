package com.rental.auth.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登出请求DTO
 */
@Data
@Schema(description = "用户登出请求")
public class LogoutRequest {

    @Schema(description = "会话ID（可选）", example = "session_12345")
    private String sessionId;

    @Schema(description = "是否登出所有设备", example = "false")
    private Boolean logoutAllDevices = false;
}
