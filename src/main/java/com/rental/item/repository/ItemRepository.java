package com.rental.item.repository;

import com.rental.item.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // 根据状态查询物品
    List<Item> findByStatus(Item.ItemStatus status);

    // 根据分类查询物品
    List<Item> findByCategoryId(Long categoryId);

    // 根据所有者查询物品
    List<Item> findByOwnerId(Long ownerId);

    // 根据审核状态查询物品
    List<Item> findByApprovalStatus(Item.ApprovalStatus approvalStatus);

    // 查询可租赁的物品
    @Query("SELECT i FROM Item i WHERE i.status = 'AVAILABLE' AND i.approvalStatus = 'APPROVED'")
    List<Item> findAvailableItems();

    // 分页查询可租赁的物品
    @Query("SELECT i FROM Item i WHERE i.status = 'AVAILABLE' AND i.approvalStatus = 'APPROVED'")
    Page<Item> findAvailableItems(Pageable pageable);

    // 根据名称模糊查询
    Page<Item> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // 根据价格范围查询
    Page<Item> findByPricePerDayBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // 根据位置查询
    Page<Item> findByLocationContainingIgnoreCase(String location, Pageable pageable);

    // 复合查询
    @Query("SELECT i FROM Item i WHERE " +
           "(:name IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:categoryId IS NULL OR i.category.id = :categoryId) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:approvalStatus IS NULL OR i.approvalStatus = :approvalStatus) AND " +
           "(:ownerId IS NULL OR i.owner.id = :ownerId) AND " +
           "(:minPrice IS NULL OR i.pricePerDay >= :minPrice) AND " +
           "(:maxPrice IS NULL OR i.pricePerDay <= :maxPrice) AND " +
           "(:location IS NULL OR LOWER(i.location) LIKE LOWER(CONCAT('%', :location, '%')))")
    Page<Item> findBySearchCriteria(
        @Param("name") String name,
        @Param("categoryId") Long categoryId,
        @Param("status") Item.ItemStatus status,
        @Param("approvalStatus") Item.ApprovalStatus approvalStatus,
        @Param("ownerId") Long ownerId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("location") String location,
        Pageable pageable
    );

    // 统计查询
    long countByStatus(Item.ItemStatus status);
    long countByApprovalStatus(Item.ApprovalStatus approvalStatus);
    long countByOwnerId(Long ownerId);
    long countByCategoryId(Long categoryId);

    // 查询待审核的物品
    @Query("SELECT i FROM Item i WHERE i.approvalStatus = 'PENDING' ORDER BY i.createdAt ASC")
    Page<Item> findPendingApprovalItems(Pageable pageable);
}