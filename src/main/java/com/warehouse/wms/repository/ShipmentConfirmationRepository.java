package com.warehouse.wms.repository;

import com.warehouse.wms.entity.ShipmentConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
}