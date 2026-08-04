// ====== FILE: src/main/java/com/warehouse/wms/repository/BinLocationRepository.java ======
package com.warehouse.wms.repository;

import com.warehouse.wms.entity.BinLocation;
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
public interface BinLocationRepository extends JpaRepository<BinLocation, Long> {

    // ====== Basic Queries ======
    
    Optional<BinLocation> findByBinId(String binId);
    
    Optional<BinLocation> findByBinBarcode(String binBarcode);
    
    List<BinLocation> findByWarehouseId(String warehouseId);
    
    List<BinLocation> findByWarehouseIdAndZone(String warehouseId, String zone);
    
    List<BinLocation> findByWarehouseIdAndIsOccupiedFalse(String warehouseId);
    
    List<BinLocation> findByWarehouseIdAndZoneAndIsOccupiedFalse(String warehouseId, String zone);
    
    List<BinLocation> findByIsActiveTrue();
    
    List<BinLocation> findByIsOccupiedTrue();
    
    // ====== Paginated Queries ======
    
    Page<BinLocation> findByWarehouseId(String warehouseId, Pageable pageable);
    
    Page<BinLocation> findByIsActiveTrue(Pageable pageable);
    
    Page<BinLocation> findByWarehouseIdAndIsActiveTrue(String warehouseId, Pageable pageable);
    
    // ====== Capacity Queries ======
    
    @Query("SELECT b FROM BinLocation b WHERE b.warehouseId = :warehouseId " +
           "AND b.availableCapacity >= :requiredQuantity " +
           "AND b.isOccupied = false AND b.isActive = true " +
           "ORDER BY b.priority ASC, b.distanceFromDispatch ASC")
    List<BinLocation> findBestAvailableLocation(@Param("warehouseId") String warehouseId,
                                                  @Param("requiredQuantity") Integer requiredQuantity);
    
    
    
    
    
    @Query("SELECT b FROM BinLocation b WHERE b.warehouseId = :warehouseId " +
           "AND b.zone = :zone AND b.availableCapacity >= :requiredQuantity " +
           "AND b.isOccupied = false AND b.isActive = true " +
           "ORDER BY b.priority ASC, b.distanceFromDispatch ASC")
    List<BinLocation> findBestAvailableLocationByZone(@Param("warehouseId") String warehouseId,
                                                       @Param("zone") String zone,
                                                       @Param("requiredQuantity") Integer requiredQuantity);
    
    @Query("SELECT b FROM BinLocation b WHERE b.warehouseId = :warehouseId " +
           "AND b.itemCode = :itemCode AND b.availableCapacity >= :quantity")
    List<BinLocation> findLocationsWithItem(@Param("warehouseId") String warehouseId,
                                             @Param("itemCode") String itemCode,
                                             @Param("quantity") Integer quantity);
    
    @Query("SELECT b FROM BinLocation b WHERE b.warehouseId = :warehouseId " +
           "AND b.zoneType = :zoneType ORDER BY b.priority ASC")
    List<BinLocation> findLocationsByZoneType(@Param("warehouseId") String warehouseId,
                                               @Param("zoneType") String zoneType);
    
    // ====== Update Queries ======
    
    @Modifying
    @Transactional
    @Query("UPDATE BinLocation b SET b.availableCapacity = b.availableCapacity - :quantity, " +
           "b.usedCapacity = b.usedCapacity + :quantity, b.isOccupied = true, " +
           "b.itemCode = :itemCode, b.itemName = :itemName, b.uom = :uom " +
           "WHERE b.binId = :binId AND b.availableCapacity >= :quantity")
    int allocateBinCapacity(@Param("binId") String binId, 
                            @Param("quantity") Integer quantity,
                            @Param("itemCode") String itemCode,
                            @Param("itemName") String itemName,
                            @Param("uom") String uom);
    
    @Modifying
    @Transactional
    @Query("UPDATE BinLocation b SET b.availableCapacity = b.availableCapacity + :quantity, " +
           "b.usedCapacity = b.usedCapacity - :quantity, " +
           "b.isOccupied = CASE WHEN (b.usedCapacity - :quantity) > 0 THEN true ELSE false END " +
           "WHERE b.binId = :binId")
    int releaseBinCapacity(@Param("binId") String binId, @Param("quantity") Integer quantity);
    
    @Modifying
    @Transactional
    @Query("UPDATE BinLocation b SET b.isOccupied = :isOccupied WHERE b.binId = :binId")
    int updateOccupiedStatus(@Param("binId") String binId, @Param("isOccupied") Boolean isOccupied);
    
    @Modifying
    @Transactional
    @Query("UPDATE BinLocation b SET b.isActive = :isActive WHERE b.id = :id")
    int updateActiveStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);
    
    @Modifying
    @Transactional
    @Query("UPDATE BinLocation b SET b.lastAccessedAt = CURRENT_TIMESTAMP WHERE b.binId = :binId")
    int updateLastAccessed(@Param("binId") String binId);
    
    @Modifying
    @Transactional
    @Query("UPDATE BinLocation b SET b.lastPutawayAt = CURRENT_TIMESTAMP WHERE b.binId = :binId")
    int updateLastPutaway(@Param("binId") String binId);
    
    @Modifying
    @Transactional
    @Query("UPDATE BinLocation b SET b.lastPickAt = CURRENT_TIMESTAMP WHERE b.binId = :binId")
    int updateLastPick(@Param("binId") String binId);
    
    // ====== Count Queries ======
    
    @Query("SELECT COUNT(b) FROM BinLocation b WHERE b.warehouseId = :warehouseId AND b.isActive = true")
    Long countActiveByWarehouse(@Param("warehouseId") String warehouseId);
    
    @Query("SELECT COUNT(b) FROM BinLocation b WHERE b.warehouseId = :warehouseId AND b.isOccupied = true")
    Long countOccupiedByWarehouse(@Param("warehouseId") String warehouseId);
    
    @Query("SELECT SUM(b.availableCapacity) FROM BinLocation b WHERE b.warehouseId = :warehouseId AND b.isActive = true")
    Long sumAvailableCapacityByWarehouse(@Param("warehouseId") String warehouseId);
    
    // ====== Existence Checks ======
    
    boolean existsByBinId(String binId);
    
    boolean existsByBinBarcode(String binBarcode);
    
    @Modifying
    @Transactional
    @Query("UPDATE BinLocation b SET b.availableCapacity = b.availableCapacity - :quantity, " +
           "b.usedCapacity = b.usedCapacity + :quantity, " +
           "b.isOccupied = CASE WHEN (b.usedCapacity + :quantity) > 0 THEN true ELSE false END " +
           "WHERE b.binId = :binId AND b.availableCapacity >= :quantity")
    int allocateBinCapacitySimple(@Param("binId") String binId, 
                                   @Param("quantity") Integer quantity);
    
}