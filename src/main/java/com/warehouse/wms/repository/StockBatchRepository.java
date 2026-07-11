package com.warehouse.wms.repository;

import com.warehouse.wms.entity.StockBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StockBatchRepository extends JpaRepository<StockBatch, Long> {
    List<StockBatch> findBySkuId(Long skuId);
    List<StockBatch> findBySkuIdAndStatus(Long skuId, StockBatch.BatchStatus status);
    Optional<StockBatch> findBySkuIdAndBinIdAndBatchNumber(Long skuId, Long binId, String batchNumber);
    List<StockBatch> findByExpiryDateBeforeAndStatusNot(LocalDateTime expiryDate, StockBatch.BatchStatus status);
    List<StockBatch> findByBatchNumber(String batchNumber);
}
