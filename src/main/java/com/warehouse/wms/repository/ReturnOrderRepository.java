package com.warehouse.wms.repository;

import com.warehouse.wms.entity.ReturnOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReturnOrderRepository extends JpaRepository<ReturnOrder, Long> {
}
