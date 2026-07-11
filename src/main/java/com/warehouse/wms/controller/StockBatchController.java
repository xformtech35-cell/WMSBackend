package com.warehouse.wms.controller;

import com.warehouse.wms.entity.Inventory;
import com.warehouse.wms.entity.MovementLog;
import com.warehouse.wms.entity.StockBatch;
import com.warehouse.wms.repository.InventoryRepository;
import com.warehouse.wms.repository.MovementLogRepository;
import com.warehouse.wms.repository.StockBatchRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/batches")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('INVENTORY_VIEW')")
public class StockBatchController {

    private final StockBatchRepository stockBatchRepository;
    private final InventoryRepository inventoryRepository;
    private final MovementLogRepository movementLogRepository;

    @GetMapping("/sku/{skuId}")
    public ResponseEntity<List<StockBatch>> getBySku(@PathVariable Long skuId) {
        return ResponseEntity.ok(stockBatchRepository.findBySkuId(skuId));
    }

    @GetMapping("/expiry-watchlist")
    public ResponseEntity<List<StockBatch>> getExpiryWatchlist() {
        // Find all NEAR_EXPIRY or EXPIRED stock batches
        List<StockBatch> all = stockBatchRepository.findAll();
        List<StockBatch> filtered = all.stream()
                .filter(b -> b.getStatus() == StockBatch.BatchStatus.NEAR_EXPIRY || b.getStatus() == StockBatch.BatchStatus.EXPIRED)
                .collect(Collectors.toList());
        return ResponseEntity.ok(filtered);
    }

    @PostMapping("/{id}/quarantine")
    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
    public ResponseEntity<StockBatch> quarantineBatch(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        StockBatch batch = stockBatchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Stock batch not found: " + id));

        boolean quarantine = body.getOrDefault("quarantine", true);
        batch.setStatus(quarantine ? StockBatch.BatchStatus.QUARANTINED : StockBatch.BatchStatus.ACTIVE);
        StockBatch saved = stockBatchRepository.save(batch);

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/trace/{batchNumber}")
    public ResponseEntity<BatchTraceResponse> traceBatch(@PathVariable String batchNumber) {
        // Find all inventory items belonging to this batch
        List<StockBatch> batches = stockBatchRepository.findByBatchNumber(batchNumber);
        
        // Find movement logs that reference this batch
        List<MovementLog> logs = movementLogRepository.findByInventoryBatchNo(batchNumber);
        
        List<MovementLogDto> mappedLogs = logs.stream().map(log -> MovementLogDto.builder()
                .id(log.getId())
                .serialNo(log.getInventory().getSerialNo())
                .fromState(log.getFromState() != null ? log.getFromState().name() : null)
                .toState(log.getToState().name())
                .binBarcode(log.getBin() != null ? log.getBin().getBarcode() : null)
                .action(log.getAction())
                .userId(log.getUserId())
                .createdAt(log.getCreatedAt())
                .build()).collect(Collectors.toList());

        return ResponseEntity.ok(BatchTraceResponse.builder()
                .batchNumber(batchNumber)
                .batches(batches)
                .movementLogs(mappedLogs)
                .build());
    }

    @Data
    @Builder
    public static class BatchTraceResponse {
        private String batchNumber;
        private List<StockBatch> batches;
        private List<MovementLogDto> movementLogs;
    }

    @Data
    @Builder
    public static class MovementLogDto {
        private Long id;
        private String serialNo;
        private String fromState;
        private String toState;
        private String binBarcode;
        private String action;
        private Long userId;
        private LocalDateTime createdAt;
    }
}
