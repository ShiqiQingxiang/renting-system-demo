package com.rental.user.repository;

import com.rental.user.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /**
     * 根据用户ID查找用户资料
     */
    Optional<UserProfile> findByUserId(Long userId);

    /**
     * 根据身份证号查找用户资料
     */
    Optional<UserProfile> findByIdCard(String idCard);

    /**
     * 检查身份证号是否存在
     */
    boolean existsByIdCard(String idCard);

    /**
     * 删除指定用户的资料
     */
    void deleteByUserId(Long userId);

    /**
     * 查询用户完整信息（包含用户基本信息和资料）
     */
    @Query("SELECT up FROM UserProfile up JOIN FETCH up.user WHERE up.user.id = :userId")
    Optional<UserProfile> findByUserIdWithUser(@Param("userId") Long userId);
}
