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
    
    // Find by status
    List<PurchaseRequest> findByStatus(RequestStatus status);
    
    // Find by priority
    List<PurchaseRequest> findByPriority(Priority priority);
    
    // Find by requestedBy (String - employee name) - REMOVE the Long version
    List<PurchaseRequest> findByRequestedBy(String requestedBy);
    
    // Find by requestedBy containing (for search)
    List<PurchaseRequest> findByRequestedByContaining(String requestedBy);
    
    // Find by department
    List<PurchaseRequest> findByDepartment(String department);
    
    // Find by warehouse
    List<PurchaseRequest> findByWarehouse(String warehouse);
    
    // Page by status
    Page<PurchaseRequest> findByStatus(RequestStatus status, Pageable pageable);
    
    // Count by status
    Long countByStatus(RequestStatus status);
    
    // Find with filters
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
    
    // Find by date range
    @Query("SELECT pr FROM PurchaseRequest pr WHERE pr.prDate BETWEEN :startDate AND :endDate")
    List<PurchaseRequest> findByPrDateBetween(@Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);
    
    // Find by status and requestedBy
    List<PurchaseRequest> findByStatusAndRequestedBy(RequestStatus status, String requestedBy);
    
    // Find by status and department
    List<PurchaseRequest> findByStatusAndDepartment(RequestStatus status, String department);
    
    // Find the maximum sequence number for a specific date (for PR number generation)
    @Query(value = "SELECT MAX(CAST(SUBSTRING(pr_number, -4) AS INTEGER)) FROM purchase_requests WHERE pr_number LIKE CONCAT(:datePattern, '%')", nativeQuery = true)
    Integer findMaxSequenceForDate(@Param("datePattern") String datePattern);
}