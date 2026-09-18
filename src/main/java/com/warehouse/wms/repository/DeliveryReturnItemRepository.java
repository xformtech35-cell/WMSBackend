package com.warehouse.wms.repository;

import com.warehouse.wms.entity.DeliveryReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryReturnItemRepository extends JpaRepository<DeliveryReturnItem, Long> {

    List<DeliveryReturnItem> findByDeliveryReturnId(Long returnId);
}