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

import com.warehouse.wms.entity.ShippingLabel;

@Repository
public interface ShippingLabelRepository extends JpaRepository<ShippingLabel, Long> {

    Optional<ShippingLabel> findByLabelNumber(String labelNumber);

    Optional<ShippingLabel> findByTrackingNumber(String trackingNumber);

    List<ShippingLabel> findByPackageNumber(String packageNumber);

    List<ShippingLabel> findBySoNumber(String soNumber);

    List<ShippingLabel> findByLabelStatus(String labelStatus);

    @Modifying
    @Transactional
    @Query("UPDATE ShippingLabel sl SET sl.labelStatus = :labelStatus WHERE sl.labelNumber = :labelNumber")
    void updateLabelStatus(@Param("labelNumber") String labelNumber, @Param("labelStatus") String labelStatus);

    @Modifying
    @Transactional
    @Query("UPDATE ShippingLabel sl SET sl.trackingNumber = :trackingNumber WHERE sl.labelNumber = :labelNumber")
    void updateTrackingNumber(@Param("labelNumber") String labelNumber, @Param("trackingNumber") String trackingNumber);
    
    
    
    @Query("SELECT sl FROM ShippingLabel sl WHERE " +
            "(:labelNumber IS NULL OR sl.labelNumber LIKE %:labelNumber%) AND " +
            "(:packageNumber IS NULL OR sl.packageNumber LIKE %:packageNumber%) AND " +
            "(:packageBarcode IS NULL OR sl.packageBarcode LIKE %:packageBarcode%) AND " +
            "(:soNumber IS NULL OR sl.soNumber LIKE %:soNumber%) AND " +
            "(:customerCode IS NULL OR sl.customerCode LIKE %:customerCode%) AND " +
            "(:customerName IS NULL OR LOWER(sl.customerName) LIKE LOWER(CONCAT('%', :customerName, '%'))) AND " +
            "(:itemCode IS NULL OR sl.itemCode LIKE %:itemCode%) AND " +
            "(:itemName IS NULL OR LOWER(sl.itemName) LIKE LOWER(CONCAT('%', :itemName, '%'))) AND " +
            "(:trackingNumber IS NULL OR sl.trackingNumber LIKE %:trackingNumber%) AND " +
            "(:labelStatus IS NULL OR sl.labelStatus = :labelStatus) AND " +
            "(:shippingMethod IS NULL OR sl.shippingMethod = :shippingMethod) AND " +
            "(:printedBy IS NULL OR LOWER(sl.printedBy) LIKE LOWER(CONCAT('%', :printedBy, '%'))) AND " +
            "(:startDate IS NULL OR sl.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR sl.createdAt <= :endDate) AND " +
            "(:startPrintedDate IS NULL OR sl.printedDate >= :startPrintedDate) AND " +
            "(:endPrintedDate IS NULL OR sl.printedDate <= :endPrintedDate) AND " +
            "(:minWeight IS NULL OR sl.weight >= :minWeight) AND " +
            "(:maxWeight IS NULL OR sl.weight <= :maxWeight) AND " +
            "(:minQuantity IS NULL OR sl.quantity >= :minQuantity) AND " +
            "(:maxQuantity IS NULL OR sl.quantity <= :maxQuantity)")
     Page<ShippingLabel> findByFilters(
             @Param("labelNumber") String labelNumber,
             @Param("packageNumber") String packageNumber,
             @Param("packageBarcode") String packageBarcode,
             @Param("soNumber") String soNumber,
             @Param("customerCode") String customerCode,
             @Param("customerName") String customerName,
             @Param("itemCode") String itemCode,
             @Param("itemName") String itemName,
             @Param("trackingNumber") String trackingNumber,
             @Param("labelStatus") String labelStatus,
             @Param("shippingMethod") String shippingMethod,
             @Param("printedBy") String printedBy,
             @Param("startDate") LocalDateTime startDate,
             @Param("endDate") LocalDateTime endDate,
             @Param("startPrintedDate") LocalDateTime startPrintedDate,
             @Param("endPrintedDate") LocalDateTime endPrintedDate,
             @Param("minWeight") Double minWeight,
             @Param("maxWeight") Double maxWeight,
             @Param("minQuantity") Integer minQuantity,
             @Param("maxQuantity") Integer maxQuantity,
             Pageable pageable);

     // ====== SEARCH ======
     @Query("SELECT sl FROM ShippingLabel sl WHERE " +
            "LOWER(sl.labelNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(sl.packageNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(sl.packageBarcode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(sl.soNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(sl.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(sl.customerCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(sl.itemCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(sl.itemName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(sl.trackingNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(sl.labelStatus) LIKE LOWER(CONCAT('%', :search, '%'))")
     Page<ShippingLabel> searchShippingLabels(@Param("search") String search, Pageable pageable);
}