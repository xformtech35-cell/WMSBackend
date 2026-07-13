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
    
    
    boolean existsByPrNumber(String prNumber);

    
    
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
}