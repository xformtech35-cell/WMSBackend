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
    
    
    
    @Query("""
    	    SELECT DISTINCT r FROM VendorReceipt r
    	    LEFT JOIN r.returnOrder o
    	    LEFT JOIN r.dispatch d
    	    LEFT JOIN r.lines l
    	    WHERE (:receiptNumber IS NULL OR LOWER(r.receiptNumber) LIKE LOWER(CONCAT('%', :receiptNumber, '%')))
    	      AND (:acknowledgmentNumber IS NULL OR LOWER(r.acknowledgmentNumber) LIKE LOWER(CONCAT('%', :acknowledgmentNumber, '%')))
    	      AND (:returnOrderId IS NULL OR o.id = :returnOrderId)
    	      AND (:dispatchId IS NULL OR d.id = :dispatchId)
    	      AND (:vroNumber IS NULL OR LOWER(o.vroNumber) LIKE LOWER(CONCAT('%', :vroNumber, '%')))
    	      AND (:dispatchNumber IS NULL OR LOWER(d.dispatchNumber) LIKE LOWER(CONCAT('%', :dispatchNumber, '%')))
    	      AND (:supplierName IS NULL OR LOWER(r.supplierName) LIKE LOWER(CONCAT('%', :supplierName, '%')))
    	      AND (:supplierCode IS NULL OR LOWER(o.supplierCode) LIKE LOWER(CONCAT('%', :supplierCode, '%')))
    	      AND (:status IS NULL OR r.status = :status)
    	      AND (:receivedBy IS NULL OR LOWER(r.receivedBy) LIKE LOWER(CONCAT('%', :receivedBy, '%')))
    	      AND (:receiptFromDate IS NULL OR r.receiptDate >= :receiptFromDate)
    	      AND (:receiptToDate IS NULL OR r.receiptDate <= :receiptToDate)
    	      AND (:ackFromDate IS NULL OR r.acknowledgmentDate >= :ackFromDate)
    	      AND (:ackToDate IS NULL OR r.acknowledgmentDate <= :ackToDate)
    	      AND (:minReceivedQuantity IS NULL OR r.totalReceivedQuantity >= :minReceivedQuantity)
    	      AND (:maxReceivedQuantity IS NULL OR r.totalReceivedQuantity <= :maxReceivedQuantity)
    	      AND (:minAcceptedQuantity IS NULL OR r.totalAcceptedQuantity >= :minAcceptedQuantity)
    	      AND (:maxAcceptedQuantity IS NULL OR r.totalAcceptedQuantity <= :maxAcceptedQuantity)
    	      AND (:minRejectedQuantity IS NULL OR r.totalRejectedQuantity >= :minRejectedQuantity)
    	      AND (:maxRejectedQuantity IS NULL OR r.totalRejectedQuantity <= :maxRejectedQuantity)
    	      AND (:minShortQuantity IS NULL OR r.totalShortQuantity >= :minShortQuantity)
    	      AND (:maxShortQuantity IS NULL OR r.totalShortQuantity <= :maxShortQuantity)
    	      AND (:minDamagedQuantity IS NULL OR r.totalDamagedQuantity >= :minDamagedQuantity)
    	      AND (:maxDamagedQuantity IS NULL OR r.totalDamagedQuantity <= :maxDamagedQuantity)
    	      AND (:hasAcknowledgment IS NULL OR
    	           (:hasAcknowledgment = TRUE AND r.acknowledgmentNumber IS NOT NULL) OR
    	           (:hasAcknowledgment = FALSE AND r.acknowledgmentNumber IS NULL))
    	      AND (:hasDocument IS NULL OR
    	           (:hasDocument = TRUE AND r.receiptDocumentPath IS NOT NULL) OR
    	           (:hasDocument = FALSE AND r.receiptDocumentPath IS NULL))
    	      AND (:itemCode IS NULL OR EXISTS (
    	            SELECT 1 FROM VendorReceiptLine rl
    	            WHERE rl.receipt = r AND LOWER(rl.itemCode) LIKE LOWER(CONCAT('%', :itemCode, '%'))
    	      ))
    	      AND (:itemName IS NULL OR EXISTS (
    	            SELECT 1 FROM VendorReceiptLine rn
    	            WHERE rn.receipt = r AND LOWER(rn.itemName) LIKE LOWER(CONCAT('%', :itemName, '%'))
    	      ))
    	      AND (:searchTerm IS NULL OR (
    	            LOWER(r.receiptNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
    	         OR LOWER(r.acknowledgmentNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
    	         OR LOWER(r.supplierName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
    	         OR LOWER(o.vroNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
    	         OR LOWER(d.dispatchNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
    	         OR EXISTS (
    	              SELECT 1 FROM VendorReceiptLine sl
    	              WHERE sl.receipt = r
    	                AND (LOWER(sl.itemCode) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
    	                  OR LOWER(sl.itemName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
    	         )
    	      ))
    	    """)
    	Page<VendorReceipt> findAllWithFilters(
    	        @Param("receiptNumber") String receiptNumber,
    	        @Param("acknowledgmentNumber") String acknowledgmentNumber,
    	        @Param("returnOrderId") Long returnOrderId,
    	        @Param("dispatchId") Long dispatchId,
    	        @Param("vroNumber") String vroNumber,
    	        @Param("dispatchNumber") String dispatchNumber,
    	        @Param("supplierName") String supplierName,
    	        @Param("supplierCode") String supplierCode,
    	        @Param("status") VendorReceipt.ReceiptStatus status,
    	        @Param("receivedBy") String receivedBy,
    	        @Param("receiptFromDate") LocalDate receiptFromDate,
    	        @Param("receiptToDate") LocalDate receiptToDate,
    	        @Param("ackFromDate") LocalDate ackFromDate,
    	        @Param("ackToDate") LocalDate ackToDate,
    	        @Param("minReceivedQuantity") Integer minReceivedQuantity,
    	        @Param("maxReceivedQuantity") Integer maxReceivedQuantity,
    	        @Param("minAcceptedQuantity") Integer minAcceptedQuantity,
    	        @Param("maxAcceptedQuantity") Integer maxAcceptedQuantity,
    	        @Param("minRejectedQuantity") Integer minRejectedQuantity,
    	        @Param("maxRejectedQuantity") Integer maxRejectedQuantity,
    	        @Param("minShortQuantity") Integer minShortQuantity,
    	        @Param("maxShortQuantity") Integer maxShortQuantity,
    	        @Param("minDamagedQuantity") Integer minDamagedQuantity,
    	        @Param("maxDamagedQuantity") Integer maxDamagedQuantity,
    	        @Param("hasAcknowledgment") Boolean hasAcknowledgment,
    	        @Param("hasDocument") Boolean hasDocument,
    	        @Param("itemCode") String itemCode,
    	        @Param("itemName") String itemName,
    	        @Param("searchTerm") String searchTerm,
    	        Pageable pageable
    	);
}