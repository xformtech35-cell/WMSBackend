package com.warehouse.wms.repository;

import com.warehouse.wms.entity.MovementLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovementLogRepository extends JpaRepository<MovementLog, Long> {
    List<MovementLog> findByInventoryBatchNo(String batchNo);
    List<MovementLog> findByInventorySerialNo(String serialNo);
    List<MovementLog> findByInventoryId(Long inventoryId);
}
