package com.rental.permission.repository;

import com.rental.permission.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // 基础查询
    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    // 系统角色查询
    List<Role> findByIsSystem(Boolean isSystem);

    Page<Role> findByIsSystem(Boolean isSystem, Pageable pageable);

    // 模糊查询
    @Query("SELECT r FROM Role r WHERE r.name LIKE %:keyword% OR r.description LIKE %:keyword%")
    Page<Role> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 查询用户的所有角色
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    List<Role> findByUserId(@Param("userId") Long userId);

    // 查询具有特定权限的角色
    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    List<Role> findByPermissionName(@Param("permissionName") String permissionName);

    // 查询非系统角色
    @Query("SELECT r FROM Role r WHERE r.isSystem = false ORDER BY r.name")
    List<Role> findCustomRoles();

    // 统计拥有角色的用户数量
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.id = :roleId")
    long countUsersByRoleId(@Param("roleId") Long roleId);

    // 新增：根据角色ID查询角色
    Optional<Role> findById(Long id);

    // 新增：删除角色
    void deleteById(Long id);

    // 新增：保存角色
    <S extends Role> S save(S entity);

    // 新增：批量保存角色
    <S extends Role> List<S> saveAll(Iterable<S> entities);

    // 新增：根据ID集合查询角色
    List<Role> findAllById(Iterable<Long> ids);

    // 新增：分页查询所有角色
    Page<Role> findAll(Pageable pageable);
}