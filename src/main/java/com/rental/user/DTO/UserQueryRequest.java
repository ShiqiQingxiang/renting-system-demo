package com.rental.user.DTO;

import com.rental.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户查询请求DTO
 */
@Data
@Schema(description = "用户查询请求")
public class UserQueryRequest {

    @Schema(description = "搜索关键词（用户名或邮箱）", example = "admin")
    private String keyword;

    @Schema(description = "用户状态", example = "ACTIVE")
    private User.UserStatus status;

    @Schema(description = "角色名称", example = "ADMIN")
    private String roleName;

    @Schema(description = "页码（从0开始）", example = "0")
    private int page = 0;

    @Schema(description = "每页大小", example = "10")
    private int size = 10;

    @Schema(description = "排序字段", example = "createdAt")
    private String sortBy = "createdAt";

    @Schema(description = "排序方向（asc/desc）", example = "desc")
    private String sortDir = "desc";
}
