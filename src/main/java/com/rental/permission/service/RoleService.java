package com.rental.permission.service;

import com.rental.permission.DTO.RoleDTO;
import com.rental.permission.DTO.RolePermissionAssignRequest;
import com.rental.permission.model.Role;
import com.rental.permission.repository.RoleRepository;
import com.rental.common.exception.BusinessException;
import com.rental.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RoleService {

    private final RoleRepository roleRepository;
    private final RolePermissionService rolePermissionService;

    /**
     * 获取所有角色
     */
    public List<RoleDTO> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 分页获取角色
     */
    public Page<RoleDTO> getRoles(Pageable pageable) {
        Page<Role> roles = roleRepository.findAll(pageable);
        return roles.map(this::convertToDTO);
    }

    /**
     * 根据ID获取角色
     */
    public RoleDTO getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("角色不存在，ID: " + id));
        return convertToDTOWithPermissions(role);
    }

    /**
     * 获取用户角色
     */
    public List<RoleDTO> getUserRoles(Long userId) {
        List<Role> roles = roleRepository.findUserRoles(userId);
        return roles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取系统角色
     */
    public List<RoleDTO> getSystemRoles() {
        List<Role> roles = roleRepository.findByIsSystemTrue();
        return roles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取非系统角色
     */
    public List<RoleDTO> getNonSystemRoles() {
        List<Role> roles = roleRepository.findByIsSystemFalse();
        return roles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 创建角色
     */
    @Transactional
    public RoleDTO createRole(RoleDTO roleDTO) {
        // 检查角色名称是否已存在
        if (roleRepository.findByName(roleDTO.getName()).isPresent()) {
            throw new BusinessException("角色名称已存在: " + roleDTO.getName());
        }

        Role role = convertToEntity(roleDTO);
        role.setIsSystem(false); // 新创建的角色都是非系统角色
        role = roleRepository.save(role);

        log.info("创建角色成功，ID: {}, 名称: {}", role.getId(), role.getName());
        return convertToDTO(role);
    }

    /**
     * 更新角色
     */
    @Transactional
    public RoleDTO updateRole(Long id, RoleDTO roleDTO) {
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("角色不存在，ID: " + id));

        // 系统角色不允许修改名称
        if (existingRole.getIsSystem() && !existingRole.getName().equals(roleDTO.getName())) {
            throw new BusinessException("系统角色不允许修改名称");
        }

        // 检查角色名称是否已被其他角色使用
        if (roleRepository.existsByNameAndIdNot(roleDTO.getName(), id)) {
            throw new BusinessException("角色名称已存在: " + roleDTO.getName());
        }

        // 更新角色信息
        existingRole.setName(roleDTO.getName());
        existingRole.setDescription(roleDTO.getDescription());

        existingRole = roleRepository.save(existingRole);
        log.info("更新角色成功，ID: {}, 名称: {}", existingRole.getId(), existingRole.getName());

        return convertToDTO(existingRole);
    }

    /**
     * 删除角色
     */
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("角色不存在，ID: " + id));

        // 系统角色不允许删除
        if (role.getIsSystem()) {
            throw new BusinessException("系统角色不允许删除");
        }

        // 检查是否有用户使用该角色
        long userCount = roleRepository.countUsersByRoleId(id);
        if (userCount > 0) {
            throw new BusinessException("该角色已分配给用户，无法删除");
        }

        // 删除角色的权限关联
        rolePermissionService.removeAllPermissionsFromRole(id);

        // 删除角色
        roleRepository.delete(role);
        log.info("删除角色成功，ID: {}, 名称: {}", role.getId(), role.getName());
    }

    /**
     * 为角色分配权限
     */
    @Transactional
    public void assignPermissionsToRole(RolePermissionAssignRequest request) {
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("角色不存在，ID: " + request.getRoleId()));

        rolePermissionService.assignPermissionsToRole(request.getRoleId(), request.getPermissionIds());
        log.info("为角色分配权限成功，角色ID: {}, 权限数量: {}", request.getRoleId(), request.getPermissionIds().size());
    }

    /**
     * 移除角色权限
     */
    @Transactional
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("角色不存在，ID: " + roleId));

        rolePermissionService.removePermissionFromRole(roleId, permissionId);
        log.info("移除角色权限成功，角色ID: {}, 权限ID: {}", roleId, permissionId);
    }

    /**
     * 转换为DTO
     */
    private RoleDTO convertToDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setIsSystem(role.getIsSystem());
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());
        return dto;
    }

    /**
     * 转换为DTO（包含权限信息）
     */
    private RoleDTO convertToDTOWithPermissions(Role role) {
        RoleDTO dto = convertToDTO(role);
        dto.setPermissionIds(rolePermissionService.getRolePermissionIds(role.getId()));
        return dto;
    }

    /**
     * 转换为实体
     */
    private Role convertToEntity(RoleDTO dto) {
        Role role = new Role();
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        return role;
    }
}
