package com.rental.permission.repository;

import com.rental.permission.model.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    /**
     * 根据角色ID查找所有权限关联
     */
    List<RolePermission> findByRoleId(Long roleId);

    /**
     * 根据权限ID查找所有角色关联
     */
    List<RolePermission> findByPermissionId(Long permissionId);

    /**
     * 检查角色是否拥有指定权限
     */
    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);

    /**
     * 删除角色的指定权限
     */
    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.roleId = :roleId AND rp.permissionId = :permissionId")
    void deleteByRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * 删除角色的所有权限
     */
    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除权限的所有角色关联
     */
    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.permissionId = :permissionId")
    void deleteByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 获取角色的权限ID集合
     */
    @Query("SELECT rp.permissionId FROM RolePermission rp WHERE rp.roleId = :roleId")
    Set<Long> findPermissionIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 获取拥有指定权限的角色ID集合
     */
    @Query("SELECT rp.roleId FROM RolePermission rp WHERE rp.permissionId = :permissionId")
    Set<Long> findRoleIdsByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 统计角色拥有的权限数量
     */
    long countByRoleId(Long roleId);

    /**
     * 统计权限分配给的角色数量
     */
    long countByPermissionId(Long permissionId);
}
