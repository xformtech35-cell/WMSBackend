package com.warehouse.wms.repository;

import com.warehouse.wms.entity.PickTaskItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PickTaskItemRepository
        extends JpaRepository<PickTaskItem, Long>, JpaSpecificationExecutor<PickTaskItem> {

    // ================================================================
    // BASIC LOOKUPS
    // ================================================================

    /** All items for a given pick task (by numeric id) */
    List<PickTaskItem> findByPickTaskId(Long pickTaskId);

    /** All items for a given pick task (by task number) */
    List<PickTaskItem> findByPickTaskPickTaskNumber(String pickTaskNumber);

    /** All items for a given pick list number */
    List<PickTaskItem> findByPickTaskPickListNumber(String pickListNumber);

    /** All items for a given item code */
    List<PickTaskItem> findByItemCode(String itemCode);

    /** Find specific item in a task */
    Optional<PickTaskItem> findByPickTaskIdAndItemCode(Long pickTaskId, String itemCode);

    /** All items for a task by status */
    List<PickTaskItem> findByPickTaskIdAndStatus(Long pickTaskId, String status);

    /** All items by status (global) */
    List<PickTaskItem> findByStatus(String status);

    /** All unscanned items for a task */
    List<PickTaskItem> findByPickTaskIdAndIsScannedFalse(Long pickTaskId);

    /** All scanned items for a task */
    List<PickTaskItem> findByPickTaskIdAndIsScannedTrue(Long pickTaskId);

    /** Items by picker (via parent task) */
    List<PickTaskItem> findByPickTaskAssignedTo(String assignedTo);

    /** Items by bin */
    List<PickTaskItem> findByBinId(String binId);

    /** Items by batch number */
    List<PickTaskItem> findByBatchNumber(String batchNumber);

    /** Items by barcode */
    Optional<PickTaskItem> findByItemBarcode(String itemBarcode);

    // ================================================================
    // EXISTS / COUNT
    // ================================================================

    boolean existsByPickTaskIdAndItemCode(Long pickTaskId, String itemCode);

    boolean existsByPickTaskId(Long pickTaskId);

    long countByPickTaskId(Long pickTaskId);

    long countByStatus(String status);

    long countByPickTaskIdAndStatus(Long pickTaskId, String status);

    long countByPickTaskIdAndIsScannedTrue(Long pickTaskId);

    long countByPickTaskIdAndIsScannedFalse(Long pickTaskId);

    // ================================================================
    // AGGREGATES (useful for progress %)
    // ================================================================

    /** Sum of required quantity for a task */
    @Query("SELECT COALESCE(SUM(i.requiredQuantity), 0) FROM PickTaskItem i WHERE i.pickTask.id = :pickTaskId")
    Integer sumRequiredQuantityByPickTaskId(@Param("pickTaskId") Long pickTaskId);

    /** Sum of picked quantity for a task */
    @Query("SELECT COALESCE(SUM(i.pickedQuantity), 0) FROM PickTaskItem i WHERE i.pickTask.id = :pickTaskId")
    Integer sumPickedQuantityByPickTaskId(@Param("pickTaskId") Long pickTaskId);

    /** Sum of short quantity for a task */
    @Query("SELECT COALESCE(SUM(i.shortQuantity), 0) FROM PickTaskItem i WHERE i.pickTask.id = :pickTaskId")
    Integer sumShortQuantityByPickTaskId(@Param("pickTaskId") Long pickTaskId);

    // ================================================================
    // SEARCH (across item code / name / barcode / bin / batch)
    // ================================================================

    @Query("""
        SELECT i FROM PickTaskItem i
        WHERE LOWER(i.itemCode)  LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(i.itemName)  LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(i.itemBarcode) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(i.binId)     LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(i.batchNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<PickTaskItem> searchItems(@Param("keyword") String keyword, Pageable pageable);

    // ================================================================
    // FILTER QUERY (all optional)
    // ================================================================

    @Query("""
        SELECT i FROM PickTaskItem i
        WHERE (:pickTaskId IS NULL OR i.pickTask.id = :pickTaskId)
          AND (:pickListNumber IS NULL OR i.pickTask.pickListNumber = :pickListNumber)
          AND (:itemCode IS NULL OR i.itemCode = :itemCode)
          AND (:status IS NULL OR i.status = :status)
          AND (:binId IS NULL OR i.binId = :binId)
          AND (:batchNumber IS NULL OR i.batchNumber = :batchNumber)
          AND (:isScanned IS NULL OR i.isScanned = :isScanned)
          AND (:startDate IS NULL OR i.createdAt >= :startDate)
          AND (:endDate IS NULL OR i.createdAt <= :endDate)
    """)
    Page<PickTaskItem> findByFilters(
            @Param("pickTaskId") Long pickTaskId,
            @Param("pickListNumber") String pickListNumber,
            @Param("itemCode") String itemCode,
            @Param("status") String status,
            @Param("binId") String binId,
            @Param("batchNumber") String batchNumber,
            @Param("isScanned") Boolean isScanned,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // ================================================================
    // DELETE HELPERS
    // ================================================================

    void deleteByPickTaskId(Long pickTaskId);

    void deleteByPickTaskPickTaskNumber(String pickTaskNumber);
}