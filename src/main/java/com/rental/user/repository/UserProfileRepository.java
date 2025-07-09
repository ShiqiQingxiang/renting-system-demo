package com.rental.user.repository;

import com.rental.user.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    // 根据用户ID查询用户资料
    Optional<UserProfile> findByUserId(Long userId);

    // 根据身份证号查询
    Optional<UserProfile> findByIdCard(String idCard);

    // 检查身份证号是否存在
    boolean existsByIdCard(String idCard);

    // 根据真实姓名模糊查询
    List<UserProfile> findByRealNameContaining(String realName);

    // 根据性别查询
    List<UserProfile> findByGender(UserProfile.Gender gender);

    // 查询有头像的用户资料
    @Query("SELECT up FROM UserProfile up WHERE up.avatarUrl IS NOT NULL")
    List<UserProfile> findProfilesWithAvatar();

    // 查询完善资料的用户（有真实姓名和身份证）
    @Query("SELECT up FROM UserProfile up WHERE up.realName IS NOT NULL AND up.idCard IS NOT NULL")
    List<UserProfile> findCompleteProfiles();

    // 根据用户ID删除用户资料
    void deleteByUserId(Long userId);

    // 根据真实姓名和性别查询用户资料
    List<UserProfile> findByRealNameAndGender(String realName, UserProfile.Gender gender);

    // 根据用户ID列表查询用户资料
    List<UserProfile> findByUserIdIn(List<Long> userIds);

    // 根据身份证号列表查询用户资料
    List<UserProfile> findByIdCardIn(List<String> idCards);

    // 查询所有用户资料
    List<UserProfile> findAll();

    // 根据条件分页查询用户资料
    @Query("SELECT up FROM UserProfile up WHERE (:realName IS NULL OR up.realName LIKE %:realName%) " +
           "AND (:gender IS NULL OR up.gender = :gender) " +
           "AND (:idCard IS NULL OR up.idCard LIKE %:idCard%)")
    List<UserProfile> findByConditions(@Param("realName") String realName,
                                       @Param("gender") UserProfile.Gender gender,
                                       @Param("idCard") String idCard,
                                       Pageable pageable);
}
