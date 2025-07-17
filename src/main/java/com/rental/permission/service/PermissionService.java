package com.rental.permission.service;

import com.rental.permission.DTO.PermissionDTO;
import com.rental.permission.model.Permission;
import com.rental.permission.repository.PermissionRepository;
import com.rental.common.exception.BusinessException;
import com.rental.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PermissionService {

    private final PermissionRepository permissionRepository;

    /**
     * 获取所有权限
     */
    public List<PermissionDTO> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findAll();
        return permissions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取权限树结构
     */
    public List<PermissionDTO> getPermissionTree() {
        List<Permission> rootPermissions = permissionRepository.findByParentIdIsNullOrderBySortOrder();
        return rootPermissions.stream()
                .map(this::convertToDTOWithChildren)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取权限
     */
    public PermissionDTO getPermissionById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("权限不存在，ID: " + id));
        return convertToDTO(permission);
    }

    /**
     * 根据类型获取权限
     */
    public List<PermissionDTO> getPermissionsByType(Permission.PermissionType type) {
        List<Permission> permissions = permissionRepository.findByType(type);
        return permissions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户权限
     */
    public List<PermissionDTO> getUserPermissions(Long userId) {
        List<Permission> permissions = permissionRepository.findUserPermissions(userId);
        return permissions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 创建权限
     */
    @Transactional
    public PermissionDTO createPermission(PermissionDTO permissionDTO) {
        // 检查权限名称是否已存在
        if (permissionRepository.findByName(permissionDTO.getName()).isPresent()) {
            throw new BusinessException("权限名称已存在: " + permissionDTO.getName());
        }

        Permission permission = convertToEntity(permissionDTO);
        permission = permissionRepository.save(permission);
        log.info("创建权限成功，ID: {}, 名称: {}", permission.getId(), permission.getName());

        return convertToDTO(permission);
    }

    /**
     * 更新权限
     */
    @Transactional
    public PermissionDTO updatePermission(Long id, PermissionDTO permissionDTO) {
        Permission existingPermission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("权限不存在，ID: " + id));

        // 检查权限名称是否已被其他权限使用
        if (permissionRepository.existsByNameAndIdNot(permissionDTO.getName(), id)) {
            throw new BusinessException("权限名称已存在: " + permissionDTO.getName());
        }

        // 更新权限信息
        existingPermission.setName(permissionDTO.getName());
        existingPermission.setDescription(permissionDTO.getDescription());
        existingPermission.setType(permissionDTO.getType());
        existingPermission.setResource(permissionDTO.getResource());
        existingPermission.setSortOrder(permissionDTO.getSortOrder());

        // 处理父权限
        if (permissionDTO.getParentId() != null) {
            if (permissionDTO.getParentId().equals(id)) {
                throw new BusinessException("权限不能设置自己为父权限");
            }
            Permission parent = permissionRepository.findById(permissionDTO.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("父权限不存在，ID: " + permissionDTO.getParentId()));
            existingPermission.setParent(parent);
        } else {
            existingPermission.setParent(null);
        }

        existingPermission = permissionRepository.save(existingPermission);
        log.info("更新权限成功，ID: {}, 名称: {}", existingPermission.getId(), existingPermission.getName());

        return convertToDTO(existingPermission);
    }

    /**
     * 删除权限
     */
    @Transactional
    public void deletePermission(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("权限不存在，ID: " + id));

        // 检查是否有子权限
        if (permissionRepository.existsByParentId(id)) {
            throw new BusinessException("存在子权限，无法删除");
        }

        permissionRepository.delete(permission);
        log.info("删除权限成功，ID: {}, 名称: {}", permission.getId(), permission.getName());
    }

    /**
     * 转换为DTO
     */
    private PermissionDTO convertToDTO(Permission permission) {
        PermissionDTO dto = new PermissionDTO();
        dto.setId(permission.getId());
        dto.setName(permission.getName());
        dto.setDescription(permission.getDescription());
        dto.setType(permission.getType());
        dto.setResource(permission.getResource());
        dto.setSortOrder(permission.getSortOrder());
        dto.setCreatedAt(permission.getCreatedAt());

        if (permission.getParent() != null) {
            dto.setParentId(permission.getParent().getId());
            dto.setParentName(permission.getParent().getName());
        }

        return dto;
    }

    /**
     * 转换为DTO（包含子权限）
     */
    private PermissionDTO convertToDTOWithChildren(Permission permission) {
        PermissionDTO dto = convertToDTO(permission);

        if (permission.getChildren() != null && !permission.getChildren().isEmpty()) {
            List<PermissionDTO> children = permission.getChildren().stream()
                    .map(this::convertToDTOWithChildren)
                    .collect(Collectors.toList());
            dto.setChildren(children);
        }

        return dto;
    }

    /**
     * 转换为实体
     */
    private Permission convertToEntity(PermissionDTO dto) {
        Permission permission = new Permission();
        permission.setName(dto.getName());
        permission.setDescription(dto.getDescription());
        permission.setType(dto.getType());
        permission.setResource(dto.getResource());
        permission.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);

        if (dto.getParentId() != null) {
            Permission parent = permissionRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("父权限不存在，ID: " + dto.getParentId()));
            permission.setParent(parent);
        }

        return permission;
    }
}
