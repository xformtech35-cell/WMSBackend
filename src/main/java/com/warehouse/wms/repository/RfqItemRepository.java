package com.warehouse.wms.repository;

import com.warehouse.wms.entity.RfqItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RfqItemRepository extends JpaRepository<RfqItem, Long> {
    
    List<RfqItem> findByRfqId(Long rfqId);
    
    List<RfqItem> findByPurchaseRequestItemId(Long purchaseRequestItemId);
    
    @Query("SELECT ri FROM RfqItem ri WHERE ri.rfq.id = :rfqId AND ri.itemCode = :itemCode")
    RfqItem findByRfqIdAndItemCode(@Param("rfqId") Long rfqId, @Param("itemCode") String itemCode);
}