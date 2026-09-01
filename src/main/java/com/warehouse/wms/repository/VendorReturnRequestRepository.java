package com.warehouse.wms.repository;

import com.warehouse.wms.entity.VendorReturnRequest;
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
public interface VendorReturnRequestRepository extends JpaRepository<VendorReturnRequest, Long> {
    
    /**
     * Find return request by request number
     */
    Optional<VendorReturnRequest> findByReturnRequestNumber(String returnRequestNumber);
    
    /**
     * Find return requests by supplier ID
     */
    List<VendorReturnRequest> findBySupplierId(Long supplierId);
    
    /**
     * Find return requests by status
     */
    List<VendorReturnRequest> findByStatus(VendorReturnRequest.RequestStatus status);
    
    /**
     * Find return requests by status and priority
     */
    @Query("SELECT r FROM VendorReturnRequest r WHERE r.status = :status AND r.priority = :priority")
    List<VendorReturnRequest> findByStatusAndPriority(
            @Param("status") VendorReturnRequest.RequestStatus status,
            @Param("priority") VendorReturnRequest.Priority priority);
    
    /**
     * Search return requests with filters
     */
    @Query("SELECT r FROM VendorReturnRequest r WHERE " +
           "(:supplierName IS NULL OR LOWER(r.supplierName) LIKE LOWER(CONCAT('%', :supplierName, '%'))) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:searchTerm IS NULL OR " +
           "LOWER(r.returnRequestNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.poNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.grnNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.supplierName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<VendorReturnRequest> searchRequests(
            @Param("supplierName") String supplierName,
            @Param("status") VendorReturnRequest.RequestStatus status,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);
    
    /**
     * Count requests by number prefix for generating sequence
     */
    @Query("SELECT COUNT(r) FROM VendorReturnRequest r WHERE r.returnRequestNumber LIKE CONCAT(:prefix, '%')")
    Long countByReturnRequestNumberStartingWith(@Param("prefix") String prefix);
    
    /**
     * Find requests by date range
     */
    List<VendorReturnRequest> findByRequestDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Count requests by status
     */
    Long countByStatus(VendorReturnRequest.RequestStatus status);
    
    /**
     * Find pending approval requests
     */
    @Query("SELECT r FROM VendorReturnRequest r WHERE r.status = 'PENDING_APPROVAL' ORDER BY r.createdAt ASC")
    List<VendorReturnRequest> findPendingApprovalRequests();
    
    /**
     * Find requests by supplier name
     */
    List<VendorReturnRequest> findBySupplierNameContainingIgnoreCase(String supplierName);
    
    /**
     * Find urgent requests
     */
    @Query("SELECT r FROM VendorReturnRequest r WHERE r.priority = 'URGENT' AND r.status != 'REJECTED' AND r.status != 'CANCELLED'")
    List<VendorReturnRequest> findUrgentRequests();
    
    /**
     * Count requests by return type
     */
    @Query("SELECT r.returnType, COUNT(r) FROM VendorReturnRequest r GROUP BY r.returnType")
    List<Object[]> countByReturnType();
    
    /**
     * Get requests summary statistics
     */
    @Query("SELECT COUNT(r), SUM(CASE WHEN r.status = 'PENDING_APPROVAL' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.status = 'APPROVED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.status = 'REJECTED' THEN 1 ELSE 0 END) " +
           "FROM VendorReturnRequest r")
    Object[] getRequestStatistics();
}