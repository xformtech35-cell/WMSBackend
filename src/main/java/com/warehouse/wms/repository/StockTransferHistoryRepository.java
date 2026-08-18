// ====== FILE: src/main/java/com/warehouse/wms/repository/StockTransferHistoryRepository.java ======
package com.warehouse.wms.repository;

import com.warehouse.wms.entity.StockTransferHistory;
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
public interface StockTransferHistoryRepository extends JpaRepository<StockTransferHistory, Long> {
    
    Optional<StockTransferHistory> findByTransferNumber(String transferNumber);
    
    List<StockTransferHistory> findByItemCode(String itemCode);
    
    List<StockTransferHistory> findBySourceLocationPath(String sourceLocationPath);
    
    List<StockTransferHistory> findByTargetLocationPath(String targetLocationPath);
    
    List<StockTransferHistory> findByBatchNumber(String batchNumber);
    
    List<StockTransferHistory> findByGrnNumber(String grnNumber);
    
    List<StockTransferHistory> findByInventoryNumber(String inventoryNumber);
    
    @Query("SELECT t FROM StockTransferHistory t WHERE " +
            "(:itemCode IS NULL OR t.itemCode LIKE %:itemCode%) AND " +
            "(:sourceLocation IS NULL OR t.sourceLocationPath LIKE %:sourceLocation%) AND " +
            "(:targetLocation IS NULL OR t.targetLocationPath LIKE %:targetLocation%) AND " +
            "(:batchNumber IS NULL OR t.batchNumber LIKE %:batchNumber%) AND " +
            "(:grnNumber IS NULL OR t.grnNumber LIKE %:grnNumber%) AND " +
            "(:inventoryNumber IS NULL OR t.inventoryNumber LIKE %:inventoryNumber%) AND " +
            "(:search IS NULL OR " +
            "   t.transferNumber LIKE %:search% OR " +
            "   t.itemCode LIKE %:search% OR " +
            "   t.itemName LIKE %:search% OR " +
            "   t.inventoryNumber LIKE %:search% OR " +
            "   t.grnNumber LIKE %:search% OR " +
            "   t.batchNumber LIKE %:search% OR " +
            "   t.sourceLocationPath LIKE %:search% OR " +
            "   t.targetLocationPath LIKE %:search%" +
            ") AND " +
            "(:startDate IS NULL OR t.transferDate >= :startDate) AND " +
            "(:endDate IS NULL OR t.transferDate <= :endDate) AND " +
            "(:transferStatus IS NULL OR t.transferStatus = :transferStatus)")
     Page<StockTransferHistory> findByFilters(
         @Param("itemCode") String itemCode,
         @Param("sourceLocation") String sourceLocation,
         @Param("targetLocation") String targetLocation,
         @Param("batchNumber") String batchNumber,
         @Param("grnNumber") String grnNumber,
         @Param("inventoryNumber") String inventoryNumber,
         @Param("search") String search,
         @Param("startDate") LocalDateTime startDate,
         @Param("endDate") LocalDateTime endDate,
         @Param("transferStatus") String transferStatus,
         Pageable pageable
     );
     
}