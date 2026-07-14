package com.warehouse.wms.repository;

import com.warehouse.wms.entity.PurchaseOrder;
import com.warehouse.wms.entity.PurchaseOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    
    // Basic find methods
    List<PurchaseOrder> findByStatus(PurchaseOrderStatus status);
    List<PurchaseOrder> findBySupplierId(Long supplierId);
    List<PurchaseOrder> findByPurchaseRequestId(Long purchaseRequestId);
    
    // Exists by PO number
    boolean existsByPoNumber(String poNumber);
    Long countByPoNumberStartingWith(String prefix);
    
    // Advanced filter
    @Query("SELECT DISTINCT po FROM PurchaseOrder po " +
           "LEFT JOIN po.lines l " +
           "WHERE " +
           "(:status IS NULL OR po.status = :status) AND " +
           "(:statuses IS NULL OR po.status IN :statuses) AND " +
           "(:poDateFrom IS NULL OR po.poDate >= :poDateFrom) AND " +
           "(:poDateTo IS NULL OR po.poDate <= :poDateTo) AND " +
           "(:expectedArrivalFrom IS NULL OR po.expectedArrivalDate >= :expectedArrivalFrom) AND " +
           "(:expectedArrivalTo IS NULL OR po.expectedArrivalDate <= :expectedArrivalTo) AND " +
           "(:poNumber IS NULL OR po.poNumber LIKE CONCAT('%', :poNumber, '%')) AND " +
           "(:supplierName IS NULL OR po.supplierName LIKE CONCAT('%', :supplierName, '%')) AND " +
           "(:itemCode IS NULL OR l.itemCode LIKE CONCAT('%', :itemCode, '%')) AND " +
           "(:itemName IS NULL OR l.itemName LIKE CONCAT('%', :itemName, '%')) AND " +
           "(:supplierId IS NULL OR po.supplier.id = :supplierId) AND " +
           "(:purchaseRequestId IS NULL OR po.purchaseRequestId = :purchaseRequestId) AND " +
           "(:minAmount IS NULL OR po.grandTotal >= :minAmount) AND " +
           "(:maxAmount IS NULL OR po.grandTotal <= :maxAmount) AND " +
           "(:searchTerm IS NULL OR " +
           "po.poNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
           "po.supplierName LIKE CONCAT('%', :searchTerm, '%') OR " +
           "l.itemCode LIKE CONCAT('%', :searchTerm, '%') OR " +
           "l.itemName LIKE CONCAT('%', :searchTerm, '%'))")
    Page<PurchaseOrder> filterPurchaseOrders(
            @Param("status") PurchaseOrderStatus status,
            @Param("statuses") List<PurchaseOrderStatus> statuses,
            @Param("poDateFrom") LocalDate poDateFrom,
            @Param("poDateTo") LocalDate poDateTo,
            @Param("expectedArrivalFrom") LocalDate expectedArrivalFrom,
            @Param("expectedArrivalTo") LocalDate expectedArrivalTo,
            @Param("poNumber") String poNumber,
            @Param("supplierName") String supplierName,
            @Param("itemCode") String itemCode,
            @Param("itemName") String itemName,
            @Param("supplierId") Long supplierId,
            @Param("purchaseRequestId") Long purchaseRequestId,
            @Param("minAmount") Double minAmount,
            @Param("maxAmount") Double maxAmount,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );
}