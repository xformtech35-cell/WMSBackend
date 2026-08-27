package com.warehouse.wms.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.warehouse.wms.entity.GoodsReceipt;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {
    Optional<GoodsReceipt> findByGrnNo(String grnNo);
    
    @Query("SELECT COUNT(g) FROM GoodsReceipt g WHERE g.status = :status")
    long countByStatus(@Param("status") String status);
    
    @Query("SELECT COUNT(g) FROM GoodsReceipt g WHERE g.status = :status AND g.createdAt BETWEEN :startDate AND :endDate")
    long countByStatusAndDateRange(@Param("status") String status, 
                                   @Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);
    
    // Count by created date
    @Query("SELECT COUNT(g) FROM GoodsReceipt g WHERE g.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedDateBetween(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT g.supplierName, COUNT(g) FROM GoodsReceipt g WHERE g.createdAt BETWEEN :startDate AND :endDate GROUP BY g.supplierName ORDER BY COUNT(g) DESC")
    List<Object[]> findTopSuppliers(@Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate);
    
    // Get top supplier
    @Query("SELECT g.supplierName, COUNT(g) FROM GoodsReceipt g GROUP BY g.supplierName ORDER BY COUNT(g) DESC")
    List<Object[]> findTopSupplier();
    
    
    @Query("SELECT g FROM GoodsReceipt g ORDER BY g.createdAt DESC")
    List<GoodsReceipt> findTop10ByOrderByCreatedAtDesc();
}
