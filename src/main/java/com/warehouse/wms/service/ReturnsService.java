package com.warehouse.wms.service;

import com.warehouse.wms.dto.*;
import com.warehouse.wms.entity.*;
import com.warehouse.wms.event.RefundEvent;
import com.warehouse.wms.exception.InventoryStateException;
import com.warehouse.wms.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import com.warehouse.wms.config.AuditLogged;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReturnsService {

    private final ReturnOrderRepository returnOrderRepository;
    private final ReturnLineRepository returnLineRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SkuRepository skuRepository;
    private final BinRepository binRepository;
    private final SkuDimensionRepository skuDimensionRepository;
    private final InventoryRepository inventoryRepository;
    private final StockBatchRepository stockBatchRepository;
    private final MovementLogRepository movementLogRepository;
    private final StateTransitionValidator stateTransitionValidator;
    private final EventBroadcastService eventBroadcastService;
    private final ApplicationEventPublisher eventPublisher;

    @AuditLogged(module = "RETURNS", action = "CREATE_RMA")
    @Transactional
    public ReturnOrderResponse createReturnOrder(ReturnOrderRequest request) {
        SalesOrder salesOrder = salesOrderRepository.findById(request.getOriginalOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Original sales order not found: " + request.getOriginalOrderId()));

        ReturnOrder returnOrder = new ReturnOrder();
        returnOrder.setOriginalOrder(salesOrder);
        returnOrder.setCustomerRef(request.getCustomerRef());
        returnOrder.setStatus(ReturnOrder.ReturnStatus.RETURN_REQUESTED);

        List<ReturnLine> lines = new ArrayList<>();
        for (ReturnLineRequest lineRequest : request.getLines()) {
            Sku sku = skuRepository.findBySkuCode(lineRequest.getSkuCode())
                    .orElseThrow(() -> new EntityNotFoundException("SKU not found: " + lineRequest.getSkuCode()));

            ReturnLine line = new ReturnLine();
            line.setReturnOrder(returnOrder);
            line.setSku(sku);
            line.setOrderedQty(lineRequest.getOrderedQty());
            line.setReturnedQty(lineRequest.getReturnedQty());
            line.setBatchNumber(lineRequest.getBatchNumber());
            lines.add(line);
        }
        returnOrder.setLines(lines);

        ReturnOrder saved = returnOrderRepository.save(returnOrder);
        eventBroadcastService.broadcastDashboardUpdate("RETURN_CREATED");
        return toResponse(saved);
    }

    public ReturnOrderResponse getReturnOrder(Long id) {
        ReturnOrder order = returnOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Return order not found: " + id));
        return toResponse(order);
    }

    public List<ReturnOrderResponse> listReturnOrders() {
        return returnOrderRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @AuditLogged(module = "RETURNS", action = "UPDATE_RMA_STATUS")
    @Transactional
    public ReturnOrderResponse updateStatus(Long id, ReturnOrder.ReturnStatus newStatus) {
        ReturnOrder order = returnOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Return order not found: " + id));

        if (!stateTransitionValidator.isValidTransition(order.getStatus(), newStatus)) {
            throw new InventoryStateException("Invalid return order status transition from " + order.getStatus() + " to " + newStatus);
        }

        order.setStatus(newStatus);
        ReturnOrder saved = returnOrderRepository.save(order);

        if (newStatus == ReturnOrder.ReturnStatus.REFUND_TRIGGERED) {
            eventPublisher.publishEvent(new RefundEvent(this, saved.getId(), saved.getOriginalOrder().getId(), "COMPLETED"));
        }

        eventBroadcastService.broadcastDashboardUpdate("RETURN_STATUS_UPDATED");
        return toResponse(saved);
    }

    @AuditLogged(module = "RETURNS", action = "GRADE_RETURN_LINE")
    @Transactional
    public ReturnOrderResponse gradeLine(Long returnOrderId, Long lineId, GradeLineRequest request) {
        ReturnOrder order = returnOrderRepository.findById(returnOrderId)
                .orElseThrow(() -> new EntityNotFoundException("Return order not found: " + returnOrderId));

        ReturnLine line = returnLineRepository.findById(lineId)
                .orElseThrow(() -> new EntityNotFoundException("Return line not found: " + lineId));

        if (!line.getReturnOrder().getId().equals(order.getId())) {
            throw new IllegalArgumentException("Line does not belong to specified return order");
        }

        line.setConditionGrade(request.getConditionGrade());
        line.setInspectionNotes(request.getInspectionNotes());
        if (request.getBatchNumber() != null && !request.getBatchNumber().trim().isEmpty()) {
            line.setBatchNumber(request.getBatchNumber());
        }

        returnLineRepository.save(line);
        return toResponse(order);
    }

    @AuditLogged(module = "RETURNS", action = "RESTOCK_RETURN_LINE")
    @Transactional
    public ReturnOrderResponse restockLine(Long returnOrderId, Long lineId, RestockLineRequest request, Long userId) {
        ReturnOrder order = returnOrderRepository.findById(returnOrderId)
                .orElseThrow(() -> new EntityNotFoundException("Return order not found: " + returnOrderId));

        ReturnLine line = returnLineRepository.findById(lineId)
                .orElseThrow(() -> new EntityNotFoundException("Return line not found: " + lineId));

        if (!line.getReturnOrder().getId().equals(order.getId())) {
            throw new IllegalArgumentException("Line does not belong to specified return order");
        }

        if (line.getConditionGrade() != ReturnLine.ConditionGrade.RESELLABLE) {
            throw new InventoryStateException("Only RESELLABLE lines can be restocked. Current grade: " + line.getConditionGrade());
        }

        if (line.getRestockedBin() != null) {
            throw new InventoryStateException("Line has already been restocked to bin: " + line.getRestockedBin().getBarcode());
        }

        Bin bin = binRepository.findByBarcode(request.getBinBarcode())
                .orElseThrow(() -> new EntityNotFoundException("Bin not found: " + request.getBinBarcode()));

        Sku sku = line.getSku();
        SkuDimension dimension = skuDimensionRepository.findBySkuId(sku.getId())
                .orElseThrow(() -> new EntityNotFoundException("SKU dimensions not found for skuId=" + sku.getId()));

        BigDecimal itemVolume = dimension.getLengthCm().multiply(dimension.getWidthCm()).multiply(dimension.getHeightCm());
        BigDecimal itemWeight = dimension.getWeightG();

        int returnedQty = line.getReturnedQty();
        BigDecimal totalVolume = itemVolume.multiply(BigDecimal.valueOf(returnedQty));
        BigDecimal totalWeight = itemWeight.multiply(BigDecimal.valueOf(returnedQty));

        BigDecimal freeVolume = bin.getVolumeCm3().subtract(bin.getOccupiedVolumeCm3());
        BigDecimal freeWeight = bin.getMaxWeightG().subtract(bin.getOccupiedWeightG());

        if (freeVolume.compareTo(totalVolume) < 0 || freeWeight.compareTo(totalWeight) < 0) {
            throw new InventoryStateException("Target bin " + bin.getBarcode() + " does not have enough capacity");
        }

        // Deduct/Add capacity
        bin.setOccupiedVolumeCm3(bin.getOccupiedVolumeCm3().add(totalVolume));
        bin.setOccupiedWeightG(bin.getOccupiedWeightG().add(totalWeight));
        if (bin.getOccupiedVolumeCm3().divide(bin.getVolumeCm3(), 4, java.math.RoundingMode.HALF_UP).compareTo(new BigDecimal("0.95")) >= 0) {
            bin.setStatus(Bin.BinStatus.FULL);
        }
        binRepository.save(bin);

        // Fetch original batch details if possible
        String batchNo = line.getBatchNumber();
        if (batchNo == null || batchNo.trim().isEmpty()) {
            batchNo = "RET-" + order.getId();
        }

        List<StockBatch> existingBatches = stockBatchRepository.findByBatchNumber(batchNo);
        LocalDateTime manufactureDate = null;
        LocalDateTime expiryDate = null;
        if (!existingBatches.isEmpty()) {
            manufactureDate = existingBatches.get(0).getManufactureDate();
            expiryDate = existingBatches.get(0).getExpiryDate();
        }

        final LocalDateTime mfgFinal = manufactureDate;
        final LocalDateTime expFinal = expiryDate;
        final String batchNoFinal = batchNo;

        // StockBatch update/creation
        StockBatch targetBatch = stockBatchRepository.findBySkuIdAndBinIdAndBatchNumber(sku.getId(), bin.getId(), batchNoFinal)
                .orElseGet(() -> {
                    StockBatch b = new StockBatch();
                    b.setSku(sku);
                    b.setBin(bin);
                    b.setBatchNumber(batchNoFinal);
                    b.setQuantity(0);
                    b.setManufactureDate(mfgFinal);
                    b.setExpiryDate(expFinal);
                    b.setStatus(StockBatch.BatchStatus.ACTIVE);
                    return b;
                });
        targetBatch.setQuantity(targetBatch.getQuantity() + returnedQty);
        stockBatchRepository.save(targetBatch);

        // Create individual inventory records
        long existingCount = inventoryRepository.countBySkuIdAndBatchNo(sku.getId(), batchNoFinal);
        for (int i = 1; i <= returnedQty; i++) {
            Inventory inv = new Inventory();
            inv.setSku(sku);
            inv.setBin(bin);
            inv.setBatchNo(batchNoFinal);
            inv.setQuantity(1);
            inv.setState(Inventory.InventoryState.AVAILABLE);
            inv.setSerialNo(sku.getSkuCode() + "-" + batchNoFinal + "-R-" + String.format("%05d", existingCount + i));
            inv.setManufactureDate(mfgFinal);
            inv.setExpiryDate(expFinal);
            Inventory savedInv = inventoryRepository.save(inv);

            // Log movement
            MovementLog movementLog = new MovementLog();
            movementLog.setInventory(savedInv);
            movementLog.setFromState(null);
            movementLog.setToState(Inventory.InventoryState.AVAILABLE);
            movementLog.setBin(bin);
            movementLog.setUserId(userId);
            movementLog.setAction("RESTOCK_RETURN");
            movementLogRepository.save(movementLog);
        }

        line.setRestockedBin(bin);
        returnLineRepository.save(line);

        eventBroadcastService.broadcastDashboardUpdate("RETURN_RESTOCKED");
        eventBroadcastService.broadcastInventoryChange(sku.getId(), "RETURN_RESTOCKED");

        return toResponse(order);
    }

    private ReturnOrderResponse toResponse(ReturnOrder order) {
        List<ReturnLineResponse> lineResponses = order.getLines().stream()
                .map(line -> ReturnLineResponse.builder()
                        .id(line.getId())
                        .skuId(line.getSku().getId())
                        .skuCode(line.getSku().getSkuCode())
                        .orderedQty(line.getOrderedQty())
                        .returnedQty(line.getReturnedQty())
                        .conditionGrade(line.getConditionGrade() != null ? line.getConditionGrade().name() : null)
                        .inspectionNotes(line.getInspectionNotes())
                        .restockedBinBarcode(line.getRestockedBin() != null ? line.getRestockedBin().getBarcode() : null)
                        .batchNumber(line.getBatchNumber())
                        .build())
                .toList();

        return ReturnOrderResponse.builder()
                .id(order.getId())
                .originalOrderId(order.getOriginalOrder().getId())
                .customerRef(order.getCustomerRef())
                .status(order.getStatus().name())
                .lines(lineResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
