package com.rental.permission.repository;

import com.rental.permission.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * 根据名称查找角色
     */
    Optional<Role> findByName(String name);

    /**
     * 查找系统角色
     */
    List<Role> findByIsSystemTrue();

    /**
     * 查找非系统角色
     */
    List<Role> findByIsSystemFalse();

    /**
     * 查找用户的所有角色
     */
    @Query("""
        SELECT r FROM Role r 
        JOIN UserRole ur ON r.id = ur.roleId 
        WHERE ur.userId = :userId
        """)
    List<Role> findUserRoles(@Param("userId") Long userId);

    /**
     * 检查角色名称是否已存在（排除指定ID）
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * 统计拥有指定角色的用户数量
     */
    @Query("SELECT COUNT(ur.userId) FROM UserRole ur WHERE ur.roleId = :roleId")
    long countUsersByRoleId(@Param("roleId") Long roleId);
}
