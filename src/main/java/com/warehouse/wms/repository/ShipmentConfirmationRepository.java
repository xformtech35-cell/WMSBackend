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

import com.warehouse.wms.entity.ShipmentConfirmation;

@Repository
public interface ShipmentConfirmationRepository extends JpaRepository<ShipmentConfirmation, Long> {

    Optional<ShipmentConfirmation> findByShipmentNumber(String shipmentNumber);

    Optional<ShipmentConfirmation> findByTrackingNumber(String trackingNumber);

    List<ShipmentConfirmation> findBySoNumber(String soNumber);

    List<ShipmentConfirmation> findByStatus(String status);

    @Modifying
    @Transactional
    @Query("UPDATE ShipmentConfirmation sc SET sc.status = :status, sc.actualDeliveryDate = :actualDeliveryDate WHERE sc.shipmentNumber = :shipmentNumber")
    void updateDeliveryStatus(@Param("shipmentNumber") String shipmentNumber,
                             @Param("status") String status,
                             @Param("actualDeliveryDate") java.time.LocalDateTime actualDeliveryDate);
    
    
    
    // ====== ADVANCED FILTERS ======
    @Query("SELECT sc FROM ShipmentConfirmation sc WHERE " +
           "(:shipmentNumber IS NULL OR sc.shipmentNumber LIKE %:shipmentNumber%) AND " +
           "(:dispatchNumber IS NULL OR sc.dispatchNumber LIKE %:dispatchNumber%) AND " +
           "(:soNumber IS NULL OR sc.soNumber LIKE %:soNumber%) AND " +
           "(:packageNumber IS NULL OR sc.packageNumber LIKE %:packageNumber%) AND " +
           "(:trackingNumber IS NULL OR sc.trackingNumber LIKE %:trackingNumber%) AND " +
           "(:transporter IS NULL OR LOWER(sc.transporter) LIKE LOWER(CONCAT('%', :transporter, '%'))) AND " +
           "(:shippingMethod IS NULL OR sc.shippingMethod = :shippingMethod) AND " +
           "(:vehicleNumber IS NULL OR sc.vehicleNumber LIKE %:vehicleNumber%) AND " +
           "(:status IS NULL OR sc.status = :status) AND " +
           "(:confirmedBy IS NULL OR LOWER(sc.confirmedBy) LIKE LOWER(CONCAT('%', :confirmedBy, '%'))) AND " +
           "(:startDate IS NULL OR sc.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR sc.createdAt <= :endDate) AND " +
           "(:startDispatchDate IS NULL OR sc.dispatchDate >= :startDispatchDate) AND " +
           "(:endDispatchDate IS NULL OR sc.dispatchDate <= :endDispatchDate) AND " +
           "(:startExpectedDelivery IS NULL OR sc.expectedDeliveryDate >= :startExpectedDelivery) AND " +
           "(:endExpectedDelivery IS NULL OR sc.expectedDeliveryDate <= :endExpectedDelivery) AND " +
           "(:startActualDelivery IS NULL OR sc.actualDeliveryDate >= :startActualDelivery) AND " +
           "(:endActualDelivery IS NULL OR sc.actualDeliveryDate <= :endActualDelivery)")
    Page<ShipmentConfirmation> findByFilters(
            @Param("shipmentNumber") String shipmentNumber,
            @Param("dispatchNumber") String dispatchNumber,
            @Param("soNumber") String soNumber,
            @Param("packageNumber") String packageNumber,
            @Param("trackingNumber") String trackingNumber,
            @Param("transporter") String transporter,
            @Param("shippingMethod") String shippingMethod,
            @Param("vehicleNumber") String vehicleNumber,
            @Param("status") String status,
            @Param("confirmedBy") String confirmedBy,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("startDispatchDate") LocalDateTime startDispatchDate,
            @Param("endDispatchDate") LocalDateTime endDispatchDate,
            @Param("startExpectedDelivery") LocalDateTime startExpectedDelivery,
            @Param("endExpectedDelivery") LocalDateTime endExpectedDelivery,
            @Param("startActualDelivery") LocalDateTime startActualDelivery,
            @Param("endActualDelivery") LocalDateTime endActualDelivery,
            Pageable pageable);

    // ====== SEARCH ======
    @Query("SELECT sc FROM ShipmentConfirmation sc WHERE " +
           "LOWER(sc.shipmentNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(sc.dispatchNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(sc.soNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(sc.trackingNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(sc.transporter) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(sc.status) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<ShipmentConfirmation> searchShipments(@Param("search") String search, Pageable pageable);
}