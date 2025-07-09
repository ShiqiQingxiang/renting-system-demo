package com.rental.auth.DTO;

import lombok.Data;

/**
 * 登出请求DTO
 */
@Data
public class LogoutRequest {

    /**
     * 会话ID（可选）
     */
    private String sessionId;
}
