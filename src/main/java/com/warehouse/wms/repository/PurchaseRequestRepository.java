package com.warehouse.wms.repository;

import com.warehouse.wms.entity.PurchaseRequest;
import com.warehouse.wms.entity.RequestStatus;
import com.warehouse.wms.entity.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {
    
    List<PurchaseRequest> findByStatus(RequestStatus status);
    
    List<PurchaseRequest> findByPriority(Priority priority);
    
    List<PurchaseRequest> findByRequestedBy(String requestedBy);
    
    List<PurchaseRequest> findByRequestedByContaining(String requestedBy);
    
    List<PurchaseRequest> findByDepartment(String department);
    
    List<PurchaseRequest> findByWarehouse(String warehouse);
    
    Page<PurchaseRequest> findByStatus(RequestStatus status, Pageable pageable);
    
    @Query("SELECT COUNT(pr) FROM PurchaseRequest pr WHERE pr.prNumber LIKE CONCAT(:prefix, '%')")
    Long countByPrNumberStartingWith(@Param("prefix") String prefix);
    
    boolean existsByPrNumber(String prNumber);

    List<PurchaseRequest> findByCreatedBy(Long createdBy);

    
    Long countByStatus(RequestStatus status);
    
    @Query("SELECT pr FROM PurchaseRequest pr WHERE " +
           "(:status IS NULL OR pr.status = :status) AND " +
           "(:priority IS NULL OR pr.priority = :priority) AND " +
           "(:startDate IS NULL OR pr.prDate >= :startDate) AND " +
           "(:endDate IS NULL OR pr.prDate <= :endDate)")
    Page<PurchaseRequest> findWithFilters(@Param("status") RequestStatus status,
                                         @Param("priority") Priority priority,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate,
                                         Pageable pageable);
    
    @Query("SELECT pr FROM PurchaseRequest pr WHERE pr.prDate BETWEEN :startDate AND :endDate")
    List<PurchaseRequest> findByPrDateBetween(@Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);
    
    List<PurchaseRequest> findByStatusAndRequestedBy(RequestStatus status, String requestedBy);
    
    List<PurchaseRequest> findByStatusAndDepartment(RequestStatus status, String department);
    
    // FIX: Updated query to properly find max sequence
    @Query(value = "SELECT MAX(CAST(SUBSTRING(pr_number, LOCATE('-', pr_number, LOCATE('-', pr_number) + 1) + 1) AS UNSIGNED)) FROM purchase_requests WHERE pr_number LIKE CONCAT(:datePattern, '%')", nativeQuery = true)
    Integer findMaxSequenceForDate(@Param("datePattern") String datePattern);
    
    
    
    
    
    @Query("SELECT DISTINCT pr FROM PurchaseRequest pr " +
           "LEFT JOIN pr.items i " +
           "WHERE " +
           "(:status IS NULL OR pr.status = :status) AND " +
           "(:statuses IS NULL OR pr.status IN :statuses) AND " +
           "(:priority IS NULL OR pr.priority = :priority) AND " +
           "(:priorities IS NULL OR pr.priority IN :priorities) AND " +
           "(:startDate IS NULL OR pr.prDate >= :startDate) AND " +
           "(:endDate IS NULL OR pr.prDate <= :endDate) AND " +
           "(:prDateFrom IS NULL OR pr.prDate >= :prDateFrom) AND " +
           "(:prDateTo IS NULL OR pr.prDate <= :prDateTo) AND " +
           "(:requiredDateFrom IS NULL OR pr.requiredDate >= :requiredDateFrom) AND " +
           "(:requiredDateTo IS NULL OR pr.requiredDate <= :requiredDateTo) AND " +
           "(:prNumber IS NULL OR pr.prNumber LIKE CONCAT('%', :prNumber, '%')) AND " +
           "(:requestedBy IS NULL OR pr.requestedBy LIKE CONCAT('%', :requestedBy, '%')) AND " +
           "(:department IS NULL OR pr.department LIKE CONCAT('%', :department, '%')) AND " +
           "(:warehouse IS NULL OR pr.warehouse LIKE CONCAT('%', :warehouse, '%')) AND " +
           "(:remarks IS NULL OR pr.remarks LIKE CONCAT('%', :remarks, '%')) AND " +
           "(:itemCode IS NULL OR i.itemCode LIKE CONCAT('%', :itemCode, '%')) AND " +
           "(:itemName IS NULL OR i.itemName LIKE CONCAT('%', :itemName, '%')) AND " +
           "(:supplierId IS NULL OR pr.supplier.id = :supplierId) AND " +
           "(:supplierName IS NULL OR pr.supplier.name LIKE CONCAT('%', :supplierName, '%')) AND " +
           "(:isActive IS NULL OR pr.status IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'IN_PROGRESS', 'PARTIAL')) AND " +
           "(:hasSupplier IS NULL OR (CASE WHEN :hasSupplier = true THEN pr.supplier IS NOT NULL ELSE pr.supplier IS NULL END)) AND " +
           "(:hasItems IS NULL OR (CASE WHEN :hasItems = true THEN pr.items IS NOT EMPTY ELSE pr.items IS EMPTY END)) AND " +
           "(:createdFrom IS NULL OR pr.createdAt >= :createdFrom) AND " +
           "(:createdTo IS NULL OR pr.createdAt <= :createdTo) AND " +
           "(:searchTerm IS NULL OR " +
           "pr.prNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
           "pr.requestedBy LIKE CONCAT('%', :searchTerm, '%') OR " +
           "pr.department LIKE CONCAT('%', :searchTerm, '%') OR " +
           "pr.warehouse LIKE CONCAT('%', :searchTerm, '%') OR " +
           "i.itemCode LIKE CONCAT('%', :searchTerm, '%') OR " +
           "i.itemName LIKE CONCAT('%', :searchTerm, '%'))")
    Page<PurchaseRequest> filterPurchaseRequests(
            @Param("status") RequestStatus status,
            @Param("statuses") List<RequestStatus> statuses,
            @Param("priority") Priority priority,
            @Param("priorities") List<Priority> priorities,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("prDateFrom") LocalDate prDateFrom,
            @Param("prDateTo") LocalDate prDateTo,
            @Param("requiredDateFrom") LocalDate requiredDateFrom,
            @Param("requiredDateTo") LocalDate requiredDateTo,
            @Param("prNumber") String prNumber,
            @Param("requestedBy") String requestedBy,
            @Param("department") String department,
            @Param("warehouse") String warehouse,
            @Param("remarks") String remarks,
            @Param("itemCode") String itemCode,
            @Param("itemName") String itemName,
            @Param("supplierId") Long supplierId,
            @Param("supplierName") String supplierName,
            @Param("isActive") Boolean isActive,
            @Param("hasSupplier") Boolean hasSupplier,
            @Param("hasItems") Boolean hasItems,
            @Param("createdFrom") LocalDate createdFrom,
            @Param("createdTo") LocalDate createdTo,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

}