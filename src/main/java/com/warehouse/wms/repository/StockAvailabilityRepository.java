// ====== FILE: src/main/java/com/warehouse/wms/repository/StockAvailabilityRepository.java ======
package com.warehouse.wms.repository;

import com.warehouse.wms.entity.StockAvailability;
import com.warehouse.wms.entity.StockAvailability.LocationLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockAvailabilityRepository extends JpaRepository<StockAvailability, Long> {

    // ====== Find by specific location and item ======
    
    @Query("SELECT s FROM StockAvailability s WHERE " +
           "s.warehouseId = :warehouseId AND " +
           "(:zoneId IS NULL OR s.zoneId = :zoneId) AND " +
           "(:aisleId IS NULL OR s.aisleId = :aisleId) AND " +
           "(:rackId IS NULL OR s.rackId = :rackId) AND " +
           "(:levelId IS NULL OR s.levelId = :levelId) AND " +
           "(:binId IS NULL OR s.binId = :binId) AND " +
           "s.itemCode = :itemCode AND " +
           "s.locationLevel = :locationLevel")
    Optional<StockAvailability> findByLocationAndItem(
            @Param("warehouseId") String warehouseId,
            @Param("zoneId") String zoneId,
            @Param("aisleId") String aisleId,
            @Param("rackId") String rackId,
            @Param("levelId") String levelId,
            @Param("binId") String binId,
            @Param("itemCode") String itemCode,
            @Param("locationLevel") LocationLevel locationLevel);

    // ====== Find by warehouse level ======
    
    List<StockAvailability> findByWarehouseIdAndLocationLevel(String warehouseId, LocationLevel locationLevel);
    
    List<StockAvailability> findByWarehouseIdAndItemCodeAndLocationLevel(
            String warehouseId, String itemCode, LocationLevel locationLevel);

    // ====== Find by zone level ======
    
    List<StockAvailability> findByWarehouseIdAndZoneIdAndLocationLevel(
            String warehouseId, String zoneId, LocationLevel locationLevel);
    
    List<StockAvailability> findByWarehouseIdAndZoneIdAndItemCodeAndLocationLevel(
            String warehouseId, String zoneId, String itemCode, LocationLevel locationLevel);

    // ====== Find by aisle level ======
    
    List<StockAvailability> findByWarehouseIdAndZoneIdAndAisleIdAndLocationLevel(
            String warehouseId, String zoneId, String aisleId, LocationLevel locationLevel);
    
    List<StockAvailability> findByWarehouseIdAndZoneIdAndAisleIdAndItemCodeAndLocationLevel(
            String warehouseId, String zoneId, String aisleId, String itemCode, LocationLevel locationLevel);

    // ====== Find by rack level ======
    
    List<StockAvailability> findByWarehouseIdAndZoneIdAndAisleIdAndRackIdAndLocationLevel(
            String warehouseId, String zoneId, String aisleId, String rackId, LocationLevel locationLevel);
    
    List<StockAvailability> findByWarehouseIdAndZoneIdAndAisleIdAndRackIdAndItemCodeAndLocationLevel(
            String warehouseId, String zoneId, String aisleId, String rackId, String itemCode, LocationLevel locationLevel);

    // ====== Find by level ======
    
    List<StockAvailability> findByWarehouseIdAndZoneIdAndAisleIdAndRackIdAndLevelIdAndLocationLevel(
            String warehouseId, String zoneId, String aisleId, String rackId, String levelId, LocationLevel locationLevel);
    
    List<StockAvailability> findByWarehouseIdAndZoneIdAndAisleIdAndRackIdAndLevelIdAndItemCodeAndLocationLevel(
            String warehouseId, String zoneId, String aisleId, String rackId, String levelId, String itemCode, LocationLevel locationLevel);

    // ====== Find by bin level ======
    
    @Query("SELECT s FROM StockAvailability s WHERE s.binId = :binId AND (:itemCode IS NULL OR s.itemCode = :itemCode)")
    List<StockAvailability> findByBinIdAndItemCode(
            @Param("binId") String binId, 
            @Param("itemCode") String itemCode);
    
    @Query("SELECT s FROM StockAvailability s WHERE s.binBarcode = :binBarcode AND (:itemCode IS NULL OR s.itemCode = :itemCode)")
    List<StockAvailability> findByBinBarcodeAndItemCode(
            @Param("binBarcode") String binBarcode, 
            @Param("itemCode") String itemCode);

    @Query("SELECT s FROM StockAvailability s WHERE s.binId = :binId")
    List<StockAvailability> findByBinId(@Param("binId") String binId);

    @Query("SELECT s FROM StockAvailability s WHERE s.binBarcode = :binBarcode")
    List<StockAvailability> findByBinBarcode(@Param("binBarcode") String binBarcode);

    List<StockAvailability> findByLocationLevel(LocationLevel locationLevel);

    // ====== MAX CAPACITY QUERIES at each level ======
    
    @Query("SELECT s.maxCapacity FROM StockAvailability s WHERE s.warehouseId = :warehouseId AND s.locationLevel = :locationLevel")
    List<Integer> findMaxCapacityByWarehouseAndLevel(
            @Param("warehouseId") String warehouseId, 
            @Param("locationLevel") LocationLevel locationLevel);

    @Query("SELECT s.maxCapacity FROM StockAvailability s WHERE s.warehouseId = :warehouseId AND s.zoneId = :zoneId AND s.locationLevel = :locationLevel")
    List<Integer> findMaxCapacityByZoneAndLevel(
            @Param("warehouseId") String warehouseId, 
            @Param("zoneId") String zoneId, 
            @Param("locationLevel") LocationLevel locationLevel);

    @Query("SELECT s.maxCapacity FROM StockAvailability s WHERE s.warehouseId = :warehouseId AND s.zoneId = :zoneId AND s.aisleId = :aisleId AND s.locationLevel = :locationLevel")
    List<Integer> findMaxCapacityByAisleAndLevel(
            @Param("warehouseId") String warehouseId, 
            @Param("zoneId") String zoneId, 
            @Param("aisleId") String aisleId, 
            @Param("locationLevel") LocationLevel locationLevel);

    @Query("SELECT s.maxCapacity FROM StockAvailability s WHERE s.warehouseId = :warehouseId AND s.zoneId = :zoneId AND s.aisleId = :aisleId AND s.rackId = :rackId AND s.locationLevel = :locationLevel")
    List<Integer> findMaxCapacityByRackAndLevel(
            @Param("warehouseId") String warehouseId, 
            @Param("zoneId") String zoneId, 
            @Param("aisleId") String aisleId, 
            @Param("rackId") String rackId, 
            @Param("locationLevel") LocationLevel locationLevel);

    @Query("SELECT s.maxCapacity FROM StockAvailability s WHERE s.warehouseId = :warehouseId AND s.zoneId = :zoneId AND s.aisleId = :aisleId AND s.rackId = :rackId AND s.levelId = :levelId AND s.locationLevel = :locationLevel")
    List<Integer> findMaxCapacityByLevelAndLevel(
            @Param("warehouseId") String warehouseId, 
            @Param("zoneId") String zoneId, 
            @Param("aisleId") String aisleId, 
            @Param("rackId") String rackId, 
            @Param("levelId") String levelId, 
            @Param("locationLevel") LocationLevel locationLevel);

    @Query("SELECT s.maxCapacity FROM StockAvailability s WHERE s.binId = :binId AND s.locationLevel = :locationLevel")
    List<Integer> findMaxCapacityByBin(
            @Param("binId") String binId, 
            @Param("locationLevel") LocationLevel locationLevel);

    // ====== MIN CAPACITY QUERIES at each level ======
    
    @Query("SELECT s.minCapacity FROM StockAvailability s WHERE s.warehouseId = :warehouseId AND s.locationLevel = :locationLevel")
    List<Integer> findMinCapacityByWarehouseAndLevel(
            @Param("warehouseId") String warehouseId, 
            @Param("locationLevel") LocationLevel locationLevel);

    @Query("SELECT s.minCapacity FROM StockAvailability s WHERE s.warehouseId = :warehouseId AND s.zoneId = :zoneId AND s.locationLevel = :locationLevel")
    List<Integer> findMinCapacityByZoneAndLevel(
            @Param("warehouseId") String warehouseId, 
            @Param("zoneId") String zoneId, 
            @Param("locationLevel") LocationLevel locationLevel);

    @Query("SELECT s.minCapacity FROM StockAvailability s WHERE s.warehouseId = :warehouseId AND s.zoneId = :zoneId AND s.aisleId = :aisleId AND s.locationLevel = :locationLevel")
    List<Integer> findMinCapacityByAisleAndLevel(
            @Param("warehouseId") String warehouseId, 
            @Param("zoneId") String zoneId, 
            @Param("aisleId") String aisleId, 
            @Param("locationLevel") LocationLevel locationLevel);

    @Query("SELECT s.minCapacity FROM StockAvailability s WHERE s.warehouseId = :warehouseId AND s.zoneId = :zoneId AND s.aisleId = :aisleId AND s.rackId = :rackId AND s.locationLevel = :locationLevel")
    List<Integer> findMinCapacityByRackAndLevel(
            @Param("warehouseId") String warehouseId, 
            @Param("zoneId") String zoneId, 
            @Param("aisleId") String aisleId, 
            @Param("rackId") String rackId, 
            @Param("locationLevel") LocationLevel locationLevel);

    @Query("SELECT s.minCapacity FROM StockAvailability s WHERE s.warehouseId = :warehouseId AND s.zoneId = :zoneId AND s.aisleId = :aisleId AND s.rackId = :rackId AND s.levelId = :levelId AND s.locationLevel = :locationLevel")
    List<Integer> findMinCapacityByLevelAndLevel(
            @Param("warehouseId") String warehouseId, 
            @Param("zoneId") String zoneId, 
            @Param("aisleId") String aisleId, 
            @Param("rackId") String rackId, 
            @Param("levelId") String levelId, 
            @Param("locationLevel") LocationLevel locationLevel);

    @Query("SELECT s.minCapacity FROM StockAvailability s WHERE s.binId = :binId AND s.locationLevel = :locationLevel")
    List<Integer> findMinCapacityByBin(
            @Param("binId") String binId, 
            @Param("locationLevel") LocationLevel locationLevel);

    // ====== Aggregation Queries ======

    @Query("SELECT SUM(s.totalQuantity) FROM StockAvailability s WHERE s.itemCode = :itemCode AND s.warehouseId = :warehouseId")
    Integer getTotalStockForItemInWarehouse(@Param("itemCode") String itemCode, @Param("warehouseId") String warehouseId);

    @Query("SELECT SUM(s.totalQuantity) FROM StockAvailability s WHERE s.itemCode = :itemCode AND s.warehouseId = :warehouseId AND s.zoneId = :zoneId")
    Integer getTotalStockForItemInZone(@Param("itemCode") String itemCode, @Param("warehouseId") String warehouseId, @Param("zoneId") String zoneId);

    @Query("SELECT SUM(s.totalQuantity) FROM StockAvailability s WHERE s.itemCode = :itemCode AND s.warehouseId = :warehouseId AND s.zoneId = :zoneId AND s.aisleId = :aisleId")
    Integer getTotalStockForItemInAisle(@Param("itemCode") String itemCode, @Param("warehouseId") String warehouseId, @Param("zoneId") String zoneId, @Param("aisleId") String aisleId);

    @Query("SELECT SUM(s.totalQuantity) FROM StockAvailability s WHERE s.itemCode = :itemCode AND s.warehouseId = :warehouseId AND s.zoneId = :zoneId AND s.aisleId = :aisleId AND s.rackId = :rackId")
    Integer getTotalStockForItemInRack(@Param("itemCode") String itemCode, @Param("warehouseId") String warehouseId, @Param("zoneId") String zoneId, @Param("aisleId") String aisleId, @Param("rackId") String rackId);

    @Query("SELECT SUM(s.totalQuantity) FROM StockAvailability s WHERE s.itemCode = :itemCode AND s.warehouseId = :warehouseId AND s.zoneId = :zoneId AND s.aisleId = :aisleId AND s.rackId = :rackId AND s.levelId = :levelId")
    Integer getTotalStockForItemInLevel(@Param("itemCode") String itemCode, @Param("warehouseId") String warehouseId, @Param("zoneId") String zoneId, @Param("aisleId") String aisleId, @Param("rackId") String rackId, @Param("levelId") String levelId);

    @Query("SELECT s.availableQuantity FROM StockAvailability s WHERE s.binId = :binId AND s.itemCode = :itemCode")
    Optional<Integer> getAvailableQuantityInBin(@Param("binId") String binId, @Param("itemCode") String itemCode);

    // ====== Dashboard Queries ======

    @Query("SELECT s.warehouseId, SUM(s.totalQuantity) as total, SUM(s.availableQuantity) as available " +
           "FROM StockAvailability s WHERE s.locationLevel = :locationLevel GROUP BY s.warehouseId")
    List<Object[]> getWarehouseStockSummary(@Param("locationLevel") LocationLevel locationLevel);

    @Query("SELECT s.zoneId, SUM(s.totalQuantity) as total, SUM(s.availableQuantity) as available " +
           "FROM StockAvailability s WHERE s.warehouseId = :warehouseId AND s.locationLevel = :locationLevel GROUP BY s.zoneId")
    List<Object[]> getZoneStockSummary(@Param("warehouseId") String warehouseId, @Param("locationLevel") LocationLevel locationLevel);

    @Query("SELECT s.aisleId, SUM(s.totalQuantity) as total, SUM(s.availableQuantity) as available " +
           "FROM StockAvailability s WHERE s.warehouseId = :warehouseId AND s.zoneId = :zoneId AND s.locationLevel = :locationLevel GROUP BY s.aisleId")
    List<Object[]> getAisleStockSummary(@Param("warehouseId") String warehouseId, @Param("zoneId") String zoneId, @Param("locationLevel") LocationLevel locationLevel);

    @Query("SELECT s.rackId, SUM(s.totalQuantity) as total, SUM(s.availableQuantity) as available " +
           "FROM StockAvailability s WHERE s.warehouseId = :warehouseId AND s.zoneId = :zoneId AND s.aisleId = :aisleId AND s.locationLevel = :locationLevel GROUP BY s.rackId")
    List<Object[]> getRackStockSummary(@Param("warehouseId") String warehouseId, @Param("zoneId") String zoneId, @Param("aisleId") String aisleId, @Param("locationLevel") LocationLevel locationLevel);

    @Query("SELECT s.levelId, SUM(s.totalQuantity) as total, SUM(s.availableQuantity) as available " +
           "FROM StockAvailability s WHERE s.warehouseId = :warehouseId AND s.zoneId = :zoneId AND s.aisleId = :aisleId AND s.rackId = :rackId AND s.locationLevel = :locationLevel GROUP BY s.levelId")
    List<Object[]> getLevelStockSummary(@Param("warehouseId") String warehouseId, @Param("zoneId") String zoneId, @Param("aisleId") String aisleId, @Param("rackId") String rackId, @Param("locationLevel") LocationLevel locationLevel);

    // ====== Utility Queries ======

    @Modifying
    @Query("DELETE FROM StockAvailability s WHERE s.warehouseId = :warehouseId AND s.locationLevel = :locationLevel")
    void deleteByWarehouseAndLevel(@Param("warehouseId") String warehouseId, @Param("locationLevel") LocationLevel locationLevel);

    @Query("SELECT s FROM StockAvailability s WHERE s.warehouseId = :warehouseId AND s.availableQuantity > 0")
    List<StockAvailability> findAvailableStockInWarehouse(@Param("warehouseId") String warehouseId);

    @Query("SELECT s FROM StockAvailability s WHERE s.warehouseId = :warehouseId AND s.zoneId = :zoneId AND s.availableQuantity > 0")
    List<StockAvailability> findAvailableStockInZone(@Param("warehouseId") String warehouseId, @Param("zoneId") String zoneId);

    @Query("SELECT s FROM StockAvailability s WHERE s.warehouseId = :warehouseId AND s.zoneId = :zoneId AND s.aisleId = :aisleId AND s.availableQuantity > 0")
    List<StockAvailability> findAvailableStockInAisle(@Param("warehouseId") String warehouseId, @Param("zoneId") String zoneId, @Param("aisleId") String aisleId);

    @Query("SELECT s FROM StockAvailability s WHERE s.warehouseId = :warehouseId AND s.zoneId = :zoneId AND s.aisleId = :aisleId AND s.rackId = :rackId AND s.availableQuantity > 0")
    List<StockAvailability> findAvailableStockInRack(@Param("warehouseId") String warehouseId, @Param("zoneId") String zoneId, @Param("aisleId") String aisleId, @Param("rackId") String rackId);

    @Query("SELECT s FROM StockAvailability s WHERE s.warehouseId = :warehouseId AND s.zoneId = :zoneId AND s.aisleId = :aisleId AND s.rackId = :rackId AND s.levelId = :levelId AND s.availableQuantity > 0")
    List<StockAvailability> findAvailableStockInLevel(@Param("warehouseId") String warehouseId, @Param("zoneId") String zoneId, @Param("aisleId") String aisleId, @Param("rackId") String rackId, @Param("levelId") String levelId);

    @Query("SELECT s FROM StockAvailability s WHERE s.warehouseId = :warehouseId AND s.zoneId = :zoneId AND s.aisleId = :aisleId AND s.rackId = :rackId AND s.levelId = :levelId AND s.binId = :binId AND s.availableQuantity > 0")
    List<StockAvailability> findAvailableStockInBin(@Param("warehouseId") String warehouseId, @Param("zoneId") String zoneId, @Param("aisleId") String aisleId, @Param("rackId") String rackId, @Param("levelId") String levelId, @Param("binId") String binId);
}