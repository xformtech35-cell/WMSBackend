// ====== FILE: src/main/java/com/warehouse/wms/repository/RackRepository.java ======
package com.warehouse.wms.repository;

import com.warehouse.wms.entity.Rack;
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
public interface RackRepository extends JpaRepository<Rack, Long> {

    // ====== Basic Queries ======
    
    Optional<Rack> findByRackId(String rackId);
    
    List<Rack> findByAisleId(Long aisleId);
    
    List<Rack> findByAisleIdAndIsActiveTrue(Long aisleId);
    
    List<Rack> findByIsActiveTrue();
    
    // ====== Paginated Queries ======
    
    Page<Rack> findByAisleId(Long aisleId, Pageable pageable);
    
    Page<Rack> findByIsActiveTrue(Pageable pageable);
    
    @Query("SELECT r FROM Rack r WHERE r.aisle.id = :aisleId AND r.isActive = true")
    Page<Rack> findActiveByAisleId(@Param("aisleId") Long aisleId, Pageable pageable);
    
    // ====== Search Queries ======
    
    @Query("SELECT r FROM Rack r WHERE r.isActive = true AND " +
           "(LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.rackId) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Rack> searchRacks(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT r FROM Rack r WHERE r.aisle.id = :aisleId AND " +
           "(LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.rackId) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Rack> searchRacksByAisle(@Param("aisleId") Long aisleId, 
                                   @Param("search") String search, 
                                   Pageable pageable);
    
    // ====== Aisle Queries ======
    
    @Query("SELECT r FROM Rack r WHERE r.aisle.aisleId = :aisleId AND r.isActive = true")
    List<Rack> findByAisleAisleId(@Param("aisleId") String aisleId);
    
    @Query("SELECT r FROM Rack r WHERE r.aisle.zone.id = :zoneId AND r.isActive = true")
    List<Rack> findByZoneId(@Param("zoneId") Long zoneId);
    
    @Query("SELECT r FROM Rack r WHERE r.aisle.zone.warehouse.id = :warehouseId AND r.isActive = true")
    List<Rack> findByWarehouseId(@Param("warehouseId") Long warehouseId);
    
    // ====== Count Queries ======
    
    long countByAisleId(Long aisleId);
    
    @Query("SELECT COUNT(r) FROM Rack r WHERE r.aisle.id = :aisleId AND r.isActive = true")
    long countActiveByAisleId(@Param("aisleId") Long aisleId);
    
    // ====== Existence Checks ======
    
    boolean existsByRackId(String rackId);
    
    boolean existsByRackIdAndIdNot(String rackId, Long id);
    
    // ====== Update Queries ======
    
    @Modifying
    @Transactional
    @Query("UPDATE Rack r SET r.isActive = :isActive WHERE r.id = :id")
    int updateActiveStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);
    
    @Modifying
    @Transactional
    @Query("UPDATE Rack r SET r.totalShelves = :count WHERE r.id = :id")
    int updateTotalShelves(@Param("id") Long id, @Param("count") Integer count);
    
    
    
    
    Optional<Rack> findByAisle_Zone_Warehouse_WarehouseIdAndAisle_Zone_ZoneIdAndAisle_AisleIdAndRackId(
            String warehouseId, String zoneId, String aisleId, String rackId);
    
    List<Rack> findByAisle_Zone_Warehouse_WarehouseIdAndAisle_Zone_ZoneIdAndAisle_AisleId(
            String warehouseId, String zoneId, String aisleId);
    
    List<Rack> findByAisle_Zone_Warehouse_WarehouseIdAndAisle_Zone_ZoneId(
            String warehouseId, String zoneId);
    
    List<Rack> findByAisle_Zone_Warehouse_WarehouseId(String warehouseId);
    
    
    boolean existsByAisle_Zone_Warehouse_WarehouseIdAndAisle_Zone_ZoneIdAndAisle_AisleIdAndRackId(
            String warehouseId, String zoneId, String aisleId, String rackId);
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @Query("SELECT r FROM Rack r WHERE r.aisle.zone.warehouse.warehouseId = :warehouseId " +
           "AND r.aisle.zone.zoneId = :zoneId " +
           "AND r.aisle.aisleId = :aisleId " +
           "AND r.rackId = :rackId")
    Optional<Rack> findByWarehouseIdAndZoneIdAndAisleIdAndRackId(
            @Param("warehouseId") String warehouseId,
            @Param("zoneId") String zoneId,
            @Param("aisleId") String aisleId,
            @Param("rackId") String rackId);
    
    @Query("SELECT r FROM Rack r WHERE r.aisle.zone.warehouse.warehouseId = :warehouseId " +
           "AND r.aisle.zone.zoneId = :zoneId " +
           "AND r.aisle.aisleId = :aisleId")
    List<Rack> findByWarehouseIdAndZoneIdAndAisleId(
            @Param("warehouseId") String warehouseId,
            @Param("zoneId") String zoneId,
            @Param("aisleId") String aisleId);
    
    
    
    @Query("SELECT r FROM Rack r LEFT JOIN FETCH r.aisle WHERE r.aisle.id = :aisleId")
    List<Rack> findByAisleIdWithFullHierarchy(@Param("aisleId") Long aisleId);
}