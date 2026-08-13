package com.warehouse.wms.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.warehouse.wms.entity.Inbound;
import com.warehouse.wms.entity.InboundStage;
import com.warehouse.wms.entity.InboundStatus;

@Repository
public interface InboundRepository extends JpaRepository<Inbound, Long> {
    
    Optional<Inbound> findByInboundNumber(String inboundNumber);

    boolean existsByPurchaseOrderId(Long purchaseOrderId);
    
    List<Inbound> findByStatus(InboundStatus status);
    
    Optional<Inbound> findByGrnNumber(String grnNumber);

    
    
    Page<Inbound> findByStatus(InboundStatus status, Pageable pageable);
    
    Long countByInboundNumberStartingWith(String prefix);
    
    @Query("SELECT i FROM Inbound i WHERE " +
            // ============================================
            // STATUS & STAGE FILTERS
            // ============================================
            "(:status IS NULL OR i.status = :status) AND " +
            "(:stage IS NULL OR i.stage = :stage) AND " +
            "(:approvalStatus IS NULL OR i.approvalStatus = :approvalStatus) AND " +
            
            // ============================================
            // TEXT FILTERS
            // ============================================
            "(:poNumber IS NULL OR i.poNumber LIKE CONCAT('%', :poNumber, '%')) AND " +
            "(:supplierName IS NULL OR i.supplierName LIKE CONCAT('%', :supplierName, '%')) AND " +
            "(:qualityStatus IS NULL OR i.qualityStatus LIKE CONCAT('%', :qualityStatus, '%')) AND " +
            "(:grnStatus IS NULL OR i.grnStatus LIKE CONCAT('%', :grnStatus, '%')) AND " +
            "(:searchTerm IS NULL OR " +
            "i.inboundNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
            "i.poNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
            "i.supplierName LIKE CONCAT('%', :searchTerm, '%') OR " +
            "i.invoiceNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
            "i.deliveryChallan LIKE CONCAT('%', :searchTerm, '%') OR " +
            "i.trackingNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
            "i.grnNumber LIKE CONCAT('%', :searchTerm, '%')) AND " +
            
            // ============================================
            // DATE FILTERS - Inbound Date
            // ============================================
            "(:inboundDateFrom IS NULL OR i.inboundDate >= :inboundDateFrom) AND " +
            "(:inboundDateTo IS NULL OR i.inboundDate <= :inboundDateTo) AND " +
            
            // Expected Arrival Date
            "(:expectedArrivalDateFrom IS NULL OR i.expectedArrivalDate >= :expectedArrivalDateFrom) AND " +
            "(:expectedArrivalDateTo IS NULL OR i.expectedArrivalDate <= :expectedArrivalDateTo) AND " +
            
            // Gate Entry Date Time
            "(:gateEntryDateTimeFrom IS NULL OR DATE(i.gateEntryDateTime) >= :gateEntryDateTimeFrom) AND " +
            "(:gateEntryDateTimeTo IS NULL OR DATE(i.gateEntryDateTime) <= :gateEntryDateTimeTo) AND " +
            
            // Unloading Start Time
            "(:unloadingStartTimeFrom IS NULL OR DATE(i.unloadingStartTime) >= :unloadingStartTimeFrom) AND " +
            "(:unloadingStartTimeTo IS NULL OR DATE(i.unloadingStartTime) <= :unloadingStartTimeTo) AND " +
            
            // Received Date
            "(:receivedDateFrom IS NULL OR DATE(i.receivedDate) >= :receivedDateFrom) AND " +
            "(:receivedDateTo IS NULL OR DATE(i.receivedDate) <= :receivedDateTo) AND " +
            
            // Inspection Date
            "(:inspectionDateFrom IS NULL OR DATE(i.inspectionDate) >= :inspectionDateFrom) AND " +
            "(:inspectionDateTo IS NULL OR DATE(i.inspectionDate) <= :inspectionDateTo) AND " +
            
            // GRN Date
            "(:grnDateFrom IS NULL OR DATE(i.grnDate) >= :grnDateFrom) AND " +
            "(:grnDateTo IS NULL OR DATE(i.grnDate) <= :grnDateTo) AND " +
            
            // Approval Date
            "(:approvalDateFrom IS NULL OR DATE(i.approvalDate) >= :approvalDateFrom) AND " +
            "(:approvalDateTo IS NULL OR DATE(i.approvalDate) <= :approvalDateTo) AND " +
            
            // ============================================
            // QUANTITY FILTERS
            // ============================================
            "(:minBoxesUnloaded IS NULL OR i.boxesUnloadedQuantity >= :minBoxesUnloaded) AND " +
            "(:maxBoxesUnloaded IS NULL OR i.boxesUnloadedQuantity <= :maxBoxesUnloaded) AND " +
            "(:minBoxesInTruck IS NULL OR i.boxesInTruckQuantity >= :minBoxesInTruck) AND " +
            "(:maxBoxesInTruck IS NULL OR i.boxesInTruckQuantity <= :maxBoxesInTruck)")
     Page<Inbound> filterInbounds(
             // Status & Stage
             @Param("status") InboundStatus status,
             @Param("stage") InboundStage stage,
             @Param("approvalStatus") String approvalStatus,
             
             // Text Filters
             @Param("poNumber") String poNumber,
             @Param("supplierName") String supplierName,
             @Param("qualityStatus") String qualityStatus,
             @Param("grnStatus") String grnStatus,
             @Param("searchTerm") String searchTerm,
             
             // Date Filters - Inbound Date
             @Param("inboundDateFrom") LocalDate inboundDateFrom,
             @Param("inboundDateTo") LocalDate inboundDateTo,
             
             // Expected Arrival Date
             @Param("expectedArrivalDateFrom") LocalDate expectedArrivalDateFrom,
             @Param("expectedArrivalDateTo") LocalDate expectedArrivalDateTo,
             
             // Gate Entry Date Time
             @Param("gateEntryDateTimeFrom") LocalDate gateEntryDateTimeFrom,
             @Param("gateEntryDateTimeTo") LocalDate gateEntryDateTimeTo,
             
             // Unloading Start Time
             @Param("unloadingStartTimeFrom") LocalDate unloadingStartTimeFrom,
             @Param("unloadingStartTimeTo") LocalDate unloadingStartTimeTo,
             
             // Received Date
             @Param("receivedDateFrom") LocalDate receivedDateFrom,
             @Param("receivedDateTo") LocalDate receivedDateTo,
             
             // Inspection Date
             @Param("inspectionDateFrom") LocalDate inspectionDateFrom,
             @Param("inspectionDateTo") LocalDate inspectionDateTo,
             
             // GRN Date
             @Param("grnDateFrom") LocalDate grnDateFrom,
             @Param("grnDateTo") LocalDate grnDateTo,
             
             // Approval Date
             @Param("approvalDateFrom") LocalDate approvalDateFrom,
             @Param("approvalDateTo") LocalDate approvalDateTo,
             
             // Quantity Filters
             @Param("minBoxesUnloaded") Integer minBoxesUnloaded,
             @Param("maxBoxesUnloaded") Integer maxBoxesUnloaded,
             @Param("minBoxesInTruck") Integer minBoxesInTruck,
             @Param("maxBoxesInTruck") Integer maxBoxesInTruck,
             
             Pageable pageable
     );

 
 
 
 
 


