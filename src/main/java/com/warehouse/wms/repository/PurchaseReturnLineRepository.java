package com.warehouse.wms.repository;

import com.warehouse.wms.entity.PurchaseReturnLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseReturnLineRepository extends JpaRepository<PurchaseReturnLine, Long> {
    
    /**
     * Find lines by purchase return ID
     */
    List<PurchaseReturnLine> findByPurchaseReturnId(Long purchaseReturnId);
    
    /**
     * Find a line with its purchase return loaded
     */
    @Query("SELECT l FROM PurchaseReturnLine l LEFT JOIN FETCH l.purchaseReturn WHERE l.id = :lineId")
    Optional<PurchaseReturnLine> findByIdWithPurchaseReturn(@Param("lineId") Long lineId);
    
    /**
     * Count lines by purchase return ID
     */
    Long countByPurchaseReturnId(Long purchaseReturnId);
    
    /**
     * Delete all lines by purchase return ID
     */
    void deleteByPurchaseReturnId(Long purchaseReturnId);
}