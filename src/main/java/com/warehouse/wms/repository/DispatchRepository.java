package com.warehouse.wms.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.warehouse.wms.entity.Dispatch;

@Repository
public interface DispatchRepository extends JpaRepository<Dispatch, Long> {

    Optional<Dispatch> findByDispatchNumber(String dispatchNumber);

    Optional<Dispatch> findByShipmentNumber(String shipmentNumber);

    List<Dispatch> findBySoNumber(String soNumber);

    List<Dispatch> findByStatus(String status);

    @Modifying
    @Transactional
    @Query("UPDATE Dispatch d SET d.status = :status WHERE d.dispatchNumber = :dispatchNumber")
    void updateStatus(@Param("dispatchNumber") String dispatchNumber, @Param("status") String status);

    @Modifying
    @Transactional
    @Query("UPDATE Dispatch d SET d.shipmentNumber = :shipmentNumber WHERE d.dispatchNumber = :dispatchNumber")
    void updateShipmentNumber(@Param("dispatchNumber") String dispatchNumber, @Param("shipmentNumber") String shipmentNumber);
    
    
    
    @Query("SELECT d FROM Dispatch d WHERE " +
            "(:dispatchNumber IS NULL OR d.dispatchNumber LIKE %:dispatchNumber%) AND " +
            "(:shipmentNumber IS NULL OR d.shipmentNumber LIKE %:shipmentNumber%) AND " +
            "(:soNumber IS NULL OR d.soNumber LIKE %:soNumber%) AND " +
            "(:packageNumber IS NULL OR d.packageNumber LIKE %:packageNumber%) AND " +
            "(:customerCode IS NULL OR d.customerCode LIKE %:customerCode%) AND " +
            "(:customerName IS NULL OR LOWER(d.customerName) LIKE LOWER(CONCAT('%', :customerName, '%'))) AND " +
            "(:transporter IS NULL OR LOWER(d.transporter) LIKE LOWER(CONCAT('%', :transporter, '%'))) AND " +
            "(:vehicleNumber IS NULL OR d.vehicleNumber LIKE %:vehicleNumber%) AND " +
            "(:driverName IS NULL OR LOWER(d.driverName) LIKE LOWER(CONCAT('%', :driverName, '%'))) AND " +
            "(:invoiceNumber IS NULL OR d.invoiceNumber LIKE %:invoiceNumber%) AND " +
            "(:deliveryChallan IS NULL OR d.deliveryChallan LIKE %:deliveryChallan%) AND " +
            "(:status IS NULL OR d.status = :status) AND " +
            "(:dispatchedBy IS NULL OR LOWER(d.dispatchedBy) LIKE LOWER(CONCAT('%', :dispatchedBy, '%'))) AND " +
            "(:startDate IS NULL OR d.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR d.createdAt <= :endDate) AND " +
            "(:startDispatchDate IS NULL OR d.dispatchDate >= :startDispatchDate) AND " +
            "(:endDispatchDate IS NULL OR d.dispatchDate <= :endDispatchDate)")
     Page<Dispatch> findByFilters(
             @Param("dispatchNumber") String dispatchNumber,
             @Param("shipmentNumber") String shipmentNumber,
             @Param("soNumber") String soNumber,
             @Param("packageNumber") String packageNumber,
             @Param("customerCode") String customerCode,
             @Param("customerName") String customerName,
             @Param("transporter") String transporter,
             @Param("vehicleNumber") String vehicleNumber,
             @Param("driverName") String driverName,
             @Param("invoiceNumber") String invoiceNumber,
             @Param("deliveryChallan") String deliveryChallan,
             @Param("status") String status,
             @Param("dispatchedBy") String dispatchedBy,
             @Param("startDate") LocalDateTime startDate,
             @Param("endDate") LocalDateTime endDate,
             @Param("startDispatchDate") LocalDateTime startDispatchDate,
             @Param("endDispatchDate") LocalDateTime endDispatchDate,
             Pageable pageable);

     // ====== SEARCH ======
     @Query("SELECT d FROM Dispatch d WHERE " +
            "LOWER(d.dispatchNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(d.shipmentNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(d.soNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(d.packageNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(d.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(d.transporter) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(d.driverName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(d.status) LIKE LOWER(CONCAT('%', :search, '%'))")
     Page<Dispatch> searchDispatches(@Param("search") String search, Pageable pageable);
     
     
     
     // Get top transporter
     @Query("SELECT d.transporter, COUNT(d) as count FROM Dispatch d GROUP BY d.transporter ORDER BY COUNT(d) DESC")
     List<Object[]> findTopTransporter();

     // Get top transporter with date range
     @Query("SELECT d.transporter, COUNT(d) as count FROM Dispatch d " +
            "WHERE d.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY d.transporter ORDER BY COUNT(d) DESC")
     List<Object[]> findTopTransporterByDateRange(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);
}