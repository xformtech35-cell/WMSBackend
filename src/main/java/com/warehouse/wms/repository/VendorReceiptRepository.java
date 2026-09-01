package com.warehouse.wms.repository;

import com.warehouse.wms.entity.VendorReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VendorReceiptRepository extends JpaRepository<VendorReceipt, Long> {
    
    /**
     * Find receipt by receipt number
     */
    Optional<VendorReceipt> findByReceiptNumber(String receiptNumber);
    
    /**
     * Find receipts by return order ID
     */
    List<VendorReceipt> findByReturnOrderId(Long returnOrderId);
    
    /**
     * Find receipts by status
     */
    List<VendorReceipt> findByStatus(VendorReceipt.ReceiptStatus status);
    
    /**
     * Find latest receipt for an order
     */
    @Query("SELECT r FROM VendorReceipt r WHERE r.returnOrder.id = :orderId ORDER BY r.createdAt DESC")
    List<VendorReceipt> findLatestByOrder(@Param("orderId") Long orderId);
    
    /**
     * Count receipts by number prefix for generating sequence
     */
    @Query("SELECT COUNT(r) FROM VendorReceipt r WHERE r.receiptNumber LIKE CONCAT(:prefix, '%')")
    Long countByReceiptNumberStartingWith(@Param("prefix") String prefix);
    
    /**
     * Find receipts by date range
     */
    List<VendorReceipt> findByReceiptDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find receipts by supplier
     */
    List<VendorReceipt> findBySupplierId(Long supplierId);
    
    /**
     * Find receipts with acknowledgment pending
     */
    @Query("SELECT r FROM VendorReceipt r WHERE r.acknowledgmentNumber IS NULL AND r.status != 'REJECTED'")
    List<VendorReceipt> findReceiptsWithoutAcknowledgment();
    
    /**
     * Count receipts by status
     */
    Long countByStatus(VendorReceipt.ReceiptStatus status);
    
    /**
     * Find receipts by return order with pagination
     */
    @Query("SELECT r FROM VendorReceipt r WHERE r.returnOrder.id = :orderId")
    Page<VendorReceipt> findByReturnOrderId(@Param("orderId") Long orderId, Pageable pageable);
    
    /**
     * Get receipt summary statistics
     */
    @Query("SELECT SUM(r.totalReceivedQuantity), SUM(r.totalAcceptedQuantity), " +
           "SUM(r.totalRejectedQuantity), SUM(r.totalShortQuantity), SUM(r.totalDamagedQuantity) " +
           "FROM VendorReceipt r WHERE r.returnOrder.id = :orderId")
    Object[] getReceiptSummaryByOrder(@Param("orderId") Long orderId);
    
    /**
     * Find receipts with partial acceptance
     */
    @Query("SELECT r FROM VendorReceipt r WHERE r.status = 'PARTIAL'")
    List<VendorReceipt> findPartialReceipts();
}