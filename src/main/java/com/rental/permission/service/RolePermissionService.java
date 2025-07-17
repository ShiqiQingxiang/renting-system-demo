package com.rental.permission.service;

import com.rental.permission.model.RolePermission;
import com.rental.permission.repository.RolePermissionRepository;
import com.rental.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;

    /**
     * 获取角色的权限ID集合
     */
    public Set<Long> getRolePermissionIds(Long roleId) {
        return rolePermissionRepository.findPermissionIdsByRoleId(roleId);
    }

    /**
     * 获取拥有指定权限的角色ID集合
     */
    public Set<Long> getRoleIdsByPermissionId(Long permissionId) {
        return rolePermissionRepository.findRoleIdsByPermissionId(permissionId);
    }

    /**
     * 为角色分配权限
     */
    @Transactional
    public void assignPermissionsToRole(Long roleId, Set<Long> permissionIds) {
        // 先删除角色现有的所有权限
        rolePermissionRepository.deleteByRoleId(roleId);

        // 添加新的权限关联
        Set<RolePermission> rolePermissions = permissionIds.stream()
                .map(permissionId -> new RolePermission(null, roleId, permissionId, null))
                .collect(Collectors.toSet());

        rolePermissionRepository.saveAll(rolePermissions);
        log.info("为角色分配权限成功，角色ID: {}, 权限数量: {}", roleId, permissionIds.size());
    }

    /**
     * 为角色添加权限
     */
    @Transactional
    public void addPermissionToRole(Long roleId, Long permissionId) {
        if (!rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            RolePermission rolePermission = new RolePermission(null, roleId, permissionId, null);
            rolePermissionRepository.save(rolePermission);
            log.info("为角色添加权限成功，角色ID: {}, 权限ID: {}", roleId, permissionId);
        }
    }

    /**
     * 移除角色的指定权限
     */
    @Transactional
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        rolePermissionRepository.deleteByRoleIdAndPermissionId(roleId, permissionId);
        log.info("移除角色权限成功，角色ID: {}, 权限ID: {}", roleId, permissionId);
    }

    /**
     * 移除角色的所有权限
     */
    @Transactional
    public void removeAllPermissionsFromRole(Long roleId) {
        rolePermissionRepository.deleteByRoleId(roleId);
        log.info("移除角色所有权限成功，角色ID: {}", roleId);
    }

    /**
     * 移除权限的所有角色关联
     */
    @Transactional
    public void removeAllRolesFromPermission(Long permissionId) {
        rolePermissionRepository.deleteByPermissionId(permissionId);
        log.info("移除权限所有角色关联成功，权限ID: {}", permissionId);
    }

    /**
     * 检查角色是否拥有权限
     */
    public boolean hasPermission(Long roleId, Long permissionId) {
        return rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId);
    }

    /**
     * 统计角色拥有的权限数量
     */
    public long getRolePermissionCount(Long roleId) {
        return rolePermissionRepository.countByRoleId(roleId);
    }

    /**
     * 统计权限分配给的角色数量
     */
    public long getPermissionRoleCount(Long permissionId) {
        return rolePermissionRepository.countByPermissionId(permissionId);
    }
}
