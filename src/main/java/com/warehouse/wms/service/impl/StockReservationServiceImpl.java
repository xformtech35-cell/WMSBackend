package com.warehouse.wms.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.warehouse.wms.dto.request.CustomReservationRequest;
import com.warehouse.wms.dto.request.EditReservationRequest;
import com.warehouse.wms.dto.request.ReservationItemRequest;
import com.warehouse.wms.dto.response.CustomReservationResponse;
import com.warehouse.wms.dto.response.LocationAvailability;
import com.warehouse.wms.dto.response.ReservationItemResponse;
import com.warehouse.wms.dto.response.ReservationLocationResponse;
import com.warehouse.wms.dto.response.ReservationSummaryResponse;
import com.warehouse.wms.dto.response.StockAvailabilityResponse;
import com.warehouse.wms.dto.response.StockReservationResponse;
import com.warehouse.wms.entity.InventoryStock;
import com.warehouse.wms.entity.SalesOrder;
import com.warehouse.wms.entity.SalesOrderItem;
import com.warehouse.wms.entity.StockReservation;
import com.warehouse.wms.exception.BusinessException;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.repository.InventoryStockRepository;
import com.warehouse.wms.repository.SalesOrderItemRepository;
import com.warehouse.wms.repository.SalesOrderRepository;
import com.warehouse.wms.repository.StockReservationRepository;
import com.warehouse.wms.service.StockReservationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StockReservationServiceImpl implements StockReservationService {

    private final InventoryStockRepository inventoryStockRepository;
    private final StockReservationRepository stockReservationRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;

    @Override
    public StockReservation reserveStock(String soNumber) {
        // Existing auto-reserve logic
        log.info("Reserving stock for SO: {}", soNumber);

        SalesOrder salesOrder = salesOrderRepository.findBySoNumber(soNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found: " + soNumber));
        
        
        
        List<SalesOrderItem> items = salesOrderItemRepository.findBySoNumber(soNumber);

        for (SalesOrderItem item : items) {
            List<InventoryStock> stocks = inventoryStockRepository.findByItemCode(item.getItemCode());
            int totalAvailable = stocks.stream()
                    .mapToInt(s -> s.getAvailableQuantity() != null ? s.getAvailableQuantity() : 0)
                    .sum();

            if (totalAvailable < item.getOrderedQuantity()) {
                throw new BusinessException("Insufficient stock for item: " + item.getItemCode() +
                        ". Available: " + totalAvailable + ", Required: " + item.getOrderedQuantity());
            }

            int remainingToReserve = item.getOrderedQuantity();
            for (InventoryStock stock : stocks) {
                if (remainingToReserve <= 0) break;

                if (stock.getReservedQuantity() == null) {
                    stock.setReservedQuantity(0);
                }
                if (stock.getAvailableQuantity() == null) {
                    stock.setAvailableQuantity(0);
                }

                int available = stock.getAvailableQuantity();
                int toReserve = Math.min(available, remainingToReserve);

                if (toReserve > 0) {
                    stock.reserveQuantity(toReserve);
                    inventoryStockRepository.save(stock);

                    String reservationNumber = generateReservationNumber();
                    StockReservation reservation = StockReservation.builder()
                            .reservationNumber(reservationNumber)
                            .soNumber(soNumber)
                            .itemCode(item.getItemCode())
                            .itemName(item.getItemName())
                            .uom(item.getUom())
                            .requiredQuantity(item.getOrderedQuantity())
                            .availableQuantity(available)
                            .pysicalQuantity(stock.getQuantity())
                            .reservedQuantity(toReserve)
                            .warehouseId(stock.getWarehouseId())
                            .zoneId(stock.getZone())
                            .aisleId(stock.getAisle())
                            .rackId(stock.getRack())
                            .levelId(stock.getLevel())
                            .binId(stock.getBinId())
                            .batchNumber(stock.getBatchNumber())
                            .status("RESERVED")
                            .reservationDate(LocalDateTime.now())
                            .createdBy("SYSTEM")
                            .build();
                    stockReservationRepository.save(reservation);

                    remainingToReserve -= toReserve;
                }
            }

            salesOrderItemRepository.updateReservedQuantity(item.getId(), item.getOrderedQuantity());
            
            salesOrder.setStatus("");
            
            salesOrderRepository.save(salesOrder);

        }

        return null;
    }

    // ====== CUSTOM RESERVE STOCK ======

    @Override
    public CustomReservationResponse customReserveStock(CustomReservationRequest request) {
        log.info("Custom reserving stock for SO: {}", request.getSoNumber());

        // Validate Sales Order exists
        SalesOrder salesOrder = salesOrderRepository.findBySoNumber(request.getSoNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found: " + request.getSoNumber()));

        List<ReservationSummaryResponse> summary = new ArrayList<>();
        List<ReservationItemResponse> itemResponses = new ArrayList<>();
        List<StockReservationResponse> allReservations = new ArrayList<>();

        int totalRequired = 0;
        int totalReserved = 0;
        int totalAvailable = 0;

        for (ReservationItemRequest itemRequest : request.getItems()) {
            log.info("Reserving item: {} - Quantity: {}", itemRequest.getItemCode(), itemRequest.getQuantity());

            // Check availability
            List<InventoryStock> stocks = findAvailableStocks(itemRequest);

            int availableQty = stocks.stream()
                    .mapToInt(s -> s.getAvailableQuantity() != null ? s.getAvailableQuantity() : 0)
                    .sum();

            if (availableQty < itemRequest.getQuantity()) {
                throw new BusinessException("Insufficient stock for item: " + itemRequest.getItemCode() +
                        ". Available: " + availableQty + ", Required: " + itemRequest.getQuantity());
            }

            // Reserve logic based on priority
            List<ReservationLocationResponse> locationResponses = new ArrayList<>();
            int reservedQty = 0;
            int remainingToReserve = itemRequest.getQuantity();

            // Sort stocks based on reservation priority
            List<InventoryStock> sortedStocks = sortStocksByPriority(stocks, itemRequest.getReservePriority());

            for (InventoryStock stock : sortedStocks) {
                if (remainingToReserve <= 0) break;

                if (stock.getReservedQuantity() == null) {
                    stock.setReservedQuantity(0);
                }
                if (stock.getAvailableQuantity() == null) {
                    stock.setAvailableQuantity(0);
                }

                int available = stock.getAvailableQuantity();
                int pysical = stock.getQuantity();

                int toReserve = Math.min(available, remainingToReserve);

                if (toReserve > 0) {
                    stock.reserveQuantity(toReserve);
                    inventoryStockRepository.save(stock);

                    String reservationNumber = generateReservationNumber();
                    StockReservation reservation = StockReservation.builder()
                            .reservationNumber(reservationNumber)
                            .soNumber(request.getSoNumber())
                            .itemCode(itemRequest.getItemCode())
                            .itemName(itemRequest.getItemName() != null ? 
                                    itemRequest.getItemName() : stock.getItemName())
                            .uom(itemRequest.getUom() != null ? itemRequest.getUom() : stock.getUom())
                            .requiredQuantity(itemRequest.getQuantity())
                            .availableQuantity(available)
                            .pysicalQuantity(pysical)
                            .reservedQuantity(toReserve)
                            .warehouseId(stock.getWarehouseId())
                            .zoneId(stock.getZone())
                            .aisleId(stock.getAisle())
                            .rackId(stock.getRack())
                            .levelId(stock.getLevel())
                            .binId(stock.getBinId())
                            .batchNumber(itemRequest.getBatchNumber() != null ? 
                                    itemRequest.getBatchNumber() : stock.getBatchNumber())
                            .status("RESERVED")
                            .reservationDate(LocalDateTime.now())
                            .createdBy(request.getCreatedBy() != null ? 
                                    request.getCreatedBy() : "SYSTEM")
                            .build();
                    stockReservationRepository.save(reservation);

                    allReservations.add(buildStockReservationResponse(reservation));

                    // Add location response
                    locationResponses.add(ReservationLocationResponse.builder()
                            .warehouseId(stock.getWarehouseId())
                            .zoneId(stock.getZone())
                            .aisleId(stock.getAisle())
                            .rackId(stock.getRack())
                            .levelId(stock.getLevel())
                            .binId(stock.getBinId())
                            .reservedQuantity(toReserve)
                            .availableQuantity(stock.getAvailableQuantity())
                            .pysicalQuantity(stock.getQuantity())
                            .batchNumber(stock.getBatchNumber())
                            .build());

                    reservedQty += toReserve;
                    remainingToReserve -= toReserve;
                }
            }

            // Update Sales Order Item reserved quantity
            List<SalesOrderItem> orderItems = salesOrderItemRepository.findBySoNumber(request.getSoNumber());
            orderItems.stream()
                    .filter(oi -> oi.getItemCode().equals(itemRequest.getItemCode()))
                    .findFirst()
                    .ifPresent(oi -> {
                        salesOrderItemRepository.updateReservedQuantity(oi.getId(), oi.getOrderedQuantity());
                    });

            // Build item response
            ReservationItemResponse itemResponse = ReservationItemResponse.builder()
                    .itemCode(itemRequest.getItemCode())
                    .itemName(itemRequest.getItemName())
                    .uom(itemRequest.getUom())
                    .requiredQuantity(itemRequest.getQuantity())
                    .reservedQuantity(reservedQty)
                    .availableQuantity(availableQty)
                    
                    .batchNumber(itemRequest.getBatchNumber())
                    .status(reservedQty >= itemRequest.getQuantity() ? "FULLY_RESERVED" : 
                            reservedQty > 0 ? "PARTIALLY_RESERVED" : "NOT_RESERVED")
                    .locations(locationResponses)
                    .build();
            itemResponses.add(itemResponse);

            // Build summary
            summary.add(ReservationSummaryResponse.builder()
                    .itemCode(itemRequest.getItemCode())
                    .itemName(itemRequest.getItemName())
                    .requested(itemRequest.getQuantity())
                    .reserved(reservedQty)
                    .shortQuantity(itemRequest.getQuantity() - reservedQty)
                    .status(reservedQty >= itemRequest.getQuantity() ? "FULLY_RESERVED" : 
                            reservedQty > 0 ? "PARTIALLY_RESERVED" : "NOT_RESERVED")
                    .build());

            totalRequired += itemRequest.getQuantity();
            totalReserved += reservedQty;
            totalAvailable += availableQty;
        }

        // Update Sales Order status
        salesOrder.setStatus("PROCESSING");
        salesOrderRepository.save(salesOrder);

        log.info("Custom stock reservation completed for SO: {}", request.getSoNumber());

        return CustomReservationResponse.builder()
                .soNumber(request.getSoNumber())
                .warehouseId(request.getWarehouseId())
                .status("RESERVED")
                .reservationDate(LocalDateTime.now())
                .createdBy(request.getCreatedBy())
                .totalItems(request.getItems().size())
                .totalRequiredQuantity(totalRequired)
                .totalReservedQuantity(totalReserved)
                .totalAvailableQuantity(totalAvailable)
                .items(itemResponses)
                .reservations(allReservations)
                .summary(summary)
                .build();
    }

    // ====== RESERVE FROM SPECIFIC BIN ======

    @Override
    public StockReservation reserveFromBin(String soNumber, String itemCode, String binId, Integer quantity) {
        log.info("Reserving from bin: {} for SO: {} - Item: {} - Qty: {}", binId, soNumber, itemCode, quantity);

        InventoryStock stock = inventoryStockRepository.findByItemCodeAndBinId(itemCode, binId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found for item: " + itemCode + " at bin: " + binId));

        if (stock.getAvailableQuantity() < quantity) {
            throw new BusinessException("Insufficient stock at bin. Available: " + 
                    stock.getAvailableQuantity() + ", Requested: " + quantity);
        }

        stock.reserveQuantity(quantity);
        inventoryStockRepository.save(stock);

        String reservationNumber = generateReservationNumber();
        StockReservation reservation = StockReservation.builder()
                .reservationNumber(reservationNumber)
                .soNumber(soNumber)
                .itemCode(itemCode)
                .itemName(stock.getItemName())
                .uom(stock.getUom())
                .requiredQuantity(quantity)
                .availableQuantity(stock.getAvailableQuantity())
                .pysicalQuantity(stock.getQuantity())
                .reservedQuantity(quantity)
                .warehouseId(stock.getWarehouseId())
                .zoneId(stock.getZone())
                .aisleId(stock.getAisle())
                .rackId(stock.getRack())
                .levelId(stock.getLevel())
                .binId(stock.getBinId())
                .batchNumber(stock.getBatchNumber())
                .status("RESERVED")
                .reservationDate(LocalDateTime.now())
                .createdBy("SYSTEM")
                .build();

        return stockReservationRepository.save(reservation);
    }

    // ====== RESERVE FROM SPECIFIC INVENTORY STOCK ======

    @Override
    public StockReservation reserveFromInventory(String soNumber, Long inventoryStockId, Integer quantity) {
        log.info("Reserving from inventory stock: {} for SO: {}", inventoryStockId, soNumber);

        InventoryStock stock = inventoryStockRepository.findById(inventoryStockId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory stock not found: " + inventoryStockId));

        if (stock.getAvailableQuantity() < quantity) {
            throw new BusinessException("Insufficient stock. Available: " + 
                    stock.getAvailableQuantity() + ", Requested: " + quantity);
        }

        stock.reserveQuantity(quantity);
        inventoryStockRepository.save(stock);

        String reservationNumber = generateReservationNumber();
        StockReservation reservation = StockReservation.builder()
                .reservationNumber(reservationNumber)
                .soNumber(soNumber)
                .itemCode(stock.getItemCode())
                .itemName(stock.getItemName())
                .uom(stock.getUom())
                .requiredQuantity(quantity)
                .availableQuantity(stock.getAvailableQuantity())
                .pysicalQuantity(stock.getQuantity())
                .reservedQuantity(quantity)
                .warehouseId(stock.getWarehouseId())
                .zoneId(stock.getZone())
                .aisleId(stock.getAisle())
                .rackId(stock.getRack())
                .levelId(stock.getLevel())
                .binId(stock.getBinId())
                .batchNumber(stock.getBatchNumber())
                .status("RESERVED")
                .reservationDate(LocalDateTime.now())
                .createdBy("SYSTEM")
                .build();

        return stockReservationRepository.save(reservation);
    }

    // ====== RELEASE RESERVATIONS ======

    @Override
    public void releaseReservation(String reservationNumber) {
        StockReservation reservation = stockReservationRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + reservationNumber));

        if ("CANCELLED".equals(reservation.getStatus()) || "RELEASED".equals(reservation.getStatus())) {
            throw new BusinessException("Reservation already " + reservation.getStatus());
        }

        // Release stock
        List<InventoryStock> stocks = inventoryStockRepository.findByItemCode(reservation.getItemCode());
        int toRelease = reservation.getReservedQuantity();

        for (InventoryStock stock : stocks) {
            if (toRelease <= 0) break;
            if (stock.getBinId() != null && stock.getBinId().equals(reservation.getBinId())) {
                int available = stock.getReservedQuantity() != null ? stock.getReservedQuantity() : 0;
                int release = Math.min(available, toRelease);
                stock.unreserveQuantity(release);
                inventoryStockRepository.save(stock);
                toRelease -= release;
            }
        }

        reservation.setStatus("CANCELLED");
        stockReservationRepository.save(reservation);
        log.info("Reservation released: {}", reservationNumber);
    }

    @Override
    public void releaseAllReservations(String soNumber) {
        List<StockReservation> reservations = stockReservationRepository.findBySoNumber(soNumber);
        for (StockReservation reservation : reservations) {
            releaseReservation(reservation.getReservationNumber());
        }
        log.info("All reservations released for SO: {}", soNumber);
    }

    // ====== GET RESERVATIONS ======

    @Override
    public List<StockReservation> getReservationsBySoNumber(String soNumber) {
        return stockReservationRepository.findBySoNumber(soNumber);
    }

    // ====== CHECK AVAILABILITY ======

    @Override
    public StockAvailabilityResponse checkAvailability(String itemCode, Integer requestedQuantity) {
        List<InventoryStock> stocks = inventoryStockRepository.findByItemCode(itemCode);
        int totalAvailable = stocks.stream()
                .mapToInt(s -> s.getAvailableQuantity() != null ? s.getAvailableQuantity() : 0)
                .sum();
        int totalReserved = stocks.stream()
                .mapToInt(s -> s.getReservedQuantity() != null ? s.getReservedQuantity() : 0)
                .sum();

        List<LocationAvailability> locationAvailability = stocks.stream()
                .map(s -> LocationAvailability.builder()
                        .warehouseId(s.getWarehouseId())
                        .zone(s.getZone())
                        .aisle(s.getAisle())
                        .rack(s.getRack())
                        .level(s.getLevel())
                        .binId(s.getBinId())
                        .availableQuantity(s.getAvailableQuantity() != null ? s.getAvailableQuantity() : 0)
                        .reservedQuantity(s.getReservedQuantity() != null ? s.getReservedQuantity() : 0)
                        .build())
                .collect(Collectors.toList());

        return StockAvailabilityResponse.builder()
                .itemCode(itemCode)
                .totalAvailable(totalAvailable)
                .totalReserved(totalReserved)
                .requestedQuantity(requestedQuantity)
                .isAvailable(totalAvailable >= requestedQuantity)
                .locationAvailability(locationAvailability)
                .build();
    }

    // ====== HELPER METHODS ======

    private List<InventoryStock> findAvailableStocks(ReservationItemRequest request) {
        if (request.getInventoryStockId() != null) {
            return inventoryStockRepository.findById(request.getInventoryStockId())
                    .map(List::of)
                    .orElse(Collections.emptyList());
        }

        if (StringUtils.hasText(request.getBinId())) {
            return inventoryStockRepository.findByItemCodeAndBinId(request.getItemCode(), request.getBinId())
                    .map(List::of)
                    .orElse(Collections.emptyList());
        }

        return inventoryStockRepository.findByItemCode(request.getItemCode());
    }

    private List<InventoryStock> sortStocksByPriority(List<InventoryStock> stocks, String priority) {
        List<InventoryStock> sorted = new ArrayList<>(stocks);
        
        if ("FIFO".equalsIgnoreCase(priority)) {
            // First In First Out - sort by received date
            sorted.sort(Comparator.comparing(InventoryStock::getReceivedDate, 
                    Comparator.nullsLast(Comparator.naturalOrder())));
        } else if ("LIFO".equalsIgnoreCase(priority)) {
            // Last In First Out - sort by received date descending
            sorted.sort(Comparator.comparing(InventoryStock::getReceivedDate, 
                    Comparator.nullsFirst(Comparator.reverseOrder())));
        } else if ("SPECIFIC".equalsIgnoreCase(priority)) {
            // Already sorted by specific bin/location
            // Keep as is
        }
        return sorted;
    }

    private String generateReservationNumber() {
        return "RES-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private StockReservationResponse buildStockReservationResponse(StockReservation reservation) {
        return StockReservationResponse.builder()
                .id(reservation.getId())
                .reservationNumber(reservation.getReservationNumber())
                .soNumber(reservation.getSoNumber())
//                .itemCode(reservation.getItemCode())
//                .itemName(reservation.getItemName())
//                .uom(reservation.getUom())
                .requiredQuantity(reservation.getRequiredQuantity())
                .availableQuantity(reservation.getAvailableQuantity())
                .pysicalQuantity(reservation.getPysicalQuantity())
                .reservedQuantity(reservation.getReservedQuantity())
                .warehouseId(reservation.getWarehouseId())
                .zoneId(reservation.getZoneId())
                .aisleId(reservation.getAisleId())
                .rackId(reservation.getRackId())
                .levelId(reservation.getLevelId())
                .binId(reservation.getBinId())
                .batchNumber(reservation.getBatchNumber())
                .status(reservation.getStatus())
                .reservationDate(reservation.getReservationDate())
                .expiryDate(reservation.getExpiryDate())
                .remarks(reservation.getRemarks())
                .createdBy(reservation.getCreatedBy())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @Override
    public StockReservationResponse editReservation(EditReservationRequest request) {
        log.info("Editing reservation: {}", request.getReservationNumber());

        StockReservation reservation = stockReservationRepository.findByReservationNumber(request.getReservationNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + request.getReservationNumber()));

        // Check if reservation can be edited
        if ("CANCELLED".equals(reservation.getStatus()) || "RELEASED".equals(reservation.getStatus())) {
            throw new BusinessException("Cannot edit cancelled or released reservation");
        }

        // Validate new quantity
        if (request.getQuantity() != null && request.getQuantity() > 0) {
            updateReservationQuantity(reservation, request.getQuantity());
        }

        // Update batch number
        if (request.getBatchNumber() != null) {
            reservation.setBatchNumber(request.getBatchNumber());
        }

        // Update bin
        if (request.getBinId() != null) {
            updateReservationBin(reservation, request.getBinId());
        }

        // Update remarks
        if (request.getRemarks() != null) {
            reservation.setRemarks(request.getRemarks());
        }

        reservation.setUpdatedBy(request.getUpdatedBy() != null ? request.getUpdatedBy() : "SYSTEM");
        reservation.setUpdatedAt(LocalDateTime.now());

        StockReservation updated = stockReservationRepository.save(reservation);
        log.info("Reservation updated successfully: {}", updated.getReservationNumber());

        return buildStockReservationResponse(updated);
    }

    @Override
    public StockReservationResponse editReservationQuantity(String reservationNumber, Integer newQuantity) {
        log.info("Editing reservation quantity: {} to {}", reservationNumber, newQuantity);

        if (newQuantity <= 0) {
            throw new BusinessException("Quantity must be greater than 0");
        }

        StockReservation reservation = stockReservationRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + reservationNumber));

        if ("CANCELLED".equals(reservation.getStatus()) || "RELEASED".equals(reservation.getStatus())) {
            throw new BusinessException("Cannot edit cancelled or released reservation");
        }

        updateReservationQuantity(reservation, newQuantity);
        reservation.setUpdatedBy("SYSTEM");
        reservation.setUpdatedAt(LocalDateTime.now());

        StockReservation updated = stockReservationRepository.save(reservation);
        log.info("Reservation quantity updated: {}", updated.getReservationNumber());

        return buildStockReservationResponse(updated);
    }

    @Override
    public StockReservationResponse editReservationLocation(String reservationNumber, String newBinId) {
        log.info("Editing reservation location: {} to bin: {}", reservationNumber, newBinId);

        StockReservation reservation = stockReservationRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + reservationNumber));

        if ("CANCELLED".equals(reservation.getStatus()) || "RELEASED".equals(reservation.getStatus())) {
            throw new BusinessException("Cannot edit cancelled or released reservation");
        }

        updateReservationBin(reservation, newBinId);
        reservation.setUpdatedBy("SYSTEM");
        reservation.setUpdatedAt(LocalDateTime.now());

        StockReservation updated = stockReservationRepository.save(reservation);
        log.info("Reservation location updated: {}", updated.getReservationNumber());

        return buildStockReservationResponse(updated);
    }

    @Override
    public StockReservationResponse editReservationBatch(String reservationNumber, String batchNumber) {
        log.info("Editing reservation batch: {} to {}", reservationNumber, batchNumber);

        StockReservation reservation = stockReservationRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + reservationNumber));

        if ("CANCELLED".equals(reservation.getStatus()) || "RELEASED".equals(reservation.getStatus())) {
            throw new BusinessException("Cannot edit cancelled or released reservation");
        }

        reservation.setBatchNumber(batchNumber);
        reservation.setUpdatedBy("SYSTEM");
        reservation.setUpdatedAt(LocalDateTime.now());

        StockReservation updated = stockReservationRepository.save(reservation);
        log.info("Reservation batch updated: {}", updated.getReservationNumber());

        return buildStockReservationResponse(updated);
    }

    @Override
    public StockReservationResponse editReservationRemarks(String reservationNumber, String remarks) {
        log.info("Editing reservation remarks: {}", reservationNumber);

        StockReservation reservation = stockReservationRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + reservationNumber));

        if ("CANCELLED".equals(reservation.getStatus()) || "RELEASED".equals(reservation.getStatus())) {
            throw new BusinessException("Cannot edit cancelled or released reservation");
        }

        reservation.setRemarks(remarks);
        reservation.setUpdatedBy("SYSTEM");
        reservation.setUpdatedAt(LocalDateTime.now());

        StockReservation updated = stockReservationRepository.save(reservation);
        log.info("Reservation remarks updated: {}", updated.getReservationNumber());

        return buildStockReservationResponse(updated);
    }

   

    @Override
    public StockReservationResponse extendReservationExpiry(String reservationNumber, LocalDateTime newExpiryDate) {
        log.info("Extending reservation expiry: {} to {}", reservationNumber, newExpiryDate);

        StockReservation reservation = stockReservationRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + reservationNumber));

        if ("CANCELLED".equals(reservation.getStatus()) || "RELEASED".equals(reservation.getStatus())) {
            throw new BusinessException("Cannot edit cancelled or released reservation");
        }

        reservation.setExpiryDate(newExpiryDate);
        reservation.setUpdatedBy("SYSTEM");
        reservation.setUpdatedAt(LocalDateTime.now());

        StockReservation updated = stockReservationRepository.save(reservation);
        log.info("Reservation expiry extended: {}", updated.getReservationNumber());

        return buildStockReservationResponse(updated);
    }

    // ====== PRIVATE HELPER METHODS ======

    private void updateReservationQuantity(StockReservation reservation, Integer newQuantity) {
        int currentReserved = reservation.getReservedQuantity();
        int difference = newQuantity - currentReserved;

        if (difference == 0) {
            return;
        }

        if (difference > 0) {
            // Need to reserve more - check availability
            List<InventoryStock> stocks = inventoryStockRepository.findByItemCode(reservation.getItemCode());
            int totalAvailable = stocks.stream()
                    .mapToInt(s -> s.getAvailableQuantity() != null ? s.getAvailableQuantity() : 0)
                    .sum();

            if (totalAvailable < difference) {
                throw new BusinessException("Insufficient stock to increase reservation. Available: " + 
                        totalAvailable + ", Required additional: " + difference);
            }

            // Reserve additional quantity
            int remainingToReserve = difference;
            for (InventoryStock stock : stocks) {
                if (remainingToReserve <= 0) break;

                if (stock.getReservedQuantity() == null) {
                    stock.setReservedQuantity(0);
                }
                if (stock.getAvailableQuantity() == null) {
                    stock.setAvailableQuantity(0);
                }

                int available = stock.getAvailableQuantity();
                int toReserve = Math.min(available, remainingToReserve);

                if (toReserve > 0) {
                    stock.reserveQuantity(toReserve);
                    inventoryStockRepository.save(stock);
                    remainingToReserve -= toReserve;
                }
            }
        } else {
            // Need to release some reservation
            int toRelease = Math.abs(difference);
            List<InventoryStock> stocks = inventoryStockRepository.findByItemCode(reservation.getItemCode());

            for (InventoryStock stock : stocks) {
                if (toRelease <= 0) break;
                if (stock.getBinId() != null && stock.getBinId().equals(reservation.getBinId())) {
                    int reserved = stock.getReservedQuantity() != null ? stock.getReservedQuantity() : 0;
                    int release = Math.min(reserved, toRelease);
                    stock.unreserveQuantity(release);
                    inventoryStockRepository.save(stock);
                    toRelease -= release;
                }
            }
        }

        reservation.setRequiredQuantity(newQuantity);
        reservation.setReservedQuantity(newQuantity);
    }

    private void updateReservationBin(StockReservation reservation, String newBinId) {
        // Find stock at new bin
        InventoryStock newStock = inventoryStockRepository.findByItemCodeAndBinId(
                reservation.getItemCode(), newBinId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found at bin: " + newBinId));

        // Release from old bin
        List<InventoryStock> oldStocks = inventoryStockRepository.findByItemCode(reservation.getItemCode());
        int toRelease = reservation.getReservedQuantity();

        for (InventoryStock stock : oldStocks) {
            if (toRelease <= 0) break;
            if (stock.getBinId() != null && stock.getBinId().equals(reservation.getBinId())) {
                int reserved = stock.getReservedQuantity() != null ? stock.getReservedQuantity() : 0;
                int release = Math.min(reserved, toRelease);
                stock.unreserveQuantity(release);
                inventoryStockRepository.save(stock);
                toRelease -= release;
            }
        }

        // Reserve from new bin
        if (newStock.getReservedQuantity() == null) {
            newStock.setReservedQuantity(0);
        }
        if (newStock.getAvailableQuantity() == null) {
            newStock.setAvailableQuantity(0);
        }

        if (newStock.getAvailableQuantity() < reservation.getReservedQuantity()) {
            throw new BusinessException("Insufficient stock at new bin. Available: " + 
                    newStock.getAvailableQuantity() + ", Required: " + reservation.getReservedQuantity());
        }

        newStock.reserveQuantity(reservation.getReservedQuantity());
        inventoryStockRepository.save(newStock);

        // Update reservation with new bin details
        reservation.setBinId(newBinId);
        reservation.setWarehouseId(newStock.getWarehouseId());
        reservation.setZoneId(newStock.getZone());
        reservation.setAisleId(newStock.getAisle());
        reservation.setRackId(newStock.getRack());
        reservation.setLevelId(newStock.getLevel());
        reservation.setBatchNumber(newStock.getBatchNumber());
    }

   
}