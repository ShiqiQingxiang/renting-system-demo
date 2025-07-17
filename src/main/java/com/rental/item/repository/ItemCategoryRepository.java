package com.rental.item.repository;

import com.rental.item.model.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemCategoryRepository extends JpaRepository<ItemCategory, Long> {

    // 查找根分类（没有父分类的分类）
    List<ItemCategory> findByParentIsNullOrderBySortOrder();

    // 根据父分类ID查找子分类
    List<ItemCategory> findByParentIdOrderBySortOrder(Long parentId);

    // 根据分类名称查找
    Optional<ItemCategory> findByName(String name);

    // 检查分类名称是否存在（排除指定ID）
    boolean existsByNameAndIdNot(String name, Long id);

    // 检查分类名称是否存在
    boolean existsByName(String name);

    // 根据父分类查找所有子分类
    List<ItemCategory> findByParent(ItemCategory parent);

    // 查询指定分类的所有子分类（递归查询）
    @Query("SELECT c FROM ItemCategory c WHERE c.parent.id = :parentId OR c.id IN " +
           "(SELECT c2.id FROM ItemCategory c2 WHERE c2.parent.id IN " +
           "(SELECT c3.id FROM ItemCategory c3 WHERE c3.parent.id = :parentId))")
    List<ItemCategory> findAllSubCategories(@Param("parentId") Long parentId);

    // 查询分类树（包含所有层级）
    @Query("SELECT c FROM ItemCategory c ORDER BY " +
           "CASE WHEN c.parent IS NULL THEN c.sortOrder " +
           "ELSE c.parent.sortOrder * 1000 + c.sortOrder END")
    List<ItemCategory> findAllInTreeOrder();

    // 统计分类下的物品数量
    @Query("SELECT COUNT(i) FROM Item i WHERE i.category.id = :categoryId")
    long countItemsByCategoryId(@Param("categoryId") Long categoryId);

    // 查询有物品的分类
    @Query("SELECT DISTINCT c FROM ItemCategory c INNER JOIN c.items i")
    List<ItemCategory> findCategoriesWithItems();
}
