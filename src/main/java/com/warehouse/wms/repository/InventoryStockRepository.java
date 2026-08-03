// ====== FILE: src/main/java/com/warehouse/wms/repository/InventoryStockRepository.java ======
package com.warehouse.wms.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.warehouse.wms.constant.InventoryStatus;
import com.warehouse.wms.entity.InventoryStock;

@Repository
public interface InventoryStockRepository extends JpaRepository<InventoryStock, Long> {

    Optional<InventoryStock> findByInventoryNumber(String inventoryNumber);

    Optional<InventoryStock> findByBinIdAndItemCode(String binId, String itemCode);

    List<InventoryStock> findByWarehouseId(String warehouseId);

    List<InventoryStock> findByItemCode(String itemCode);

    List<InventoryStock> findByBinId(String binId);

    List<InventoryStock> findByItemCodeAndBinId(String itemCode, String binId);

    List<InventoryStock> findByStatus(InventoryStatus status);

    @Query("SELECT i FROM InventoryStock i WHERE i.availableQuantity > 0 AND i.itemCode = :itemCode")
    List<InventoryStock> findAvailableStockByItem(@Param("itemCode") String itemCode);

    @Query("SELECT SUM(i.availableQuantity) FROM InventoryStock i WHERE i.itemCode = :itemCode")
    Integer getTotalAvailableQuantityByItem(@Param("itemCode") String itemCode);

    @Query("SELECT i FROM InventoryStock i WHERE i.itemCode = :itemCode AND i.availableQuantity > 0 " +
           "ORDER BY i.mfgDate ASC")
    List<InventoryStock> findFIFOStock(@Param("itemCode") String itemCode);

    @Query("SELECT i FROM InventoryStock i WHERE i.itemCode = :itemCode AND i.availableQuantity > 0 " +
           "ORDER BY i.expiryDate ASC")
    List<InventoryStock> findFEFOStock(@Param("itemCode") String itemCode);

    @Query("SELECT i FROM InventoryStock i WHERE i.itemCode = :itemCode AND i.availableQuantity > 0 " +
           "ORDER BY i.updatedAt DESC")
    List<InventoryStock> findLIFOStock(@Param("itemCode") String itemCode);

    @Query("SELECT i FROM InventoryStock i WHERE i.expiryDate < CURRENT_TIMESTAMP AND i.availableQuantity > 0")
    List<InventoryStock> findExpiredStock();

    @Query("SELECT i FROM InventoryStock i WHERE i.availableQuantity < :threshold")
    List<InventoryStock> findLowStock(@Param("threshold") Integer threshold);

    @Modifying
    @Transactional
    @Query("UPDATE InventoryStock i SET i.availableQuantity = i.availableQuantity + :quantity, " +
           "i.lastUpdatedDate = :updatedDate WHERE i.id = :id")
    int addStockQuantity(@Param("id") Long id, @Param("quantity") Integer quantity, 
                         @Param("updatedDate") LocalDateTime updatedDate);

    @Modifying
    @Transactional
    @Query("UPDATE InventoryStock i SET i.availableQuantity = i.availableQuantity - :quantity, " +
           "i.lastUpdatedDate = :updatedDate WHERE i.id = :id AND i.availableQuantity >= :quantity")
    int deductStockQuantity(@Param("id") Long id, @Param("quantity") Integer quantity,
                            @Param("updatedDate") LocalDateTime updatedDate);

    @Modifying
    @Transactional
    @Query("UPDATE InventoryStock i SET i.reservedQuantity = i.reservedQuantity + :quantity, " +
           "i.availableQuantity = i.availableQuantity - :quantity WHERE i.id = :id " +
           "AND i.availableQuantity >= :quantity")
    int reserveStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Modifying
    @Transactional
    @Query("UPDATE InventoryStock i SET i.reservedQuantity = i.reservedQuantity - :quantity, " +
           "i.availableQuantity = i.availableQuantity + :quantity WHERE i.id = :id " +
           "AND i.reservedQuantity >= :quantity")
    int unreserveStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}