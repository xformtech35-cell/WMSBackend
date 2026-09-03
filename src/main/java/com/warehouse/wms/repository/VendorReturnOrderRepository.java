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

import com.warehouse.wms.entity.VendorReturnOrder;
import com.warehouse.wms.entity.VendorReturnRequest;

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
    
    @Query("SELECT o FROM VendorReturnOrder o WHERE o.pickListGenerated = true")
    Page<VendorReturnOrder> findOrdersWithPickList(Pageable pageable);
    
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
    
    
    
    
    
    
    @Query("SELECT o FROM VendorReturnOrder o " +
            "WHERE (:vroNumber IS NULL OR o.vroNumber LIKE CONCAT('%', :vroNumber, '%')) " +
            "AND (:supplierName IS NULL OR LOWER(o.supplierName) LIKE LOWER(CONCAT('%', :supplierName, '%'))) " +
            "AND (:supplierCode IS NULL OR LOWER(o.supplierCode) LIKE LOWER(CONCAT('%', :supplierCode, '%'))) " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:priority IS NULL OR o.priority = :priority) " +
            "AND (:returnType IS NULL OR o.returnType = :returnType) " +
            "AND (:assignTo IS NULL OR o.assignTo LIKE CONCAT('%', :assignTo, '%')) " +
            "AND (:pickListGenerated IS NULL OR o.pickListGenerated = :pickListGenerated) " +
            "AND (:orderFromDate IS NULL OR DATE(o.orderDate) >= :orderFromDate) " +
            "AND (:orderToDate IS NULL OR DATE(o.orderDate) <= :orderToDate) " +
            "AND (:expectedFromDate IS NULL OR DATE(o.expectedReturnDate) >= :expectedFromDate) " +
            "AND (:expectedToDate IS NULL OR DATE(o.expectedReturnDate) <= :expectedToDate) " +
            "AND (:actualFromDate IS NULL OR DATE(o.actualReturnDate) >= :actualFromDate) " +
            "AND (:actualToDate IS NULL OR DATE(o.actualReturnDate) <= :actualToDate) " +
            "AND (:minQuantity IS NULL OR o.totalQuantity >= :minQuantity) " +
            "AND (:maxQuantity IS NULL OR o.totalQuantity <= :maxQuantity) " +
            "AND (:minAmount IS NULL OR o.totalAmount >= :minAmount) " +
            "AND (:maxAmount IS NULL OR o.totalAmount <= :maxAmount) " +
            "AND (:searchTerm IS NULL OR " +
            "LOWER(o.vroNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(o.supplierName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(o.supplierCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(o.dispatchNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(o.trackingNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(o.assignTo) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
     Page<VendorReturnOrder> findAllWithFiltersAndSearch(
             @Param("vroNumber") String vroNumber,
             @Param("supplierName") String supplierName,
             @Param("supplierCode") String supplierCode,
             @Param("status") VendorReturnOrder.OrderStatus status,
             @Param("priority") VendorReturnRequest.Priority priority,
             @Param("returnType") VendorReturnRequest.ReturnType returnType,
             @Param("assignTo") String assignTo,
             @Param("pickListGenerated") Boolean pickListGenerated,
             @Param("orderFromDate") LocalDate orderFromDate,
             @Param("orderToDate") LocalDate orderToDate,
             @Param("expectedFromDate") LocalDate expectedFromDate,
             @Param("expectedToDate") LocalDate expectedToDate,
             @Param("actualFromDate") LocalDate actualFromDate,
             @Param("actualToDate") LocalDate actualToDate,
             @Param("minQuantity") Integer minQuantity,
             @Param("maxQuantity") Integer maxQuantity,
             @Param("minAmount") Double minAmount,
             @Param("maxAmount") Double maxAmount,
             @Param("searchTerm") String searchTerm,
             Pageable pageable);

     // ========== SEARCH ONLY ==========

     /**
      * Search return orders by search term only
      */
     @Query("SELECT o FROM VendorReturnOrder o " +
            "WHERE (:searchTerm IS NULL OR " +
            "LOWER(o.vroNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(o.supplierName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(o.supplierCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(o.dispatchNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(o.trackingNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(o.assignTo) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
     Page<VendorReturnOrder> searchOrders(
             @Param("searchTerm") String searchTerm,
             Pageable pageable);
     
     
     
     
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
    
    @Query("SELECT o FROM VendorReturnOrder o " +
            "WHERE o.pickListGenerated = true " +
            "AND (:vroNumber IS NULL OR o.vroNumber LIKE CONCAT('%', :vroNumber, '%')) " +
            "AND (:assignTo IS NULL OR o.assignTo LIKE CONCAT('%', :assignTo, '%')) " +
            "AND (:supplierName IS NULL OR LOWER(o.supplierName) LIKE LOWER(CONCAT('%', :supplierName, '%'))) " +
            "AND (:assignedFromDate IS NULL OR DATE(o.pickListGeneratedAt) >= :assignedFromDate) " +
            "AND (:assignedToDate IS NULL OR DATE(o.pickListGeneratedAt) <= :assignedToDate) " +
            "AND (:pickedFromDate IS NULL OR DATE(o.pickedAt) >= :pickedFromDate) " +
            "AND (:pickedToDate IS NULL OR DATE(o.pickedAt) <= :pickedToDate) " +
            "AND (:searchTerm IS NULL OR " +
            "LOWER(o.vroNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(o.supplierName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(o.supplierCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(o.assignTo) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(o.dispatchNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(o.trackingNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
     Page<VendorReturnOrder> findPickListsWithAdvancedFilters(
             @Param("vroNumber") String vroNumber,
             @Param("assignTo") String assignTo,
             @Param("supplierName") String supplierName,
             @Param("assignedFromDate") LocalDate assignedFromDate,
             @Param("assignedToDate") LocalDate assignedToDate,
             @Param("pickedFromDate") LocalDate pickedFromDate,
             @Param("pickedToDate") LocalDate pickedToDate,
             @Param("searchTerm") String searchTerm,
             Pageable pageable);
    
    
    
}