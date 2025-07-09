package com.rental.item.repository;

import com.rental.item.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    // 基础查询
    List<Item> findByNameContaining(String name);

    Page<Item> findByNameContaining(String name, Pageable pageable);

    // 状态查询
    List<Item> findByStatus(Item.ItemStatus status);

    Page<Item> findByStatus(Item.ItemStatus status, Pageable pageable);

    // 分类查询
    List<Item> findByCategoryId(Long categoryId);

    Page<Item> findByCategoryId(Long categoryId, Pageable pageable);

    // 价格范围查询
    List<Item> findByPricePerDayBetween(BigDecimal minPrice, BigDecimal maxPrice);

    Page<Item> findByPricePerDayBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // 复合查询
    @Query("SELECT i FROM Item i WHERE " +
           "(:categoryId IS NULL OR i.category.id = :categoryId) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:minPrice IS NULL OR i.pricePerDay >= :minPrice) AND " +
           "(:maxPrice IS NULL OR i.pricePerDay <= :maxPrice) AND " +
           "(:keyword IS NULL OR i.name LIKE %:keyword% OR i.description LIKE %:keyword%)")
    Page<Item> findItemsByConditions(
        @Param("categoryId") Long categoryId,
        @Param("status") Item.ItemStatus status,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    // 地区查询
    List<Item> findByLocationContaining(String location);

    // 可租用物品查询
    @Query("SELECT i FROM Item i WHERE i.status = 'AVAILABLE'")
    List<Item> findAvailableItems();

    @Query("SELECT i FROM Item i WHERE i.status = 'AVAILABLE'")
    Page<Item> findAvailableItems(Pageable pageable);

    // 热门物品查询（根据订单数量）
    @Query("SELECT i FROM Item i JOIN i.orderItems oi GROUP BY i ORDER BY COUNT(oi) DESC")
    List<Item> findPopularItems(Pageable pageable);

    // 统计查询
    long countByStatus(Item.ItemStatus status);

    long countByCategoryId(Long categoryId);

    @Query("SELECT COUNT(i) FROM Item i WHERE i.createdAt >= :date")
    long countNewItemsAfter(@Param("date") LocalDateTime date);

    // 价格统计
    @Query("SELECT AVG(i.pricePerDay) FROM Item i WHERE i.status = 'AVAILABLE'")
    BigDecimal getAveragePricePerDay();

    @Query("SELECT MIN(i.pricePerDay) FROM Item i WHERE i.status = 'AVAILABLE'")
    BigDecimal getMinPricePerDay();

    @Query("SELECT MAX(i.pricePerDay) FROM Item i WHERE i.status = 'AVAILABLE'")
    BigDecimal getMaxPricePerDay();
}