package com.rental.user.DTO;

import com.rental.user.model.UserProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户资料更新请求DTO
 */
@Data
@Schema(description = "用户资料更新请求")
public class UserProfileUpdateRequest {

    @Size(max = 100, message = "真实姓名长度不能超过100")
    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$",
             message = "身份证号格式不正确")
    @Schema(description = "身份证号", example = "110101199001011234")
    private String idCard;

    @Size(max = 500, message = "地址长度不能超过500")
    @Schema(description = "详细地址", example = "北京市朝阳区xxx街道xxx号")
    private String address;

    @Schema(description = "头像URL", example = "http://example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "出生日期", example = "1990-01-01")
    private LocalDate birthDate;

    @Schema(description = "性别", example = "MALE", allowableValues = {"MALE", "FEMALE", "OTHER"})
    private UserProfile.Gender gender;
}
