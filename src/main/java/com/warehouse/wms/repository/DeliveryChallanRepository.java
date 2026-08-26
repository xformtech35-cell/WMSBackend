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

    List<DeliveryChallan> findBySoNumber(String soNumber);

    List<DeliveryChallan> findByPackageNumber(String packageNumber);

    List<DeliveryChallan> findByStatus(String status);

    List<DeliveryChallan> findByTransporter(String transporter);


    Page<DeliveryChallan> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(String status);

    @Query("SELECT dc FROM DeliveryChallan dc WHERE " +
           "(:challanNumber IS NULL OR dc.challanNumber LIKE %:challanNumber%) AND " +
           "(:soNumber IS NULL OR dc.soNumber LIKE %:soNumber%) AND " +
           "(:packageNumber IS NULL OR dc.packageNumber LIKE %:packageNumber%) AND " +
           "(:shipmentNumber IS NULL OR dc.shipmentNumber LIKE %:shipmentNumber%) AND " +
           "(:customerCode IS NULL OR dc.customerCode LIKE %:customerCode%) AND " +
           "(:customerName IS NULL OR LOWER(dc.customerName) LIKE LOWER(CONCAT('%', :customerName, '%'))) AND " +
           "(:status IS NULL OR dc.status = :status) AND " +
           "(:transporter IS NULL OR LOWER(dc.transporter) LIKE LOWER(CONCAT('%', :transporter, '%'))) AND " +
           "(:vehicleNumber IS NULL OR dc.vehicleNumber LIKE %:vehicleNumber%) AND " +
           "(:startDate IS NULL OR dc.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR dc.createdAt <= :endDate) AND " +
           "(:startDispatchDate IS NULL OR dc.dispatchDate >= :startDispatchDate) AND " +
           "(:endDispatchDate IS NULL OR dc.dispatchDate <= :endDispatchDate)")
    Page<DeliveryChallan> findByFilters(
            @Param("challanNumber") String challanNumber,
            @Param("soNumber") String soNumber,
            @Param("packageNumber") String packageNumber,
            @Param("shipmentNumber") String shipmentNumber,
            @Param("customerCode") String customerCode,
            @Param("customerName") String customerName,
            @Param("status") String status,
            @Param("transporter") String transporter,
            @Param("vehicleNumber") String vehicleNumber,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("startDispatchDate") LocalDateTime startDispatchDate,
            @Param("endDispatchDate") LocalDateTime endDispatchDate,
            Pageable pageable);

    @Query("SELECT dc FROM DeliveryChallan dc WHERE " +
           "LOWER(dc.challanNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(dc.soNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(dc.packageNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(dc.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(dc.transporter) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(dc.status) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<DeliveryChallan> searchDeliveryChallans(@Param("search") String search, Pageable pageable);

    @Query("SELECT COALESCE(SUM(dc.totalQuantity), 0) FROM DeliveryChallan dc")
    Integer getTotalQuantity();

    @Query("SELECT COALESCE(SUM(dc.totalWeight), 0) FROM DeliveryChallan dc")
    Double getTotalWeight();
}