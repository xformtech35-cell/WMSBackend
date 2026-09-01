package com.warehouse.wms.repository;

import com.warehouse.wms.entity.ReturnSettlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnSettlementRepository extends JpaRepository<ReturnSettlement, Long> {
    
    /**
     * Find settlement by settlement number
     */
    Optional<ReturnSettlement> findBySettlementNumber(String settlementNumber);
    
    /**
     * Find settlements by return order ID
     */
    List<ReturnSettlement> findByReturnOrderId(Long returnOrderId);
    
    /**
     * Find settlements by status
     */
    List<ReturnSettlement> findByStatus(ReturnSettlement.SettlementStatus status);
    
    /**
     * Find settlements by settlement type
     */
    List<ReturnSettlement> findBySettlementType(ReturnSettlement.SettlementType type);
    
    /**
     * Find active settlements for an order
     */
    @Query("SELECT s FROM ReturnSettlement s WHERE s.returnOrder.id = :orderId AND s.status != 'CANCELLED'")
    List<ReturnSettlement> findActiveSettlementsByOrder(@Param("orderId") Long orderId);
    
    /**
     * Count settlements by number prefix for generating sequence
     */
    @Query("SELECT COUNT(s) FROM ReturnSettlement s WHERE s.settlementNumber LIKE CONCAT(:prefix, '%')")
    Long countBySettlementNumberStartingWith(@Param("prefix") String prefix);
    
    /**
     * Find settlements by date range
     */
    List<ReturnSettlement> findBySettlementDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find settlements by credit note number
     */
    Optional<ReturnSettlement> findByCreditNoteNumber(String creditNoteNumber);
    
    /**
     * Find settlements by refund reference
     */
    Optional<ReturnSettlement> findByRefundReference(String refundReference);
    
    /**
     * Count settlements by type
     */
    @Query("SELECT s.settlementType, COUNT(s) FROM ReturnSettlement s WHERE s.status != 'CANCELLED' GROUP BY s.settlementType")
    List<Object[]> countBySettlementType();
    
    /**
     * Find pending settlements
     */
    @Query("SELECT s FROM ReturnSettlement s WHERE s.status IN ('PENDING', 'PROCESSING') ORDER BY s.createdAt ASC")
    List<ReturnSettlement> findPendingSettlements();
    
    /**
     * Get settlement amount summary by order
     */
    @Query("SELECT SUM(s.settlementAmount), SUM(s.creditNoteAmount), SUM(s.refundAmount) " +
           "FROM ReturnSettlement s WHERE s.returnOrder.id = :orderId AND s.status = 'COMPLETED'")
    Object[] getSettlementSummaryByOrder(@Param("orderId") Long orderId);
    
    /**
     * Find settlements by replacement order ID
     */
    List<ReturnSettlement> findByReplacementOrderId(Long replacementOrderId);
    
    /**
     * Count settlements by refund status
     */
    @Query("SELECT s.refundStatus, COUNT(s) FROM ReturnSettlement s WHERE s.settlementType = 'REFUND' AND s.status != 'CANCELLED' GROUP BY s.refundStatus")
    List<Object[]> countByRefundStatus();
    
    /**
     * Find settlements by receipt ID
     */
    List<ReturnSettlement> findByReceiptId(Long receiptId);
}