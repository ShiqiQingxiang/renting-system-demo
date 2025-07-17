package com.rental.user.service.impl;

import com.rental.user.service.UserService;
import com.rental.user.repository.UserRepository;
import com.rental.user.repository.UserProfileRepository;
import com.rental.permission.repository.RoleRepository;
import com.rental.user.model.User;
import com.rental.user.model.UserProfile;
import com.rental.permission.model.Role;
import com.rental.user.DTO.*;
import com.rental.common.response.PageResponse;
import com.rental.common.exception.BusinessException;
import com.rental.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDTO createUser(UserCreateRequest request) {
        log.info("Creating user with username: {}", request.getUsername());

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已存在");
        }

        // 检查手机号是否已存在（如果提供）
        if (request.getPhone() != null && !request.getPhone().isEmpty() &&
            userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("手机号已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(User.UserStatus.ACTIVE);

        // 分配角色
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (Long roleId : request.getRoleIds()) {
                Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("角色不存在: " + roleId));
                roles.add(role);
            }
            user.setRoles(roles);
        } else {
            // 默认分配客户角色
            Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("默认客户角色不存在"));
            user.setRoles(Set.of(customerRole));
        }

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return convertToDTO(savedUser);
    }

    @Override
    public UserDTO updateUser(Long userId, UserUpdateRequest request) {
        log.info("Updating user with ID: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 更新邮箱
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("邮箱已存在");
            }
            user.setEmail(request.getEmail());
        }

        // 更新手机号
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            if (!request.getPhone().isEmpty() && userRepository.existsByPhone(request.getPhone())) {
                throw new BusinessException("手机号已存在");
            }
            user.setPhone(request.getPhone());
        }

        // 更新状态
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        // 更新角色
        if (request.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>();
            for (Long roleId : request.getRoleIds()) {
                Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("角色不存在: " + roleId));
                roles.add(role);
            }
            user.setRoles(roles);
        }

        User savedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", savedUser.getId());

        return convertToDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        return convertToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        return convertToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserDTO> queryUsers(UserQueryRequest request) {
        // 构建排序
        Sort sort = Sort.by(
            "desc".equalsIgnoreCase(request.getSortDir()) ?
            Sort.Direction.DESC : Sort.Direction.ASC,
            request.getSortBy()
        );

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<User> userPage;
        if (request.getRoleName() != null && !request.getRoleName().isEmpty()) {
            // 按角色查询
            List<User> users = userRepository.findByRoleName(request.getRoleName());
            // 这里需要手动实现分页，为简化示例，直接返回所有结果
            userPage = userRepository.findUsersWithFilters(
                request.getKeyword(), request.getStatus(), pageable);
        } else {
            userPage = userRepository.findUsersWithFilters(
                request.getKeyword(), request.getStatus(), pageable);
        }

        List<UserDTO> userDTOs = userPage.getContent().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        return PageResponse.<UserDTO>builder()
            .content(userDTOs)
            .totalElements(userPage.getTotalElements())
            .totalPages(userPage.getTotalPages())
            .currentPage(userPage.getNumber())
            .pageSize(userPage.getSize())
            .hasNext(userPage.hasNext())
            .hasPrevious(userPage.hasPrevious())
            .build();
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("用户不存在");
        }

        userRepository.deleteById(userId);
        log.info("User deleted successfully with ID: {}", userId);
    }

    @Override
    public void deleteUsers(List<Long> userIds) {
        log.info("Batch deleting users with IDs: {}", userIds);

        List<User> users = userRepository.findAllById(userIds);
        if (users.size() != userIds.size()) {
            throw new BusinessException("部分用户不存在");
        }

        userRepository.deleteAllById(userIds);
        log.info("Users deleted successfully, count: {}", userIds.size());
    }

    @Override
    public void updateUserStatus(Long userId, User.UserStatus status) {
        log.info("Updating user status for ID: {} to {}", userId, status);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        user.setStatus(status);
        userRepository.save(user);

        log.info("User status updated successfully for ID: {}", userId);
    }

    @Override
    public void batchUpdateUserStatus(UserBatchOperationRequest request) {
        log.info("Batch updating user status for IDs: {} to {}",
                request.getUserIds(), request.getStatus());

        int updatedCount = userRepository.updateUserStatusBatch(
            request.getUserIds(), request.getStatus());

        if (updatedCount != request.getUserIds().size()) {
            throw new BusinessException("部分用户状态更新失败");
        }

        log.info("User status updated successfully, count: {}", updatedCount);
    }

    @Override
    public void assignRoles(Long userId, Long[] roleIds) {
        log.info("Assigning roles to user ID: {}, roles: {}", userId, Arrays.toString(roleIds));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Set<Role> roles = new HashSet<>();
        for (Long roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("角色不存在: " + roleId));
            roles.add(role);
        }

        user.setRoles(roles);
        userRepository.save(user);

        log.info("Roles assigned successfully to user ID: {}", userId);
    }

    @Override
    public void removeRoleFromUser(Long userId, Long roleId) {
        log.info("Removing role {} from user {}", roleId, userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Role roleToRemove = roleRepository.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("角色不存在"));

        user.getRoles().remove(roleToRemove);
        userRepository.save(user);

        log.info("Role {} removed successfully from user {}", roleId, userId);
    }

    @Override
    public void clearUserRoles(Long userId) {
        log.info("Clearing all roles for user {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        user.getRoles().clear();
        userRepository.save(user);

        log.info("All roles cleared successfully for user {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> getUserRoleNames(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        return user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        log.info("Resetting password for user ID: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password reset successfully for user ID: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 总用户数
        long totalUsers = userRepository.count();
        statistics.put("totalUsers", totalUsers);

        // 按状态统计
        List<Object[]> statusCounts = userRepository.countUsersByStatus();
        Map<String, Long> statusStatistics = new HashMap<>();
        for (Object[] result : statusCounts) {
            User.UserStatus status = (User.UserStatus) result[0];
            Long count = (Long) result[1];
            statusStatistics.put(status.name(), count);
        }
        statistics.put("usersByStatus", statusStatistics);

        // 活跃用户数
        List<User> activeUsers = userRepository.findByStatus(User.UserStatus.ACTIVE);
        statistics.put("activeUsers", activeUsers.size());

        return statistics;
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        // 转换角色名称
        if (user.getRoles() != null) {
            Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
            dto.setRoleNames(roleNames);
        }

        // 转换用户资料
        if (user.getProfile() != null) {
            dto.setProfile(convertProfileToDTO(user.getProfile()));
        }

        return dto;
    }

    private UserProfileDTO convertProfileToDTO(UserProfile profile) {
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
