// ====== FILE: src/main/java/com/warehouse/wms/repository/ZoneRepository.java ======
package com.warehouse.wms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.warehouse.wms.entity.Zone;

import jakarta.transaction.Transactional;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {

    // ====== Basic Queries ======
    
    Optional<Zone> findByZoneId(String zoneId);
    
    List<Zone> findByWarehouseId(Long warehouseId);
    
    List<Zone> findByWarehouseIdAndIsActiveTrue(Long warehouseId);
    
    List<Zone> findByIsActiveTrue();
    
    // ====== Paginated Queries ======
    
    Page<Zone> findByWarehouseId(Long warehouseId, Pageable pageable);
    
    Page<Zone> findByIsActiveTrue(Pageable pageable);
    
    @Query("SELECT z FROM Zone z WHERE z.warehouse.id = :warehouseId AND z.isActive = true")
    Page<Zone> findActiveByWarehouseId(@Param("warehouseId") Long warehouseId, Pageable pageable);
    
    // ====== Search Queries ======
    
    @Query("SELECT z FROM Zone z WHERE z.isActive = true AND " +
           "(LOWER(z.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(z.zoneId) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Zone> searchZones(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT z FROM Zone z WHERE z.warehouse.id = :warehouseId AND " +
           "(LOWER(z.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(z.zoneId) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Zone> searchZonesByWarehouse(@Param("warehouseId") Long warehouseId, 
                                       @Param("search") String search, 
                                       Pageable pageable);
    
    // ====== Zone Type Queries ======
    
    List<Zone> findByZoneType(String zoneType);
    
    List<Zone> findByWarehouseIdAndZoneType(Long warehouseId, String zoneType);
    
    @Query("SELECT z FROM Zone z WHERE z.warehouse.warehouseId = :warehouseId AND z.isActive = true")
    List<Zone> findByWarehouseWarehouseId(@Param("warehouseId") String warehouseId);
    
    // ====== Count Queries ======
    
    long countByWarehouseId(Long warehouseId);
    
    @Query("SELECT COUNT(z) FROM Zone z WHERE z.warehouse.id = :warehouseId AND z.isActive = true")
    long countActiveByWarehouseId(@Param("warehouseId") Long warehouseId);
    
    // ====== Existence Checks ======
    
    boolean existsByZoneId(String zoneId);
    
    boolean existsByZoneIdAndIdNot(String zoneId, Long id);
    
    // ====== Update Queries ======
    
    @Modifying
    @Transactional
    @Query("UPDATE Zone z SET z.isActive = :isActive WHERE z.id = :id")
    int updateActiveStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);
    
    @Modifying
    @Transactional
    @Query("UPDATE Zone z SET z.totalAisles = :count WHERE z.id = :id")
    int updateTotalAisles(@Param("id") Long id, @Param("count") Integer count);
    
    
    
    
    
    
    
 Optional<Zone> findByWarehouse_WarehouseIdAndZoneId(String warehouseId, String zoneId);
    
    List<Zone> findByWarehouse_WarehouseId(String warehouseId);
    
    List<Zone> findByWarehouse_WarehouseIdAndIsActiveTrue(String warehouseId);
    
    
    boolean existsByWarehouse_WarehouseIdAndZoneId(String warehouseId, String zoneId);
}