 /**
  * Find all inbounds with GRN status APPROVED and search by multiple fields
  */
 @Query("SELECT i FROM Inbound i WHERE i.grnStatus = 'APPROVED' AND " +
        "(LOWER(i.inboundNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.poNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.supplierName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.grnNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
 Page<Inbound> findByGrnStatusAndSearch(@Param("search") String search, Pageable pageable);
 
 
 
 
 



 // ====== With barcodeGenerate Filter ======
 
 @Query("SELECT i FROM Inbound i WHERE i.grnStatus = :grnStatus AND " +
        "(:search IS NULL OR LOWER(i.inboundNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.poNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.supplierName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.grnNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
        "EXISTS (SELECT il FROM InboundLine il WHERE il.inbound = i AND il.barcodeGenerate = :barcodeGenerate)")
 Page<Inbound> findByGrnStatusAndSearchWithBarcodeGenerate(
         @Param("grnStatus") String grnStatus,
         @Param("search") String search,
         @Param("barcodeGenerate") Boolean barcodeGenerate,
         Pageable pageable);

 // ====== With taskAssigned Filter ======
 
 @Query("SELECT i FROM Inbound i WHERE i.grnStatus = :grnStatus AND " +
        "(:search IS NULL OR LOWER(i.inboundNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.poNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.supplierName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.grnNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
        "EXISTS (SELECT il FROM InboundLine il WHERE il.inbound = i AND il.taskAssinged = :taskAssigned)")
 Page<Inbound> findByGrnStatusAndSearchWithTaskAssigned(
         @Param("grnStatus") String grnStatus,
         @Param("search") String search,
         @Param("taskAssigned") Boolean taskAssigned,
         Pageable pageable);

 // ====== With Both Filters ======
 
 @Query("SELECT i FROM Inbound i WHERE i.grnStatus = :grnStatus AND " +
        "(:search IS NULL OR LOWER(i.inboundNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.poNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.supplierName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.grnNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
        "EXISTS (SELECT il FROM InboundLine il WHERE il.inbound = i AND il.barcodeGenerate = :barcodeGenerate) AND " +
        "EXISTS (SELECT il FROM InboundLine il WHERE il.inbound = i AND il.taskAssinged = :taskAssigned)")
 Page<Inbound> findByGrnStatusAndSearchWithBothFlags(
         @Param("grnStatus") String grnStatus,
         @Param("search") String search,
         @Param("barcodeGenerate") Boolean barcodeGenerate,
         @Param("taskAssigned") Boolean taskAssigned,
         Pageable pageable);
 
 
 
 
 
 
 
 
 
 
 
 Page<Inbound> findByGrnStatus(String grnStatus, Pageable pageable);

 @Query("SELECT i FROM Inbound i WHERE i.grnStatus = :grnStatus AND " +
        "(:search IS NULL OR LOWER(i.inboundNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.poNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.supplierName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.grnNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
 Page<Inbound> findByGrnStatusAndSearch(@Param("grnStatus") String grnStatus,
                                         @Param("search") String search,
                                         Pageable pageable);

 // ====== ALL lines have barcodeGenerate = true ======
 
 @Query("SELECT i FROM Inbound i WHERE i.grnStatus = :grnStatus AND " +
	       "(:search IS NULL OR LOWER(i.inboundNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
	       "LOWER(i.poNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
	       "LOWER(i.supplierName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
	       "LOWER(i.grnNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
	       "LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
	       "NOT EXISTS (SELECT il FROM InboundLine il WHERE il.inbound = i " +
	       "AND il.qualityStatus != 'REJECTED' " +  // Ignore REJECTED lines
	       "AND (il.barcodeGenerate IS NULL OR il.barcodeGenerate = false))")
	Page<Inbound> findByGrnStatusAndAllLinesBarcodeGenerated(
	         @Param("grnStatus") String grnStatus,
	         @Param("search") String search,
	         Pageable pageable);

 // ====== ALL lines have taskAssigned = true ======
 
 @Query("SELECT i FROM Inbound i WHERE i.grnStatus = :grnStatus AND " +
        "(:search IS NULL OR LOWER(i.inboundNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.poNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.supplierName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.grnNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
        "NOT EXISTS (SELECT il FROM InboundLine il WHERE il.inbound = i AND (il.taskAssinged IS NULL OR il.taskAssinged = false))")
 Page<Inbound> findByGrnStatusAndAllLinesTaskAssigned(
         @Param("grnStatus") String grnStatus,
         @Param("search") String search,
         Pageable pageable);

 // ====== ALL lines have both barcodeGenerate = true AND taskAssigned = true ======
 
 @Query("SELECT i FROM Inbound i WHERE i.grnStatus = :grnStatus AND " +
        "(:search IS NULL OR LOWER(i.inboundNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.poNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.supplierName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.grnNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
        "NOT EXISTS (SELECT il FROM InboundLine il WHERE il.inbound = i AND (il.barcodeGenerate IS NULL OR il.barcodeGenerate = false)) AND " +
        "NOT EXISTS (SELECT il FROM InboundLine il WHERE il.inbound = i AND (il.taskAssinged IS NULL OR il.taskAssinged = false))")
 Page<Inbound> findByGrnStatusAndAllLinesBothFlags(
         @Param("grnStatus") String grnStatus,
         @Param("search") String search,
         Pageable pageable);
 
 
 
 
 
 
 
 
 
 
 
 
 
 
}