// ====== FILE: src/main/java/com/warehouse/wms/repository/PutawayConfirmationRepository.java ======
package com.warehouse.wms.repository;

import com.warehouse.wms.entity.PutawayConfirmation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PutawayConfirmationRepository extends JpaRepository<PutawayConfirmation, Long> {

    // ====== Basic CRUD Operations ======
    
    /**
     * Find confirmation by confirmation number
     */
    Optional<PutawayConfirmation> findByConfirmationNumber(String confirmationNumber);
    
    /**
     * Find confirmation by task number
     */
    Optional<PutawayConfirmation> findByTaskNumber(String taskNumber);
    
    /**
     * Find confirmation by putaway task ID
     */
    Optional<PutawayConfirmation> findByPutawayTaskId(Long putawayTaskId);
    
    /**
     * Find confirmation by GRN number
     */
    List<PutawayConfirmation> findByGrnNumber(String grnNumber);
    
    /**
     * Find confirmation by bin barcode
     */
    Optional<PutawayConfirmation> findByBinBarcode(String binBarcode);
    
    /**
     * Find confirmation by bin ID
     */
    List<PutawayConfirmation> findByBinId(String binId);
    
    /**
     * Find confirmation by confirmed by user
     */
    List<PutawayConfirmation> findByConfirmedBy(String confirmedBy);
    
    /**
     * Find confirmation by verified status
     */
    List<PutawayConfirmation> findByIsVerified(Boolean isVerified);
    
    /**
     * Find confirmation by inventory updated status
     */
    List<PutawayConfirmation> findByInventoryUpdated(Boolean inventoryUpdated);
    
    
    // ====== Date Range Queries ======
    
    /**
     * Find confirmations between dates
     */
    List<PutawayConfirmation> findByConfirmedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find confirmations created between dates
     */
    List<PutawayConfirmation> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find confirmations by confirmed date
     */
    List<PutawayConfirmation> findByConfirmedAt(LocalDateTime confirmedAt);
    
    /**
     * Find confirmations after a specific date
     */
    List<PutawayConfirmation> findByConfirmedAtAfter(LocalDateTime date);
    
    /**
     * Find confirmations before a specific date
     */
    List<PutawayConfirmation> findByConfirmedAtBefore(LocalDateTime date);
    
    
    // ====== Status Queries ======
    
    /**
     * Find pending inventory updates (inventory not yet updated)
     */
    @Query("SELECT p FROM PutawayConfirmation p WHERE p.inventoryUpdated = false")
    List<PutawayConfirmation> findPendingInventoryUpdates();
    
    /**
     * Find confirmations that need verification
     */
    @Query("SELECT p FROM PutawayConfirmation p WHERE p.isVerified = false")
    List<PutawayConfirmation> findUnverifiedConfirmations();
    
    /**
     * Count confirmed tasks by inventory status
     */
    @Query("SELECT COUNT(p) FROM PutawayConfirmation p WHERE p.inventoryUpdated = :inventoryUpdated")
    Long countByInventoryUpdated(@Param("inventoryUpdated") Boolean inventoryUpdated);
    
    /**
     * Count confirmed tasks by verification status
     */
    @Query("SELECT COUNT(p) FROM PutawayConfirmation p WHERE p.isVerified = :isVerified")
    Long countByIsVerified(@Param("isVerified") Boolean isVerified);
    
    
    // ====== Bin Location Queries ======
    
    /**
     * Find confirmations by bin with time filter
     */
    @Query("SELECT p FROM PutawayConfirmation p WHERE p.binId = :binId AND p.confirmedAt > :since")
    List<PutawayConfirmation> findConfirmationsByBinSince(@Param("binId") String binId,
                                                           @Param("since") LocalDateTime since);
    
    /**
     * Find confirmations by warehouse
     */
    List<PutawayConfirmation> findByWarehouseId(String warehouseId);
    
    /**
     * Find confirmations by zone
     */
    List<PutawayConfirmation> findByZone(String zone);
    
    /**
     * Find confirmations by warehouse and zone
     */
    List<PutawayConfirmation> findByWarehouseIdAndZone(String warehouseId, String zone);
    
    /**
     * Find confirmations by full location
     */
    @Query("SELECT p FROM PutawayConfirmation p WHERE p.warehouseId = :warehouseId " +
           "AND p.zone = :zone AND p.aisle = :aisle AND p.rack = :rack AND p.shelf = :shelf")
    List<PutawayConfirmation> findByFullLocation(@Param("warehouseId") String warehouseId,
                                                  @Param("zone") String zone,
                                                  @Param("aisle") String aisle,
                                                  @Param("rack") String rack,
                                                  @Param("shelf") String shelf);
    
    /**
     * Find distinct bins used in confirmations
     */
    @Query("SELECT DISTINCT p.binId FROM PutawayConfirmation p WHERE p.warehouseId = :warehouseId")
    List<String> findDistinctBinsByWarehouse(@Param("warehouseId") String warehouseId);
    
    
    // ====== Quantity and Summary Queries ======
    
    /**
     * Get total confirmed quantity by task
     */
    @Query("SELECT SUM(p.confirmedQuantity) FROM PutawayConfirmation p WHERE p.putawayTaskId = :taskId")
    Integer getTotalConfirmedQuantityByTask(@Param("taskId") Long taskId);
    
    /**
     * Get total confirmed quantity by GRN
     */
    @Query("SELECT SUM(p.confirmedQuantity) FROM PutawayConfirmation p WHERE p.grnNumber = :grnNumber")
    Integer getTotalConfirmedQuantityByGrn(@Param("grnNumber") String grnNumber);
    
    /**
     * Get total confirmed quantity by warehouse
     */
    @Query("SELECT SUM(p.confirmedQuantity) FROM PutawayConfirmation p WHERE p.warehouseId = :warehouseId")
    Integer getTotalConfirmedQuantityByWarehouse(@Param("warehouseId") String warehouseId);
    
    /**
     * Get total confirmed quantity by bin
     */
    @Query("SELECT SUM(p.confirmedQuantity) FROM PutawayConfirmation p WHERE p.binId = :binId")
    Integer getTotalConfirmedQuantityByBin(@Param("binId") String binId);
    
    /**
     * Get confirmation summary by warehouse
     */
    @Query("SELECT p.warehouseId, COUNT(p), SUM(p.confirmedQuantity), " +
           "SUM(CASE WHEN p.inventoryUpdated = true THEN 1 ELSE 0 END) " +
           "FROM PutawayConfirmation p GROUP BY p.warehouseId")
    List<Object[]> getConfirmationSummaryByWarehouse();
    
    /**
     * Get confirmation summary by date range
     */
    @Query("SELECT DATE(p.confirmedAt), COUNT(p), SUM(p.confirmedQuantity) " +
           "FROM PutawayConfirmation p " +
           "WHERE p.confirmedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(p.confirmedAt)")
    List<Object[]> getConfirmationSummaryByDate(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);
    
    
    // ====== Pagination Queries ======
    
    /**
     * Find confirmations with pagination
     */
    Page<PutawayConfirmation> findAll(Pageable pageable);
    
    /**
     * Find confirmations by verified status with pagination
     */
    Page<PutawayConfirmation> findByIsVerified(Boolean isVerified, Pageable pageable);
    
    /**
     * Find confirmations by inventory updated status with pagination
     */
    Page<PutawayConfirmation> findByInventoryUpdated(Boolean inventoryUpdated, Pageable pageable);
    
    /**
     * Find confirmations by warehouse with pagination
     */
    Page<PutawayConfirmation> findByWarehouseId(String warehouseId, Pageable pageable);
    
    /**
     * Find confirmations by bin with pagination
     */
    Page<PutawayConfirmation> findByBinId(String binId, Pageable pageable);
    
    /**
     * Find confirmations by date range with pagination
     */
    Page<PutawayConfirmation> findByConfirmedAtBetween(LocalDateTime startDate, 
                                                        LocalDateTime endDate, 
                                                        Pageable pageable);
    
    
    // ====== Update Operations ======
    
    /**
     * Update inventory status for confirmation
     */
    @Modifying
    @Transactional
    @Query("UPDATE PutawayConfirmation p SET p.inventoryUpdated = true, " +
           "p.inventoryUpdatedAt = :updatedAt, p.inventoryUpdateId = :updateId " +
           "WHERE p.id = :id")
    int markInventoryUpdated(@Param("id") Long id,
                             @Param("updatedAt") LocalDateTime updatedAt,
                             @Param("updateId") Long updateId);
    
    /**
     * Update verification status for confirmation
     */
    @Modifying
    @Transactional
    @Query("UPDATE PutawayConfirmation p SET p.isVerified = :isVerified, " +
           "p.verifiedBy = :verifiedBy, p.verifiedAt = :verifiedAt " +
           "WHERE p.id = :id")
    int updateVerificationStatus(@Param("id") Long id,
                                 @Param("isVerified") Boolean isVerified,
                                 @Param("verifiedBy") String verifiedBy,
                                 @Param("verifiedAt") LocalDateTime verifiedAt);
    
    /**
     * Update bin details for confirmation
     */
    @Modifying
    @Transactional
    @Query("UPDATE PutawayConfirmation p SET p.binId = :binId, " +
           "p.binBarcode = :binBarcode, p.zone = :zone, p.aisle = :aisle, " +
           "p.rack = :rack, p.shelf = :shelf WHERE p.id = :id")
    int updateBinDetails(@Param("id") Long id,
                         @Param("binId") String binId,
                         @Param("binBarcode") String binBarcode,
                         @Param("zone") String zone,
                         @Param("aisle") String aisle,
                         @Param("rack") String rack,
                         @Param("shelf") String shelf);
    
    /**
     * Update remarks for confirmation
     */
    @Modifying
    @Transactional
    @Query("UPDATE PutawayConfirmation p SET p.remarks = :remarks WHERE p.id = :id")
    int updateRemarks(@Param("id") Long id, @Param("remarks") String remarks);
    
    
    // ====== Existence Checks ======
    
    /**
     * Check if confirmation exists by number
     */
    boolean existsByConfirmationNumber(String confirmationNumber);
    
    /**
     * Check if confirmation exists by task number
     */
    boolean existsByTaskNumber(String taskNumber);
    
    /**
     * Check if confirmation exists by putaway task ID
     */
    boolean existsByPutawayTaskId(Long putawayTaskId);
    
    /**
     * Check if confirmation exists by GRN number
     */
    boolean existsByGrnNumber(String grnNumber);
    
    
    // ====== Advanced Search Queries ======
    
    /**
     * Search confirmations by multiple criteria
     */
    @Query("SELECT p FROM PutawayConfirmation p WHERE " +
           "(:confirmationNumber IS NULL OR p.confirmationNumber = :confirmationNumber) AND " +
           "(:taskNumber IS NULL OR p.taskNumber = :taskNumber) AND " +
           "(:grnNumber IS NULL OR p.grnNumber = :grnNumber) AND " +
           "(:warehouseId IS NULL OR p.warehouseId = :warehouseId) AND " +
           "(:binId IS NULL OR p.binId = :binId) AND " +
           "(:confirmedBy IS NULL OR p.confirmedBy = :confirmedBy) AND " +
           "(:inventoryUpdated IS NULL OR p.inventoryUpdated = :inventoryUpdated) AND " +
           "(:isVerified IS NULL OR p.isVerified = :isVerified) AND " +
           "(:startDate IS NULL OR p.confirmedAt >= :startDate) AND " +
           "(:endDate IS NULL OR p.confirmedAt <= :endDate)")
    List<PutawayConfirmation> searchConfirmations(@Param("confirmationNumber") String confirmationNumber,
                                                   @Param("taskNumber") String taskNumber,
                                                   @Param("grnNumber") String grnNumber,
                                                   @Param("warehouseId") String warehouseId,
                                                   @Param("binId") String binId,
                                                   @Param("confirmedBy") String confirmedBy,
                                                   @Param("inventoryUpdated") Boolean inventoryUpdated,
                                                   @Param("isVerified") Boolean isVerified,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);
    
    /**
     * Search confirmations with pagination
     */
    @Query("SELECT p FROM PutawayConfirmation p WHERE " +
           "(:confirmationNumber IS NULL OR p.confirmationNumber LIKE %:confirmationNumber%) AND " +
           "(:taskNumber IS NULL OR p.taskNumber LIKE %:taskNumber%) AND " +
           "(:grnNumber IS NULL OR p.grnNumber LIKE %:grnNumber%) AND " +
           "(:warehouseId IS NULL OR p.warehouseId = :warehouseId)")
    Page<PutawayConfirmation> searchConfirmationsWithPagination(
            @Param("confirmationNumber") String confirmationNumber,
            @Param("taskNumber") String taskNumber,
            @Param("grnNumber") String grnNumber,
            @Param("warehouseId") String warehouseId,
            Pageable pageable);
    
    
    // ====== Statistics Queries ======
    
    /**
     * Get confirmation count by day
     */
    @Query("SELECT DATE(p.confirmedAt), COUNT(p), SUM(p.confirmedQuantity) " +
           "FROM PutawayConfirmation p GROUP BY DATE(p.confirmedAt)")
    List<Object[]> getDailyConfirmationStats();
    
    /**
     * Get confirmation count by month
     */
    @Query("SELECT YEAR(p.confirmedAt), MONTH(p.confirmedAt), COUNT(p), SUM(p.confirmedQuantity) " +
           "FROM PutawayConfirmation p GROUP BY YEAR(p.confirmedAt), MONTH(p.confirmedAt)")
    List<Object[]> getMonthlyConfirmationStats();
    
    /**
     * Get confirmation count by year
     */
    @Query("SELECT YEAR(p.confirmedAt), COUNT(p), SUM(p.confirmedQuantity) " +
           "FROM PutawayConfirmation p GROUP BY YEAR(p.confirmedAt)")
    List<Object[]> getYearlyConfirmationStats();
    
    /**
     * Get top bins by confirmed quantity
     */
    @Query("SELECT p.binId, COUNT(p), SUM(p.confirmedQuantity) " +
           "FROM PutawayConfirmation p " +
           "GROUP BY p.binId ORDER BY SUM(p.confirmedQuantity) DESC")
    List<Object[]> getTopBinsByConfirmedQuantity();
    
    /**
     * Get top users by confirmation count
     */
    @Query("SELECT p.confirmedBy, COUNT(p), SUM(p.confirmedQuantity) " +
           "FROM PutawayConfirmation p " +
           "WHERE p.confirmedBy IS NOT NULL " +
           "GROUP BY p.confirmedBy ORDER BY COUNT(p) DESC")
    List<Object[]> getTopUsersByConfirmationCount();
    
    
    // ====== Bulk Operations ======
    
    /**
     * Delete confirmations older than a date
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PutawayConfirmation p WHERE p.createdAt < :date")
    int deleteOldConfirmations(@Param("date") LocalDateTime date);
    
    /**
     * Delete confirmations by confirmation number list
     */
    @Modifying
    @Transactional
    void deleteByConfirmationNumberIn(List<String> confirmationNumbers);
    
    /**
     * Update inventory status for multiple confirmations
     */
    @Modifying
    @Transactional
    @Query("UPDATE PutawayConfirmation p SET p.inventoryUpdated = true, " +
           "p.inventoryUpdatedAt = :updatedAt, p.inventoryUpdateId = :updateId " +
           "WHERE p.id IN :ids")
    int bulkMarkInventoryUpdated(@Param("ids") List<Long> ids,
                                  @Param("updatedAt") LocalDateTime updatedAt,
                                  @Param("updateId") Long updateId);
    
    
    // ====== Native SQL Queries ======
    
    /**
     * Get confirmation report using native query
     */
    @Query(value = "SELECT pc.*, pt.task_number, pt.assigned_to " +
                   "FROM wms_putaway_confirmations pc " +
                   "JOIN wms_putaway_tasks pt ON pc.putaway_task_id = pt.id " +
                   "WHERE pc.confirmed_at BETWEEN ?1 AND ?2 " +
                   "ORDER BY pc.confirmed_at DESC", 
           nativeQuery = true)
    List<Object[]> getConfirmationReport(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get warehouse performance report using native query
     */
    @Query(value = "SELECT warehouse_id, COUNT(*) as total_confirmations, " +
                   "SUM(confirmed_quantity) as total_quantity, " +
                   "AVG(TIMESTAMPDIFF(HOUR, created_at, confirmed_at)) as avg_hours " +
                   "FROM wms_putaway_confirmations " +
                   "GROUP BY warehouse_id " +
                   "ORDER BY total_quantity DESC", 
           nativeQuery = true)
    List<Object[]> getWarehousePerformanceReport();
}