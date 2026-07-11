package com.warehouse.wms.service;

import com.warehouse.wms.entity.*;
import com.warehouse.wms.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CycleCountService {

    private final CountTaskRepository countTaskRepository;
    private final CountLineRepository countLineRepository;
    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final InventoryRepository inventoryRepository;
    private final SkuRepository skuRepository;
    private final BinRepository binRepository;
    private final ZoneRepository zoneRepository;
    private final UserRepository userRepository;
    private final StockBatchRepository stockBatchRepository;
    private final SkuDimensionRepository skuDimensionRepository;
    private final MovementLogRepository movementLogRepository;
    private final EventBroadcastService eventBroadcastService;

    @Transactional
    public CountTask createAdHocTask(Long zoneId, Long binId, Long skuId, Long assignedToId) {
        CountTask task = new CountTask();
        if (zoneId != null) {
            task.setZone(zoneRepository.findById(zoneId).orElseThrow(() -> new EntityNotFoundException("Zone not found")));
        }
        if (binId != null) {
            task.setBin(binRepository.findById(binId).orElseThrow(() -> new EntityNotFoundException("Bin not found")));
        }
        if (skuId != null) {
            task.setSku(skuRepository.findById(skuId).orElseThrow(() -> new EntityNotFoundException("SKU not found")));
        }
        if (assignedToId != null) {
            task.setAssignedTo(userRepository.findById(assignedToId).orElseThrow(() -> new EntityNotFoundException("User not found")));
        }
        task.setStatus(CountTask.CountTaskStatus.SCHEDULED);
        task.setScheduledDate(LocalDateTime.now());
        
        CountTask saved = countTaskRepository.save(task);
        eventBroadcastService.broadcastDashboardUpdate("CYCLE_COUNT_CREATED");
        return saved;
    }

    @Transactional
    public CountTask startCountTask(Long taskId) {
        CountTask task = countTaskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Count task not found: " + taskId));
        
        if (task.getStatus() != CountTask.CountTaskStatus.SCHEDULED) {
            throw new IllegalStateException("Task must be in SCHEDULED status to start");
        }

        task.setStatus(CountTask.CountTaskStatus.IN_PROGRESS);
        
        // Populate expected lines from live Inventory balances
        List<Inventory> activeInventory;
        if (task.getBin() != null) {
            activeInventory = inventoryRepository.findByStateAndBinBarcode(Inventory.InventoryState.AVAILABLE, task.getBin().getBarcode());
        } else if (task.getZone() != null) {
            activeInventory = new ArrayList<>();
            List<Bin> bins = binRepository.findAll().stream()
                    .filter(b -> b.getRack() != null && b.getRack().getAisle() != null && b.getRack().getAisle().getZone() != null 
                            && b.getRack().getAisle().getZone().getId().equals(task.getZone().getId()))
                    .toList();
            for (Bin bin : bins) {
                activeInventory.addAll(inventoryRepository.findByStateAndBinBarcode(Inventory.InventoryState.AVAILABLE, bin.getBarcode()));
            }
        } else {
            activeInventory = inventoryRepository.findAll().stream()
                    .filter(i -> i.getState() == Inventory.InventoryState.AVAILABLE)
                    .toList();
        }

        if (task.getSku() != null) {
            activeInventory = activeInventory.stream()
                    .filter(i -> i.getSku().getId().equals(task.getSku().getId()))
                    .collect(Collectors.toList());
        }

        // Group by Sku and BatchNumber to construct expected quantities
        Map<String, List<Inventory>> grouped = activeInventory.stream()
                .collect(Collectors.groupingBy(i -> i.getSku().getId() + "_" + (i.getBatchNo() != null ? i.getBatchNo() : "")));

        List<CountLine> lines = new ArrayList<>();
        for (Map.Entry<String, List<Inventory>> entry : grouped.entrySet()) {
            List<Inventory> list = entry.getValue();
            Inventory first = list.get(0);
            
            CountLine line = new CountLine();
            line.setCountTask(task);
            line.setSku(first.getSku());
            line.setBatchNumber(first.getBatchNo());
            line.setExpectedQty(list.size());
            line.setCountedQty(0);
            line.setVariance(0);
            line.setStatus(CountLine.LineStatus.PENDING);
            
            lines.add(countLineRepository.save(line));
        }

        task.setLines(lines);
        CountTask saved = countTaskRepository.save(task);
        eventBroadcastService.broadcastCycleCountUpdate(taskId, "COUNT_STARTED");
        return saved;
    }

    @Transactional
    public CountTask submitCount(Long taskId, List<CountSubmitRequest> counts) {
        CountTask task = countTaskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Count task not found: " + taskId));

        if (task.getStatus() != CountTask.CountTaskStatus.IN_PROGRESS) {
            throw new IllegalStateException("Task must be IN_PROGRESS to submit counts");
        }

        boolean hasVariance = false;
        for (CountSubmitRequest req : counts) {
            CountLine line = task.getLines().stream()
                    .filter(l -> l.getSku().getId().equals(req.getSkuId()) 
                            && Objects.equals(l.getBatchNumber(), req.getBatchNumber()))
                    .findFirst()
                    .orElseGet(() -> {
                        // If worker found item that wasn't expected in this bin/task
                        CountLine l = new CountLine();
                        l.setCountTask(task);
                        l.setSku(skuRepository.findById(req.getSkuId()).orElseThrow());
                        l.setBatchNumber(req.getBatchNumber());
                        l.setExpectedQty(0);
                        l.setStatus(CountLine.LineStatus.PENDING);
                        return l;
                    });

            line.setCountedQty(req.getCountedQty());
            int variance = req.getCountedQty() - line.getExpectedQty();
            line.setVariance(variance);
            line.setReasonCode(req.getReasonCode());
            
            if (variance != 0) {
                hasVariance = true;
                line.setStatus(CountLine.LineStatus.PENDING);
            } else {
                line.setStatus(CountLine.LineStatus.APPROVED);
            }
            countLineRepository.save(line);
        }

        task.setCompletedAt(LocalDateTime.now());
        if (hasVariance) {
            task.setStatus(CountTask.CountTaskStatus.VARIANCE_REVIEW);
        } else {
            task.setStatus(CountTask.CountTaskStatus.CLOSED);
        }

        CountTask saved = countTaskRepository.save(task);
        eventBroadcastService.broadcastCycleCountUpdate(taskId, "COUNT_SUBMITTED");
        eventBroadcastService.broadcastDashboardUpdate("CYCLE_COUNT_COMPLETED");
        return saved;
    }

    @Transactional
    public CountLine approveLine(Long lineId, Long supervisorId) {
        CountLine line = countLineRepository.findById(lineId)
                .orElseThrow(() -> new EntityNotFoundException("Count line not found: " + lineId));
        
        if (line.getStatus() != CountLine.LineStatus.PENDING) {
            throw new IllegalStateException("Line is already approved or rejected");
        }

        User supervisor = userRepository.findById(supervisorId)
                .orElseThrow(() -> new EntityNotFoundException("Supervisor not found"));

        line.setStatus(CountLine.LineStatus.APPROVED);
        countLineRepository.save(line);

        // Commit stock adjustment
        commitStockAdjustment(line, supervisor);

        // Check if all lines are approved/rejected for task to close it
        CountTask task = line.getCountTask();
        boolean allResolved = task.getLines().stream().allMatch(l -> l.getStatus() != CountLine.LineStatus.PENDING);
        if (allResolved) {
            task.setStatus(CountTask.CountTaskStatus.CLOSED);
            countTaskRepository.save(task);
        }

        eventBroadcastService.broadcastCycleCountUpdate(task.getId(), "LINE_APPROVED");
        return line;
    }

    @Transactional
    public CountLine rejectLine(Long lineId, Long supervisorId) {
        CountLine line = countLineRepository.findById(lineId)
                .orElseThrow(() -> new EntityNotFoundException("Count line not found: " + lineId));

        if (line.getStatus() != CountLine.LineStatus.PENDING) {
            throw new IllegalStateException("Line is already approved or rejected");
        }

        line.setStatus(CountLine.LineStatus.REJECTED);
        countLineRepository.save(line);

        CountTask task = line.getCountTask();
        boolean allResolved = task.getLines().stream().allMatch(l -> l.getStatus() != CountLine.LineStatus.PENDING);
        if (allResolved) {
            task.setStatus(CountTask.CountTaskStatus.CLOSED);
            countTaskRepository.save(task);
        }

        eventBroadcastService.broadcastCycleCountUpdate(task.getId(), "LINE_REJECTED");
        return line;
    }

    private void commitStockAdjustment(CountLine line, User supervisor) {
        int variance = line.getVariance();
        if (variance == 0) return;

        Bin bin = line.getCountTask().getBin();
        if (bin == null) {
            // Find default or first bin in zone/warehouse
            bin = binRepository.findAll().stream().findFirst().orElseThrow();
        }

        StockAdjustment adjustment = new StockAdjustment();
        adjustment.setSku(line.getSku());
        adjustment.setBin(bin);
        adjustment.setBatchNumber(line.getBatchNumber());
        adjustment.setQuantityAdjusted(variance);
        adjustment.setReason(line.getReasonCode() != null ? line.getReasonCode() : "CYCLE_COUNT_ADJUSTMENT");
        adjustment.setAdjustedBy(supervisor);
        stockAdjustmentRepository.save(adjustment);

        SkuDimension dimension = skuDimensionRepository.findBySkuId(line.getSku().getId()).orElse(null);
        BigDecimal volPerItem = dimension != null ? dimension.getLengthCm().multiply(dimension.getWidthCm()).multiply(dimension.getHeightCm()) : BigDecimal.ZERO;
        BigDecimal wtPerItem = dimension != null ? dimension.getWeightG() : BigDecimal.ZERO;

        if (variance > 0) {
            // Add items to inventory
            for (int i = 0; i < variance; i++) {
                Inventory inv = new Inventory();
                inv.setSku(line.getSku());
                inv.setBin(bin);
                inv.setBatchNo(line.getBatchNumber());
                inv.setQuantity(1);
                inv.setState(Inventory.InventoryState.AVAILABLE);
                inv.setSerialNo(line.getSku().getSkuCode() + "-ADJ-" + UUID.randomUUID().toString().substring(0, 8));
                inventoryRepository.save(inv);
            }

            // Add batch volume/weight
            bin.setOccupiedVolumeCm3(bin.getOccupiedVolumeCm3().add(volPerItem.multiply(BigDecimal.valueOf(variance))));
            bin.setOccupiedWeightG(bin.getOccupiedWeightG().add(wtPerItem.multiply(BigDecimal.valueOf(variance))));
            binRepository.save(bin);

            // Add to StockBatch quantity
            final Bin finalBin = bin;
            StockBatch batch = stockBatchRepository.findBySkuIdAndBinIdAndBatchNumber(line.getSku().getId(), bin.getId(), line.getBatchNumber())
                    .orElseGet(() -> {
                        StockBatch b = new StockBatch();
                        b.setSku(line.getSku());
                        b.setBin(finalBin);
                        b.setBatchNumber(line.getBatchNumber());
                        b.setQuantity(0);
                        b.setStatus(StockBatch.BatchStatus.ACTIVE);
                        return b;
                    });
            batch.setQuantity(batch.getQuantity() + variance);
            stockBatchRepository.save(batch);

        } else {
            // Deduct items from inventory (remove 'AVAILABLE' items first)
            int itemsToRemove = Math.abs(variance);
            List<Inventory> items = inventoryRepository.findByStateAndBinBarcode(Inventory.InventoryState.AVAILABLE, bin.getBarcode()).stream()
                    .filter(i -> i.getSku().getId().equals(line.getSku().getId()) && Objects.equals(i.getBatchNo(), line.getBatchNumber()))
                    .limit(itemsToRemove)
                    .toList();

            for (Inventory inv : items) {
                inventoryRepository.delete(inv);
            }

            // Subtract batch volume/weight
            int actualRemoved = items.size();
            bin.setOccupiedVolumeCm3(bin.getOccupiedVolumeCm3().subtract(volPerItem.multiply(BigDecimal.valueOf(actualRemoved))).max(BigDecimal.ZERO));
            bin.setOccupiedWeightG(bin.getOccupiedWeightG().subtract(wtPerItem.multiply(BigDecimal.valueOf(actualRemoved))).max(BigDecimal.ZERO));
            binRepository.save(bin);

            // Deduct StockBatch quantity
            stockBatchRepository.findBySkuIdAndBinIdAndBatchNumber(line.getSku().getId(), bin.getId(), line.getBatchNumber())
                    .ifPresent(b -> {
                        b.setQuantity(Math.max(0, b.getQuantity() - actualRemoved));
                        stockBatchRepository.save(b);
                    });
        }
        eventBroadcastService.broadcastInventoryChange(line.getSku().getId(), "ADJUSTED");
    }

    @Data
    public static class CountSubmitRequest {
        private Long skuId;
        private String batchNumber;
        private Integer countedQty;
        private String reasonCode;
    }
}
