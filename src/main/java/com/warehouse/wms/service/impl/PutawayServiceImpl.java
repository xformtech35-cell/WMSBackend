// ====== FILE: src/main/java/com/warehouse/wms/service/impl/PutawayServiceImpl.java ======
package com.warehouse.wms.service.impl;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.warehouse.wms.constant.InventoryStatus;
import com.warehouse.wms.constant.PutawayLineStatus;
import com.warehouse.wms.constant.PutawayStage;
import com.warehouse.wms.constant.PutawayStatus;
import com.warehouse.wms.dto.request.PutawayConfirmRequest;
import com.warehouse.wms.dto.request.PutawayExecuteRequest;
import com.warehouse.wms.dto.request.PutawayInitiateRequest;
import com.warehouse.wms.dto.request.QRCodeGenerateRequest;
import com.warehouse.wms.dto.response.LocationSuggestionResponse;
import com.warehouse.wms.dto.response.PutawayTaskResponse;
import com.warehouse.wms.dto.response.QRCodeResponse;
import com.warehouse.wms.entity.Aisle;
import com.warehouse.wms.entity.Bin;
import com.warehouse.wms.entity.BinLocation;
import com.warehouse.wms.entity.InboundLine;
import com.warehouse.wms.entity.InventoryStock;
import com.warehouse.wms.entity.Level;
import com.warehouse.wms.entity.PutawayConfirmation;
import com.warehouse.wms.entity.PutawayLine;
import com.warehouse.wms.entity.PutawayTask;
import com.warehouse.wms.entity.QRCode;
import com.warehouse.wms.entity.Rack;
// Add these imports at the top
import com.warehouse.wms.entity.StockAvailability;
import com.warehouse.wms.entity.StockAvailability.LocationLevel;
import com.warehouse.wms.entity.Warehouse;
import com.warehouse.wms.entity.Zone;
import com.warehouse.wms.exception.InvalidOperationException;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.mapper.PutawayTaskMapper;
import com.warehouse.wms.repository.BinLocationRepository;
import com.warehouse.wms.repository.BinRepository;
import com.warehouse.wms.repository.InboundLineRepository;
import com.warehouse.wms.repository.InventoryStockRepository;
import com.warehouse.wms.repository.PutawayConfirmationRepository;
import com.warehouse.wms.repository.PutawayLineRepository;
import com.warehouse.wms.repository.PutawayTaskRepository;
import com.warehouse.wms.repository.QRCodeRepository;
import com.warehouse.wms.repository.StockAvailabilityRepository;
import com.warehouse.wms.repository.WarehouseRepository;
import com.warehouse.wms.service.PutawayService;
import com.warehouse.wms.service.QRCodeService;
import com.warehouse.wms.service.StockAvailabilityService;

