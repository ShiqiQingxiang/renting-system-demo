package com.rental.file.repository;

import com.rental.file.model.FileCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 文件分类数据访问接口
 */
@Repository
public interface FileCategoryRepository extends JpaRepository<FileCategory, Long>, JpaSpecificationExecutor<FileCategory> {
    
    /**
     * 根据分类代码查找分类
     * @param code 分类代码
     * @return 文件分类
     */
    Optional<FileCategory> findByCode(String code);
    
    /**
     * 查找所有激活的分类，按排序顺序排列
     * @return 文件分类列表
     */
    List<FileCategory> findByIsActiveTrueOrderBySortOrder();
    
    /**
     * 根据名称模糊查询激活的分类
     * @param name 分类名称（可为空）
     * @param pageable 分页参数
     * @return 分页的文件分类
     */
    @Query("SELECT fc FROM FileCategory fc WHERE fc.isActive = true AND " +
           "(:name IS NULL OR fc.name LIKE %:name%)")
    Page<FileCategory> findActiveCategories(@Param("name") String name, Pageable pageable);
    
    /**
     * 检查分类代码是否已存在（排除指定ID）
     * @param code 分类代码
     * @param excludeId 排除的ID
     * @return 是否存在
     */
    @Query("SELECT COUNT(fc) > 0 FROM FileCategory fc WHERE fc.code = :code AND " +
           "(:excludeId IS NULL OR fc.id != :excludeId)")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("excludeId") Long excludeId);
    
    /**
     * 查找所有激活分类的代码列表
     * @return 分类代码列表
     */
    @Query("SELECT fc.code FROM FileCategory fc WHERE fc.isActive = true")
    List<String> findAllActiveCodes();
    
    /**
     * 根据分类代码批量查询分类
     * @param codes 分类代码列表
     * @return 文件分类列表
     */
    @Query("SELECT fc FROM FileCategory fc WHERE fc.code IN :codes AND fc.isActive = true")
    List<FileCategory> findByCodesAndActive(@Param("codes") List<String> codes);
}