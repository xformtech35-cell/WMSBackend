package com.warehouse.wms.repository;

import com.warehouse.wms.entity.PurchaseOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderLineRepository extends JpaRepository<PurchaseOrderLine, Long> {
    
    // ============ FIXED: Change Long to String for skuId ============
    
    // ============ ADDITIONAL USEFUL METHODS ============
    
    // Find all lines by purchase order ID
    List<PurchaseOrderLine> findByPurchaseOrderId(Long purchaseOrderId);
    
    // Find by purchase order ID and item code
    Optional<PurchaseOrderLine> findByPurchaseOrderIdAndItemCode(Long purchaseOrderId, String itemCode);
    
    // Find by purchase order ID and item ID
    Optional<PurchaseOrderLine> findByPurchaseOrderIdAndItemId(Long purchaseOrderId, Long itemId);
    
    // Find by line status
    List<PurchaseOrderLine> findByLineStatus(String lineStatus);
    
    // Find by line status and purchase order ID
    List<PurchaseOrderLine> findByLineStatusAndPurchaseOrderId(String lineStatus, Long purchaseOrderId);
    
    // ============ QUERY METHODS ============
    
    // Find lines with pending quantity > 0
    @Query("SELECT pol FROM PurchaseOrderLine pol WHERE pol.pendingQuantity > 0")
    List<PurchaseOrderLine> findLinesWithPendingQuantity();
    
    // Find pending lines for a specific purchase order
    @Query("SELECT pol FROM PurchaseOrderLine pol WHERE pol.purchaseOrder.id = :poId AND pol.pendingQuantity > 0")
    List<PurchaseOrderLine> findPendingLinesByPurchaseOrderId(@Param("poId") Long poId);
    
    // Find lines by purchase order ID and status
    @Query("SELECT pol FROM PurchaseOrderLine pol WHERE pol.purchaseOrder.id = :poId AND pol.lineStatus = :status")
    List<PurchaseOrderLine> findByPurchaseOrderIdAndStatus(@Param("poId") Long poId, @Param("status") String status);
    
    // ============ COUNT METHODS ============
    
    // Count lines by purchase order ID
    Long countByPurchaseOrderId(Long purchaseOrderId);
    
    // Count lines by purchase order ID and status
    Long countByPurchaseOrderIdAndLineStatus(Long purchaseOrderId, String lineStatus);
    
    // ============ SUM METHODS ============
    
    // Sum total price by purchase order ID
    @Query("SELECT COALESCE(SUM(pol.totalPrice), 0) FROM PurchaseOrderLine pol WHERE pol.purchaseOrder.id = :poId")
    Double sumTotalPriceByPurchaseOrderId(@Param("poId") Long poId);
    
    // Sum GST amount by purchase order ID
    @Query("SELECT COALESCE(SUM(pol.gstAmount), 0) FROM PurchaseOrderLine pol WHERE pol.purchaseOrder.id = :poId")
    Double sumGstAmountByPurchaseOrderId(@Param("poId") Long poId);
    
    // Sum total with GST by purchase order ID
    @Query("SELECT COALESCE(SUM(pol.totalWithGst), 0) FROM PurchaseOrderLine pol WHERE pol.purchaseOrder.id = :poId")
    Double sumTotalWithGstByPurchaseOrderId(@Param("poId") Long poId);
}