package com.rental.user.service.impl;

import com.rental.user.service.UserProfileService;
import com.rental.user.repository.UserRepository;
import com.rental.user.repository.UserProfileRepository;
import com.rental.user.model.User;
import com.rental.user.model.UserProfile;
import com.rental.user.DTO.UserProfileDTO;
import com.rental.user.DTO.UserProfileUpdateRequest;
import com.rental.common.exception.BusinessException;
import com.rental.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getProfileByUserId(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
            .orElse(null);

        if (profile == null) {
            // 如果用户资料不存在，返回一个空的DTO但包含用户ID
            UserProfileDTO dto = new UserProfileDTO();
            dto.setUserId(userId);
            return dto;
        }

        return convertToDTO(profile);
    }

    @Override
    public UserProfileDTO createOrUpdateProfile(Long userId, UserProfileUpdateRequest request) {
        log.info("Creating or updating profile for user ID: {}", userId);

        // 验证用户是否存在
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 检查身份证号是否已存在（如果提供且与当前用户不同）
        if (request.getIdCard() != null && !request.getIdCard().isEmpty()) {
            UserProfile existingProfile = userProfileRepository.findByIdCard(request.getIdCard()).orElse(null);
            if (existingProfile != null && !existingProfile.getUser().getId().equals(userId)) {
                throw new BusinessException("身份证号已存在");
            }
        }

        // 查找现有资料或创建新资料
        UserProfile profile = userProfileRepository.findByUserId(userId)
            .orElse(new UserProfile());

        // 如果是新创建的资料，设置用户关联
        if (profile.getId() == null) {
            profile.setUser(user);
        }

        // 更新资料信息
        if (request.getRealName() != null) {
            profile.setRealName(request.getRealName());
        }
        if (request.getIdCard() != null) {
            profile.setIdCard(request.getIdCard().isEmpty() ? null : request.getIdCard());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getBirthDate() != null) {
            profile.setBirthDate(request.getBirthDate());
        }
        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }

        UserProfile savedProfile = userProfileRepository.save(profile);
        log.info("Profile saved successfully for user ID: {}", userId);

        return convertToDTO(savedProfile);
    }

    @Override
    public void deleteProfile(Long userId) {
        log.info("Deleting profile for user ID: {}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户资料不存在"));

        userProfileRepository.delete(profile);
        log.info("Profile deleted successfully for user ID: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIdCard(String idCard) {
        return userProfileRepository.existsByIdCard(idCard);
    }

    @Override
    public UserProfileDTO updateAvatar(Long userId, String avatarUrl) {
        log.info("Updating avatar for user ID: {}", userId);

        // 验证用户是否存在
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 查找或创建用户资料
        UserProfile profile = userProfileRepository.findByUserId(userId)
            .orElse(new UserProfile());

        if (profile.getId() == null) {
            profile.setUser(user);
        }

        profile.setAvatarUrl(avatarUrl);
        UserProfile savedProfile = userProfileRepository.save(profile);

        log.info("Avatar updated successfully for user ID: {}", userId);
        return convertToDTO(savedProfile);
    }

    private UserProfileDTO convertToDTO(UserProfile profile) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(profile.getId());
        dto.setUserId(profile.getUser().getId());
        dto.setRealName(profile.getRealName());
        dto.setIdCard(profile.getIdCard());
        dto.setAddress(profile.getAddress());
        dto.setAvatarUrl(profile.getAvatarUrl());
        dto.setBirthDate(profile.getBirthDate());
        dto.setGender(profile.getGender());
        dto.setCreatedAt(profile.getCreatedAt());
        dto.setUpdatedAt(profile.getUpdatedAt());
        return dto;
    }
}
