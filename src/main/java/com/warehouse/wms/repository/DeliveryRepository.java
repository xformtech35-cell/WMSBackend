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

import com.warehouse.wms.entity.Delivery;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByDeliveryNumber(String deliveryNumber);

    Optional<Delivery> findByShipmentNumber(String shipmentNumber);

    List<Delivery> findBySoNumber(String soNumber);

    List<Delivery> findByDeliveryStatus(String deliveryStatus);
    
    
    // ====== ADVANCED FILTERS ======
    @Query("SELECT d FROM Delivery d WHERE " +
           "(:deliveryNumber IS NULL OR d.deliveryNumber LIKE %:deliveryNumber%) AND " +
           "(:shipmentNumber IS NULL OR d.shipmentNumber LIKE %:shipmentNumber%) AND " +
           "(:soNumber IS NULL OR d.soNumber LIKE %:soNumber%) AND " +
           "(:packageNumber IS NULL OR d.packageNumber LIKE %:packageNumber%) AND " +
           "(:customerCode IS NULL OR d.customerCode LIKE %:customerCode%) AND " +
           "(:customerName IS NULL OR LOWER(d.customerName) LIKE LOWER(CONCAT('%', :customerName, '%'))) AND " +
           "(:trackingNumber IS NULL OR d.trackingNumber LIKE %:trackingNumber%) AND " +
           "(:deliveryStatus IS NULL OR d.deliveryStatus = :deliveryStatus) AND " +
           "(:receivedBy IS NULL OR LOWER(d.receivedBy) LIKE LOWER(CONCAT('%', :receivedBy, '%'))) AND " +
           "(:startDate IS NULL OR d.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR d.createdAt <= :endDate) AND " +
           "(:startDeliveryDate IS NULL OR d.deliveryDate >= :startDeliveryDate) AND " +
           "(:endDeliveryDate IS NULL OR d.deliveryDate <= :endDeliveryDate) AND " +
           "(:minQuantity IS NULL OR d.deliveredQuantity >= :minQuantity) AND " +
           "(:maxQuantity IS NULL OR d.deliveredQuantity <= :maxQuantity)")
    Page<Delivery> findByFilters(
            @Param("deliveryNumber") String deliveryNumber,
            @Param("shipmentNumber") String shipmentNumber,
            @Param("soNumber") String soNumber,
            @Param("packageNumber") String packageNumber,
            @Param("customerCode") String customerCode,
            @Param("customerName") String customerName,
            @Param("trackingNumber") String trackingNumber,
            @Param("deliveryStatus") String deliveryStatus,
            @Param("receivedBy") String receivedBy,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("startDeliveryDate") LocalDateTime startDeliveryDate,
            @Param("endDeliveryDate") LocalDateTime endDeliveryDate,
            @Param("minQuantity") Integer minQuantity,
            @Param("maxQuantity") Integer maxQuantity,
            Pageable pageable);

    // ====== SEARCH ======
    @Query("SELECT d FROM Delivery d WHERE " +
           "LOWER(d.deliveryNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.shipmentNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.soNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.packageNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.customerCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.trackingNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.deliveryStatus) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Delivery> searchDeliveries(@Param("search") String search, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Delivery d SET d.deliveryStatus = :deliveryStatus WHERE d.deliveryNumber = :deliveryNumber")
    void updateDeliveryStatus(@Param("deliveryNumber") String deliveryNumber, @Param("deliveryStatus") String deliveryStatus);
}