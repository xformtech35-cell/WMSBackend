package com.warehouse.wms.repository;

import com.warehouse.wms.entity.Rfq;
import com.warehouse.wms.entity.RfqStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RfqRepository extends JpaRepository<Rfq, Long> {
    
    // Basic find methods
    List<Rfq> findByStatus(RfqStatus status);
    List<Rfq> findByPurchaseRequestId(Long purchaseRequestId);
    
    // Exists and count
    boolean existsByRfqNumber(String rfqNumber);
    Long countByRfqNumberStartingWith(String prefix);
    
    // Find by RFQ number
    Rfq findByRfqNumber(String rfqNumber);
    
    // Find by date range
    List<Rfq> findByRfqDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Find by status and date
    List<Rfq> findByStatusAndRfqDateBefore(RfqStatus status, LocalDate date);
    
    // Advanced filter with pagination
    @Query("SELECT DISTINCT r FROM Rfq r " +
           "LEFT JOIN r.items i " +
           "LEFT JOIN r.vendorQuotations vq " +
           "LEFT JOIN r.purchaseRequest pr " +
           "WHERE " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:statuses IS NULL OR r.status IN :statuses) AND " +
           "(:rfqNumber IS NULL OR r.rfqNumber LIKE CONCAT('%', :rfqNumber, '%')) AND " +
           "(:prNumber IS NULL OR pr.prNumber LIKE CONCAT('%', :prNumber, '%')) AND " +
           "(:rfqDateFrom IS NULL OR r.rfqDate >= :rfqDateFrom) AND " +
           "(:rfqDateTo IS NULL OR r.rfqDate <= :rfqDateTo) AND " +
           "(:closingDateFrom IS NULL OR r.closingDate >= :closingDateFrom) AND " +
           "(:closingDateTo IS NULL OR r.closingDate <= :closingDateTo) AND " +
           "(:itemCode IS NULL OR i.itemCode LIKE CONCAT('%', :itemCode, '%')) AND " +
           "(:itemName IS NULL OR i.itemName LIKE CONCAT('%', :itemName, '%')) AND " +
           "(:supplierId IS NULL OR vq.supplier.id = :supplierId) AND " +
           "(:hasQuotations IS NULL OR " +
           "(CASE WHEN :hasQuotations = true THEN r.vendorQuotations IS NOT EMPTY ELSE r.vendorQuotations IS EMPTY END)) AND " +
           "(:searchTerm IS NULL OR " +
           "r.rfqNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
           "r.referenceNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
           "pr.prNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
           "i.itemCode LIKE CONCAT('%', :searchTerm, '%') OR " +
           "i.itemName LIKE CONCAT('%', :searchTerm, '%'))")
    Page<Rfq> filterRfqs(
            @Param("status") RfqStatus status,
            @Param("statuses") List<RfqStatus> statuses,
            @Param("rfqNumber") String rfqNumber,
            @Param("prNumber") String prNumber,
            @Param("rfqDateFrom") LocalDate rfqDateFrom,
            @Param("rfqDateTo") LocalDate rfqDateTo,
            @Param("closingDateFrom") LocalDate closingDateFrom,
            @Param("closingDateTo") LocalDate closingDateTo,
            @Param("itemCode") String itemCode,
            @Param("itemName") String itemName,
            @Param("supplierId") Long supplierId,
            @Param("hasQuotations") Boolean hasQuotations,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );
    
    // Count by status
    Long countByStatus(RfqStatus status);
}