package com.warehouse.wms.repository;

import com.warehouse.wms.entity.PickTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PickTaskRepository
        extends JpaRepository<PickTask, Long>, JpaSpecificationExecutor<PickTask> {

    Optional<PickTask> findByPickTaskNumber(String pickTaskNumber);

    boolean existsByPickTaskNumber(String pickTaskNumber);

    List<PickTask> findByPickListNumber(String pickListNumber);

    List<PickTask> findByStatus(String status);

    List<PickTask> findByAssignedTo(String assignedTo);

    long countByStatus(String status);

    // ================================================================
    // TOTAL PICKED QUANTITY PER PICK LIST — via items
    // ================================================================
    @Query("""
        SELECT COALESCE(SUM(i.pickedQuantity), 0)
        FROM PickTask pt
        JOIN pt.items i
        WHERE pt.pickListNumber = :pickListNumber
    """)
    Integer getTotalPickedQuantityByPickList(@Param("pickListNumber") String pickListNumber);

    // ================================================================
    // SEARCH — JOIN through items
    // ================================================================
    @Query("""
        SELECT DISTINCT pt FROM PickTask pt
        LEFT JOIN pt.items i
        WHERE LOWER(pt.pickTaskNumber) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(pt.pickListNumber) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(pt.soNumber)       LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(pt.status)         LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(pt.assignedTo)     LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(i.itemCode)        LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(i.itemName)        LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(i.binId)           LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(i.batchNumber)     LIKE LOWER(CONCAT('%', :search, '%'))
    """)
    Page<PickTask> searchPickTasks(@Param("search") String search, Pageable pageable);

    // ================================================================
    // FILTERS — JOIN through items
    // ================================================================
    @Query("""
        SELECT DISTINCT pt FROM PickTask pt
        LEFT JOIN pt.items i
        WHERE (:pickTaskNumber IS NULL OR pt.pickTaskNumber LIKE %:pickTaskNumber%)
          AND (:pickListNumber IS NULL OR pt.pickListNumber LIKE %:pickListNumber%)
          AND (:soNumber       IS NULL OR pt.soNumber       LIKE %:soNumber%)
          AND (:status         IS NULL OR pt.status         = :status)
          AND (:pickerId       IS NULL OR pt.assignedTo     = :pickerId)
          AND (:pickerName     IS NULL OR LOWER(i.updatedBy) LIKE LOWER(CONCAT('%', :pickerName, '%')))
          AND (:itemCode       IS NULL OR i.itemCode        = :itemCode)
          AND (:itemName       IS NULL OR LOWER(i.itemName) LIKE LOWER(CONCAT('%', :itemName, '%')))
          AND (:binId          IS NULL OR i.binId           = :binId)
          AND (:locationBarcode IS NULL OR i.locationBarcode = :locationBarcode)
          AND (:batchNumber    IS NULL OR i.batchNumber     = :batchNumber)
          AND (:isScanned      IS NULL OR i.isScanned       = :isScanned)
          AND (:createdBy      IS NULL OR pt.createdBy      = :createdBy)
          AND (:startDate      IS NULL OR pt.createdAt      >= :startDate)
          AND (:endDate        IS NULL OR pt.createdAt      <= :endDate)
          AND (:startScanDate  IS NULL OR i.scanTime        >= :startScanDate)
          AND (:endScanDate    IS NULL OR i.scanTime        <= :endScanDate)
          AND (:minRequiredQuantity IS NULL OR i.requiredQuantity >= :minRequiredQuantity)
          AND (:maxRequiredQuantity IS NULL OR i.requiredQuantity <= :maxRequiredQuantity)
          AND (:minPickedQuantity   IS NULL OR i.pickedQuantity   >= :minPickedQuantity)
          AND (:maxPickedQuantity   IS NULL OR i.pickedQuantity   <= :maxPickedQuantity)
    """)
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

    // ================================================================
    // AGGREGATES — via items
    // ================================================================
    @Query("SELECT COALESCE(SUM(i.pickedQuantity), 0) FROM PickTask pt JOIN pt.items i")
    Long sumPickedQuantity();

    @Query("SELECT COALESCE(SUM(i.requiredQuantity), 0) FROM PickTask pt JOIN pt.items i")
    Long sumRequiredQuantity();

    @Query("""
        SELECT pt.assignedTo, SUM(i.pickedQuantity)
        FROM PickTask pt
        JOIN pt.items i
        WHERE pt.assignedTo IS NOT NULL
        GROUP BY pt.assignedTo
        ORDER BY SUM(i.pickedQuantity) DESC
    """)
    List<Object[]> findTopPerformer();

    // ================================================================
    // UPDATE HELPERS
    // ================================================================
    @Modifying
    @Transactional
    @Query("""
        UPDATE PickTask pt
        SET pt.status = :status
        WHERE pt.pickTaskNumber = :pickTaskNumber
    """)
    void updateStatus(
            @Param("pickTaskNumber") String pickTaskNumber,
            @Param("status") String status);

    @Modifying
    @Transactional
    @Query("""
        UPDATE PickTask pt
        SET pt.status = :status,
            pt.updatedBy = :updatedBy,
            pt.completedDate = :completedDate
        WHERE pt.pickTaskNumber = :pickTaskNumber
    """)
    void completePickTask(
            @Param("pickTaskNumber") String pickTaskNumber,
            @Param("status") String status,
            @Param("updatedBy") String updatedBy,
            @Param("completedDate") LocalDateTime completedDate);
}