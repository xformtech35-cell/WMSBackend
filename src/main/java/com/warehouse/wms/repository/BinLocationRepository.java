// ====== FILE: src/main/java/com/warehouse/wms/repository/BinLocationRepository.java ======
package com.warehouse.wms.repository;

import com.warehouse.wms.entity.BinLocation;
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

    Optional<BinLocation> findByBinId(String binId);

    Optional<BinLocation> findByBinBarcode(String binBarcode);

    List<BinLocation> findByWarehouseId(String warehouseId);

    List<BinLocation> findByWarehouseIdAndZone(String warehouseId, String zone);

    List<BinLocation> findByWarehouseIdAndZoneAndAisle(String warehouseId, String zone, String aisle);

    List<BinLocation> findByWarehouseIdAndZoneAndAisleAndRack(String warehouseId, String zone, String aisle, String rack);

    List<BinLocation> findByIsOccupiedFalse();

    List<BinLocation> findByIsActiveTrue();

    @Query("SELECT b FROM BinLocation b WHERE b.warehouseId = :warehouseId " +
           "AND b.zone = :zone AND b.availableCapacity >= :requiredQuantity " +
           "AND b.isOccupied = false AND b.isActive = true " +
           "ORDER BY b.priority ASC, b.distanceFromDispatch ASC")
    List<BinLocation> findBestAvailableLocation(@Param("warehouseId") String warehouseId,
                                                  @Param("zone") String zone,
                                                  @Param("requiredQuantity") Integer requiredQuantity);

    @Query("SELECT b FROM BinLocation b WHERE b.warehouseId = :warehouseId " +
           "AND b.zone = :zone AND b.itemCode = :itemCode AND b.availableCapacity >= :quantity")
    List<BinLocation> findLocationsWithItem(@Param("warehouseId") String warehouseId,
                                             @Param("zone") String zone,
                                             @Param("itemCode") String itemCode,
                                             @Param("quantity") Integer quantity);

    @Query("SELECT b FROM BinLocation b WHERE b.warehouseId = :warehouseId " +
           "AND b.zoneType = :zoneType ORDER BY b.priority ASC")
    List<BinLocation> findLocationsByZoneType(@Param("warehouseId") String warehouseId,
                                               @Param("zoneType") String zoneType);

    @Modifying
    @Transactional
    @Query("UPDATE BinLocation b SET b.availableCapacity = b.availableCapacity - :quantity, " +
           "b.usedCapacity = b.usedCapacity + :quantity, b.isOccupied = true " +
           "WHERE b.binId = :binId AND b.availableCapacity >= :quantity")
    int allocateBinCapacity(@Param("binId") String binId, @Param("quantity") Integer quantity);

    @Modifying
    @Transactional
    @Query("UPDATE BinLocation b SET b.availableCapacity = b.availableCapacity + :quantity, " +
           "b.usedCapacity = b.usedCapacity - :quantity " +
           "WHERE b.binId = :binId")
    int releaseBinCapacity(@Param("binId") String binId, @Param("quantity") Integer quantity);

    @Modifying
    @Transactional
    @Query("UPDATE BinLocation b SET b.isOccupied = :isOccupied WHERE b.binId = :binId")
    int updateOccupiedStatus(@Param("binId") String binId, @Param("isOccupied") Boolean isOccupied);
}