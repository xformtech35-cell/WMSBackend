package com.warehouse.wms.repository;

import com.warehouse.wms.entity.StockAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockAdjustmentRepository extends JpaRepository<StockAdjustment, Long> {
    List<StockAdjustment> findBySkuId(Long skuId);
}
