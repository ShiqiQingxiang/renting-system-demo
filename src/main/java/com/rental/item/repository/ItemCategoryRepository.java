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

    // 基础查询
    Optional<ItemCategory> findByName(String name);

    boolean existsByName(String name);

    List<ItemCategory> findByNameContaining(String name);

    // 父子分类查询
    List<ItemCategory> findByParentId(Long parentId);

    List<ItemCategory> findByParentIsNull(); // 根分类

    // 递归查询所有子分类
    @Query("SELECT c FROM ItemCategory c WHERE c.parent.id = :parentId ORDER BY c.sortOrder")
    List<ItemCategory> findChildrenByParentId(@Param("parentId") Long parentId);

    // 按照排序顺序查询
    List<ItemCategory> findAllByOrderBySortOrder();

    List<ItemCategory> findByParentIdOrderBySortOrder(Long parentId);

    // 查询有物品的分类
    @Query("SELECT DISTINCT c FROM ItemCategory c JOIN c.items i WHERE i.status = 'AVAILABLE'")
    List<ItemCategory> findCategoriesWithAvailableItems();

    // 统计分类下的物品数量
    @Query("SELECT COUNT(i) FROM Item i WHERE i.category.id = :categoryId")
    long countItemsByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 获取分类的完整路径 (从根分类到当前分类)
     * 例如: "电子产品 > 手机 > 苹果手机"
     */
    @Query(value = "WITH RECURSIVE category_path (id, name, path) AS (" +
            "  SELECT id, name, name as path FROM item_categories WHERE id = :categoryId " +
            "  UNION ALL " +
            "  SELECT c.id, c.name, CONCAT(c.name, ' > ', cp.path) " +
            "  FROM item_categories c " +
            "  JOIN category_path cp ON c.id = cp.parent_id " +
            ") " +
            "SELECT path FROM category_path WHERE parent_id IS NULL LIMIT 1", nativeQuery = true)
    String getCategoryPath(@Param("categoryId") Long categoryId);
}
