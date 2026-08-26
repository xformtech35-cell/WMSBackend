package com.warehouse.wms.repository;

import com.warehouse.wms.entity.DeliveryChallan;
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
public interface DeliveryChallanRepository extends JpaRepository<DeliveryChallan, Long> {

    Optional<DeliveryChallan> findByChallanNumber(String challanNumber);


    List<DeliveryChallan> findByStatus(String status);

    Page<DeliveryChallan> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(String status);

    @Query("SELECT dc FROM DeliveryChallan dc WHERE " +
           "(:challanNumber IS NULL OR dc.challanNumber LIKE %:challanNumber%) AND " +
           "(:shipmentNumber IS NULL OR dc.shipmentNumber LIKE %:shipmentNumber%) AND " +
           "(:transporter IS NULL OR LOWER(dc.transporter) LIKE LOWER(CONCAT('%', :transporter, '%'))) AND " +
           "(:vehicleNumber IS NULL OR dc.vehicleNumber LIKE %:vehicleNumber%) AND " +
           "(:status IS NULL OR dc.status = :status) AND " +
           "(:startDate IS NULL OR dc.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR dc.createdAt <= :endDate)")
    Page<DeliveryChallan> findByFilters(
            @Param("challanNumber") String challanNumber,
            @Param("shipmentNumber") String shipmentNumber,
            @Param("transporter") String transporter,
            @Param("vehicleNumber") String vehicleNumber,
            @Param("status") String status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
    
    @Query("SELECT dc FROM DeliveryChallan dc WHERE " +
            "LOWER(dc.challanNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(dc.shipmentNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(dc.transporter) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(dc.vehicleNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(dc.driverName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(dc.status) LIKE LOWER(CONCAT('%', :search, '%'))")
     Page<DeliveryChallan> searchDeliveryChallans(@Param("search") String search, Pageable pageable);

    @Query("SELECT COALESCE(SUM(dc.totalQuantity), 0) FROM DeliveryChallan dc")
    Integer getTotalQuantity();

    @Query("SELECT COALESCE(SUM(dc.totalWeight), 0) FROM DeliveryChallan dc")
    Double getTotalWeight();
}