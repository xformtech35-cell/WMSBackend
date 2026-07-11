package com.warehouse.wms.scheduler;

import com.warehouse.wms.entity.StockBatch;
import com.warehouse.wms.repository.StockBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiryMonitoringJob {

    private final StockBatchRepository stockBatchRepository;

    @Value("${app.expiry.threshold-days:30}")
    private int thresholdDays;

    @Scheduled(cron = "0 0 1 * * ?") // Daily at 1:00 AM
    @Transactional
    public void monitorExpiries() {
        log.info("[ExpiryMonitoringJob] Starting expiry monitoring scan...");
        LocalDateTime now = LocalDateTime.now();

        // 1. Mark expired
        List<StockBatch> expiredBatches = stockBatchRepository.findByExpiryDateBeforeAndStatusNot(
                now, StockBatch.BatchStatus.EXPIRED
        );
        for (StockBatch batch : expiredBatches) {
            if (batch.getStatus() != StockBatch.BatchStatus.QUARANTINED) {
                batch.setStatus(StockBatch.BatchStatus.EXPIRED);
                stockBatchRepository.save(batch);
                log.info("[ExpiryMonitoringJob] Batch {} marked as EXPIRED", batch.getBatchNumber());
            }
        }

        // 2. Mark near expiry
        LocalDateTime warningThreshold = now.plusDays(thresholdDays);
        List<StockBatch> nearExpiryBatches = stockBatchRepository.findByExpiryDateBeforeAndStatusNot(
                warningThreshold, StockBatch.BatchStatus.NEAR_EXPIRY
        );
        for (StockBatch batch : nearExpiryBatches) {
            if (batch.getExpiryDate().isAfter(now) && batch.getStatus() == StockBatch.BatchStatus.ACTIVE) {
                batch.setStatus(StockBatch.BatchStatus.NEAR_EXPIRY);
                stockBatchRepository.save(batch);
                log.info("[ExpiryMonitoringJob] Batch {} marked as NEAR_EXPIRY", batch.getBatchNumber());
            }
        }
        log.info("[ExpiryMonitoringJob] Expiry monitoring scan complete.");
    }
}
