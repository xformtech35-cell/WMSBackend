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
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {
    
    List<PurchaseRequest> findByStatus(RequestStatus status);
    
    List<PurchaseRequest> findByPriority(Priority priority);
    
    Page<PurchaseRequest> findByStatus(RequestStatus status, Pageable pageable);
    
    @Query("SELECT pr FROM PurchaseRequest pr WHERE " +
           "(:status IS NULL OR pr.status = :status) AND " +
           "(:priority IS NULL OR pr.priority = :priority) AND " +
           "(:startDate IS NULL OR pr.requestedDate >= :startDate) AND " +
           "(:endDate IS NULL OR pr.requestedDate <= :endDate)")
    Page<PurchaseRequest> findWithFilters(@Param("status") RequestStatus status,
                                         @Param("priority") Priority priority,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate,
                                         Pageable pageable);
    
    @Query("SELECT pr FROM PurchaseRequest pr WHERE pr.createdBy = :userId ORDER BY pr.createdAt DESC")
    List<PurchaseRequest> findByCreatedBy(@Param("userId") Long userId);
    
    Long countByStatus(RequestStatus status);
    
    @Query("SELECT COALESCE(SUM(pr.totalAmount), 0) FROM PurchaseRequest pr WHERE pr.status = 'APPROVED' AND pr.createdAt BETWEEN :startDate AND :endDate")
    Double getTotalApprovedAmountBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}