// Add this field

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PutawayServiceImpl implements PutawayService {

    private final BinRepository binRepository;
    
    private final WarehouseRepository warehouseRepository;

    private final StockAvailabilityRepository stockAvailabilityRepository;

    private final PutawayTaskRepository putawayTaskRepository;
    private final PutawayLineRepository putawayLineRepository;
    private final PutawayConfirmationRepository putawayConfirmationRepository;
    private final BinLocationRepository binLocationRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final InboundLineRepository inboundLineRepository; // ✅ ADD THIS
    private final QRCodeService qrCodeService;
    
    private final QRCodeRepository qRCodeRepository; // ✅ ADD THIS

    
    private final PutawayTaskMapper putawayTaskMapper;
    
    private final StockAvailabilityService stockAvailabilityService;


    private static final String TASK_PREFIX = "PUT";
    private static final String CONFIRMATION_PREFIX = "PCN";
    private static final String INVENTORY_PREFIX = "INV";

 @Override
public PutawayTaskResponse initiatePutaway(PutawayInitiateRequest request) {
    log.info("Initiating putaway for GRN: {}", request.getGrnNumber());

    // Create putaway task
    PutawayTask task = PutawayTask.builder()
            .taskNumber(generateTaskNumber())
            .grnNumber(request.getGrnNumber())
            .warehouseId(request.getWarehouseId())
            .receivingArea(request.getReceivingArea())
            .assignedTo(request.getAssignedTo())
            .assignedAt(request.getAssignedTo() != null ? LocalDateTime.now() : null)
            .createdBy(request.getCreatedBy())
            .status(PutawayStatus.PENDING)
            .stage(PutawayStage.INITIATED)
            .build();

    int totalQuantity = 0;

    // Process each line
    for (PutawayInitiateRequest.PutawayLineRequest lineRequest : request.getLines()) {
        // Suggest location
        LocationSuggestionResponse suggestion = suggestLocation(
            lineRequest.getItemCode(), 
            lineRequest.getQuantity(), 
            request.getWarehouseId()
        );

        // Build line with proper fields
        PutawayLine.PutawayLineBuilder lineBuilder = PutawayLine.builder()
                .lineNumber(task.getLines().size() + 1)
                .itemCode(lineRequest.getItemCode())
                .itemName(lineRequest.getItemName())
                .uom(lineRequest.getUom())
                .quantity(lineRequest.getQuantity())
                .putawayQuantity(0)
                .remainingQuantity(lineRequest.getQuantity())
                .batchNumber(lineRequest.getBatchNumber())
                .serialNumber(lineRequest.getSerialNumber())
                .remarks(lineRequest.getRemarks())
                .status(PutawayLineStatus.PENDING);

//        // Declare suggested outside the if block so it's accessible later
//        LocationSuggestionResponse.SuggestedLocation suggested = null;
//        String fullPath = null;
//        
//        // Set suggested location
//        if (suggestion != null && suggestion.getSuggestedLocations() != null 
//            && !suggestion.getSuggestedLocations().isEmpty()) {
//            
//            suggested = suggestion.getSuggestedLocations().get(0);
//            
//            fullPath = suggested.getWarehouseId() + "/" + 
//                      suggested.getZone() + "/" + 
//                      suggested.getAisle() + "/" + 
//                      suggested.getRack() + "/" + 
//                      suggested.getShelf() + "/" + 
//                      suggested.getLevel();
//            
//            lineBuilder.suggestedWarehouse(suggested.getWarehouseId())
//                       .suggestedZone(suggested.getZone())
//                       .suggestedAisle(suggested.getAisle())
//                       .suggestedRack(suggested.getRack())
//                       .suggestedShelf(suggested.getShelf())
//                       .suggestedLevel(suggested.getLevel())
//                       .suggestedBin(suggested.getBinId())
//                       .fullpath(fullPath);
//        }

        // Build the line first so we have the fullpath
        
        QRCode inboundLine1 = qRCodeRepository.findById(lineRequest.getInboundLineId())
                .orElse(null);
      
            lineBuilder.suggestedWarehouse(inboundLine1.getWarehouseId());
            lineBuilder.suggestedZone(inboundLine1.getZone());
            lineBuilder.suggestedAisle(inboundLine1.getAisle());
            lineBuilder.suggestedRack(inboundLine1.getRack());
            lineBuilder.suggestedLevel(inboundLine1.getLevel());
            lineBuilder.suggestedBin(inboundLine1.getBinId());
//            lineBuilder.fullpath(inboundLine1.getFullpath());
            
            inboundLine1.setTaskAssinged(true);
            
            qRCodeRepository.save(inboundLine1);

        
        
        
        PutawayLine line = lineBuilder.build();
        

        // Set InboundLine if inboundLineId is provided
        if (lineRequest.getInboundLineId() != null) {
            InboundLine inboundLine = inboundLineRepository.findById(lineRequest.getInboundLineId())
                    .orElse(null);
            if (inboundLine != null) {
                line.setInboundLine(inboundLine);
                inboundLine.setTaskAssinged(true);
                lineBuilder.actualWarehouse(inboundLine.getWarehouseId());
                lineBuilder.actualZone(inboundLine.getZone());
                lineBuilder.actualAisle(inboundLine.getAisle());
                lineBuilder.actualRack(inboundLine.getRack());
                lineBuilder.actualLevel(inboundLine.getLevel());
                lineBuilder.actualBin(inboundLine.getBinId());

                inboundLineRepository.save(inboundLine);
            }
        }

        task.addLine(line);
        totalQuantity += lineRequest.getQuantity();
    }

    task.setTotalQuantity(totalQuantity);
    task.setPendingQuantity(totalQuantity);

    PutawayTask savedTask = putawayTaskRepository.save(task);
    log.info("✅ Putaway task created: {}", savedTask.getTaskNumber());

    // Generate QR Codes for the task
    generateQRCodesForTask(savedTask);

    return putawayTaskMapper.toResponse(savedTask);
}

    @Override
    public PutawayTaskResponse executePutawayStage(PutawayExecuteRequest request) {
        log.info("Executing putaway stage: {} for task: {}", request.getStage(), request.getTaskNumber());

        PutawayTask task = putawayTaskRepository.findByTaskNumber(request.getTaskNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + request.getTaskNumber()));

        if (task.getStatus() == PutawayStatus.COMPLETED ) {
            throw new InvalidOperationException("Task is already completed/confirmed");
        }

        PutawayStage stage = PutawayStage.valueOf(request.getStage());
        LocalDateTime now = LocalDateTime.now();

        switch (stage) {
            case PICKED:
                task.setStage(PutawayStage.PICKED);
                task.setPickedAt(now);
                task.setStatus(PutawayStatus.IN_PROGRESS);
                break;
            case TRANSPORTED:
                task.setStage(PutawayStage.TRANSPORTED);
                task.setTransportedAt(now);
                break;
// ====== FILE: src/main/java/com/warehouse/wms/service/impl/PutawayServiceImpl.java ======
// Replace your PLACED case with this improved version

// ====== FILE: src/main/java/com/warehouse/wms/service/impl/PutawayServiceImpl.java ======
// Replace your PLACED case with this corrected version

case PLACED:
    // Verify bin barcode is provided
    if (request.getBinBarcode() == null || request.getBinBarcode().trim().isEmpty()) {
        throw new InvalidOperationException("Bin barcode is required for PLACED stage");
    }
    
    // Find the specific line by ID
    PutawayLine targetLine = null;
    for (PutawayLine line : task.getLines()) {
        if (line.getId().equals(request.getPutawayLineId())) {
            targetLine = line;
            break;
        }
    }
    
    if (targetLine == null) {
        throw new ResourceNotFoundException(
            "Putaway line not found with ID: " + request.getPutawayLineId());
    }
    
    // Check if the line is already placed
    if (targetLine.getStatus() == PutawayLineStatus.PLACED || 
        targetLine.getStatus() == PutawayLineStatus.CONFIRMED) {
        throw new InvalidOperationException(
            "Line " + targetLine.getLineNumber() + " is already " + targetLine.getStatus());
    }
    
    // ====== FIX: Extract the bin barcode from the full location ======
    // The full location format: WH-002-XCXVCX-ASLE-1-WEEWRWE-EW34-DFDSDSD
    // The bin barcode is the last part: DFDSDSD
    
    String fullLocationFromRequest = request.getBinBarcode().trim().replaceAll("\\s+", "");
    log.info("Full location from request: {}", fullLocationFromRequest);
    
    // Extract the bin barcode (last part after the last dash)
    String[] parts = fullLocationFromRequest.split("-");
    String binBarcode = parts[parts.length - 1]; // Get the last part
    log.info("Extracted bin barcode: {}", binBarcode);
    
    // ====== FIX: Search for bin using ONLY the bin barcode ======
    Bin bin = binRepository.findByBarcode(binBarcode)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Bin not found with barcode: " + binBarcode));
    
    // ====== FIX: Build expected full location from target line ======
    String expectedFullLocation = targetLine.getSuggestedWarehouse() + "-" +
                                  targetLine.getSuggestedZone() + "-" +
                                  targetLine.getSuggestedAisle() + "-" +
                                  targetLine.getSuggestedRack() + "-" +
                                  (targetLine.getSuggestedLevel() != null ? targetLine.getSuggestedLevel() : "") + "-" +
                                  (targetLine.getSuggestedBin() != null ? targetLine.getSuggestedBin() : "");
    
    log.info("Expected full location: {}", expectedFullLocation);
    
    // ====== FIX: Get actual full location from bin ======
    String actualFullLocation = null;
    
    try {
        // Try to get full location from bin
        actualFullLocation = bin.getFullLocation();
        
        // If still null, build it manually with detailed checks
        if (actualFullLocation == null || actualFullLocation.isEmpty()) {
            Level level = bin.getLevel();
            if (level == null) {
                log.error("Bin has no level assigned. Bin ID: {}, Barcode: {}", bin.getId(), bin.getBarcode());
                throw new InvalidOperationException(
                    "Bin has no level assigned. Please assign a level to this bin. Bin: " + bin.getBarcode());
            }
            
            Rack rack = level.getRack();
            if (rack == null) {
                log.error("Level has no rack assigned. Level ID: {}, Bin Barcode: {}", level.getId(), bin.getBarcode());
                throw new InvalidOperationException(
                    "Level has no rack assigned. Level: " + level.getLevelId());
            }
            
            Aisle aisle = rack.getAisle();
            if (aisle == null) {
                log.error("Rack has no aisle assigned. Rack ID: {}", rack.getId());
                throw new InvalidOperationException(
                    "Rack has no aisle assigned. Rack: " + rack.getRackId());
            }
            
            Zone zone = aisle.getZone();
            if (zone == null) {
                log.error("Aisle has no zone assigned. Aisle ID: {}", aisle.getId());
                throw new InvalidOperationException(
                    "Aisle has no zone assigned. Aisle: " + aisle.getAisleId());
            }
            
            Warehouse warehouse = zone.getWarehouse();
            if (warehouse == null) {
                log.error("Zone has no warehouse assigned. Zone ID: {}", zone.getId());
                throw new InvalidOperationException(
                    "Zone has no warehouse assigned. Zone: " + zone.getZoneId());
            }
            
            actualFullLocation = String.format("%s-%s-%s-%s-%s-%s",
                    warehouse.getWarehouseId(),
                    zone.getZoneId(),
                    aisle.getAisleId(),
                    rack.getRackId(),
                    level.getLevelId(),
                    bin.getBarcode());
        }
    } catch (NullPointerException e) {
        log.error("Bin hierarchy incomplete. Bin ID: {}, Barcode: {}", bin.getId(), bin.getBarcode(), e);
        throw new InvalidOperationException(
            "Bin location hierarchy is incomplete. Please ensure the bin is properly assigned to: Level -> Rack -> Aisle -> Zone -> Warehouse");
    }
    
    log.info("Actual full location from bin: {}", actualFullLocation);
    log.info("Bin barcode from DB: {}", bin.getBarcode());
    
    // ====== FIX: Compare the full locations ======
    // Clean both for comparison (remove all spaces)
    String cleanedExpected = expectedFullLocation.replaceAll("\\s+", "");
    String cleanedActual = actualFullLocation.replaceAll("\\s+", "");
    
    log.info("Cleaned Expected: {}", cleanedExpected);
    log.info("Cleaned Actual: {}", cleanedActual);
    
    // ====== FIX: Validate that the scanned bin matches the suggested location ======
    if (!cleanedExpected.equals(cleanedActual)) {
        throw new InvalidOperationException(
            "Scanned bin does not match suggested location. " +
            "Expected: " + expectedFullLocation + ", " +
            "Got: " + actualFullLocation);
    }
    
    // ====== FIX: Update line with actual location ======
    targetLine.setActualWarehouse(targetLine.getSuggestedWarehouse());
    targetLine.setActualZone(targetLine.getSuggestedZone());
    targetLine.setActualAisle(targetLine.getSuggestedAisle());
    targetLine.setActualRack(targetLine.getSuggestedRack());
    targetLine.setActualShelf(targetLine.getSuggestedShelf());
    targetLine.setActualLevel(targetLine.getSuggestedLevel());
    targetLine.setActualBin(targetLine.getSuggestedBin());
    targetLine.setBinBarcode(bin.getBarcode());
    targetLine.setStatus(PutawayLineStatus.PLACED);
    
    // Update task
    task.setPlacedAt(now);
    
    // Check if ALL lines are placed
    boolean allPlaced = true;
    for (PutawayLine line : task.getLines()) {
        if (line.getStatus() != PutawayLineStatus.PLACED && 
            line.getStatus() != PutawayLineStatus.CONFIRMED) {
            allPlaced = false;
            break;
        }
    }
    
    if (allPlaced) {
        task.setStatus(PutawayStatus.CONFIRMED);
        task.setStage(PutawayStage.CONFIRMED);
        task.setCompletedAt(now);
        log.info("All lines placed. Task {} completed", task.getTaskNumber());
    }
    
    log.info("Line {} (ID: {}) placed at bin: {}", 
        targetLine.getLineNumber(), targetLine.getId(), bin.getBarcode());
    
    
    
    
    
    
            case SCANNED:
                task.setStage(PutawayStage.PLACED);
                task.setPlacedAt(now);
                break;
            default:
                throw new InvalidOperationException("Invalid stage: " + stage);
        }

        PutawayTask updatedTask = putawayTaskRepository.save(task);
        return putawayTaskMapper.toResponse(updatedTask);
    }

 @Override
    @Transactional
    public PutawayTaskResponse confirmPutaway(PutawayConfirmRequest request) {
        log.info("Confirming putaway for task: {}", request.getTaskNumber());

        // 1. Validate and get task
        PutawayTask task = putawayTaskRepository.findByTaskNumber(request.getTaskNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + request.getTaskNumber()));

        // Check if task is already completed
        if (task.getStatus() == PutawayStatus.COMPLETED) {
            throw new InvalidOperationException("Task already completed");
        }

        // Check if task is cancelled
        if (task.getStatus() == PutawayStatus.CANCELLED) {
            throw new InvalidOperationException("Task is cancelled");
        }

        // 2. Process confirmation lines
        int confirmedQuantity = 0;
        for (PutawayConfirmRequest.PutawayConfirmLineRequest lineRequest : request.getLines()) {
            PutawayLine line = putawayLineRepository.findById(lineRequest.getLineId())
                    .orElseThrow(() -> new ResourceNotFoundException("Line not found: " + lineRequest.getLineId()));

            // Get confirmed quantity (use line quantity if not provided)
            Integer qty = lineRequest.getConfirmedQuantity() != null ? 
                          lineRequest.getConfirmedQuantity() : line.getQuantity();

            // Validate quantity
            if (qty > line.getQuantity()) {
                throw new InvalidOperationException(
                    String.format("Confirmed quantity (%d) exceeds line quantity (%d) for item: %s", 
                        qty, line.getQuantity(), line.getItemCode())
                );
            }

            if (qty <= 0) {
                throw new InvalidOperationException(
                    "Confirmed quantity must be greater than 0 for item: " + line.getItemCode()
                );
            }

            // Update line
            line.setPutawayQuantity(qty);
            line.setRemainingQuantity(line.getQuantity() - qty);
            line.setStatus(PutawayLineStatus.COMPLETED);
            
            // Update actual bin if provided
            if (lineRequest.getActualBin() != null) {
                line.setActualBin(lineRequest.getActualBin());
                line.setBinBarcode(lineRequest.getActualBinBarcode());
            }

            confirmedQuantity += qty;
            putawayLineRepository.save(line);
        }

        // 3. Validate total quantity
      

        // 4. Update task header
        task.setPutawayQuantity(confirmedQuantity);
        task.setPendingQuantity(task.getTotalQuantity() - confirmedQuantity);
        task.setStage(PutawayStage.COMPLETED);
        task.setStatus(PutawayStatus.COMPLETED);
        task.setConfirmedAt(LocalDateTime.now());
        task.setCompletedAt(LocalDateTime.now());
        task.setConfirmedBy(request.getConfirmedBy());
        task.setRemarks(request.getRemarks());

        // 5. Generate confirmation number
        String confirmationNumber = generateConfirmationNumber();
        task.setConfirmationNumber(confirmationNumber);

        // 6. Create confirmation record
        PutawayConfirmation confirmation = buildConfirmation(task, request, confirmationNumber, confirmedQuantity);
        putawayConfirmationRepository.save(confirmation);

        // 7. Save task
        PutawayTask updatedTask = putawayTaskRepository.save(task);

        // 8. Update inventory if verified
        if (request.getIsVerified() != null && request.getIsVerified()) {
            updateInventoryAfterPutaway(confirmationNumber);
        }

        // 9. Update QR Code status
        updateQRCodeStatusForTask(task.getId());

        log.info("✅ Putaway completed with confirmation number: {}", confirmationNumber);
        return putawayTaskMapper.toResponse(updatedTask);
    }

    private PutawayConfirmation buildConfirmation(
            PutawayTask task, 
            PutawayConfirmRequest request, 
            String confirmationNumber, 
            Integer confirmedQuantity) {
        
        PutawayConfirmation confirmation = PutawayConfirmation.builder()
                .confirmationNumber(confirmationNumber)
                .taskNumber(task.getTaskNumber())
                .putawayTaskId(task.getId())
                .grnNumber(task.getGrnNumber())
                .warehouseId(task.getWarehouseId())
                .confirmedBy(request.getConfirmedBy())
                .confirmedAt(LocalDateTime.now())
                .totalQuantity(task.getTotalQuantity())
                .confirmedQuantity(confirmedQuantity)
                .isVerified(request.getIsVerified() != null && request.getIsVerified())
                .verifiedBy(request.getIsVerified() ? request.getVerifiedBy() : null)
                .verifiedAt(request.getIsVerified() ? LocalDateTime.now() : null)
                .inventoryUpdated(false)
                .remarks(request.getRemarks())
                .build();

        // Set bin details from first line with actual bin
        for (PutawayLine line : task.getLines()) {
            if (line.getActualBin() != null) {
                confirmation.setBinId(line.getActualBin());
                confirmation.setBinBarcode(line.getBinBarcode());
                confirmation.setZone(line.getActualZone());
                confirmation.setAisle(line.getActualAisle());
                confirmation.setRack(line.getActualRack());
                confirmation.setLevel(line.getActualLevel());
                confirmation.setShelf(line.getActualShelf());
                break;
            }
        }

        return confirmation;
    }
