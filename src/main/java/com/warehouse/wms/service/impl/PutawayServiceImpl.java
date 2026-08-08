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
import com.warehouse.wms.entity.BinLocation;
import com.warehouse.wms.entity.InboundLine;
import com.warehouse.wms.entity.InventoryStock;
import com.warehouse.wms.entity.PutawayConfirmation;
import com.warehouse.wms.entity.PutawayLine;
import com.warehouse.wms.entity.PutawayTask;
import com.warehouse.wms.exception.InvalidOperationException;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.mapper.PutawayTaskMapper;
import com.warehouse.wms.repository.BinLocationRepository;
import com.warehouse.wms.repository.InboundLineRepository;
import com.warehouse.wms.repository.InventoryStockRepository;
import com.warehouse.wms.repository.PutawayConfirmationRepository;
import com.warehouse.wms.repository.PutawayLineRepository;
import com.warehouse.wms.repository.PutawayTaskRepository;
import com.warehouse.wms.service.PutawayService;
import com.warehouse.wms.service.QRCodeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PutawayServiceImpl implements PutawayService {

    private final PutawayTaskRepository putawayTaskRepository;
    private final PutawayLineRepository putawayLineRepository;
    private final PutawayConfirmationRepository putawayConfirmationRepository;
    private final BinLocationRepository binLocationRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final InboundLineRepository inboundLineRepository; // ✅ ADD THIS
    private final QRCodeService qrCodeService;
    private final PutawayTaskMapper putawayTaskMapper;

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

        // Declare suggested outside the if block so it's accessible later
        LocationSuggestionResponse.SuggestedLocation suggested = null;
        String fullPath = null;
        
        // Set suggested location
        if (suggestion != null && suggestion.getSuggestedLocations() != null 
            && !suggestion.getSuggestedLocations().isEmpty()) {
            
            suggested = suggestion.getSuggestedLocations().get(0);
            
            fullPath = suggested.getWarehouseId() + "/" + 
                      suggested.getZone() + "/" + 
                      suggested.getAisle() + "/" + 
                      suggested.getRack() + "/" + 
                      suggested.getShelf() + "/" + 
                      suggested.getLevel();
            
            lineBuilder.suggestedWarehouse(suggested.getWarehouseId())
                       .suggestedZone(suggested.getZone())
                       .suggestedAisle(suggested.getAisle())
                       .suggestedRack(suggested.getRack())
                       .suggestedShelf(suggested.getShelf())
                       .suggestedLevel(suggested.getLevel())
                       .suggestedBin(suggested.getBinId())
                       .fullpath(fullPath);
        }

        // Build the line first so we have the fullpath
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

        if (task.getStatus() == PutawayStatus.COMPLETED || task.getStatus() == PutawayStatus.CONFIRMED) {
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
            case SCANNED:
                // Verify bin scan
                if (request.getBinBarcode() != null) {
                    BinLocation bin = binLocationRepository.findByBinBarcode(request.getBinBarcode())
                            .orElseThrow(() -> new ResourceNotFoundException("Bin not found for barcode: " + request.getBinBarcode()));
                    
                    // Update task with scanned location
                    task.setStage(PutawayStage.SCANNED);
                    task.setScannedAt(now);
                    
                    // Update line with actual bin
                    for (PutawayLine line : task.getLines()) {
                        if (line.getStatus() == PutawayLineStatus.PENDING) {
                            line.setActualWarehouse(bin.getWarehouseId());
                            line.setActualZone(bin.getZone());
                            line.setActualAisle(bin.getAisle());
                            line.setActualRack(bin.getRack());
                            line.setActualShelf(bin.getShelf());
                            line.setActualBin(bin.getBinId());
                            line.setBinBarcode(bin.getBinBarcode());
                        }
                    }
                }
                break;
            case PLACED:
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

        PutawayTask task = putawayTaskRepository.findByTaskNumber(request.getTaskNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + request.getTaskNumber()));

        if (task.getStatus() == PutawayStatus.CONFIRMED) {
            throw new InvalidOperationException("Task already confirmed");
        }

        // Process confirmation lines
        int confirmedQuantity = 0;
        for (PutawayConfirmRequest.PutawayConfirmLineRequest lineRequest : request.getLines()) {
            PutawayLine line = putawayLineRepository.findById(lineRequest.getLineId())
                    .orElseThrow(() -> new ResourceNotFoundException("Line not found: " + lineRequest.getLineId()));

            Integer qty = lineRequest.getConfirmedQuantity() != null ? 
                          lineRequest.getConfirmedQuantity() : line.getQuantity();
            
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

        // Update task
        task.setPutawayQuantity(confirmedQuantity);
        task.setPendingQuantity(task.getTotalQuantity() - confirmedQuantity);
        task.setStage(PutawayStage.CONFIRMED);
        task.setConfirmedAt(LocalDateTime.now());
        task.setConfirmedBy(request.getConfirmedBy());
        task.setStatus(PutawayStatus.CONFIRMED);
        task.setRemarks(request.getRemarks());

        // Generate confirmation number
        String confirmationNumber = generateConfirmationNumber();
        task.setConfirmationNumber(confirmationNumber);

        // Create confirmation record
        PutawayConfirmation confirmation = PutawayConfirmation.builder()
                .confirmationNumber(confirmationNumber)
                .taskNumber(task.getTaskNumber())
                .putawayTaskId(task.getId())
                .grnNumber(task.getGrnNumber())
                .confirmedBy(request.getConfirmedBy())
                .confirmedAt(LocalDateTime.now())
                .totalQuantity(task.getTotalQuantity())
                .confirmedQuantity(confirmedQuantity)
                .warehouseId(task.getWarehouseId())
                .isVerified(request.getIsVerified())
                .verifiedBy(request.getVerifiedBy())
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
                confirmation.setShelf(line.getActualShelf());
                break;
            }
        }

        putawayConfirmationRepository.save(confirmation);

        PutawayTask updatedTask = putawayTaskRepository.save(task);

        // Update inventory
        if (request.getIsVerified()) {
            updateInventoryAfterPutaway(confirmationNumber);
        }

        // Update QR Code status
        updateQRCodeStatusForTask(task.getId());

        log.info("✅ Putaway confirmed with number: {}", confirmationNumber);
        return putawayTaskMapper.toResponse(updatedTask);
    }

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

        // Check if stock exists in this bin
        Optional<InventoryStock> existingStock = inventoryStockRepository
                .findByBinIdAndItemCode(line.getActualBin(), line.getItemCode());

        InventoryStock stock;
        if (existingStock.isPresent()) {
            stock = existingStock.get();
            stock.addQuantity(line.getPutawayQuantity());
        } else {
            // Create new stock entry
            String serialNumbers = line.getSerialNumber() != null ? 
                                   line.getSerialNumber() : null;
            
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
                    .shelf(line.getActualShelf())
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
        }

        inventoryStockRepository.save(stock);

        // ✅ FIX: Update bin capacity using the simple method
        if (line.getActualBin() != null) {
            binLocationRepository.allocateBinCapacitySimple(line.getActualBin(), line.getPutawayQuantity());
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

    @Override
    public Page<PutawayTaskResponse> getAllPutawayTasks(Pageable pageable) {
        return putawayTaskRepository.findAll(pageable)
                .map(putawayTaskMapper::toResponse);
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
}