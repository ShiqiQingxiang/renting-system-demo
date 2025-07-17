package com.rental.user.service;

import com.rental.user.DTO.UserProfileDTO;
import com.rental.user.DTO.UserProfileUpdateRequest;

public interface UserProfileService {

    /**
     * 根据用户ID获取用户资料
     */
    UserProfileDTO getProfileByUserId(Long userId);

    /**
     * 创建或更新用户资料
     */
    UserProfileDTO createOrUpdateProfile(Long userId, UserProfileUpdateRequest request);

    /**
     * 删除用户资料
     */
    void deleteProfile(Long userId);

    /**
     * 检查身份证号是否存在
     */
    boolean existsByIdCard(String idCard);

    /**
     * 更新用户头像
     */
    UserProfileDTO updateAvatar(Long userId, String avatarUrl);
}