//@Override
//@Transactional
//public void updateInventoryAfterPutaway(String confirmationNumber) {
//    log.info("Updating inventory for confirmation: {}", confirmationNumber);
//
//    PutawayConfirmation confirmation = putawayConfirmationRepository
//            .findByConfirmationNumber(confirmationNumber)
//            .orElseThrow(() -> new ResourceNotFoundException("Confirmation not found: " + confirmationNumber));
//
//    if (confirmation.getInventoryUpdated()) {
//        log.info("Inventory already updated for confirmation: {}", confirmationNumber);
//        return;
//    }
//
//    PutawayTask task = putawayTaskRepository.findById(confirmation.getPutawayTaskId())
//            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
//
//    // Update inventory for each line
//    for (PutawayLine line : task.getLines()) {
//        if (line.getPutawayQuantity() == null || line.getPutawayQuantity() == 0) {
//            continue;
//        }
//
//        // Check if stock exists in this bin
//        Optional<InventoryStock> existingStock = inventoryStockRepository
//                .findByBinIdAndItemCode(line.getActualBin(), line.getItemCode());
//
//        InventoryStock stock;
//        if (existingStock.isPresent()) {
//            stock = existingStock.get();
//            stock.addQuantity(line.getPutawayQuantity());
//        } else {
//            // Create new stock entry
//            String serialNumbers = line.getSerialNumber() != null ? 
//                                   line.getSerialNumber() : null;
//            
//            stock = InventoryStock.builder()
//                    .inventoryNumber(generateInventoryNumber())
//                    .itemCode(line.getItemCode())
//                    .itemName(line.getItemName())
//                    .uom(line.getUom())
//                    .quantity(line.getPutawayQuantity())
//                    .availableQuantity(line.getPutawayQuantity())
//                    .warehouseId(line.getActualWarehouse() != null ? line.getActualWarehouse() : task.getWarehouseId())
//                    .zone(line.getActualZone())
//                    .aisle(line.getActualAisle())
//                    .rack(line.getActualRack())
//                    .level(line.getActualLevel())
//                    .binId(line.getActualBin())
//                    .binBarcode(line.getBinBarcode())
//                    .batchNumber(line.getBatchNumber())
//                    .serialNumbers(serialNumbers)
//                    .grnNumber(task.getGrnNumber())
//                    .putawayTaskNumber(task.getTaskNumber())
//                    .confirmationNumber(confirmationNumber)
//                    .status(InventoryStatus.ACTIVE)
//                    .isAvailable(true)
//                    .receivedDate(LocalDateTime.now())
//                    .lastUpdatedDate(LocalDateTime.now())
//                    .build();
//        }
//
//        inventoryStockRepository.save(stock);
//
//        // ✅ FIX: Update bin capacity using the simple method
//        if (line.getActualBin() != null) {
//            binLocationRepository.allocateBinCapacitySimple(line.getActualBin(), line.getPutawayQuantity());
//            binLocationRepository.updateLastPutaway(line.getActualBin());
//        }
//    }
//
//    // Update confirmation
//    confirmation.setInventoryUpdated(true);
//    confirmation.setInventoryUpdatedAt(LocalDateTime.now());
//    putawayConfirmationRepository.save(confirmation);
//
//    // Update task status
//    task.setStage(PutawayStage.COMPLETED);
//    task.setStatus(PutawayStatus.COMPLETED);
//    task.setCompletedAt(LocalDateTime.now());
//    putawayTaskRepository.save(task);
//
//    log.info("✅ Inventory updated successfully for confirmation: {}", confirmationNumber);
//}

    @Override
    public LocationSuggestionResponse suggestLocation(String itemCode, Integer quantity, String warehouseId) {
        log.info("Suggesting location for item: {}, quantity: {}", itemCode, quantity);

        List<BinLocation> availableBins = binLocationRepository.findBestAvailableLocation(
                warehouseId, quantity);

        LocationSuggestionResponse response = LocationSuggestionResponse.builder()
                .itemCode(itemCode)
                .quantityRequired(quantity)
                .warehouseId(warehouseId)
                .partialAllowed(true)
                .build();

        List<LocationSuggestionResponse.SuggestedLocation> suggested = new ArrayList<>();
        int remaining = quantity;

        for (BinLocation bin : availableBins) {
            if (remaining <= 0) break;
            
            int suggestedQty = Math.min(bin.getAvailableCapacity(), remaining);
            suggested.add(LocationSuggestionResponse.SuggestedLocation.builder()
                    .binId(bin.getBinId())
                    .binBarcode(bin.getBinBarcode())
                    .warehouseId(bin.getWarehouseId())
                    .zone(bin.getZone())
                    .aisle(bin.getAisle())
                    .rack(bin.getRack())
                    .shelf(bin.getShelf())
                    .capacity(bin.getCapacity())
                    .availableCapacity(bin.getAvailableCapacity())
                    .usedCapacity(bin.getUsedCapacity())
                    .suggestedQuantity(suggestedQty)
                    .priority(bin.getPriority())
                    .distanceFromDispatch(bin.getDistanceFromDispatch())
                    .locationType(bin.getLocationType())
                    .zoneType(bin.getZoneType())
                    .movementType(bin.getMovementType())
                    .fullLocation(bin.getFullLocation())
                    .isAvailable(true)
                    .build());
            
            remaining -= suggestedQty;
        }

        response.setSuggestedLocations(suggested);
        
        // Build summary
        LocationSuggestionResponse.Summary summary = LocationSuggestionResponse.Summary.builder()
                .totalLocationsSuggested(suggested.size())
                .totalAvailableCapacity(suggested.stream().mapToInt(LocationSuggestionResponse.SuggestedLocation::getAvailableCapacity).sum())
                .totalQuantityAllocated(quantity - remaining)
                .remainingQuantity(remaining)
                .isFullyAllocated(remaining == 0)
                .bestLocation(suggested.isEmpty() ? null : suggested.get(0).getFullLocation())
                .build();
        response.setSummary(summary);

        return response;
    }

    @Override
    public PutawayTaskResponse getPutawayTaskByNumber(String taskNumber) {
        PutawayTask task = putawayTaskRepository.findByTaskNumber(taskNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskNumber));
        return putawayTaskMapper.toResponse(task);
    }

    @Override
    public PutawayTaskResponse getPutawayTaskByGrnNumber(String grnNumber) {
        PutawayTask task = putawayTaskRepository.findByGrnNumber(grnNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found for GRN: " + grnNumber));
        return putawayTaskMapper.toResponse(task);
    }

    @Override
    public List<PutawayTaskResponse> getPutawayTasksByStatus(String status) {
        PutawayStatus putawayStatus = PutawayStatus.valueOf(status);
        List<PutawayTask> tasks = putawayTaskRepository.findByStatus(putawayStatus);
        return tasks.stream()
                .map(putawayTaskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PutawayTaskResponse> getPutawayTasksByAssignedTo(String assignedTo) {
        List<PutawayTask> tasks = putawayTaskRepository.findByAssignedTo(assignedTo);
        return tasks.stream()
                .map(putawayTaskMapper::toResponse)
                .collect(Collectors.toList());
    }

//    @Override
//    public Page<PutawayTaskResponse> getAllPutawayTasks(Pageable pageable) {
//        return putawayTaskRepository.findAll(pageable)
//                .map(putawayTaskMapper::toResponse);
//    }
    
    
    
    
    
    @Override
    @Transactional(readOnly = true)
    public Page<PutawayTaskResponse> getAllPutawayTasks(
            String search,
            PutawayStatus status,
            PutawayStage stage,
            String grnNumber,
            String assignedTo,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {
        
        log.debug("Fetching Putaway tasks with filters - search: {}, status: {}, stage: {}, grn: {}, assignedTo: {}, startDate: {}, endDate: {}",
                search, status, stage, grnNumber, assignedTo, startDate, endDate);
        
        return putawayTaskRepository.findAll(
                PutawayTaskSpecification.filterBy(search, status, stage, grnNumber, assignedTo, startDate, endDate),
                pageable
        ).map(putawayTaskMapper::toResponse);
    }
    
    
    
    
    
    
    

    @Override
    @Transactional
    public void cancelPutawayTask(String taskNumber, String reason) {
        log.info("Cancelling putaway task: {}", taskNumber);
        
        PutawayTask task = putawayTaskRepository.findByTaskNumber(taskNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskNumber));

        if (task.getStatus() == PutawayStatus.COMPLETED || task.getStatus() == PutawayStatus.CONFIRMED) {
            throw new InvalidOperationException("Cannot cancel completed/confirmed task");
        }

        task.setStatus(PutawayStatus.CANCELLED);
        task.setRejectionReason(reason);
        putawayTaskRepository.save(task);
        log.info("✅ Task cancelled: {}", taskNumber);
    }

    // ====== Private Helper Methods ======

    private String generateTaskNumber() {
        return String.format("%s-%d", TASK_PREFIX, System.currentTimeMillis() % 1000000);
    }

    private String generateConfirmationNumber() {
        return String.format("%s-%d", CONFIRMATION_PREFIX, System.currentTimeMillis() % 1000000);
    }

    private String generateInventoryNumber() {
        return String.format("%s-%d", INVENTORY_PREFIX, System.currentTimeMillis() % 1000000);
    }

    private void generateQRCodesForTask(PutawayTask task) {
        for (PutawayLine line : task.getLines()) {
            QRCodeGenerateRequest qrRequest = QRCodeGenerateRequest.builder()
                    .qrType("QR_CODE")
                    .labelLevel("BOX")
                    .labelType("PUTAWAY")
                    .grnNumber(task.getGrnNumber())
                    .putawayTaskId(task.getId())
                    .putawayLineId(line.getId())
                    .itemCode(line.getItemCode())
                    .itemName(line.getItemName())
                    .quantity(line.getQuantity())
                    .uom(line.getUom())
                    .warehouseId(task.getWarehouseId())
                    .zone(line.getSuggestedZone())
                    .aisle(line.getSuggestedAisle())
                    .rack(line.getSuggestedRack())
                    .shelf(line.getSuggestedShelf())
                    .binId(line.getSuggestedBin())
                    .batchNumber(line.getBatchNumber())
                    .generatedBy("SYSTEM")
                    .labelFormat("PNG")
                    .build();
            
            qrCodeService.generateQRCode(qrRequest);
        }
    }

    private void updateQRCodeStatusForTask(Long taskId) {
        List<QRCodeResponse> qrCodes = qrCodeService.getQRCodesByTaskId(taskId);
        for (QRCodeResponse qrCode : qrCodes) {
            qrCodeService.updateQRCodeStatus(qrCode.getId(), "USED");
        }
    }
    
    
    
    
 // ====== FILE: src/main/java/com/warehouse/wms/service/impl/PutawayServiceImpl.java ======


 // ====== FILE: src/main/java/com/warehouse/wms/service/impl/PutawayServiceImpl.java ======

    @Override
    @Transactional
    public void updateInventoryAfterPutaway(String confirmationNumber) {
        log.info("Updating inventory for confirmation: {}", confirmationNumber);

        PutawayConfirmation confirmation = putawayConfirmationRepository
                .findByConfirmationNumber(confirmationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Confirmation not found: " + confirmationNumber));

        if (confirmation.getInventoryUpdated()) {
            log.info("Inventory already updated for confirmation: {}", confirmationNumber);
            return;
        }

        PutawayTask task = putawayTaskRepository.findById(confirmation.getPutawayTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // Update inventory for each line
        for (PutawayLine line : task.getLines()) {
            if (line.getPutawayQuantity() == null || line.getPutawayQuantity() == 0) {
                continue;
            }

            String warehouseId = line.getActualWarehouse() != null ? line.getActualWarehouse() : task.getWarehouseId();
            String zoneId = line.getActualZone();
            String aisleId = line.getActualAisle();
            String rackId = line.getActualRack();
            String levelId = line.getActualLevel();
            String binId = line.getActualBin();
            String itemCode = line.getItemCode();
            Integer quantity = line.getPutawayQuantity();

            // ====== UPDATE STOCK AVAILABILITY AT ALL HIERARCHICAL LEVELS ======
            
            // 1. Update at BIN level
            updateStockAvailability(warehouseId, zoneId, aisleId, rackId, levelId, binId, 
                                   itemCode, line.getItemName(), line.getUom(), quantity, LocationLevel.BIN);

            // 2. Update at LEVEL level
            updateStockAvailability(warehouseId, zoneId, aisleId, rackId, levelId, null, 
                                   itemCode, line.getItemName(), line.getUom(), quantity, LocationLevel.LEVEL);

            // 3. Update at RACK level
            updateStockAvailability(warehouseId, zoneId, aisleId, rackId, null, null, 
                                   itemCode, line.getItemName(), line.getUom(), quantity, LocationLevel.RACK);

            // 4. Update at AISLE level
            updateStockAvailability(warehouseId, zoneId, aisleId, null, null, null, 
                                   itemCode, line.getItemName(), line.getUom(), quantity, LocationLevel.AISLE);

            // 5. Update at ZONE level
            updateStockAvailability(warehouseId, zoneId, null, null, null, null, 
                                   itemCode, line.getItemName(), line.getUom(), quantity, LocationLevel.ZONE);

            // 6. Update at WAREHOUSE level
            updateStockAvailability(warehouseId, null, null, null, null, null, 
                                   itemCode, line.getItemName(), line.getUom(), quantity, LocationLevel.WAREHOUSE);

            // ====== UPDATE EXISTING INVENTORY STOCK (Backward Compatibility) ======
            updateInventoryStock(line, task, confirmationNumber);

            // ====== UPDATE BIN CAPACITY ======
            if (line.getActualBin() != null) {
                binLocationRepository.allocateBinCapacitySimple(line.getActualBin(), quantity);
                binLocationRepository.updateLastPutaway(line.getActualBin());
            }
        }

        // Update confirmation
        confirmation.setInventoryUpdated(true);
        confirmation.setInventoryUpdatedAt(LocalDateTime.now());
        putawayConfirmationRepository.save(confirmation);

        // Update task status
        task.setStage(PutawayStage.COMPLETED);
        task.setStatus(PutawayStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        putawayTaskRepository.save(task);

        log.info("✅ Inventory updated successfully for confirmation: {}", confirmationNumber);
    }

 /**
  * Update stock availability at a specific hierarchical level
  */
// private void updateStockAvailability(String warehouseId, String zoneId, String aisleId, 
//                                      String rackId, String levelId, String binId,
//                                      String itemCode, String itemName, String uom, 
//                                      Integer quantity, LocationLevel locationLevel) {
//     
//     Optional<StockAvailability> existingStock = stockAvailabilityRepository
//             .findByLocationAndItem(warehouseId, zoneId, aisleId, rackId, levelId, binId, 
//                                    itemCode, locationLevel);
//
//     StockAvailability stock;
//     if (existingStock.isPresent()) {
//         stock = existingStock.get();
//         stock.addQuantity(quantity);
//         stock.setUpdatedAt(LocalDateTime.now());
//         log.debug("Updated stock at {} level for item {}: +{} (total: {})", 
//             locationLevel, itemCode, quantity, stock.getTotalQuantity());
//     } else {
//         stock = StockAvailability.builder()
//                 .warehouseId(warehouseId)
//                 .zoneId(zoneId)
//                 .aisleId(aisleId)
//                 .rackId(rackId)
//                 .levelId(levelId)
//                 .binId(binId)
//                 .itemCode(itemCode)
//                 .itemName(itemName)
//                 .uom(uom)
//                 .totalQuantity(quantity)
//                 .availableQuantity(quantity)
//                 .reservedQuantity(0)
//                 .inTransitQuantity(0)
//                 .locationLevel(locationLevel)
//                 .lastPutawayDate(LocalDateTime.now())
//                 .build();
//         log.debug("Created new stock at {} level for item {}: quantity {}", 
//             locationLevel, itemCode, quantity);
//     }
//
//     stockAvailabilityRepository.save(stock);
// }

 /**
  * Update the existing InventoryStock entity (for backward compatibility)
  */
 private void updateInventoryStock(PutawayLine line, PutawayTask task, String confirmationNumber) {
     Optional<InventoryStock> existingStock = inventoryStockRepository
             .findByBinIdAndItemCode(line.getActualBin(), line.getItemCode());

     InventoryStock stock;
     if (existingStock.isPresent()) {
         stock = existingStock.get();
         stock.addQuantity(line.getPutawayQuantity());
         log.debug("Updated existing inventory stock for item {} in bin {}: +{}", 
             line.getItemCode(), line.getActualBin(), line.getPutawayQuantity());
     } else {
         String serialNumbers = line.getSerialNumber() != null ? line.getSerialNumber() : null;
         
         stock = InventoryStock.builder()
                 .inventoryNumber(generateInventoryNumber())
                 .itemCode(line.getItemCode())
                 .itemName(line.getItemName())
                 .uom(line.getUom())
                 .quantity(line.getPutawayQuantity())
                 .availableQuantity(line.getPutawayQuantity())
                 .warehouseId(line.getActualWarehouse() != null ? line.getActualWarehouse() : task.getWarehouseId())
                 .zone(line.getActualZone())
                 .aisle(line.getActualAisle())
                 .rack(line.getActualRack())
                 .level(line.getActualLevel())
                 .binId(line.getActualBin())
                 .binBarcode(line.getBinBarcode())
                 .batchNumber(line.getBatchNumber())
                 .serialNumbers(serialNumbers)
                 .grnNumber(task.getGrnNumber())
                 .putawayTaskNumber(task.getTaskNumber())
                 .confirmationNumber(confirmationNumber)
                 .status(InventoryStatus.ACTIVE)
                 .isAvailable(true)
                 .receivedDate(LocalDateTime.now())
                 .lastUpdatedDate(LocalDateTime.now())
                 .build();
         log.debug("Created new inventory stock for item {} in bin {}", 
             line.getItemCode(), line.getActualBin());
     }

     inventoryStockRepository.save(stock);
 }

 // ====== FILE: src/main/java/com/warehouse/wms/service/impl/PutawayServiceImpl.java ======
 // Update the updateStockAvailability method
//====== FILE: src/main/java/com/warehouse/wms/service/impl/PutawayServiceImpl.java ======
//Completely updated updateStockAvailability method

 private void updateStockAvailability(String warehouseId, String zoneId, String aisleId, 
         String rackId, String levelId, String binId,
         String itemCode, String itemName, String uom, 
         Integer quantity, LocationLevel locationLevel) {

Optional<StockAvailability> existingStock = stockAvailabilityRepository
.findByLocationAndItem(warehouseId, zoneId, aisleId, rackId, levelId, binId, 
         itemCode, locationLevel);

StockAvailability stock;
if (existingStock.isPresent()) {
stock = existingStock.get();
stock.addQuantity(quantity);
stock.setUpdatedAt(LocalDateTime.now());
log.debug("Updated stock at {} level for item {}: +{} (total: {})", 
locationLevel, itemCode, quantity, stock.getTotalQuantity());
} else {
// ====== Calculate max and min capacity ======
Integer maxCapacity = calculateMaxCapacity(binId, locationLevel);
Integer minCapacity = calculateMinCapacity(binId, locationLevel);

stock = StockAvailability.builder()
.warehouseId(warehouseId)
.zoneId(zoneId)
.aisleId(aisleId)
.rackId(rackId)
.levelId(levelId)
.binId(binId)
.itemCode(itemCode)
.itemName(itemName)
.uom(uom)
.totalQuantity(quantity)
.availableQuantity(quantity)
.reservedQuantity(0)
.inTransitQuantity(0)
.locationLevel(locationLevel)
.maxCapacity(maxCapacity)
.minCapacity(minCapacity)
.lastPutawayDate(LocalDateTime.now())
.build();

log.debug("Created stock at {} level - item: {}, qty: {}, maxCapacity: {}, minCapacity: {}, binId: {}", 
locationLevel, itemCode, quantity, maxCapacity, minCapacity, binId);
}

stockAvailabilityRepository.save(stock);
}

/**
* Calculate max capacity from bin using the bin's capacity fields
* Priority: 
* 1. bin.getMaxCapacity() - User defined max capacity
* 2. bin.getVolumeCm3() / 100 - Calculated from volume
* 3. bin.getMaxWeightG() / 100 - Calculated from weight
* 4. bin.getMinCapacity() - Fallback to min capacity
* 5. Default based on location level


/**
*
 
    
//====== FILE: src/main/java/com/warehouse/wms/service/impl/PutawayServiceImpl.java ======

//====== FILE: src/main/java/com/warehouse/wms/service/impl/PutawayServiceImpl.java ======

/**
* Calculate max capacity - Only uses bin.getMaxCapacity()
* If not set, uses default based on location level
*/
private Integer calculateMaxCapacity(String binId, LocationLevel locationLevel) {
 // Only calculate for BIN level
 if (locationLevel != LocationLevel.BIN || binId == null) {
     return getDefaultMaxCapacity(locationLevel);
 }
 
 try {
     Optional<Bin> binOpt = binRepository.findByBarcode(binId);
     if (binOpt.isPresent()) {
         Bin bin = binOpt.get();
         
         // ✅ PRIORITY 1: Use maxCapacity from bin entity
         if (bin.getMaxCapacity() != null && bin.getMaxCapacity() > 0) {
             log.debug("Using bin maxCapacity: {} for bin: {}", bin.getMaxCapacity(), binId);
             return bin.getMaxCapacity();
         }
         
         // ✅ PRIORITY 2: Use minCapacity as fallback if maxCapacity not set
         if (bin.getMinCapacity() != null && bin.getMinCapacity() > 0) {
             log.debug("Using bin minCapacity: {} as maxCapacity for bin: {}", bin.getMinCapacity(), binId);
             return bin.getMinCapacity();
         }
     }
 } catch (Exception e) {
     log.warn("Error calculating max capacity for binId: {}", binId, e);
 }
 
 // ✅ PRIORITY 3: Default based on location level
 return getDefaultMaxCapacity(locationLevel);
}

/**
* Calculate min capacity - Only uses bin.getMinCapacity()
* If not set, uses default based on location level
*/
private Integer calculateMinCapacity(String binId, LocationLevel locationLevel) {
 // Only calculate for BIN level
 if (locationLevel != LocationLevel.BIN || binId == null) {
     return getDefaultMinCapacity(locationLevel);
 }
 
 try {
     Optional<Bin> binOpt = binRepository.findByBarcode(binId);
     if (binOpt.isPresent()) {
         Bin bin = binOpt.get();
         
         // ✅ PRIORITY 1: Use minCapacity from bin entity
         if (bin.getMinCapacity() != null && bin.getMinCapacity() > 0) {
             log.debug("Using bin minCapacity: {} for bin: {}", bin.getMinCapacity(), binId);
             return bin.getMinCapacity();
         }
         
         // ✅ PRIORITY 2: Use maxCapacity as minCapacity if minCapacity not set
         if (bin.getMaxCapacity() != null && bin.getMaxCapacity() > 0) {
             log.debug("Using bin maxCapacity: {} as minCapacity for bin: {}", bin.getMaxCapacity(), binId);
             return bin.getMaxCapacity();
         }
     }
 } catch (Exception e) {
     log.warn("Error calculating min capacity for binId: {}", binId, e);
 }
 
 // ✅ PRIORITY 3: Default based on location level
 return getDefaultMinCapacity(locationLevel);
}

/**
* Get default max capacity for different levels
*/
private Integer getDefaultMaxCapacity(LocationLevel locationLevel) {
 switch (locationLevel) {
     case WAREHOUSE: return 100000;
     case ZONE: return 50000;
     case AISLE: return 10000;
     case RACK: return 5000;
     case LEVEL: return 1000;
     case BIN: return 100;
     default: return 1000;
 }
}

/**
* Get default min capacity for different levels
*/
private Integer getDefaultMinCapacity(LocationLevel locationLevel) {
 switch (locationLevel) {
     case WAREHOUSE: return 0;
     case ZONE: return 0;
     case AISLE: return 0;
     case RACK: return 0;
     case LEVEL: return 0;
     case BIN: return 10;
     default: return 0;
 }
}
    
}