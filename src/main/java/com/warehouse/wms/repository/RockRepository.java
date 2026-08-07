// ====== FILE: src/main/java/com/warehouse/wms/repository/RockRepository.java ======
package com.warehouse.wms.repository;

import com.warehouse.wms.entity.Rock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface RockRepository extends JpaRepository<Rock, Long> {

    // ====== Basic Queries ======
    
    Optional<Rock> findByRockId(String rockId);
    
    List<Rock> findByWarehouseId(Long warehouseId);
    
    List<Rock> findByWarehouseIdAndIsActiveTrue(Long warehouseId);
    
    List<Rock> findByIsActiveTrue();
    
    List<Rock> findByRockType(String rockType);
    
    // ====== Paginated Queries ======
    
    Page<Rock> findByWarehouseId(Long warehouseId, Pageable pageable);
    
    Page<Rock> findByIsActiveTrue(Pageable pageable);
    
    Page<Rock> findByRockType(String rockType, Pageable pageable);
    
    // ====== Search Queries ======
    
    @Query("SELECT r FROM Rock r WHERE r.isActive = true AND " +
           "(LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.rockId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.rockType) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Rock> searchRocks(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT r FROM Rock r WHERE r.warehouse.id = :warehouseId AND r.isActive = true AND " +
           "(LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.rockId) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Rock> searchRocksByWarehouse(@Param("warehouseId") Long warehouseId,
                                       @Param("search") String search,
                                       Pageable pageable);
    
    // ====== Quantity Queries ======
    
    @Query("SELECT r FROM Rock r WHERE r.quantity < r.minQuantity AND r.isActive = true")
    List<Rock> findLowStockRocks();
    
    @Query("SELECT r FROM Rock r WHERE r.quantity > r.maxQuantity AND r.isActive = true")
    List<Rock> findOverStockRocks();
    
    @Query("SELECT r FROM Rock r WHERE r.quantity <= :threshold AND r.isActive = true")
    List<Rock> findRocksWithLowQuantity(@Param("threshold") Integer threshold);
    
    // ====== Count Queries ======
    
    long countByWarehouseId(Long warehouseId);
    
    @Query("SELECT COUNT(r) FROM Rock r WHERE r.warehouse.id = :warehouseId AND r.isActive = true")
    long countActiveByWarehouseId(@Param("warehouseId") Long warehouseId);
    
    // ====== Existence Checks ======
    
    boolean existsByRockId(String rockId);
    
    boolean existsByRockIdAndIdNot(String rockId, Long id);
    
    // ====== Update Queries ======
    
    @Modifying
    @Transactional
    @Query("UPDATE Rock r SET r.isActive = :isActive WHERE r.id = :id")
    int updateActiveStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);
    
    @Modifying
    @Transactional
    @Query("UPDATE Rock r SET r.quantity = r.quantity + :quantity WHERE r.id = :id")
    int addQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);
    
    @Modifying
    @Transactional
    @Query("UPDATE Rock r SET r.quantity = r.quantity - :quantity WHERE r.id = :id AND r.quantity >= :quantity")
    int deductQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);
}