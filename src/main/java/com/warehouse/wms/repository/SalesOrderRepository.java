package com.warehouse.wms.repository;

import com.warehouse.wms.entity.SalesOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    Optional<SalesOrder> findBySoNumber(String soNumber);

    List<SalesOrder> findByCustomerCode(String customerCode);

    List<SalesOrder> findByWarehouseId(String warehouseId);

    List<SalesOrder> findByStatus(String status);

    Page<SalesOrder> findByStatus(String status, Pageable pageable);

//    @Query("SELECT so FROM SalesOrder so WHERE " +
//           "(:soNumber IS NULL OR so.soNumber LIKE %:soNumber%) AND " +
//           "(:customerCode IS NULL OR so.customerCode = :customerCode) AND " +
//           "(:status IS NULL OR so.status = :status) AND " +
//           "(:startDate IS NULL OR so.soDate >= :startDate) AND " +
//           "(:endDate IS NULL OR so.soDate <= :endDate)")
//    Page<SalesOrder> findByFilters(
//            @Param("soNumber") String soNumber,
//            @Param("customerCode") String customerCode,
//            @Param("status") String status,
//            @Param("startDate") LocalDateTime startDate,
//            @Param("endDate") LocalDateTime endDate,
//            Pageable pageable);

    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(so.totalQuantity), 0) FROM SalesOrder so WHERE so.status = 'CONFIRMED'")
    Integer getTotalOrderedQuantity();
    
    
    
    
    @Query("SELECT so FROM SalesOrder so WHERE " +
            "LOWER(so.soNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(so.customerCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(so.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(so.status) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(so.warehouseId) LIKE LOWER(CONCAT('%', :search, '%'))")
     Page<SalesOrder> searchSalesOrders(@Param("search") String search, Pageable pageable);
    
    
    
    
    
    
    
    
    
    // FIX: Change soDate to orderDate
    @Query("SELECT so FROM SalesOrder so WHERE " +
           "(:soNumber IS NULL OR so.soNumber LIKE %:soNumber%) AND " +
           "(:customerCode IS NULL OR so.customerCode = :customerCode) AND " +
           "(:status IS NULL OR so.status = :status) AND " +
           "(:startDate IS NULL OR so.orderDate >= :startDate) AND " +
           "(:endDate IS NULL OR so.orderDate <= :endDate)")
    Page<SalesOrder> findByFilters(
            @Param("soNumber") String soNumber,
            @Param("customerCode") String customerCode,
            @Param("status") String status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
    
    
    
    
    
    
    
    
    
    
    
    
    
    @Query("SELECT so FROM SalesOrder so WHERE " +
            "(:soNumber IS NULL OR so.soNumber LIKE %:soNumber%) AND " +
            "(:customerCode IS NULL OR so.customerCode LIKE %:customerCode%) AND " +
            "(:customerName IS NULL OR LOWER(so.customerName) LIKE LOWER(CONCAT('%', :customerName, '%'))) AND " +
            "(:warehouseId IS NULL OR so.warehouseId = :warehouseId) AND " +
            "(:status IS NULL OR so.status = :status) AND " +
            "(:priority IS NULL OR so.priority = :priority) AND " +
            "(:startDate IS NULL OR so.orderDate >= :startDate) AND " +
            "(:endDate IS NULL OR so.orderDate <= :endDate) AND " +
            "(:startCreatedDate IS NULL OR so.createdAt >= :startCreatedDate) AND " +
            "(:endCreatedDate IS NULL OR so.createdAt <= :endCreatedDate) AND " +
            "(:startDeliveryDate IS NULL OR so.deliveryDate >= :startDeliveryDate) AND " +
            "(:endDeliveryDate IS NULL OR so.deliveryDate <= :endDeliveryDate) AND " +
            "(:minQuantity IS NULL OR so.totalQuantity >= :minQuantity) AND " +
            "(:maxQuantity IS NULL OR so.totalQuantity <= :maxQuantity) AND " +
            "(:shippingMethod IS NULL OR so.shippingMethod = :shippingMethod) AND " +
            "(:createdBy IS NULL OR so.createdBy = :createdBy)")
     Page<SalesOrder> findByFilters(
             @Param("soNumber") String soNumber,
             @Param("customerCode") String customerCode,
             @Param("customerName") String customerName,
             @Param("warehouseId") String warehouseId,
             @Param("status") String status,
             @Param("priority") String priority,
             @Param("startDate") LocalDateTime startDate,
             @Param("endDate") LocalDateTime endDate,
             @Param("startCreatedDate") LocalDateTime startCreatedDate,
             @Param("endCreatedDate") LocalDateTime endCreatedDate,
             @Param("startDeliveryDate") LocalDateTime startDeliveryDate,
             @Param("endDeliveryDate") LocalDateTime endDeliveryDate,
             @Param("minQuantity") Integer minQuantity,
             @Param("maxQuantity") Integer maxQuantity,
             @Param("shippingMethod") String shippingMethod,
             @Param("createdBy") String createdBy,
             Pageable pageable);
}