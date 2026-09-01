package com.warehouse.wms.repository;

import com.warehouse.wms.entity.VendorReturnOrder;
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
public interface VendorReturnOrderRepository extends JpaRepository<VendorReturnOrder, Long> {
    
    /**
     * Find return order by VRO number
     */
    Optional<VendorReturnOrder> findByVroNumber(String vroNumber);
    
    /**
     * Find return orders by supplier ID
     */
    List<VendorReturnOrder> findBySupplierId(Long supplierId);
    
    /**
     * Find return orders by status
     */
    List<VendorReturnOrder> findByStatus(VendorReturnOrder.OrderStatus status);
    
    /**
     * Find pending orders by priority
     */
    @Query("SELECT o FROM VendorReturnOrder o WHERE o.status = :status ORDER BY o.priority DESC, o.createdAt ASC")
    List<VendorReturnOrder> findPendingOrdersByPriority(@Param("status") VendorReturnOrder.OrderStatus status);
    
    /**
     * Search return orders with filters
     */
    @Query("SELECT o FROM VendorReturnOrder o WHERE " +
           "(:supplierName IS NULL OR LOWER(o.supplierName) LIKE LOWER(CONCAT('%', :supplierName, '%'))) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:searchTerm IS NULL OR " +
           "LOWER(o.vroNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.supplierName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.dispatchNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<VendorReturnOrder> searchOrders(
            @Param("supplierName") String supplierName,
            @Param("status") VendorReturnOrder.OrderStatus status,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);
    
    /**
     * Count orders by number prefix for generating sequence
     */
    @Query("SELECT COUNT(o) FROM VendorReturnOrder o WHERE o.vroNumber LIKE CONCAT(:prefix, '%')")
    Long countByVroNumberStartingWith(@Param("prefix") String prefix);
    
    /**
     * Find order by return request ID
     */
    @Query("SELECT o FROM VendorReturnOrder o WHERE o.returnRequest.id = :requestId")
    Optional<VendorReturnOrder> findByReturnRequestId(@Param("requestId") Long requestId);
    
    /**
     * Count orders by status
     */
    Long countByStatus(VendorReturnOrder.OrderStatus status);
    
    /**
     * Find orders by date range
     */
    List<VendorReturnOrder> findByOrderDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find orders with dispatch pending
     */
    @Query("SELECT o FROM VendorReturnOrder o WHERE o.status IN ('PENDING_PACKING', 'PACKED') ORDER BY o.priority DESC")
    List<VendorReturnOrder> findOrdersReadyForDispatch();
    
    /**
     * Find orders by warehouse execution status
     */
    @Query("SELECT o FROM VendorReturnOrder o WHERE o.pickListGenerated = :generated")
    List<VendorReturnOrder> findByPickListGenerated(@Param("generated") Boolean generated);
    
    /**
     * Get order statistics by status
     */
    @Query("SELECT o.status, COUNT(o) FROM VendorReturnOrder o GROUP BY o.status")
    List<Object[]> countByStatusGroup();
    
    /**
     * Find overdue orders
     */
    @Query("SELECT o FROM VendorReturnOrder o WHERE o.expectedReturnDate < CURRENT_DATE AND o.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<VendorReturnOrder> findOverdueOrders();
    
    /**
     * Count orders by priority
     */
    @Query("SELECT o.priority, COUNT(o) FROM VendorReturnOrder o WHERE o.status != 'CANCELLED' GROUP BY o.priority")
    List<Object[]> countByPriority();
    
    /**
     * Find in-progress orders
     */
    @Query("SELECT o FROM VendorReturnOrder o WHERE o.status IN ('PENDING_PICKING', 'PICKING', 'PENDING_QC', 'QC', 'QC_PASSED', 'QC_FAILED', 'PENDING_PACKING', 'PACKED')")
    List<VendorReturnOrder> findInProgressOrders();
}