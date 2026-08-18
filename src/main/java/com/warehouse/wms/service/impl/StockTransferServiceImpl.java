// ====== FILE: src/main/java/com/warehouse/wms/service/impl/StockTransferServiceImpl.java ======
package com.warehouse.wms.service.impl;

import com.warehouse.wms.constant.InventoryStatus;
import com.warehouse.wms.dto.request.StockTransferRequest;
import com.warehouse.wms.dto.response.StockTransferResponse;
import com.warehouse.wms.entity.InventoryStock;
import com.warehouse.wms.entity.StockTransferHistory;
import com.warehouse.wms.exception.BusinessException;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.repository.InventoryStockRepository;
import com.warehouse.wms.repository.StockTransferHistoryRepository;
import com.warehouse.wms.service.StockTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StockTransferServiceImpl implements StockTransferService {
    
    private final InventoryStockRepository inventoryStockRepository;
    private final StockTransferHistoryRepository transferHistoryRepository;
    
    @Override
    @Transactional
    public StockTransferResponse transferStock(StockTransferRequest request) {
        log.info("Starting stock transfer: {}", request);
        
        // Validate the transfer request
        validateTransfer(request);
        
        // Parse location paths
        LocationInfo sourceLocation = parseLocationPath(request.getSourceLocation());
        LocationInfo targetLocation = parseLocationPath(request.getTargetLocation());
        
        // Get source stock
        InventoryStock sourceStock = getSourceStock(request, sourceLocation);
        
        // Verify sufficient quantity
        if (sourceStock.getAvailableQuantity() < request.getQuantity()) {
            throw new BusinessException("Insufficient stock at source location. Available: " + 
                sourceStock.getAvailableQuantity() + ", Requested: " + request.getQuantity());
        }
        
        // Save old quantities before transfer
        Integer sourceOldQty = sourceStock.getQuantity();
        Integer sourceOldAvailable = sourceStock.getAvailableQuantity();
        Integer sourceOldReserved = sourceStock.getReservedQuantity();
        
        // Get or create target stock
        InventoryStock targetStock = getOrCreateTargetStock(request, targetLocation);
        
        // Save old target quantities
        Integer targetOldQty = targetStock.getQuantity();
        Integer targetOldAvailable = targetStock.getAvailableQuantity();
        Integer targetOldReserved = targetStock.getReservedQuantity();
        
        // Perform transfer
        // Remove from source
        sourceStock.removeQuantity(request.getQuantity());
        sourceStock.setLastUpdatedDate(LocalDateTime.now());
        sourceStock.setUpdatedBy(request.getCreatedBy());
        
        // Add to target
        targetStock.addQuantity(request.getQuantity());
        targetStock.setLastUpdatedDate(LocalDateTime.now());
        targetStock.setUpdatedBy(request.getCreatedBy());
        
        // Update batch number and GRN from source to target if not already set
        if (sourceStock.getBatchNumber() != null && targetStock.getBatchNumber() == null) {
            targetStock.setBatchNumber(sourceStock.getBatchNumber());
        }
        if (sourceStock.getGrnNumber() != null && targetStock.getGrnNumber() == null) {
            targetStock.setGrnNumber(sourceStock.getGrnNumber());
        }
        
        // Save both stocks
        inventoryStockRepository.save(sourceStock);
        inventoryStockRepository.save(targetStock);
        
        // Create transfer history
        StockTransferHistory history = createTransferHistory(
            request, sourceLocation, targetLocation, 
            sourceStock, targetStock, 
            sourceOldQty, sourceOldAvailable, sourceOldReserved,
            targetOldQty, targetOldAvailable, targetOldReserved
        );
        
        transferHistoryRepository.save(history);
        
        log.info("Stock transfer completed. Transfer Number: {}, Inventory: {}", 
            history.getTransferNumber(), history.getInventoryNumber());
        
        // Build response
        return buildTransferResponse(history, sourceStock, targetStock);
    }
    
    @Override
    public StockTransferResponse getTransferByNumber(String transferNumber) {
        StockTransferHistory history = transferHistoryRepository.findByTransferNumber(transferNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Transfer not found with number: " + transferNumber));
        
        // CORRECTED: Find source stock by item code and bin ID
        InventoryStock sourceStock = null;
        if (history.getSourceBinId() != null && history.getItemCode() != null) {
            List<InventoryStock> sourceStocks = inventoryStockRepository.findByBinId(history.getSourceBinId());
            sourceStock = sourceStocks.stream()
                .filter(stock -> history.getItemCode().equals(stock.getItemCode()))
                .findFirst()
                .orElse(null);
        }
        
        // CORRECTED: Find target stock by item code and bin ID
        InventoryStock targetStock = null;
        if (history.getTargetBinId() != null && history.getItemCode() != null) {
            List<InventoryStock> targetStocks = inventoryStockRepository.findByBinId(history.getTargetBinId());
            targetStock = targetStocks.stream()
                .filter(stock -> history.getItemCode().equals(stock.getItemCode()))
                .findFirst()
                .orElse(null);
        }
        
        return buildTransferResponse(history, sourceStock, targetStock);
    }
    
    @Override
    public Page<StockTransferResponse> getAllTransfers(Pageable pageable) {
        return transferHistoryRepository.findAll(pageable)
            .map(history -> buildTransferResponse(history, null, null));
    }
    
    @Override
    public Page<StockTransferResponse> getTransfersWithFilter(
            String itemCode,
            String sourceLocation,
            String targetLocation,
            String batchNumber,
            String grnNumber,
            String inventoryNumber,
            String search,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String transferStatus,
            Pageable pageable) {
        
        Page<StockTransferHistory> historyPage = transferHistoryRepository.findByFilters(
            itemCode, sourceLocation, targetLocation, batchNumber, 
            grnNumber, inventoryNumber, search,
            startDate, endDate, transferStatus, pageable);
        
        return historyPage.map(history -> buildTransferResponse(history, null, null));
    }
    
    @Override
    public List<StockTransferResponse> getTransfersByItemCode(String itemCode) {
        return transferHistoryRepository.findByItemCode(itemCode)
            .stream()
            .map(history -> buildTransferResponse(history, null, null))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<StockTransferResponse> getTransfersBySourceLocation(String locationPath) {
        return transferHistoryRepository.findBySourceLocationPath(locationPath)
            .stream()
            .map(history -> buildTransferResponse(history, null, null))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<StockTransferResponse> getTransfersByTargetLocation(String locationPath) {
        return transferHistoryRepository.findByTargetLocationPath(locationPath)
            .stream()
            .map(history -> buildTransferResponse(history, null, null))
            .collect(Collectors.toList());
    }
    
    @Override
    public StockTransferResponse cancelTransfer(String transferNumber) {
        StockTransferHistory history = transferHistoryRepository.findByTransferNumber(transferNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Transfer not found: " + transferNumber));
        
        if (history.getTransferStatus() == StockTransferHistory.TransferStatus.COMPLETED) {
            throw new BusinessException("Cannot cancel a completed transfer");
        }
        
        history.setTransferStatus(StockTransferHistory.TransferStatus.CANCELLED);
        history.setUpdatedBy("SYSTEM");
        transferHistoryRepository.save(history);
        
        return buildTransferResponse(history, null, null);
    }
    
    @Override
    public void validateTransfer(StockTransferRequest request) {
        // Validate source and target are different
        if (request.getSourceLocation().equals(request.getTargetLocation())) {
            throw new BusinessException("Source and target locations cannot be the same");
        }
        
        // Validate quantities
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BusinessException("Transfer quantity must be greater than 0");
        }
        
        // Validate location formats
        if (!isValidLocationPath(request.getSourceLocation())) {
            throw new BusinessException("Invalid source location format");
        }
        if (!isValidLocationPath(request.getTargetLocation())) {
            throw new BusinessException("Invalid target location format");
        }
    }
    
    // ====== Private Helper Methods ======
    
    private InventoryStock getSourceStock(StockTransferRequest request, LocationInfo location) {
        // If inventoryNumber is provided, find stock by inventory number and bin
        if (request.getInventoryNumber() != null && !request.getInventoryNumber().isEmpty()) {
            return inventoryStockRepository.findByInventoryNumberAndBinId(
                request.getInventoryNumber(), location.binId)
                .orElseThrow(() -> new BusinessException(
                    "No stock found with inventory number: " + request.getInventoryNumber() + 
                    " at bin: " + location.binId));
        }
        
        // Otherwise find by location and item code
        return inventoryStockRepository.findByLocationAndItemCode(
            location.warehouseId, location.zoneId, location.aisleId, 
            location.rackId, location.levelId, location.binId, request.getItemCode())
            .orElseThrow(() -> new BusinessException("No stock found at source location for item: " + request.getItemCode()));
    }
    
    private InventoryStock getOrCreateTargetStock(StockTransferRequest request, LocationInfo location) {
        return inventoryStockRepository.findByLocationAndItemCode(
            location.warehouseId, location.zoneId, location.aisleId,
            location.rackId, location.levelId, location.binId, request.getItemCode())
            .orElseGet(() -> {
                // Create new stock at target location
                InventoryStock newStock = InventoryStock.builder()
                    .inventoryNumber(generateInventoryNumber())
                    .itemCode(request.getItemCode())
                    .itemName(request.getItemCode()) // TODO: Fetch from item master
                    .uom("EA") // TODO: Fetch from item master
                    .quantity(0)
                    .availableQuantity(0)
                    .reservedQuantity(0)
                    .inTransitQuantity(0)
                    .warehouseId(location.warehouseId)
                    .zone(location.zoneId)
                    .aisle(location.aisleId)
                    .rack(location.rackId)
                    .shelf(location.levelId)
                    .level(location.levelId)
                    .binId(location.binId)
                    .status(InventoryStatus.ACTIVE)
                    .isAvailable(true)
                    .isAllocated(false)
                    .isFrozen(false)
                    .createdBy(request.getCreatedBy())
                    .build();
                return inventoryStockRepository.save(newStock);
            });
    }
    
    private StockTransferHistory createTransferHistory(
            StockTransferRequest request,
            LocationInfo sourceLocation,
            LocationInfo targetLocation,
            InventoryStock sourceStock,
            InventoryStock targetStock,
            Integer sourceOldQty, Integer sourceOldAvailable, Integer sourceOldReserved,
            Integer targetOldQty, Integer targetOldAvailable, Integer targetOldReserved) {
        
        String transferNumber = generateTransferNumber();
        
        // Get inventory number from source stock if not provided
        String inventoryNumber = request.getInventoryNumber() != null && !request.getInventoryNumber().isEmpty() 
            ? request.getInventoryNumber() 
            : sourceStock.getInventoryNumber();
        
        return StockTransferHistory.builder()
            .transferNumber(transferNumber)
            .sourceWarehouseId(sourceLocation.warehouseId)
            .sourceZoneId(sourceLocation.zoneId)
            .sourceAisleId(sourceLocation.aisleId)
            .sourceRackId(sourceLocation.rackId)
            .sourceLevelId(sourceLocation.levelId)
            .sourceBinId(sourceLocation.binId)
            .sourceLocationPath(request.getSourceLocation())
            .targetWarehouseId(targetLocation.warehouseId)
            .targetZoneId(targetLocation.zoneId)
            .targetAisleId(targetLocation.aisleId)
            .targetRackId(targetLocation.rackId)
            .targetLevelId(targetLocation.levelId)
            .targetBinId(targetLocation.binId)
            .targetLocationPath(request.getTargetLocation())
            .itemCode(request.getItemCode())
            .itemName(sourceStock.getItemName())
            .uom(sourceStock.getUom())
            .batchNumber(sourceStock.getBatchNumber())
            .inventoryNumber(inventoryNumber)
            .quantityTransferred(request.getQuantity())
            .sourceOldQuantity(sourceOldQty)
            .sourceNewQuantity(sourceStock.getQuantity())
            .targetOldQuantity(targetOldQty)
            .targetNewQuantity(targetStock.getQuantity())
            .grnNumber(sourceStock.getGrnNumber())
            .transferStatus(StockTransferHistory.TransferStatus.COMPLETED)
            .transferReason(request.getTransferReason())
            .transferDate(LocalDateTime.now())
            .remarks(request.getRemarks())
            .createdBy(request.getCreatedBy())
            .build();
    }
    
    private StockTransferResponse buildTransferResponse(
            StockTransferHistory history,
            InventoryStock sourceStock,
            InventoryStock targetStock) {
        
        StockTransferResponse.TransferDetail detail = StockTransferResponse.TransferDetail.builder()
            .sourceBinId(history.getSourceBinId())
            .sourceLocationPath(history.getSourceLocationPath())
            .sourceOldQuantity(history.getSourceOldQuantity())
            .sourceNewQuantity(history.getSourceNewQuantity())
            .targetBinId(history.getTargetBinId())
            .targetLocationPath(history.getTargetLocationPath())
            .targetOldQuantity(history.getTargetOldQuantity())
            .targetNewQuantity(history.getTargetNewQuantity())
            .transferredQuantity(history.getQuantityTransferred())
            .status(history.getTransferStatus().name())
            .grnNumber(history.getGrnNumber())
            .batchNumber(history.getBatchNumber())
            .inventoryNumber(history.getInventoryNumber())
            .build();
        
        List<StockTransferResponse.TransferDetail> details = new ArrayList<>();
        details.add(detail);
        
        return StockTransferResponse.builder()
            .transferNumber(history.getTransferNumber())
            .sourceLocation(history.getSourceLocationPath())
            .targetLocation(history.getTargetLocationPath())
            .itemCode(history.getItemCode())
            .itemName(history.getItemName())
            .quantityTransferred(history.getQuantityTransferred())
            .batchNumber(history.getBatchNumber())
            .inventoryNumber(history.getInventoryNumber())
            .status(history.getTransferStatus().name())
            .transferReason(history.getTransferReason())
            .transferDate(history.getTransferDate())
            .createdBy(history.getCreatedBy())
            .grnNumber(history.getGrnNumber())
            .details(details)
            .build();
    }
    
    private LocationInfo parseLocationPath(String locationPath) {
        String[] parts = locationPath.split("-");
        LocationInfo info = new LocationInfo();
        
        if (parts.length > 0) info.warehouseId = parts[0];
        if (parts.length > 1) info.zoneId = parts[1];
        if (parts.length > 2) info.aisleId = parts[2];
        if (parts.length > 3) info.rackId = parts[3];
        if (parts.length > 4) info.levelId = parts[4];
        if (parts.length > 5) info.binId = parts[5];
        
        return info;
    }
    
    private boolean isValidLocationPath(String locationPath) {
        if (locationPath == null || locationPath.isEmpty()) return false;
        String[] parts = locationPath.split("-");
        return parts.length >= 1 && !parts[0].isEmpty();
    }
    
    private String generateTransferNumber() {
        return "TRF-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + 
               "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private String generateInventoryNumber() {
        return "INV-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6);
    }
    
    private static class LocationInfo {
        String warehouseId;
        String zoneId;
        String aisleId;
        String rackId;
        String levelId;
        String binId;
    }
}