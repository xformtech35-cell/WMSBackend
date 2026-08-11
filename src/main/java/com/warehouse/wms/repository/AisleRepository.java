// ====== FILE: src/main/java/com/warehouse/wms/repository/AisleRepository.java ======
package com.warehouse.wms.repository;

import com.warehouse.wms.entity.Aisle;
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
public interface AisleRepository extends JpaRepository<Aisle, Long> {

    // ====== Basic Queries ======
    
    Optional<Aisle> findByAisleId(String aisleId);
    
    List<Aisle> findByZoneId(Long zoneId);
    
    List<Aisle> findByZoneIdAndIsActiveTrue(Long zoneId);
    
    List<Aisle> findByIsActiveTrue();
    
    // ====== Paginated Queries ======
    
    Page<Aisle> findByZoneId(Long zoneId, Pageable pageable);
    
    Page<Aisle> findByIsActiveTrue(Pageable pageable);
    
    @Query("SELECT a FROM Aisle a WHERE a.zone.id = :zoneId AND a.isActive = true")
    Page<Aisle> findActiveByZoneId(@Param("zoneId") Long zoneId, Pageable pageable);
    
    // ====== Search Queries ======
    
    @Query("SELECT a FROM Aisle a WHERE a.isActive = true AND " +
           "(LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.aisleId) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Aisle> searchAisles(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT a FROM Aisle a WHERE a.zone.id = :zoneId AND " +
           "(LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.aisleId) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Aisle> searchAislesByZone(@Param("zoneId") Long zoneId, 
                                    @Param("search") String search, 
                                    Pageable pageable);
    
    // ====== Zone Queries ======
    
    @Query("SELECT a FROM Aisle a WHERE a.zone.zoneId = :zoneId AND a.isActive = true")
    List<Aisle> findByZoneZoneId(@Param("zoneId") String zoneId);
    
    @Query("SELECT a FROM Aisle a WHERE a.zone.warehouse.id = :warehouseId AND a.isActive = true")
    List<Aisle> findByWarehouseId(@Param("warehouseId") Long warehouseId);
    
    // ====== Count Queries ======
    
    long countByZoneId(Long zoneId);
    
    @Query("SELECT COUNT(a) FROM Aisle a WHERE a.zone.id = :zoneId AND a.isActive = true")
    long countActiveByZoneId(@Param("zoneId") Long zoneId);
    
    // ====== Existence Checks ======
    
    boolean existsByAisleId(String aisleId);
    
    boolean existsByAisleIdAndIdNot(String aisleId, Long id);
    
    // ====== Update Queries ======
    
    @Modifying
    @Transactional
    @Query("UPDATE Aisle a SET a.isActive = :isActive WHERE a.id = :id")
    int updateActiveStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);
    
    @Modifying
    @Transactional
    @Query("UPDATE Aisle a SET a.totalRacks = :count WHERE a.id = :id")
    int updateTotalRacks(@Param("id") Long id, @Param("count") Integer count);
    
    
    Optional<Aisle> findByZone_Warehouse_WarehouseIdAndZone_ZoneIdAndAisleId(
            String warehouseId, String zoneId, String aisleId);
    
    List<Aisle> findByZone_Warehouse_WarehouseIdAndZone_ZoneId(
            String warehouseId, String zoneId);
    
    List<Aisle> findByZone_Warehouse_WarehouseId(String warehouseId);
    
    
    boolean existsByZone_Warehouse_WarehouseIdAndZone_ZoneIdAndAisleId(
            String warehouseId, String zoneId, String aisleId);
}