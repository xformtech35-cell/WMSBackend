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

import com.warehouse.wms.entity.PackageInfo;

@Repository
public interface PackageInfoRepository extends JpaRepository<PackageInfo, Long> {

    Optional<PackageInfo> findByPackageNumber(String packageNumber);

    Optional<PackageInfo> findByPackageBarcode(String packageBarcode);

    List<PackageInfo> findBySoNumber(String soNumber);

    List<PackageInfo> findByStatus(String status);

    @Modifying
    @Transactional
    @Query("UPDATE PackageInfo p SET p.status = :status WHERE p.packageNumber = :packageNumber")
    void updateStatus(@Param("packageNumber") String packageNumber, @Param("status") String status);

    @Query("SELECT COUNT(p) FROM PackageInfo p WHERE p.soNumber = :soNumber")
    Integer getPackageCountBySoNumber(@Param("soNumber") String soNumber);
    
    @Query("SELECT p FROM PackageInfo p WHERE " +
            "(:packageNumber IS NULL OR p.packageNumber LIKE %:packageNumber%) AND " +
            "(:packageBarcode IS NULL OR p.packageBarcode LIKE %:packageBarcode%) AND " +
            "(:soNumber IS NULL OR p.soNumber LIKE %:soNumber%) AND " +
            "(:pickListNumber IS NULL OR p.pickListNumber LIKE %:pickListNumber%) AND " +
            "(:itemCode IS NULL OR p.itemCode LIKE %:itemCode%) AND " +
            "(:itemName IS NULL OR LOWER(p.itemName) LIKE LOWER(CONCAT('%', :itemName, '%'))) AND " +
            "(:packageType IS NULL OR p.packageType = :packageType) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:packedBy IS NULL OR LOWER(p.packedBy) LIKE LOWER(CONCAT('%', :packedBy, '%'))) AND " +
            "(:startDate IS NULL OR p.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR p.createdAt <= :endDate) AND " +
            "(:startPackedDate IS NULL OR p.packedDate >= :startPackedDate) AND " +
            "(:endPackedDate IS NULL OR p.packedDate <= :endPackedDate) AND " +
            "(:minWeight IS NULL OR p.weight >= :minWeight) AND " +
            "(:maxWeight IS NULL OR p.weight <= :maxWeight) AND " +
            "(:minQuantity IS NULL OR p.packedQuantity >= :minQuantity) AND " +
            "(:maxQuantity IS NULL OR p.packedQuantity <= :maxQuantity)")
     Page<PackageInfo> findByFilters(
             @Param("packageNumber") String packageNumber,
             @Param("packageBarcode") String packageBarcode,
             @Param("soNumber") String soNumber,
             @Param("pickListNumber") String pickListNumber,
             @Param("itemCode") String itemCode,
             @Param("itemName") String itemName,
             @Param("packageType") String packageType,
             @Param("status") String status,
             @Param("packedBy") String packedBy,
             @Param("startDate") LocalDateTime startDate,
             @Param("endDate") LocalDateTime endDate,
             @Param("startPackedDate") LocalDateTime startPackedDate,
             @Param("endPackedDate") LocalDateTime endPackedDate,
             @Param("minWeight") Double minWeight,
             @Param("maxWeight") Double maxWeight,
             @Param("minQuantity") Integer minQuantity,
             @Param("maxQuantity") Integer maxQuantity,
             Pageable pageable);

     // ====== SEARCH ======
     @Query("SELECT p FROM PackageInfo p WHERE " +
            "LOWER(p.packageNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.packageBarcode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.soNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.pickListNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.itemCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.itemName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.packageType) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.status) LIKE LOWER(CONCAT('%', :search, '%'))")
     Page<PackageInfo> searchPackages(@Param("search") String search, Pageable pageable);
}