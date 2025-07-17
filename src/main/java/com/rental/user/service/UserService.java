package com.rental.user.service;

import com.rental.user.model.User;
import com.rental.user.DTO.*;
import com.rental.common.response.PageResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserService {

    /**
     * 创建用户
     */
    UserDTO createUser(UserCreateRequest request);

    /**
     * 更新用户信息
     */
    UserDTO updateUser(Long userId, UserUpdateRequest request);

    /**
     * 根据ID获取用户
     */
    UserDTO getUserById(Long userId);

    /**
     * 根据用户名获取用户
     */
    UserDTO getUserByUsername(String username);

    /**
     * 分页查询用户
     */
    PageResponse<UserDTO> queryUsers(UserQueryRequest request);

    /**
     * 删除用户
     */
    void deleteUser(Long userId);

    /**
     * 批量删除用户
     */
    void deleteUsers(List<Long> userIds);

    /**
     * 更新用户状态
     */
    void updateUserStatus(Long userId, User.UserStatus status);

    /**
     * 批量更新用户状态
     */
    void batchUpdateUserStatus(UserBatchOperationRequest request);

    /**
     * 为用户分配角色
     */
    void assignRoles(Long userId, Long[] roleIds);

    /**
     * 移除用户的指定角色
     */
    void removeRoleFromUser(Long userId, Long roleId);

    /**
     * 清空用户的所有角色
     */
    void clearUserRoles(Long userId);

    /**
     * 获取用户的角色名称集合
     */
    Set<String> getUserRoleNames(Long userId);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 重置用户密码
     */
    void resetPassword(Long userId, String newPassword);

    /**
     * 获取用户统计信息
     */
    Map<String, Object> getUserStatistics();
}
