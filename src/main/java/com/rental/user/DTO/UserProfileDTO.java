package com.rental.user.DTO;

import com.rental.user.model.UserProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户资料信息DTO")
public class UserProfileDTO {

    @Schema(description = "资料ID", example = "1")
    private Long id;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "身份证号", example = "110101199001011234")
    private String idCard;

    @Schema(description = "详细地址", example = "北京市朝阳区某某街道123号")
    private String address;

    @Schema(description = "头像URL", example = "http://example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "出生日期", example = "1990-01-01")
    private LocalDate birthDate;

    @Schema(description = "性别", example = "MALE", allowableValues = {"MALE", "FEMALE", "OTHER"})
    private UserProfile.Gender gender;

    @Schema(description = "创建时间", example = "2025-07-14T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2025-07-14T11:00:00")
    private LocalDateTime updatedAt;
}
