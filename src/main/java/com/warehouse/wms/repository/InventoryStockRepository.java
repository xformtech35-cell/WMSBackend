package com.warehouse.wms.repository;

import com.warehouse.wms.entity.InventoryStock;
import com.warehouse.wms.constant.InventoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryStockRepository extends JpaRepository<InventoryStock, Long>, JpaSpecificationExecutor<InventoryStock> {
    
    Optional<InventoryStock> findByInventoryNumber(String inventoryNumber);
    
    Optional<InventoryStock> findByBinIdAndItemCode(String binId, String itemCode);
    
    Optional<InventoryStock> findByItemCodeAndBinId(String itemCode, String binId);
    
    Optional<InventoryStock> findByItemCodeAndBinIdAndBatchNumber(String itemCode, String binId, String batchNumber);
    
    List<InventoryStock> findByItemCode(String itemCode);
    
    List<InventoryStock> findByBinId(String binId);
    
    List<InventoryStock> findByWarehouseId(String warehouseId);
    
    List<InventoryStock> findByGrnNumber(String grnNumber);
    
    List<InventoryStock> findByPutawayTaskNumber(String putawayTaskNumber);
    
    List<InventoryStock> findByConfirmationNumber(String confirmationNumber);
    
    @Query("SELECT i FROM InventoryStock i WHERE i.itemCode = :itemCode AND i.binId = :binId AND i.status = :status")
    Optional<InventoryStock> findStockByItemAndBinAndStatus(@Param("itemCode") String itemCode, 
                                                             @Param("binId") String binId, 
                                                             @Param("status") InventoryStatus status);
    
    @Query("SELECT i FROM InventoryStock i WHERE i.warehouseId = :warehouseId AND i.binId = :binId")
    List<InventoryStock> findByWarehouseAndBin(@Param("warehouseId") String warehouseId, 
                                                @Param("binId") String binId);
    
    @Query("SELECT i FROM InventoryStock i WHERE i.availableQuantity > 0 AND i.status = 'ACTIVE'")
    List<InventoryStock> findAvailableStock();
    
    @Query("SELECT SUM(i.quantity) FROM InventoryStock i WHERE i.itemCode = :itemCode")
    Integer getTotalQuantityByItemCode(@Param("itemCode") String itemCode);
    
    @Query("SELECT SUM(i.availableQuantity) FROM InventoryStock i WHERE i.itemCode = :itemCode")
    Integer getTotalAvailableQuantityByItemCode(@Param("itemCode") String itemCode);
    
    // REMOVE this method if minQuantity doesn't exist
    // @Query("SELECT i FROM InventoryStock i WHERE i.availableQuantity < i.minQuantity")
    // List<InventoryStock> findLowStockItems();
    
//    @Query("SELECT i FROM InventoryStock i WHERE i.availableQuantity < i.minQuantity")
//    List<InventoryStock> findLowStockItems();
    
    @Query("SELECT i FROM InventoryStock i WHERE i.availableQuantity < 10 AND i.status = 'ACTIVE'")
    List<InventoryStock> findLowStockItems();
    
    @Modifying
    @Transactional
    @Query("UPDATE InventoryStock i SET i.status = :status WHERE i.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") InventoryStatus status);
    
    @Modifying
    @Transactional
    @Query("UPDATE InventoryStock i SET i.isFrozen = :frozen WHERE i.id = :id")
    int updateFrozenStatus(@Param("id") Long id, @Param("frozen") Boolean frozen);
}