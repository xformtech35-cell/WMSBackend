package com.warehouse.wms.repository;

import com.warehouse.wms.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory> {

    // Existing methods remain unchanged
    @Query("SELECT i FROM Inventory i WHERE i.sku.id = :skuId AND i.state = 'AVAILABLE' " +
           "AND NOT EXISTS (SELECT b FROM StockBatch b WHERE b.sku.id = :skuId AND b.batchNumber = i.batchNo AND b.bin.id = i.bin.id AND b.status = 'QUARANTINED') " +
           "ORDER BY i.createdAt ASC")
    List<Inventory> findAvailableBySkuFifo(@Param("skuId") Long skuId);

    List<Inventory> findByGoodsReceiptLineGoodsReceiptIdAndState(Long goodsReceiptId, Inventory.InventoryState state);

    java.util.Optional<Inventory> findBySerialNo(String serialNo);

    long countBySkuIdAndBatchNo(Long skuId, String batchNo);

    List<Inventory> findByStateAndGoodsReceiptLineGoodsReceiptId(Inventory.InventoryState state, Long goodsReceiptId);

    List<Inventory> findByStateAndSkuIdOrderByCreatedAtAsc(Inventory.InventoryState state, Long skuId);

    long countByState(Inventory.InventoryState state);

    // ADD NEW METHODS FOR SUMMARY OPTIMIZATION
    long count(org.springframework.data.jpa.domain.Specification<Inventory> spec);

    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM Inventory i WHERE i.state = com.warehouse.wms.entity.Inventory.InventoryState.AVAILABLE")
    long sumAvailableQuantity(org.springframework.data.jpa.domain.Specification<Inventory> spec);

    @Query("SELECT i FROM Inventory i WHERE i.sku.id = :skuId AND i.state = 'AVAILABLE' " +
           "AND NOT EXISTS (SELECT b FROM StockBatch b WHERE b.sku.id = :skuId AND b.batchNumber = i.batchNo AND b.bin.id = i.bin.id AND b.status IN ('EXPIRED', 'QUARANTINED')) " +
           "ORDER BY COALESCE(i.expiryDate, '9999-12-31') ASC, i.createdAt ASC")
    List<Inventory> findAvailableBySkuFefo(@Param("skuId") Long skuId);

    List<Inventory> findByStateAndBinBarcode(Inventory.InventoryState state, String barcode);

    List<Inventory> findByStateAndUpdatedAtBetween(Inventory.InventoryState state, java.time.LocalDateTime from, java.time.LocalDateTime to);

    @Modifying
    @Query("UPDATE Inventory i SET i.state = :toState WHERE i.id IN :ids")
    int bulkUpdateState(@Param("ids") List<Long> ids, @Param("toState") Inventory.InventoryState toState);

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.goodsReceiptLine.goodsReceipt.purchaseOrder.id = :poId AND i.sku.id = :skuId")
    long countReceivedForPurchaseOrderSku(@Param("poId") Long poId, @Param("skuId") Long skuId);
}