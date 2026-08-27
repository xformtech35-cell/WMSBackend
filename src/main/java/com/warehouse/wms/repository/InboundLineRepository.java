package com.warehouse.wms.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.warehouse.wms.entity.InboundLine;

@Repository
public interface InboundLineRepository extends JpaRepository<InboundLine, Long> {
    
    List<InboundLine> findByInboundId(Long inboundId);
    
    List<InboundLine> findByInboundIdAndQualityStatus(Long inboundId, String qualityStatus);

    
    
    // ✅ Sum of received quantity with LocalDate
    @Query("SELECT COALESCE(SUM(il.receivedQuantity), 0) " +
           "FROM InboundLine il " +
           "JOIN il.inbound i " +
           "WHERE i.inboundDate BETWEEN :startDate AND :endDate")
    Long sumReceivedQuantityByDateRange(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);
    
    // ✅ Sum of total quantity (weight) with LocalDate
    @Query("SELECT COALESCE(SUM(il.totalQuantity), 0.0) " +
           "FROM InboundLine il " +
           "JOIN il.inbound i " +
           "WHERE i.inboundDate BETWEEN :startDate AND :endDate")
    Double sumWeightByDateRange(@Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);
    
    // ✅ Sum of total quantity (volume) with LocalDate
    @Query("SELECT COALESCE(SUM(il.totalQuantity), 0.0) " +
           "FROM InboundLine il " +
           "JOIN il.inbound i " +
           "WHERE i.inboundDate BETWEEN :startDate AND :endDate")
    Double sumVolumeByDateRange(@Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);
}