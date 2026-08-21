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

import com.warehouse.wms.entity.PickTask;

@Repository
public interface PickTaskRepository extends JpaRepository<PickTask, Long> {

    Optional<PickTask> findByPickTaskNumber(String pickTaskNumber);

    List<PickTask> findByPickListNumber(String pickListNumber);

    List<PickTask> findByStatus(String status);

    List<PickTask> findByPickerId(String pickerId);

    @Modifying
    @Transactional
    @Query("UPDATE PickTask pt SET pt.status = :status, pt.pickedQuantity = :pickedQuantity, pt.scanTime = :scanTime, pt.isScanned = true, pt.pickerId = :pickerId, pt.pickerName = :pickerName WHERE pt.pickTaskNumber = :pickTaskNumber")
    void completePickTask(@Param("pickTaskNumber") String pickTaskNumber,
                         @Param("status") String status,
                         @Param("pickedQuantity") Integer pickedQuantity,
                         @Param("scanTime") java.time.LocalDateTime scanTime,
                         @Param("pickerId") String pickerId,
                         @Param("pickerName") String pickerName);

    @Modifying
    @Transactional
    @Query("UPDATE PickTask pt SET pt.pickedQuantity = :pickedQuantity WHERE pt.pickTaskNumber = :pickTaskNumber")
    void updatePickedQuantity(@Param("pickTaskNumber") String pickTaskNumber, @Param("pickedQuantity") Integer pickedQuantity);

    @Query("SELECT COALESCE(SUM(pt.pickedQuantity), 0) FROM PickTask pt WHERE pt.pickListNumber = :pickListNumber")
    Integer getTotalPickedQuantityByPickList(@Param("pickListNumber") String pickListNumber);
    
    
    @Query("SELECT pt FROM PickTask pt WHERE " +
            "(:pickTaskNumber IS NULL OR pt.pickTaskNumber LIKE %:pickTaskNumber%) AND " +
            "(:pickListNumber IS NULL OR pt.pickListNumber LIKE %:pickListNumber%) AND " +
            "(:soNumber IS NULL OR pt.soNumber LIKE %:soNumber%) AND " +
            "(:itemCode IS NULL OR pt.itemCode LIKE %:itemCode%) AND " +
            "(:itemName IS NULL OR LOWER(pt.itemName) LIKE LOWER(CONCAT('%', :itemName, '%'))) AND " +
            "(:status IS NULL OR pt.status = :status) AND " +
            "(:pickerId IS NULL OR pt.pickerId LIKE %:pickerId%) AND " +
            "(:pickerName IS NULL OR LOWER(pt.pickerName) LIKE LOWER(CONCAT('%', :pickerName, '%'))) AND " +
            "(:binId IS NULL OR pt.binId LIKE %:binId%) AND " +
            "(:locationBarcode IS NULL OR pt.locationBarcode LIKE %:locationBarcode%) AND " +
            "(:batchNumber IS NULL OR pt.batchNumber LIKE %:batchNumber%) AND " +
            "(:isScanned IS NULL OR pt.isScanned = :isScanned) AND " +
            "(:startDate IS NULL OR pt.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR pt.createdAt <= :endDate) AND " +
            "(:startScanDate IS NULL OR pt.scanTime >= :startScanDate) AND " +
            "(:endScanDate IS NULL OR pt.scanTime <= :endScanDate) AND " +
            "(:minRequiredQuantity IS NULL OR pt.requiredQuantity >= :minRequiredQuantity) AND " +
            "(:maxRequiredQuantity IS NULL OR pt.requiredQuantity <= :maxRequiredQuantity) AND " +
            "(:minPickedQuantity IS NULL OR pt.pickedQuantity >= :minPickedQuantity) AND " +
            "(:maxPickedQuantity IS NULL OR pt.pickedQuantity <= :maxPickedQuantity) AND " +
            "(:createdBy IS NULL OR pt.createdBy = :createdBy)")
     Page<PickTask> findByFilters(
             @Param("pickTaskNumber") String pickTaskNumber,
             @Param("pickListNumber") String pickListNumber,
             @Param("soNumber") String soNumber,
             @Param("itemCode") String itemCode,
             @Param("itemName") String itemName,
             @Param("status") String status,
             @Param("pickerId") String pickerId,
             @Param("pickerName") String pickerName,
             @Param("binId") String binId,
             @Param("locationBarcode") String locationBarcode,
             @Param("batchNumber") String batchNumber,
             @Param("isScanned") Boolean isScanned,
             @Param("startDate") LocalDateTime startDate,
             @Param("endDate") LocalDateTime endDate,
             @Param("startScanDate") LocalDateTime startScanDate,
             @Param("endScanDate") LocalDateTime endScanDate,
             @Param("minRequiredQuantity") Integer minRequiredQuantity,
             @Param("maxRequiredQuantity") Integer maxRequiredQuantity,
             @Param("minPickedQuantity") Integer minPickedQuantity,
             @Param("maxPickedQuantity") Integer maxPickedQuantity,
             @Param("createdBy") String createdBy,
             Pageable pageable);

     // ====== SEARCH ======
     @Query("SELECT pt FROM PickTask pt WHERE " +
            "LOWER(pt.pickTaskNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(pt.pickListNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(pt.soNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(pt.itemCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(pt.itemName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(pt.status) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(pt.binId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(pt.pickerId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(pt.pickerName) LIKE LOWER(CONCAT('%', :search, '%'))")
     Page<PickTask> searchPickTasks(@Param("search") String search, Pageable pageable);
}