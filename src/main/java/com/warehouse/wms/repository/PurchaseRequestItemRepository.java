package com.warehouse.wms.repository;

import com.warehouse.wms.entity.PurchaseRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRequestItemRepository extends JpaRepository<PurchaseRequestItem, Long> {

    List<PurchaseRequestItem> findByPurchaseRequestId(Long purchaseRequestId);

    void deleteByPurchaseRequestId(Long purchaseRequestId);

    // Add this
    Optional<PurchaseRequestItem> findByItemBarcode(String itemBarcode);
}