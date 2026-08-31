// ====== FILE: src/main/java/com/warehouse/wms/repository/LevelRepository.java ======
package com.warehouse.wms.repository;

import com.warehouse.wms.entity.Level;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LevelRepository extends JpaRepository<Level, Long> {

    Optional<Level> findByLevelId(String levelId);

    List<Level> findByRackId(Long rackId);


    Page<Level> findByRackId(Long rackId, Pageable pageable);

    @Query("SELECT l FROM Level l WHERE l.rack.id = :rackId ORDER BY l.levelNumber ASC")
    List<Level> findByRackIdOrderByLevelNumberAsc(@Param("rackId") Long rackId);

    boolean existsByLevelId(String levelId);

    long countByRackId(Long rackId);
    
    
    Optional<Level> findByRack_Aisle_Zone_Warehouse_WarehouseIdAndRack_Aisle_Zone_ZoneIdAndRack_Aisle_AisleIdAndRack_RackIdAndLevelId(
            String warehouseId, String zoneId, String aisleId, String rackId, String levelId);
    
    List<Level> findByRack_Aisle_Zone_Warehouse_WarehouseIdAndRack_Aisle_Zone_ZoneIdAndRack_Aisle_AisleIdAndRack_RackId(
            String warehouseId, String zoneId, String aisleId, String rackId);
    
    List<Level> findByRack_Aisle_Zone_Warehouse_WarehouseIdAndRack_Aisle_Zone_ZoneIdAndRack_Aisle_AisleId(
            String warehouseId, String zoneId, String aisleId);
    
    List<Level> findByRack_Aisle_Zone_Warehouse_WarehouseIdAndRack_Aisle_Zone_ZoneId(
            String warehouseId, String zoneId);
    
    List<Level> findByRack_Aisle_Zone_Warehouse_WarehouseId(String warehouseId);
    
    List<Level> findByRackIdAndIsActiveTrue(Long rackId);
    
    boolean existsByRack_Aisle_Zone_Warehouse_WarehouseIdAndRack_Aisle_Zone_ZoneIdAndRack_Aisle_AisleIdAndRack_RackIdAndLevelId(
            String warehouseId, String zoneId, String aisleId, String rackId, String levelId);
    
    
    
    

    
    @Query("SELECT l FROM Level l WHERE l.rack.aisle.zone.warehouse.warehouseId = :warehouseId " +
           "AND l.rack.aisle.zone.zoneId = :zoneId " +
           "AND l.rack.aisle.aisleId = :aisleId " +
           "AND l.rack.rackId = :rackId " +
           "AND l.levelId = :levelId")
    Optional<Level> findByWarehouseIdAndZoneIdAndAisleIdAndRackIdAndLevelId(
            @Param("warehouseId") String warehouseId,
            @Param("zoneId") String zoneId,
            @Param("aisleId") String aisleId,
            @Param("rackId") String rackId,
            @Param("levelId") String levelId);
    
    @Query("SELECT l FROM Level l WHERE l.rack.aisle.zone.warehouse.warehouseId = :warehouseId " +
           "AND l.rack.aisle.zone.zoneId = :zoneId " +
           "AND l.rack.aisle.aisleId = :aisleId " +
           "AND l.rack.rackId = :rackId")
    List<Level> findByWarehouseIdAndZoneIdAndAisleIdAndRackId(
            @Param("warehouseId") String warehouseId,
            @Param("zoneId") String zoneId,
            @Param("aisleId") String aisleId,
            @Param("rackId") String rackId);
    
    
    
    
    @Query("SELECT l FROM Level l LEFT JOIN FETCH l.rack WHERE l.rack.id = :rackId")
    List<Level> findByRackIdWithFullHierarchy(@Param("rackId") Long rackId);
}