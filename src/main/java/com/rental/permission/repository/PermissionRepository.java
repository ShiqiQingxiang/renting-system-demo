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

    // 基础查询
    Optional<Permission> findByName(String name);

    boolean existsByName(String name);

    // 按类型查询
    List<Permission> findByType(Permission.PermissionType type);

    // 按资源路径查询
    List<Permission> findByResourceContaining(String resource);

    // 父子权限查询
    List<Permission> findByParentId(Long parentId);

    List<Permission> findByParentIsNull(); // 根权限

    // 递归查询所有子权限
    @Query("SELECT p FROM Permission p WHERE p.parent.id = :parentId ORDER BY p.sortOrder")
    List<Permission> findChildrenByParentId(@Param("parentId") Long parentId);

    // 查询用户的所有权限
    @Query("SELECT DISTINCT p FROM Permission p " +
           "JOIN p.roles r " +
           "JOIN r.users u " +
           "WHERE u.id = :userId")
    List<Permission> findByUserId(@Param("userId") Long userId);

    // 按排序查询
    List<Permission> findAllByOrderBySortOrder();

    // 按类型和父级查询
    List<Permission> findByTypeAndParentId(Permission.PermissionType type, Long parentId);
}