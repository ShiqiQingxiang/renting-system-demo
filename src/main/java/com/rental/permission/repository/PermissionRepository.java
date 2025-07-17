package com.rental.permission.repository;

import com.rental.permission.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * 根据名称查找权限
     */
    Optional<Permission> findByName(String name);

    /**
     * 根据类型查找权限
     */
    List<Permission> findByType(Permission.PermissionType type);

    /**
     * 查找所有顶级权限（无父权限）
     */
    List<Permission> findByParentIdIsNullOrderBySortOrder();

    /**
     * 根据父权限ID查找子权限
     */
    List<Permission> findByParentIdOrderBySortOrder(Long parentId);

    /**
     * 查找用户的所有权限
     */
    @Query("""
        SELECT DISTINCT p FROM Permission p
        JOIN RolePermission rp ON p.id = rp.permissionId
        JOIN UserRole ur ON rp.roleId = ur.roleId
        WHERE ur.userId = :userId
        ORDER BY p.sortOrder
        """)
    List<Permission> findUserPermissions(@Param("userId") Long userId);

    /**
     * 查找角色的所有权限
     */
    @Query("""
        SELECT p FROM Permission p
        JOIN RolePermission rp ON p.id = rp.permissionId
        WHERE rp.roleId = :roleId
        ORDER BY p.sortOrder
        """)
    List<Permission> findRolePermissions(@Param("roleId") Long roleId);

    /**
     * 检查权限名称是否已存在（排除指定ID）
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * 检查是否存在子权限
     */
    boolean existsByParentId(Long parentId);
}
