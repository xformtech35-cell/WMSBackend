package com.warehouse.wms.repository;

import com.warehouse.wms.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory> {
    
    Optional<Inventory> findBySerialNo(String serialNo);
    
    List<Inventory> findBySkuId(Long skuId);
    
    List<Inventory> findByBinId(Long binId);
    
    List<Inventory> findByItemCode(String itemCode);
    
    List<Inventory> findByItemCodeAndState(String itemCode, Inventory.InventoryState state);
    
    @Query("SELECT i FROM Inventory i WHERE i.state = :state")
    List<Inventory> findByState(@Param("state") Inventory.InventoryState state);
    
    @Query("SELECT i FROM Inventory i WHERE i.goodsReceiptLine.id = :lineId")
    List<Inventory> findByGoodsReceiptLineId(@Param("lineId") Long lineId);
    
    @Query("SELECT i FROM Inventory i WHERE i.itemCode = :itemCode AND i.batchNo = :batchNo")
    List<Inventory> findByItemCodeAndBatchNo(@Param("itemCode") String itemCode, 
                                             @Param("batchNo") String batchNo);
    
    @Query("SELECT i FROM Inventory i WHERE i.serialNo IN :serialNos")
    List<Inventory> findBySerialNos(@Param("serialNos") List<String> serialNos);
    
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.itemCode = :itemCode AND i.state = 'AVAILABLE'")
    long countAvailableByItemCode(@Param("itemCode") String itemCode);
    
    @Query("SELECT i FROM Inventory i WHERE i.expiryDate < CURRENT_DATE AND i.state = 'AVAILABLE'")
    List<Inventory> findExpiredInventory();
}