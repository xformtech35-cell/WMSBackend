package com.warehouse.wms.service.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.zxing.WriterException;
import com.warehouse.wms.dto.request.BarcodeScanRequest;
import com.warehouse.wms.dto.request.DeliveryRequest;
import com.warehouse.wms.dto.request.DispatchRequest;
import com.warehouse.wms.dto.request.PackageRequest;
import com.warehouse.wms.dto.request.PickConfirmationRequest;
import com.warehouse.wms.dto.request.PickListItemRequest;
import com.warehouse.wms.dto.request.PickListRequest;
import com.warehouse.wms.dto.request.PickTaskRequest;
import com.warehouse.wms.dto.request.SalesOrderItemRequest;
import com.warehouse.wms.dto.request.SalesOrderItemUpdateRequest;
import com.warehouse.wms.dto.request.SalesOrderRequest;
import com.warehouse.wms.dto.request.ShipmentConfirmationRequest;
import com.warehouse.wms.dto.response.BarcodeScanResponse;
import com.warehouse.wms.dto.response.DeliveryResponse;
import com.warehouse.wms.dto.response.DispatchResponse;
import com.warehouse.wms.dto.response.LabelImageResponse;
import com.warehouse.wms.dto.response.PackageResponse;
import com.warehouse.wms.dto.response.PickConfirmationResponse;
import com.warehouse.wms.dto.response.PickListItemResponse;
import com.warehouse.wms.dto.response.PickListResponse;
import com.warehouse.wms.dto.response.PickTaskResponse;
import com.warehouse.wms.dto.response.QrCodeResponses;
import com.warehouse.wms.dto.response.SalesOrderItemResponse;
import com.warehouse.wms.dto.response.SalesOrderResponse;
import com.warehouse.wms.dto.response.ShipmentConfirmationResponse;
import com.warehouse.wms.dto.response.ShippingLabelBarcodeResponse;
import com.warehouse.wms.dto.response.ShippingLabelResponse;
import com.warehouse.wms.dto.response.StockReservationResponse;
import com.warehouse.wms.entity.Delivery;
import com.warehouse.wms.entity.Dispatch;
import com.warehouse.wms.entity.InventoryStock;
import com.warehouse.wms.entity.PackageInfo;
import com.warehouse.wms.entity.PickConfirmation;
import com.warehouse.wms.entity.PickList;
import com.warehouse.wms.entity.PickListItem;
import com.warehouse.wms.entity.PickTask;
import com.warehouse.wms.entity.SalesOrder;
import com.warehouse.wms.entity.SalesOrderItem;
import com.warehouse.wms.entity.ShipmentConfirmation;
import com.warehouse.wms.entity.ShippingLabel;
import com.warehouse.wms.entity.StockReservation;
import com.warehouse.wms.exception.BusinessException;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.repository.DeliveryRepository;
import com.warehouse.wms.repository.DispatchRepository;
import com.warehouse.wms.repository.InventoryStockRepository;
import com.warehouse.wms.repository.PackageInfoRepository;
import com.warehouse.wms.repository.PickConfirmationRepository;
import com.warehouse.wms.repository.PickListItemRepository;
import com.warehouse.wms.repository.PickListRepository;
import com.warehouse.wms.repository.PickTaskRepository;
import com.warehouse.wms.repository.SalesOrderItemRepository;
import com.warehouse.wms.repository.SalesOrderRepository;
import com.warehouse.wms.repository.ShipmentConfirmationRepository;
import com.warehouse.wms.repository.ShippingLabelRepository;
import com.warehouse.wms.repository.StockReservationRepository;
import com.warehouse.wms.service.OutboundService;
import com.warehouse.wms.util.BarcodeUtils;
import com.warehouse.wms.util.SoNumberGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OutboundServiceImpl implements OutboundService {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final StockReservationRepository stockReservationRepository;
    private final PickListRepository pickListRepository;
    private final PickListItemRepository pickListItemRepository;
    private final PickTaskRepository pickTaskRepository;
    private final PickConfirmationRepository pickConfirmationRepository;
    private final PackageInfoRepository packageInfoRepository;
    private final ShippingLabelRepository shippingLabelRepository;
    private final DispatchRepository dispatchRepository;
    private final ShipmentConfirmationRepository shipmentConfirmationRepository;
    private final DeliveryRepository deliveryRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final SoNumberGenerator soNumberGenerator;
    private final BarcodeUtils barcodeUtils;

    // ============================================================
    // ===================== SALES ORDER ===========================
    // ============================================================

    @Override
    public SalesOrderResponse createSalesOrder(SalesOrderRequest request) {
        String soNumber = soNumberGenerator.generateSoNumber();
        log.info("Auto-generated SO Number: {}", soNumber);

//        if (salesOrderRepository.findBySoNumber(soNumber).isPresent()) {
//            throw new BusinessException("Sales Order already exists: " + soNumber);
//        }

//        int totalQuantity = 0;
//        for (SalesOrderItemRequest itemReq : request.getItems()) {
//            List<InventoryStock> stocks = inventoryStockRepository.findByItemCode(itemReq.getItemCode());
//            if (stocks.isEmpty()) {
//                throw new BusinessException("Item not found in inventory: " + itemReq.getItemCode());
//            }
//            totalQuantity += itemReq.getOrderedQuantity();
//        }

        SalesOrder salesOrder = SalesOrder.builder()
                .soNumber(soNumber)
                .orderDate(request.getSoDate() != null ? request.getSoDate() : LocalDateTime.now())
                .customerCode(request.getCustomerCode())
                .customerName(request.getCustomerName())
                .warehouseId(request.getWarehouseId())
                .deliveryDate(request.getDeliveryDate())
                .priority(request.getPriority() != null ? request.getPriority() : "MEDIUM")
                .deliveryAddress(request.getDeliveryAddress())
//                .totalQuantity(totalQuantity)
                .shippingMethod(request.getShippingMethod())
                .status("DRAFT")
                .remarks(request.getRemarks())
                .createdBy(request.getCreatedBy())
                .build();

        SalesOrder savedOrder = salesOrderRepository.save(salesOrder);

        List<SalesOrderItem> items = new ArrayList<>();
        for (SalesOrderItemRequest itemReq : request.getItems()) {
            SalesOrderItem item = SalesOrderItem.builder()
                    .soNumber(soNumber)
                    .itemCode(itemReq.getItemCode())
                    .itemName(itemReq.getItemName())
                    .uom(itemReq.getUom() != null ? itemReq.getUom() : "EA")
                    .orderedQuantity(itemReq.getOrderedQuantity())
                    .reservedQuantity(0)
                    .pickedQuantity(0)
                    .shippedQuantity(0)
                    .batchNumber(itemReq.getBatchNumber())
                    .salesOrder(savedOrder)
                    .createdBy(request.getCreatedBy())
                    .build();
            items.add(salesOrderItemRepository.save(item));
        }

        log.info("Sales Order created successfully: {}", soNumber);
        return buildSalesOrderResponse(savedOrder, items);
    }

    @Override
    public SalesOrderResponse getSalesOrderByNumber(String soNumber) {
        SalesOrder salesOrder = salesOrderRepository.findBySoNumber(soNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found: " + soNumber));

        List<SalesOrderItem> items = salesOrderItemRepository.findBySoNumber(soNumber);
        return buildSalesOrderResponse(salesOrder, items);
    }

    @Override
    public Page<SalesOrderResponse> getAllSalesOrdersWithFilters(
            String search, String soNumber, String customerCode, String customerName,
            String warehouseId, String status, String priority,
            LocalDateTime startDate, LocalDateTime endDate,
            LocalDateTime startCreatedDate, LocalDateTime endCreatedDate,
            LocalDateTime startDeliveryDate, LocalDateTime endDeliveryDate,
            Integer minQuantity, Integer maxQuantity,
            String shippingMethod, String createdBy, Pageable pageable) {

        log.info("Fetching sales orders with filters");

        if (StringUtils.hasText(search)) {
            return salesOrderRepository.searchSalesOrders(search, pageable)
                    .map(order -> buildSalesOrderResponse(order,
                            salesOrderItemRepository.findBySoNumber(order.getSoNumber())));
        }

        Page<SalesOrder> orderPage = salesOrderRepository.findByFilters(
                soNumber, customerCode, customerName, warehouseId,
                status, priority, startDate, endDate,
                startCreatedDate, endCreatedDate,
                startDeliveryDate, endDeliveryDate,
                minQuantity, maxQuantity, shippingMethod, createdBy, pageable);

        return orderPage.map(order -> buildSalesOrderResponse(order,
                salesOrderItemRepository.findBySoNumber(order.getSoNumber())));
    }

    @Override
    public SalesOrderResponse updateSalesOrder(String soNumber, SalesOrderRequest request) {
        log.info("Updating Sales Order: {}", soNumber);

        SalesOrder salesOrder = salesOrderRepository.findBySoNumber(soNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found: " + soNumber));

        if (!salesOrder.getStatus().equals("DRAFT") && !salesOrder.getStatus().equals("CONFIRMED") &&
            !salesOrder.getStatus().equals("PROCESSING")) {
            throw new BusinessException("Cannot edit order in status: " + salesOrder.getStatus());
        }

        if (request.getCustomerCode() != null) salesOrder.setCustomerCode(request.getCustomerCode());
        if (request.getCustomerName() != null) salesOrder.setCustomerName(request.getCustomerName());
        if (request.getWarehouseId() != null) salesOrder.setWarehouseId(request.getWarehouseId());
        if (request.getDeliveryDate() != null) salesOrder.setDeliveryDate(request.getDeliveryDate());
        if (request.getPriority() != null) salesOrder.setPriority(request.getPriority());
        if (request.getDeliveryAddress() != null) salesOrder.setDeliveryAddress(request.getDeliveryAddress());
        if (request.getShippingMethod() != null) salesOrder.setShippingMethod(request.getShippingMethod());
        if (request.getRemarks() != null) salesOrder.setRemarks(request.getRemarks());
        if (request.getSoDate() != null) salesOrder.setOrderDate(request.getSoDate());

        salesOrder.setUpdatedBy(request.getCreatedBy() != null ? request.getCreatedBy() : "SYSTEM");
        salesOrder.setUpdatedAt(LocalDateTime.now());

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            salesOrderItemRepository.deleteBySoNumber(soNumber);

            List<SalesOrderItem> items = new ArrayList<>();
            int totalQuantity = 0;

            for (SalesOrderItemRequest itemReq : request.getItems()) {
                SalesOrderItem item = SalesOrderItem.builder()
                        .soNumber(soNumber)
                        .itemCode(itemReq.getItemCode())
                        .itemName(itemReq.getItemName())
                        .uom(itemReq.getUom() != null ? itemReq.getUom() : "EA")
                        .orderedQuantity(itemReq.getOrderedQuantity())
                        .reservedQuantity(0)
                        .pickedQuantity(0)
                        .shippedQuantity(0)
                        .batchNumber(itemReq.getBatchNumber())
                        .sourceLocation(itemReq.getSourceLocation())
                        .salesOrder(salesOrder)
                        .createdBy(request.getCreatedBy())
                        .build();
                items.add(salesOrderItemRepository.save(item));
                totalQuantity += itemReq.getOrderedQuantity();
            }

            salesOrder.setTotalQuantity(totalQuantity);
        }

        SalesOrder updated = salesOrderRepository.save(salesOrder);
        log.info("Sales Order updated successfully: {}", soNumber);
        return buildSalesOrderResponse(updated, salesOrderItemRepository.findBySoNumber(soNumber));
    }

    @Override
    public SalesOrderResponse updateSalesOrderStatus(String soNumber, String status) {
        log.info("Updating Sales Order status: {} to {}", soNumber, status);

        SalesOrder salesOrder = salesOrderRepository.findBySoNumber(soNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found: " + soNumber));

        // FIX: Pass SO number and both statuses
        validateStatusTransition(soNumber, salesOrder.getStatus(), status);

        String oldStatus = salesOrder.getStatus();

        salesOrder.setStatus(status);
        salesOrder.setUpdatedBy("SYSTEM");
        salesOrder.setUpdatedAt(LocalDateTime.now());

        SalesOrder updated = salesOrderRepository.save(salesOrder);

        handleStatusActions(salesOrder.getSoNumber(), status);

        log.info("Sales Order status updated successfully: {} -> {}", oldStatus, status);
        return buildSalesOrderResponse(updated, salesOrderItemRepository.findBySoNumber(soNumber));
    }

    @Override
    public void deleteSalesOrder(String soNumber) {
        log.info("Deleting Sales Order: {}", soNumber);

        SalesOrder salesOrder = salesOrderRepository.findBySoNumber(soNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found: " + soNumber));

        if (!salesOrder.getStatus().equals("DRAFT") && !salesOrder.getStatus().equals("CONFIRMED") &&
            !salesOrder.getStatus().equals("CANCELLED")) {
            throw new BusinessException("Cannot delete order in status: " + salesOrder.getStatus());
        }

        List<StockReservation> reservations = stockReservationRepository.findBySoNumber(soNumber);
        for (StockReservation reservation : reservations) {
            releaseReservation(reservation.getReservationNumber());
        }

        salesOrderItemRepository.deleteBySoNumber(soNumber);
        salesOrderRepository.delete(salesOrder);

        log.info("Sales Order deleted successfully: {}", soNumber);
    }

    @Override
    public void cancelSalesOrder(String soNumber) {
        log.info("Cancelling Sales Order: {}", soNumber);

        SalesOrder salesOrder = salesOrderRepository.findBySoNumber(soNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found: " + soNumber));

        if (!salesOrder.getStatus().equals("CONFIRMED") && !salesOrder.getStatus().equals("PROCESSING") &&
            !salesOrder.getStatus().equals("PENDING") && !salesOrder.getStatus().equals("APPROVED")) {
            throw new BusinessException("Cannot cancel order in status: " + salesOrder.getStatus());
        }

        List<StockReservation> reservations = stockReservationRepository.findBySoNumber(soNumber);
        for (StockReservation reservation : reservations) {
            releaseReservation(reservation.getReservationNumber());
        }

        salesOrder.setStatus("CANCELLED");
        salesOrder.setUpdatedBy("SYSTEM");
        salesOrderRepository.save(salesOrder);

        log.info("Sales Order cancelled successfully: {}", soNumber);
    }

    // ============================================================
    // =================== SALES ORDER ITEM ========================
    // ============================================================

    @Override
    public SalesOrderItemResponse getSalesOrderItemById(Long itemId) {
        SalesOrderItem item = salesOrderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order Item not found: " + itemId));
        return buildSalesOrderItemResponse(item);
    }

    @Override
    public List<SalesOrderItemResponse> getSalesOrderItemsBySoNumber(String soNumber) {
        List<SalesOrderItem> items = salesOrderItemRepository.findBySoNumber(soNumber);
        return items.stream()
                .map(this::buildSalesOrderItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SalesOrderItemResponse updateSalesOrderItem(Long itemId, SalesOrderItemUpdateRequest request) {
        log.info("Updating Sales Order Item: {}", itemId);

        SalesOrderItem item = salesOrderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order Item not found: " + itemId));

        SalesOrder salesOrder = salesOrderRepository.findBySoNumber(item.getSoNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found: " + item.getSoNumber()));

        if (!salesOrder.getStatus().equals("DRAFT") && !salesOrder.getStatus().equals("CONFIRMED") &&
            !salesOrder.getStatus().equals("PROCESSING")&&!salesOrder.getStatus().equals("PENDING")&&!salesOrder.getStatus().equals("APPROVED")) {
            throw new BusinessException("Cannot edit item in order status: " + salesOrder.getStatus());
        }

        if (request.getItemCode() != null) item.setItemCode(request.getItemCode());
        if (request.getItemName() != null) item.setItemName(request.getItemName());
        if (request.getUom() != null) item.setUom(request.getUom());
        if (request.getOrderedQuantity() != null) item.setOrderedQuantity(request.getOrderedQuantity());
        if (request.getReservedQuantity() != null) item.setReservedQuantity(request.getReservedQuantity());
        if (request.getPickedQuantity() != null) item.setPickedQuantity(request.getPickedQuantity());
        if (request.getShippedQuantity() != null) item.setShippedQuantity(request.getShippedQuantity());
        if (request.getBatchNumber() != null) item.setBatchNumber(request.getBatchNumber());
        if (request.getSourceLocation() != null) item.setSourceLocation(request.getSourceLocation());

        item.setUpdatedAt(LocalDateTime.now());

        SalesOrderItem updated = salesOrderItemRepository.save(item);

        List<SalesOrderItem> allItems = salesOrderItemRepository.findBySoNumber(item.getSoNumber());
        int total = allItems.stream().mapToInt(SalesOrderItem::getOrderedQuantity).sum();
        salesOrder.setTotalQuantity(total);
        salesOrderRepository.save(salesOrder);

        log.info("Sales Order Item updated successfully: {}", updated.getId());
        return buildSalesOrderItemResponse(updated);
    }

    @Override
    public SalesOrderItemResponse updateSalesOrderItemQuantity(Long itemId, Integer quantity) {
        log.info("Updating Sales Order Item quantity: {} to {}", itemId, quantity);

        SalesOrderItem item = salesOrderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order Item not found: " + itemId));

        if (quantity < 0) {
            throw new BusinessException("Quantity cannot be negative");
        }

        SalesOrder salesOrder = salesOrderRepository.findBySoNumber(item.getSoNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found: " + item.getSoNumber()));

        if (!salesOrder.getStatus().equals("DRAFT") && !salesOrder.getStatus().equals("CONFIRMED")) {
            throw new BusinessException("Cannot edit quantity in order status: " + salesOrder.getStatus());
        }

        item.setOrderedQuantity(quantity);
        item.setUpdatedAt(LocalDateTime.now());

        SalesOrderItem updated = salesOrderItemRepository.save(item);

        List<SalesOrderItem> items = salesOrderItemRepository.findBySoNumber(item.getSoNumber());
        int total = items.stream().mapToInt(SalesOrderItem::getOrderedQuantity).sum();
        salesOrder.setTotalQuantity(total);
        salesOrderRepository.save(salesOrder);

        return buildSalesOrderItemResponse(updated);
    }

    @Override
    public SalesOrderItemResponse updateSalesOrderItemReservedQuantity(Long itemId, Integer quantity) {
        log.info("Updating reserved quantity: {} to {}", itemId, quantity);

        SalesOrderItem item = salesOrderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order Item not found: " + itemId));

        if (quantity < 0) {
            throw new BusinessException("Reserved quantity cannot be negative");
        }
        if (quantity > item.getOrderedQuantity()) {
            throw new BusinessException("Reserved quantity cannot exceed ordered quantity: " + item.getOrderedQuantity());
        }

        item.setReservedQuantity(quantity);
        item.setUpdatedAt(LocalDateTime.now());

        SalesOrderItem updated = salesOrderItemRepository.save(item);
        return buildSalesOrderItemResponse(updated);
    }

    @Override
    public SalesOrderItemResponse updateSalesOrderItemPickedQuantity(Long itemId, Integer quantity) {
        log.info("Updating picked quantity: {} to {}", itemId, quantity);

        SalesOrderItem item = salesOrderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order Item not found: " + itemId));

        if (quantity < 0) {
            throw new BusinessException("Picked quantity cannot be negative");
        }
        if (quantity > item.getOrderedQuantity()) {
            throw new BusinessException("Picked quantity cannot exceed ordered quantity: " + item.getOrderedQuantity());
        }

        item.setPickedQuantity(quantity);
        item.setUpdatedAt(LocalDateTime.now());

        SalesOrderItem updated = salesOrderItemRepository.save(item);
        return buildSalesOrderItemResponse(updated);
    }

    @Override
    public SalesOrderItemResponse updateSalesOrderItemShippedQuantity(Long itemId, Integer quantity) {
        log.info("Updating shipped quantity: {} to {}", itemId, quantity);

        SalesOrderItem item = salesOrderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order Item not found: " + itemId));

        if (quantity < 0) {
            throw new BusinessException("Shipped quantity cannot be negative");
        }
        if (quantity > item.getOrderedQuantity()) {
            throw new BusinessException("Shipped quantity cannot exceed ordered quantity: " + item.getOrderedQuantity());
        }

        item.setShippedQuantity(quantity);
        item.setUpdatedAt(LocalDateTime.now());

        SalesOrderItem updated = salesOrderItemRepository.save(item);
        return buildSalesOrderItemResponse(updated);
    }

    @Override
    public SalesOrderItemResponse updateSalesOrderItemLocation(Long itemId, String sourceLocation) {
        log.info("Updating location: {} to {}", itemId, sourceLocation);

        SalesOrderItem item = salesOrderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order Item not found: " + itemId));

        item.setSourceLocation(sourceLocation);
        item.setUpdatedAt(LocalDateTime.now());

        SalesOrderItem updated = salesOrderItemRepository.save(item);
        return buildSalesOrderItemResponse(updated);
    }

    @Override
    public SalesOrderItemResponse updateSalesOrderItemBatch(Long itemId, String batchNumber) {
        log.info("Updating batch: {} to {}", itemId, batchNumber);

        SalesOrderItem item = salesOrderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order Item not found: " + itemId));

        item.setBatchNumber(batchNumber);
        item.setUpdatedAt(LocalDateTime.now());

        SalesOrderItem updated = salesOrderItemRepository.save(item);
        return buildSalesOrderItemResponse(updated);
    }

    @Override
    public SalesOrderItemResponse updateSalesOrderItemName(Long itemId, String itemName) {
        log.info("Updating item name: {} to {}", itemId, itemName);

        SalesOrderItem item = salesOrderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order Item not found: " + itemId));

        item.setItemName(itemName);
        item.setUpdatedAt(LocalDateTime.now());

        SalesOrderItem updated = salesOrderItemRepository.save(item);
        return buildSalesOrderItemResponse(updated);
    }

    @Override
    public SalesOrderItemResponse updateSalesOrderItemUom(Long itemId, String uom) {
        log.info("Updating UOM: {} to {}", itemId, uom);

        SalesOrderItem item = salesOrderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order Item not found: " + itemId));

        item.setUom(uom);
        item.setUpdatedAt(LocalDateTime.now());

        SalesOrderItem updated = salesOrderItemRepository.save(item);
        return buildSalesOrderItemResponse(updated);
    }

    @Override
    public SalesOrderItemResponse updateSalesOrderItemCode(Long itemId, String itemCode) {
        log.info("Updating item code: {} to {}", itemId, itemCode);

        SalesOrderItem item = salesOrderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order Item not found: " + itemId));

        item.setItemCode(itemCode);
        item.setUpdatedAt(LocalDateTime.now());

        SalesOrderItem updated = salesOrderItemRepository.save(item);
        return buildSalesOrderItemResponse(updated);
    }

    @Override
    public void deleteSalesOrderItem(Long itemId) {
        log.info("Deleting Sales Order Item: {}", itemId);

        SalesOrderItem item = salesOrderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order Item not found: " + itemId));

        SalesOrder salesOrder = salesOrderRepository.findBySoNumber(item.getSoNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found: " + item.getSoNumber()));

        if (!salesOrder.getStatus().equals("DRAFT") && !salesOrder.getStatus().equals("CONFIRMED") &&
            !salesOrder.getStatus().equals("PROCESSING")) {
            throw new BusinessException("Cannot delete item in order status: " + salesOrder.getStatus());
        }

        List<StockReservation> reservations = stockReservationRepository.findBySalesOrderItemId(itemId);
        for (StockReservation reservation : reservations) {
            releaseReservation(reservation.getReservationNumber());
        }

        salesOrderItemRepository.delete(item);

        List<SalesOrderItem> remainingItems = salesOrderItemRepository.findBySoNumber(item.getSoNumber());
        int total = remainingItems.stream().mapToInt(SalesOrderItem::getOrderedQuantity).sum();
        salesOrder.setTotalQuantity(total);
        salesOrderRepository.save(salesOrder);

        log.info("Sales Order Item deleted successfully: {}", itemId);
    }

    @Override
    public void deleteSalesOrderItemsBySoNumber(String soNumber) {
        log.info("Deleting all Sales Order Items for SO: {}", soNumber);

        SalesOrder salesOrder = salesOrderRepository.findBySoNumber(soNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found: " + soNumber));

        if (!salesOrder.getStatus().equals("DRAFT") && !salesOrder.getStatus().equals("CONFIRMED") &&
            !salesOrder.getStatus().equals("PROCESSING")) {
            throw new BusinessException("Cannot delete items in order status: " + salesOrder.getStatus());
        }

        List<SalesOrderItem> items = salesOrderItemRepository.findBySoNumber(soNumber);
        for (SalesOrderItem item : items) {
            List<StockReservation> reservations = stockReservationRepository.findBySalesOrderItemId(item.getId());
            for (StockReservation reservation : reservations) {
                releaseReservation(reservation.getReservationNumber());
            }
        }

        salesOrderItemRepository.deleteBySoNumber(soNumber);
        salesOrder.setTotalQuantity(0);
        salesOrderRepository.save(salesOrder);

        log.info("All Sales Order Items deleted successfully for SO: {}", soNumber);
    }

    // ============================================================
    // =================== STOCK RESERVATION =======================
    // ============================================================

    @Override
    public StockReservation reserveStock(String soNumber) {
        log.info("Reserving stock for SO: {}", soNumber);

        SalesOrder salesOrder = salesOrderRepository.findBySoNumber(soNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found: " + soNumber));

        List<SalesOrderItem> items = salesOrderItemRepository.findBySoNumber(soNumber);

        if (items.isEmpty()) {
            throw new BusinessException("No items found for Sales Order: " + soNumber);
        }

        for (SalesOrderItem item : items) {
            List<InventoryStock> stocks = inventoryStockRepository.findByItemCode(item.getItemCode());

            if (stocks.isEmpty()) {
                throw new BusinessException("No stock found for item: " + item.getItemCode());
            }

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
                int physical = stock.getQuantity() != null ? stock.getQuantity() : 0;
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
                            .availableQuantity(available - toReserve)
                            .pysicalQuantity(physical)
                            .reservedQuantity(toReserve)
                            .warehouseId(stock.getWarehouseId())
                            .zoneId(stock.getZone())
                            .aisleId(stock.getAisle())
                            .rackId(stock.getRack())
                            .levelId(stock.getLevel())
                            .binId(stock.getBinId())
                            .batchNumber(stock.getBatchNumber())
                            .salesOrderItemId(item.getId())
                            .status("RESERVED")
                            .reservationDate(LocalDateTime.now())
                            .createdBy("SYSTEM")
                            .build();
                    stockReservationRepository.save(reservation);

                    remainingToReserve -= toReserve;
                }
            }

            salesOrderItemRepository.updateReservedQuantity(item.getId(), item.getOrderedQuantity());
        }

        salesOrder.setStatus("PROCESSING");
        salesOrder.setUpdatedBy("SYSTEM");
        salesOrderRepository.save(salesOrder);

        log.info("Stock reserved successfully for SO: {}", soNumber);
        return null;
    }

    @Override
    public StockReservationResponse getReservationByNumber(String reservationNumber) {
        StockReservation reservation = stockReservationRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + reservationNumber));
        return buildStockReservationResponse(reservation);
    }

    @Override
    public List<StockReservationResponse> getReservationsBySoNumber(String soNumber) {
        List<StockReservation> reservations = stockReservationRepository.findBySoNumber(soNumber);
        return reservations.stream()
                .map(this::buildStockReservationResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void releaseReservation(String reservationNumber) {
        StockReservation reservation = stockReservationRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + reservationNumber));

        if (reservation.getStatus().equals("RELEASED") || reservation.getStatus().equals("CANCELLED")) {
            return;
        }

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

        reservation.setStatus("RELEASED");
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

    // ============================================================
    // ===================== PICK LIST =============================
    // ============================================================

    @Override
    public PickListResponse createPickList(PickListRequest request) {
        log.info("Creating Pick List for SO: {}", request.getSoNumber());

        SalesOrder salesOrder = salesOrderRepository.findBySoNumber(request.getSoNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found: " + request.getSoNumber()));

        String pickListNumber = generatePickListNumber();

        int totalItems = request.getItems().size();
        int totalQuantity = request.getItems().stream().mapToInt(PickListItemRequest::getRequiredQuantity).sum();

        PickList pickList = PickList.builder()
                .pickListNumber(pickListNumber)
                .soNumber(request.getSoNumber())
                .warehouseId(request.getWarehouseId())
                .priority(request.getPriority() != null ? request.getPriority() : "MEDIUM")
                .totalItems(totalItems)
                .totalQuantity(totalQuantity)
                .status("RELEASED")
                .createdBy(request.getCreatedBy())
                .assignedTo(request.getAssignedTo())
                .build();

        PickList savedPickList = pickListRepository.save(pickList);

        List<PickListItem> items = new ArrayList<>();
        for (PickListItemRequest itemReq : request.getItems()) {
            PickListItem item = PickListItem.builder()
                    .pickListNumber(pickListNumber)
                    .soNumber(request.getSoNumber())
                    .itemCode(itemReq.getItemCode())
                    .itemName(itemReq.getItemName())
                    .uom(itemReq.getUom() != null ? itemReq.getUom() : "EA")
                    .requiredQuantity(itemReq.getRequiredQuantity())
                    .pickedQuantity(0)
                    .shortQuantity(0)
                    .sourceLocation(itemReq.getSourceLocation())
                    .batchNumber(itemReq.getBatchNumber())
                    .status("PENDING")
                    .priority(itemReq.getPriority() != null ? itemReq.getPriority() : "MEDIUM")
                    .pickList(savedPickList)
                    .build();
            items.add(pickListItemRepository.save(item));
        }

        salesOrder.setStatus("PICKING");
        salesOrder.setUpdatedBy(request.getCreatedBy());
        salesOrderRepository.save(salesOrder);

        log.info("Pick List created successfully: {}", pickListNumber);
        return buildPickListResponse(savedPickList, items);
    }

    @Override
    public PickListResponse getPickListByNumber(String pickListNumber) {
        PickList pickList = pickListRepository.findByPickListNumber(pickListNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Pick List not found: " + pickListNumber));

        List<PickListItem> items = pickListItemRepository.findByPickListNumber(pickListNumber);
        return buildPickListResponse(pickList, items);
    }

    @Override
    public Page<PickListResponse> getAllPickListsWithFilters(
            String pickListNumber, String soNumber, String warehouseId,
            String status, String priority, String assignedTo, String createdBy,
            LocalDateTime startDate, LocalDateTime endDate,
            LocalDateTime startCreatedDate, LocalDateTime endCreatedDate,
            LocalDateTime startCompletedDate, LocalDateTime endCompletedDate,
            Integer minTotalItems, Integer maxTotalItems,
            Integer minTotalQuantity, Integer maxTotalQuantity,
            String itemCode, Pageable pageable) {

        log.info("Fetching pick lists with filters");

        Page<PickList> pickListPage = pickListRepository.findByFilters(
                pickListNumber, soNumber, warehouseId, status, priority,
                assignedTo, createdBy, startDate, endDate,
                startCreatedDate, endCreatedDate,
                startCompletedDate, endCompletedDate,
                minTotalItems, maxTotalItems,
                minTotalQuantity, maxTotalQuantity,
                itemCode, pageable);

        return pickListPage.map(pl -> buildPickListResponse(pl,
                pickListItemRepository.findByPickListNumber(pl.getPickListNumber())));
    }

    @Override
    public Page<PickListResponse> searchPickLists(String search, Pageable pageable) {
        log.info("Searching pick lists with keyword: {}", search);
        return pickListRepository.searchPickLists(search, pageable)
                .map(pl -> buildPickListResponse(pl,
                        pickListItemRepository.findByPickListNumber(pl.getPickListNumber())));
    }

    @Override
    public PickListResponse updatePickListStatus(String pickListNumber, String status) {
        PickList pickList = pickListRepository.findByPickListNumber(pickListNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Pick List not found: " + pickListNumber));

        pickList.setStatus(status);
        if ("COMPLETED".equals(status)) {
            pickList.setCompletedDate(LocalDateTime.now());
        }
        pickList.setUpdatedBy("SYSTEM");
        PickList updated = pickListRepository.save(pickList);

        return buildPickListResponse(updated, pickListItemRepository.findByPickListNumber(pickListNumber));
    }

    @Override
    public void deletePickList(String pickListNumber) {
        log.info("Deleting Pick List: {}", pickListNumber);

        PickList pickList = pickListRepository.findByPickListNumber(pickListNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Pick List not found: " + pickListNumber));

        if (!pickList.getStatus().equals("RELEASED") && !pickList.getStatus().equals("PENDING") &&
            !pickList.getStatus().equals("CANCELLED")) {
            throw new BusinessException("Cannot delete pick list in status: " + pickList.getStatus());
        }

        pickListItemRepository.deleteByPickListNumber(pickListNumber);
        pickListRepository.delete(pickList);

        log.info("Pick List deleted successfully: {}", pickListNumber);
    }

    // ============================================================
    // ===================== PICK TASK =============================
    // ============================================================

    @Override
    public PickTaskResponse createPickTask(PickTaskRequest request) {
        log.info("Creating Pick Task for Pick List: {}", request.getPickListNumber());

        PickList pickList = pickListRepository.findByPickListNumber(request.getPickListNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Pick List not found: " + request.getPickListNumber()));

        List<PickListItem> items = pickListItemRepository.findByPickListNumber(request.getPickListNumber());
        PickListItem pickItem = items.stream()
                .filter(item -> item.getItemCode().equals(request.getItemCode()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in Pick List: " + request.getItemCode()));

        String pickTaskNumber = generatePickTaskNumber();

        Long inventoryId = null;

        Long salesOrderLineId = request.getSalesOrderLineId();
        if (salesOrderLineId == null) {
            List<SalesOrderItem> orderItems = salesOrderItemRepository.findByItemCode(request.getItemCode());
            if (!orderItems.isEmpty()) {
                salesOrderLineId = orderItems.get(0).getId();
            }
        }

        PickTask pickTask = PickTask.builder()
                .pickTaskNumber(pickTaskNumber)
                .pickListNumber(request.getPickListNumber())
                .soNumber(pickList.getSoNumber())
                .itemCode(request.getItemCode())
                .itemName(pickItem.getItemName())
                .uom(pickItem.getUom())
                .requiredQuantity(request.getRequiredQuantity())
                .quantityToPick(request.getRequiredQuantity())
                .inventoryId(null)
                .salesOrderLineId(salesOrderLineId)
                .pickedQuantity(0)
                .locationBarcode(request.getLocationBarcode())
                .itemBarcode(request.getItemBarcode())
                .binId(request.getBinId())
                .batchNumber(request.getBatchNumber())
                .pickerId(request.getPickerId())
                .pickerName(request.getPickerName())
                .status("PENDING")
                .isScanned(false)
                .createdBy(request.getCreatedBy())
                .build();

        PickTask savedTask = pickTaskRepository.save(pickTask);

        pickList.setStatus("PICKING");
        pickList.setUpdatedBy(request.getCreatedBy());
        pickListRepository.save(pickList);

        pickItem.setStatus("PICKING");
        pickListItemRepository.save(pickItem);

        log.info("Pick Task created successfully: {}", pickTaskNumber);
        return buildPickTaskResponse(savedTask);
    }

    @Override
    public PickTaskResponse getPickTaskByNumber(String pickTaskNumber) {
        PickTask pickTask = pickTaskRepository.findByPickTaskNumber(pickTaskNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Pick Task not found: " + pickTaskNumber));
        return buildPickTaskResponse(pickTask);
    }

    @Override
    public List<PickTaskResponse> getPickTasksByPickList(String pickListNumber) {
        return pickTaskRepository.findByPickListNumber(pickListNumber)
                .stream()
                .map(this::buildPickTaskResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PickTaskResponse> getAllPickTasksWithFilters(
            String pickTaskNumber, String pickListNumber, String soNumber,
            String itemCode, String itemName, String status,
            String pickerId, String pickerName, String binId,
            String locationBarcode, String batchNumber, Boolean isScanned,
            LocalDateTime startDate, LocalDateTime endDate,
            LocalDateTime startScanDate, LocalDateTime endScanDate,
            Integer minRequiredQuantity, Integer maxRequiredQuantity,
            Integer minPickedQuantity, Integer maxPickedQuantity,
            String createdBy, Pageable pageable) {

        log.info("Fetching pick tasks with filters");

        Page<PickTask> pickTaskPage = pickTaskRepository.findByFilters(
                pickTaskNumber, pickListNumber, soNumber, itemCode, itemName,
                status, pickerId, pickerName, binId, locationBarcode,
                batchNumber, isScanned, startDate, endDate,
                startScanDate, endScanDate,
                minRequiredQuantity, maxRequiredQuantity,
                minPickedQuantity, maxPickedQuantity,
                createdBy, pageable);

        return pickTaskPage.map(this::buildPickTaskResponse);
    }

    @Override
    public Page<PickTaskResponse> searchPickTasks(String search, Pageable pageable) {
        log.info("Searching pick tasks with keyword: {}", search);
        return pickTaskRepository.searchPickTasks(search, pageable)
                .map(this::buildPickTaskResponse);
    }

    @Override
    public PickTaskResponse scanPickTask(String pickTaskNumber, String pickerId, String pickerName) {
        PickTask pickTask = pickTaskRepository.findByPickTaskNumber(pickTaskNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Pick Task not found: " + pickTaskNumber));

        if (pickTask.getIsScanned()) {
            throw new BusinessException("Pick Task already scanned");
        }

        pickTask.setStatus("SCANNED");
        pickTask.setIsScanned(true);
        pickTask.setPickerId(pickerId);
        pickTask.setPickerName(pickerName);
        pickTask.setScanTime(LocalDateTime.now());
        pickTask.setUpdatedBy(pickerName);

        PickTask updated = pickTaskRepository.save(pickTask);

        PickList pickList = pickListRepository.findByPickListNumber(pickTask.getPickListNumber()).orElse(null);
        if (pickList != null) {
            pickList.setStatus("PICKING");
            pickListRepository.save(pickList);
        }

        List<PickListItem> items = pickListItemRepository.findByPickListNumber(pickTask.getPickListNumber());
        items.stream()
                .filter(item -> item.getItemCode().equals(pickTask.getItemCode()))
                .findFirst()
                .ifPresent(item -> {
                    item.setStatus("PICKING");
                    pickListItemRepository.save(item);
                });

        log.info("Pick Task scanned successfully: {}", pickTaskNumber);
        return buildPickTaskResponse(updated);
    }

    @Override
    public PickTaskResponse updatePickTaskStatus(String pickTaskNumber, String status) {
        PickTask pickTask = pickTaskRepository.findByPickTaskNumber(pickTaskNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Pick Task not found: " + pickTaskNumber));

        if (pickTask.getStatus().equals("CONFIRMED") || pickTask.getStatus().equals("CANCELLED")) {
            throw new BusinessException("Cannot update status of confirmed or cancelled pick task");
        }

        pickTask.setStatus(status);
        pickTask.setUpdatedBy("SYSTEM");
        pickTask.setUpdatedAt(LocalDateTime.now());

        PickTask updated = pickTaskRepository.save(pickTask);
        log.info("Pick Task status updated successfully: {}", pickTaskNumber);
        return buildPickTaskResponse(updated);
    }

    @Override
    public void deletePickTask(String pickTaskNumber) {
        PickTask pickTask = pickTaskRepository.findByPickTaskNumber(pickTaskNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Pick Task not found: " + pickTaskNumber));

        if (pickTask.getStatus().equals("CONFIRMED") || pickTask.getStatus().equals("SCANNED")) {
            throw new BusinessException("Cannot delete pick task in status: " + pickTask.getStatus());
        }

        pickTaskRepository.delete(pickTask);
        log.info("Pick Task deleted successfully: {}", pickTaskNumber);
    }

    // ============================================================
    // ================== PICK CONFIRMATION ========================
    // ============================================================

    @Override
    public PickConfirmationResponse confirmPick(PickConfirmationRequest request) {
        log.info("Confirming pick for task: {}", request.getPickTaskNumber());

        PickTask pickTask = pickTaskRepository.findByPickTaskNumber(request.getPickTaskNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Pick Task not found: " + request.getPickTaskNumber()));

        if (pickTask.getStatus().equals("CONFIRMED")) {
            throw new BusinessException("Pick Task already confirmed");
        }

        if (request.getPickedQuantity() > pickTask.getRequiredQuantity()) {
            throw new BusinessException("Picked quantity cannot exceed required quantity");
        }

        pickTask.setPickedQuantity(request.getPickedQuantity());
        pickTask.setStatus("CONFIRMED");
        pickTask.setScanTime(LocalDateTime.now());
        pickTaskRepository.save(pickTask);

        String confirmationNumber = generateConfirmationNumber();
        PickConfirmation confirmation = PickConfirmation.builder()
                .confirmationNumber(confirmationNumber)
                .pickTaskNumber(request.getPickTaskNumber())
                .pickListNumber(pickTask.getPickListNumber())
                .soNumber(pickTask.getSoNumber())
                .itemCode(request.getItemCode())
                .itemName(pickTask.getItemName())
                .requiredQuantity(pickTask.getRequiredQuantity())
                .pickedQuantity(request.getPickedQuantity())
                .shortQuantity(request.getShortQuantity() != null ? request.getShortQuantity() : 0)
                .barcode(request.getBarcode())
                .confirmedBy(request.getConfirmedBy())
                .confirmedDate(LocalDateTime.now())
                .status("CONFIRMED")
                .remarks(request.getRemarks())
                .build();

        PickConfirmation savedConfirmation = pickConfirmationRepository.save(confirmation);

        List<PickListItem> items = pickListItemRepository.findByPickListNumber(pickTask.getPickListNumber());
        items.stream()
                .filter(item -> item.getItemCode().equals(pickTask.getItemCode()))
                .findFirst()
                .ifPresent(item -> {
                    item.setPickedQuantity(request.getPickedQuantity());
                    item.setShortQuantity(request.getShortQuantity() != null ? request.getShortQuantity() : 0);
                    item.setStatus(request.getPickedQuantity().equals(item.getRequiredQuantity()) ? "COMPLETED" : "SHORT");
                    pickListItemRepository.save(item);
                });

        List<InventoryStock> stocks = inventoryStockRepository.findByItemCode(pickTask.getItemCode());
        int remainingToPick = request.getPickedQuantity();
        for (InventoryStock stock : stocks) {
            if (remainingToPick <= 0) break;
            if (stock.getBinId() != null && stock.getBinId().equals(pickTask.getBinId())) {
                int available = stock.getAvailableQuantity() != null ? stock.getAvailableQuantity() : 0;
                int toPick = Math.min(available, remainingToPick);
                stock.removeQuantity(toPick);
                inventoryStockRepository.save(stock);
                remainingToPick -= toPick;
            }
        }

        List<PickListItem> allItems = pickListItemRepository.findByPickListNumber(pickTask.getPickListNumber());
        boolean allCompleted = allItems.stream().allMatch(item -> "COMPLETED".equals(item.getStatus()));

        if (allCompleted) {
            PickList pickList = pickListRepository.findByPickListNumber(pickTask.getPickListNumber())
                    .orElse(null);
            if (pickList != null) {
                pickList.setStatus("COMPLETED");
                pickList.setCompletedDate(LocalDateTime.now());
                pickListRepository.save(pickList);
            }

            SalesOrder salesOrder = salesOrderRepository.findBySoNumber(pickTask.getSoNumber()).orElse(null);
            if (salesOrder != null) {
                salesOrder.setStatus("PACKING");
                salesOrderRepository.save(salesOrder);
            }
        }

        log.info("Pick confirmed successfully: {}", confirmationNumber);
        return buildConfirmationResponse(savedConfirmation);
    }

    @Override
    public PickConfirmationResponse getConfirmationByNumber(String confirmationNumber) {
        PickConfirmation confirmation = pickConfirmationRepository.findByConfirmationNumber(confirmationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Confirmation not found: " + confirmationNumber));
        return buildConfirmationResponse(confirmation);
    }

    
    @Override
    public Page<PickConfirmationResponse> getAllPickConfirmationsWithFilters(
            String confirmationNumber,
            String pickTaskNumber,
            String pickListNumber,
            String soNumber,
            String itemCode,
            String itemName,
            String confirmedBy,
            String status,
            String barcode,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime startConfirmedDate,
            LocalDateTime endConfirmedDate,
            Integer minPickedQuantity,
            Integer maxPickedQuantity,
            Integer minShortQuantity,
            Integer maxShortQuantity,
            Pageable pageable) {

        log.info("Fetching pick confirmations with filters");

        Page<PickConfirmation> confirmationPage = pickConfirmationRepository.findByFilters(
                confirmationNumber, pickTaskNumber, pickListNumber, soNumber,
                itemCode, itemName, confirmedBy, status, barcode,
                startDate, endDate, startConfirmedDate, endConfirmedDate,
                minPickedQuantity, maxPickedQuantity,
                minShortQuantity, maxShortQuantity, pageable);

        return confirmationPage.map(this::buildPickConfirmationResponse);
    }

    // ====== SEARCH PICK CONFIRMATIONS ======

    @Override
    public Page<PickConfirmationResponse> searchPickConfirmations(String search, Pageable pageable) {
        log.info("Searching pick confirmations with keyword: {}", search);
        return pickConfirmationRepository.searchPickConfirmations(search, pageable)
                .map(this::buildPickConfirmationResponse);
    }
    
    // ============================================================
    // ===================== PACKAGE ===============================
    // ============================================================

    @Override
    public PackageResponse createPackage(PackageRequest request) {
        log.info("Creating Package for SO: {}", request.getSoNumber());

        PickList pickList = pickListRepository.findByPickListNumber(request.getPickListNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Pick List not found: " + request.getPickListNumber()));

        String packageNumber = generatePackageNumber();
        String packageBarcode = generatePackageBarcode();

        Double volume = null;
        if (request.getLength() != null && request.getWidth() != null && request.getHeight() != null) {
            volume = request.getLength() * request.getWidth() * request.getHeight();
        }

        PackageInfo packageInfo = PackageInfo.builder()
                .packageNumber(packageNumber)
                .packageBarcode(packageBarcode)
                .soNumber(request.getSoNumber())
                .pickListNumber(request.getPickListNumber())
                .itemCode(request.getItemCode())
                .itemName(getItemNameFromPickList(request.getPickListNumber(), request.getItemCode()))
                .packedQuantity(request.getPackedQuantity())
                .packageType(request.getPackageType())
                .weight(request.getWeight() != null ? request.getWeight() : 0.0)
                .length(request.getLength() != null ? request.getLength() : 0.0)
                .width(request.getWidth() != null ? request.getWidth() : 0.0)
                .height(request.getHeight() != null ? request.getHeight() : 0.0)
                .volume(volume)
                .packedBy(request.getPackedBy())
                .packedDate(LocalDateTime.now())
                .status("PACKED")
                .remarks(request.getRemarks())
                .createdBy(request.getCreatedBy())
                .build();

        PackageInfo savedPackage = packageInfoRepository.save(packageInfo);

        SalesOrder salesOrder = salesOrderRepository.findBySoNumber(request.getSoNumber()).orElse(null);
        if (salesOrder != null) {
            salesOrder.setStatus("PACKING");
            salesOrderRepository.save(salesOrder);
        }
        
        

        log.info("Package created successfully: {}", packageNumber);
        return buildPackageResponse(savedPackage);
    }

    @Override
    public PackageResponse getPackageByNumber(String packageNumber) {
        PackageInfo packageInfo = packageInfoRepository.findByPackageNumber(packageNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found: " + packageNumber));
        return buildPackageResponse(packageInfo);
    }

    @Override
    public PackageResponse getPackageByBarcode(String packageBarcode) {
        PackageInfo packageInfo = packageInfoRepository.findByPackageBarcode(packageBarcode)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found with barcode: " + packageBarcode));
        return buildPackageResponse(packageInfo);
    }

    @Override
    public void updatePackageStatus(String packageNumber, String status) {
        PackageInfo packageInfo = packageInfoRepository.findByPackageNumber(packageNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found: " + packageNumber));
        packageInfo.setStatus(status);
        packageInfoRepository.save(packageInfo);
        log.info("Package status updated: {} -> {}", packageNumber, status);
    }

    @Override
    public void deletePackage(String packageNumber) {
        PackageInfo packageInfo = packageInfoRepository.findByPackageNumber(packageNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found: " + packageNumber));

        if (!packageInfo.getStatus().equals("PACKED") && !packageInfo.getStatus().equals("LABELED")) {
            throw new BusinessException("Cannot delete package in status: " + packageInfo.getStatus());
        }

        packageInfoRepository.delete(packageInfo);
        log.info("Package deleted successfully: {}", packageNumber);
    }
    
    
    
    @Override
    public Page<PackageResponse> getAllPackagesWithFilters(
            String packageNumber,
            String packageBarcode,
            String soNumber,
            String pickListNumber,
            String itemCode,
            String itemName,
            String packageType,
            String status,
            String packedBy,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime startPackedDate,
            LocalDateTime endPackedDate,
            Double minWeight,
            Double maxWeight,
            Integer minQuantity,
            Integer maxQuantity,
            Pageable pageable) {

        log.info("Fetching packages with filters");

        Page<PackageInfo> packagePage = packageInfoRepository.findByFilters(
                packageNumber, packageBarcode, soNumber, pickListNumber,
                itemCode, itemName, packageType, status, packedBy,
                startDate, endDate, startPackedDate, endPackedDate,
                minWeight, maxWeight, minQuantity, maxQuantity, pageable);

        return packagePage.map(this::buildPackageResponse);
    }

    // ====== SEARCH PACKAGES ======

    @Override
    public Page<PackageResponse> searchPackages(String search, Pageable pageable) {
        log.info("Searching packages with keyword: {}", search);
        return packageInfoRepository.searchPackages(search, pageable)
                .map(this::buildPackageResponse);
    }

    
    
    

    // ============================================================
    // ================== SHIPPING LABEL ===========================
    // ============================================================

    @Override
    public ShippingLabelResponse generateShippingLabel(String packageNumber) {
        log.info("Generating Shipping Label for Package: {}", packageNumber);

        PackageInfo packageInfo = packageInfoRepository.findByPackageNumber(packageNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found: " + packageNumber));

        SalesOrder salesOrder = salesOrderRepository.findBySoNumber(packageInfo.getSoNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found: " + packageInfo.getSoNumber()));

        String labelNumber = generateLabelNumber();

        ShippingLabel label = ShippingLabel.builder()
                .labelNumber(labelNumber)
                .packageNumber(packageNumber)
                .packageBarcode(packageInfo.getPackageBarcode())
                .soNumber(packageInfo.getSoNumber())
                .customerCode(salesOrder.getCustomerCode())
                .customerName(salesOrder.getCustomerName())
                .customerAddress(salesOrder.getDeliveryAddress())
                .itemCode(packageInfo.getItemCode())
                .itemName(packageInfo.getItemName())
                .quantity(packageInfo.getPackedQuantity())
                .weight(packageInfo.getWeight())
                .shippingMethod(salesOrder.getShippingMethod())
                .labelStatus("PRINTED")
                .printedBy("SYSTEM")
                .printedDate(LocalDateTime.now())
                .createdBy("SYSTEM")
                .build();

        ShippingLabel savedLabel = shippingLabelRepository.save(label);

        packageInfo.setStatus("LABELED");
        packageInfoRepository.save(packageInfo);

        log.info("Shipping Label generated: {}", labelNumber);
        
        
        getShippingLabelQr(savedLabel.getLabelNumber());
        getShippingLabelImage(savedLabel.getLabelNumber());
        getShippingLabelBarcode(savedLabel.getLabelNumber());
        
        return buildShippingLabelResponse(savedLabel);
    }

    @Override
    public ShippingLabelResponse getShippingLabelByNumber(String labelNumber) {
        ShippingLabel label = shippingLabelRepository.findByLabelNumber(labelNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping Label not found: " + labelNumber));
        return buildShippingLabelResponse(label);
    }

    @Override
    public void updateShippingLabelStatus(String labelNumber, String status) {
        ShippingLabel label = shippingLabelRepository.findByLabelNumber(labelNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping Label not found: " + labelNumber));
        label.setLabelStatus(status);
        shippingLabelRepository.save(label);
        log.info("Shipping Label status updated: {} -> {}", labelNumber, status);
    }
    
    
    @Override
    public Page<ShippingLabelResponse> getAllShippingLabelsWithFilters(
            String labelNumber,
            String packageNumber,
            String packageBarcode,
            String soNumber,
            String customerCode,
            String customerName,
            String itemCode,
            String itemName,
            String trackingNumber,
            String labelStatus,
            String shippingMethod,
            String printedBy,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime startPrintedDate,
            LocalDateTime endPrintedDate,
            Double minWeight,
            Double maxWeight,
            Integer minQuantity,
            Integer maxQuantity,
            Pageable pageable) {

        log.info("Fetching shipping labels with filters");

        Page<ShippingLabel> labelPage = shippingLabelRepository.findByFilters(
                labelNumber, packageNumber, packageBarcode, soNumber,
                customerCode, customerName, itemCode, itemName,
                trackingNumber, labelStatus, shippingMethod, printedBy,
                startDate, endDate, startPrintedDate, endPrintedDate,
                minWeight, maxWeight, minQuantity, maxQuantity, pageable);
        
        


        return labelPage.map(this::buildShippingLabelResponse);
    }

    // ====== SEARCH SHIPPING LABELS ======

    @Override
    public Page<ShippingLabelResponse> searchShippingLabels(String search, Pageable pageable) {
        log.info("Searching shipping labels with keyword: {}", search);
        return shippingLabelRepository.searchShippingLabels(search, pageable)
                .map(this::buildShippingLabelResponse);
    }
    
    
    
    @Override
    public LabelImageResponse getShippingLabelImage(String labelNumber) {
        log.info("Getting shipping label image: {}", labelNumber);

        ShippingLabel label = shippingLabelRepository.findByLabelNumber(labelNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping Label not found: " + labelNumber));

        // Generate label image as Base64
        String base64Image = generateLabelImages(label);
        
        
        label.setLabelImage(base64Image);
        
        shippingLabelRepository.save(label);
        
        // Store image data
        label.setLabelUrl(base64Image);
        shippingLabelRepository.save(label);

        return LabelImageResponse.builder()
                .labelNumber(labelNumber)
                .base64Image(base64Image)
                .imageFormat("PNG")
                .labelData(buildLabelData(label))
                .build();
    }

    // ====== GET SHIPPING LABEL QR CODE ======

    @Override
    public QrCodeResponses getShippingLabelQr(String labelNumber) {
        log.info("Getting shipping label QR code: {}", labelNumber);

        ShippingLabel label = shippingLabelRepository.findByLabelNumber(labelNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping Label not found: " + labelNumber));

        // Generate QR code as Base64
        String qrCodeBase64 = generateQrCode(label);
        
        label.setQrImage(qrCodeBase64);
        shippingLabelRepository.save(label);
        
        // Build QR data
        String qrData = buildQrData(label);

        return QrCodeResponses.builder()
                .labelNumber(labelNumber)
                .packageNumber(label.getPackageNumber())
                .soNumber(label.getSoNumber())
                .trackingNumber(label.getTrackingNumber())
                .qrCodeBase64(qrCodeBase64)
                .qrCodeData(qrData)
                .build();
    }
    
    
    

    @Override
    public byte[] getShippingLabelImageAsPng(String labelNumber) {
        log.info("Getting shipping label image as PNG: {}", labelNumber);

        ShippingLabel label = shippingLabelRepository.findByLabelNumber(labelNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping Label not found: " + labelNumber));

        return generateLabelImage(label);
    }

    // ====== GET SHIPPING LABEL QR CODE AS PNG ======

    @Override
    public byte[] getShippingLabelQRAsPng(String labelNumber) {
        log.info("Getting shipping label QR as PNG: {}", labelNumber);

        ShippingLabel label = shippingLabelRepository.findByLabelNumber(labelNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping Label not found: " + labelNumber));

        return generateQRCode(label);
    }

    // ====== GENERATE LABEL IMAGE ======

    private byte[] generateLabelImage(ShippingLabel label) {
        try {
            // Create image with dimensions
            int width = 600;
            int height = 400;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();

            // Set background color
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            // Draw border
            g2d.setColor(Color.BLACK);
            g2d.drawRect(5, 5, width - 10, height - 10);

            // Set font
            Font titleFont = new Font("Arial", Font.BOLD, 20);
            Font normalFont = new Font("Arial", Font.PLAIN, 14);
            Font boldFont = new Font("Arial", Font.BOLD, 14);

            // Draw title
            g2d.setFont(titleFont);
            g2d.setColor(Color.BLUE);
            g2d.drawString("SHIPPING LABEL", 200, 40);

            // Draw separator line
            g2d.setColor(Color.GRAY);
            g2d.drawLine(50, 50, width - 50, 50);

            // Draw content
            int y = 80;
            int x = 50;
            int spacing = 25;

            g2d.setFont(normalFont);
            g2d.setColor(Color.BLACK);

            // Label Number
            g2d.setFont(boldFont);
            g2d.drawString("Label Number:", x, y);
            g2d.setFont(normalFont);
            g2d.drawString(label.getLabelNumber(), x + 180, y);

            // Package Number
            y += spacing;
            g2d.setFont(boldFont);
            g2d.drawString("Package Number:", x, y);
            g2d.setFont(normalFont);
            g2d.drawString(label.getPackageNumber(), x + 180, y);

            // SO Number
            y += spacing;
            g2d.setFont(boldFont);
            g2d.drawString("SO Number:", x, y);
            g2d.setFont(normalFont);
            g2d.drawString(label.getSoNumber(), x + 180, y);

            // Customer Name
            y += spacing;
            g2d.setFont(boldFont);
            g2d.drawString("Customer:", x, y);
            g2d.setFont(normalFont);
            g2d.drawString(label.getCustomerName(), x + 180, y);

            // Customer Address (wrap if too long)
            y += spacing;
            g2d.setFont(boldFont);
            g2d.drawString("Address:", x, y);
            g2d.setFont(normalFont);
            String address = label.getCustomerAddress();
            if (address.length() > 30) {
                g2d.drawString(address.substring(0, 30), x + 180, y);
                y += spacing;
                g2d.drawString(address.substring(30), x + 180, y);
            } else {
                g2d.drawString(address, x + 180, y);
            }

            // Item Name
            y += spacing;
            g2d.setFont(boldFont);
            g2d.drawString("Item:", x, y);
            g2d.setFont(normalFont);
            g2d.drawString(label.getItemName(), x + 180, y);

            // Quantity
            y += spacing;
            g2d.setFont(boldFont);
            g2d.drawString("Quantity:", x, y);
            g2d.setFont(normalFont);
            g2d.drawString(String.valueOf(label.getQuantity()), x + 180, y);

            // Weight
            y += spacing;
            g2d.setFont(boldFont);
            g2d.drawString("Weight:", x, y);
            g2d.setFont(normalFont);
            g2d.drawString(label.getWeight() + " kg", x + 180, y);

            // Tracking Number
            y += spacing;
            g2d.setFont(boldFont);
            g2d.drawString("Tracking:", x, y);
            g2d.setFont(normalFont);
            g2d.drawString(label.getTrackingNumber() != null ? label.getTrackingNumber() : "N/A", x + 180, y);

            // Status
            y += spacing;
            g2d.setFont(boldFont);
            g2d.drawString("Status:", x, y);
            g2d.setFont(normalFont);
            g2d.setColor(getStatusColor(label.getLabelStatus()));
            g2d.drawString(label.getLabelStatus(), x + 180, y);

            // Draw barcode placeholder at bottom
            g2d.setColor(Color.BLACK);
            g2d.fillRect(150, height - 60, 300, 30);

            // Draw barcode text
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            String barcodeText = label.getPackageBarcode() != null ? label.getPackageBarcode() : "BARCODE";
            int textWidth = g2d.getFontMetrics().stringWidth(barcodeText);
            g2d.drawString(barcodeText, 150 + (300 - textWidth) / 2, height - 40);

            // Dispose graphics
            g2d.dispose();

            // Convert to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Error generating label image", e);
            throw new RuntimeException("Failed to generate label image", e);
        }
    }

    // ====== GENERATE QR CODE ======

    private byte[] generateQRCode(ShippingLabel label) {
        try {
            // Simple QR code generation (using barcode placeholder)
            int size = 200;
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();

            // White background
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, size, size);

            // Draw QR pattern (simplified)
            g2d.setColor(Color.BLACK);
            int blockSize = 10;
            for (int i = 0; i < size; i += blockSize) {
                for (int j = 0; j < size; j += blockSize) {
                    if ((i / blockSize + j / blockSize) % 2 == 0) {
                        g2d.fillRect(i, j, blockSize, blockSize);
                    }
                }
            }

            // Draw QR corners
            drawQRCorner(g2d, 0, 0, blockSize);
            drawQRCorner(g2d, size - 30, 0, blockSize);
            drawQRCorner(g2d, 0, size - 30, blockSize);

            // Draw center text
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            String qrText = label.getLabelNumber();
            int textWidth = g2d.getFontMetrics().stringWidth(qrText);
            g2d.drawString(qrText, (size - textWidth) / 2, size / 2);

            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Error generating QR code", e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    // ====== HELPER METHODS ======

    private void drawQRCorner(Graphics2D g2d, int x, int y, int blockSize) {
        g2d.setColor(Color.BLACK);
        // Draw corner pattern
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if ((i == 0 || i == 2 || j == 0 || j == 2) && !(i == 0 && j == 2) && !(i == 2 && j == 0)) {
                    g2d.fillRect(x + i * blockSize * 3, y + j * blockSize * 3, blockSize * 3, blockSize * 3);
                }
            }
        }
    }
    
    
    @Override
    public ShippingLabelBarcodeResponse getShippingLabelBarcode(String labelNumber) {
        log.info("Getting shipping label barcode: {}", labelNumber);

        ShippingLabel label = shippingLabelRepository.findByLabelNumber(labelNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping Label not found: " + labelNumber));

        try {
            // Generate barcode
            String barcodeData = label.getPackageBarcode() != null ? 
                    label.getPackageBarcode() : label.getLabelNumber();
            byte[] barcodeBytes = barcodeUtils.generateCode128Barcode(barcodeData, 300, 80);
            String barcodeBase64 = Base64.getEncoder().encodeToString(barcodeBytes);
            
            label.setBarcode(barcodeBase64);
            shippingLabelRepository.save(label);
            

            return ShippingLabelBarcodeResponse.builder()
                    .labelNumber(label.getLabelNumber())
                    .packageNumber(label.getPackageNumber())
                    .packageBarcode(label.getPackageBarcode())
                    .soNumber(label.getSoNumber())
                    .customerName(label.getCustomerName())
                    .itemName(label.getItemName())
                    .quantity(label.getQuantity())
                    .trackingNumber(label.getTrackingNumber())
                    .barcodeBase64(barcodeBase64)
                    .barcodeType("CODE128")
                    .barcodeData(barcodeData)
                    .generatedAt(LocalDateTime.now())
                    .build();

        } catch (WriterException | IOException e) {
            log.error("Error generating barcode", e);
            throw new RuntimeException("Failed to generate barcode", e);
        }
    }

    // ====== GET SHIPPING LABEL BARCODE AS PNG ======

    @Override
    public byte[] getShippingLabelBarcodeAsPng(String labelNumber) {
        log.info("Getting shipping label barcode as PNG: {}", labelNumber);

        ShippingLabel label = shippingLabelRepository.findByLabelNumber(labelNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping Label not found: " + labelNumber));

        try {
            String barcodeData = label.getPackageBarcode() != null ? 
                    label.getPackageBarcode() : label.getLabelNumber();
            return barcodeUtils.generateBarcodeWithText(barcodeData, 300, 80);
        } catch (WriterException | IOException e) {
            log.error("Error generating barcode PNG", e);
            throw new RuntimeException("Failed to generate barcode PNG", e);
        }
    }

    // ====== SCAN BARCODE ======

    @Override
    public BarcodeScanResponse scanBarcode(BarcodeScanRequest request) {
        log.info("Scanning barcode: {}", request.getBarcode());

        String barcode = request.getBarcode();
        boolean isValid = barcodeUtils.validateBarcode(barcode);

        if (!isValid) {
            return BarcodeScanResponse.builder()
                    .barcode(barcode)
                    .barcodeType(request.getBarcodeType())
                    .isValid(false)
                    .message("Invalid barcode format")
                    .scannedBy(request.getScannedBy())
                    .scannedAt(LocalDateTime.now())
                    .build();
        }

        // Find shipping label by barcode
        ShippingLabel label = shippingLabelRepository.findByBarcode(barcode).orElse(null);

        if (label == null) {
            return BarcodeScanResponse.builder()
                    .barcode(barcode)
                    .barcodeType(request.getBarcodeType())
                    .isValid(false)
                    .message("No shipping label found for this barcode")
                    .scannedBy(request.getScannedBy())
                    .scannedAt(LocalDateTime.now())
                    .build();
        }

        // Update label status
        label.setLabelStatus("SCANNED");
        label.setPrintedBy(request.getScannedBy());
        label.setPrintedDate(LocalDateTime.now());
        shippingLabelRepository.save(label);

        return BarcodeScanResponse.builder()
                .barcode(barcode)
                .barcodeType(request.getBarcodeType())
                .labelNumber(label.getLabelNumber())
                .packageNumber(label.getPackageNumber())
                .soNumber(label.getSoNumber())
                .customerName(label.getCustomerName())
                .itemName(label.getItemName())
                .status(label.getLabelStatus())
                .scannedBy(request.getScannedBy())
                .scannedAt(LocalDateTime.now())
                .isValid(true)
                .message("Barcode scanned successfully")
                .build();
    }
    
    
    
    

    // ============================================================
    // ===================== DISPATCH ==============================
    // ============================================================

    @Override
    public DispatchResponse createDispatch(DispatchRequest request) {
        log.info("Creating Dispatch for SO: {}", request.getSoNumber());

        SalesOrder salesOrder = salesOrderRepository.findBySoNumber(request.getSoNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found: " + request.getSoNumber()));

        PackageInfo packageInfo = packageInfoRepository.findByPackageNumber(request.getPackageNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Package not found: " + request.getPackageNumber()));

        String dispatchNumber = generateDispatchNumber();

        Dispatch dispatch = Dispatch.builder()
                .dispatchNumber(dispatchNumber)
                .soNumber(request.getSoNumber())
                .packageNumber(request.getPackageNumber())
                .customerCode(request.getCustomerCode() != null ? request.getCustomerCode() : salesOrder.getCustomerCode())
                .customerName(request.getCustomerName() != null ? request.getCustomerName() : salesOrder.getCustomerName())
                .transporter(request.getTransporter())
                .vehicleNumber(request.getVehicleNumber())
                .driverName(request.getDriverName())
                .driverMobile(request.getDriverMobile())
                .invoiceNumber(request.getInvoiceNumber())
                .deliveryChallan(request.getDeliveryChallan())
                .dispatchDate(request.getDispatchDate() != null ? request.getDispatchDate() : LocalDateTime.now())
                .status("DISPATCHED")
                .dispatchedBy(request.getDispatchedBy())
                .remarks(request.getRemarks())
                .createdBy(request.getCreatedBy())
                .build();

        Dispatch savedDispatch = dispatchRepository.save(dispatch);

        packageInfo.setStatus("DISPATCHED");
        packageInfoRepository.save(packageInfo);

        salesOrder.setStatus("DISPATCHED");
        salesOrderRepository.save(salesOrder);

        log.info("Dispatch created: {}", dispatchNumber);
        return buildDispatchResponse(savedDispatch);
    }

    @Override
    public DispatchResponse getDispatchByNumber(String dispatchNumber) {
        Dispatch dispatch = dispatchRepository.findByDispatchNumber(dispatchNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch not found: " + dispatchNumber));
        return buildDispatchResponse(dispatch);
    }

    @Override
    public DispatchResponse updateDispatchStatus(String dispatchNumber, String status) {
        Dispatch dispatch = dispatchRepository.findByDispatchNumber(dispatchNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch not found: " + dispatchNumber));

        dispatch.setStatus(status);
        Dispatch updated = dispatchRepository.save(dispatch);

        if ("IN_TRANSIT".equals(status)) {
            String shipmentNumber = generateShipmentNumber();
            ShipmentConfirmation shipment = ShipmentConfirmation.builder()
                    .shipmentNumber(shipmentNumber)
                    .dispatchNumber(dispatchNumber)
                    .soNumber(dispatch.getSoNumber())
                    .packageNumber(dispatch.getPackageNumber())
                    .transporter(dispatch.getTransporter())
                    .vehicleNumber(dispatch.getVehicleNumber())
                    .dispatchDate(dispatch.getDispatchDate())
                    .status("IN_TRANSIT")
                    .confirmedBy("SYSTEM")
                    .createdBy("SYSTEM")
                    .build();
            shipmentConfirmationRepository.save(shipment);

            dispatch.setShipmentNumber(shipmentNumber);
            dispatchRepository.save(dispatch);
        }

        log.info("Dispatch status updated: {} -> {}", dispatchNumber, status);
        return buildDispatchResponse(updated);
    }
    
    
    
    @Override
    public Page<DispatchResponse> getAllDispatchesWithFilters(
            String dispatchNumber,
            String shipmentNumber,
            String soNumber,
            String packageNumber,
            String customerCode,
            String customerName,
            String transporter,
            String vehicleNumber,
            String driverName,
            String invoiceNumber,
            String deliveryChallan,
            String status,
            String dispatchedBy,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime startDispatchDate,
            LocalDateTime endDispatchDate,
            Pageable pageable) {

        log.info("Fetching dispatches with filters");

        Page<Dispatch> dispatchPage = dispatchRepository.findByFilters(
                dispatchNumber, shipmentNumber, soNumber, packageNumber,
                customerCode, customerName, transporter, vehicleNumber,
                driverName, invoiceNumber, deliveryChallan,
                status, dispatchedBy,
                startDate, endDate, startDispatchDate, endDispatchDate, pageable);

        return dispatchPage.map(this::buildDispatchResponse);
    }

    // ====== SEARCH DISPATCHES ======

    @Override
    public Page<DispatchResponse> searchDispatches(String search, Pageable pageable) {
        log.info("Searching dispatches with keyword: {}", search);
        return dispatchRepository.searchDispatches(search, pageable)
                .map(this::buildDispatchResponse);
    }
    
    
    

    @Override
    public void deleteDispatch(String dispatchNumber) {
        Dispatch dispatch = dispatchRepository.findByDispatchNumber(dispatchNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch not found: " + dispatchNumber));

        if (!dispatch.getStatus().equals("DISPATCHED")) {
            throw new BusinessException("Cannot delete dispatch in status: " + dispatch.getStatus());
        }

        dispatchRepository.delete(dispatch);
        log.info("Dispatch deleted successfully: {}", dispatchNumber);
    }

    // ============================================================
    // ================ SHIPMENT CONFIRMATION ======================
    // ============================================================

    @Override
    public ShipmentConfirmationResponse confirmShipment(ShipmentConfirmationRequest request) {
        log.info("Confirming Shipment: {}", request.getDispatchNumber());

        Dispatch dispatch = dispatchRepository.findByDispatchNumber(request.getDispatchNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch not found: " + request.getDispatchNumber()));

        String shipmentNumber = generateShipmentNumber();

        ShipmentConfirmation shipment = ShipmentConfirmation.builder()
                .shipmentNumber(shipmentNumber)
                .dispatchNumber(request.getDispatchNumber())
                .soNumber(request.getSoNumber())
                .packageNumber(dispatch.getPackageNumber())
                .trackingNumber(request.getTrackingNumber())
                .transporter(request.getTransporter())
                .shippingMethod(request.getShippingMethod())
                .vehicleNumber(request.getVehicleNumber())
                .dispatchDate(request.getDispatchDate() != null ? request.getDispatchDate() : LocalDateTime.now())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .status("IN_TRANSIT")
                .confirmedBy(request.getConfirmedBy())
                .remarks(request.getRemarks())
                .createdBy(request.getCreatedBy())
                .build();

        ShipmentConfirmation saved = shipmentConfirmationRepository.save(shipment);

        dispatch.setShipmentNumber(shipmentNumber);
        dispatch.setStatus("IN_TRANSIT");
        dispatchRepository.save(dispatch);

        List<ShippingLabel> labels = shippingLabelRepository.findByPackageNumber(dispatch.getPackageNumber());
        for (ShippingLabel label : labels) {
            label.setTrackingNumber(request.getTrackingNumber());
            label.setLabelStatus("SHIPPED");
            shippingLabelRepository.save(label);
        }

        log.info("Shipment confirmed: {}", shipmentNumber);
        return buildShipmentConfirmationResponse(saved);
    }
    
    @Override
    public Page<ShipmentConfirmationResponse> getAllShipmentConfirmationsWithFilters(
            String shipmentNumber,
            String dispatchNumber,
            String soNumber,
            String packageNumber,
            String trackingNumber,
            String transporter,
            String shippingMethod,
            String vehicleNumber,
            String status,
            String confirmedBy,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime startDispatchDate,
            LocalDateTime endDispatchDate,
            LocalDateTime startExpectedDelivery,
            LocalDateTime endExpectedDelivery,
            LocalDateTime startActualDelivery,
            LocalDateTime endActualDelivery,
            Pageable pageable) {

        log.info("Fetching shipment confirmations with filters");

        Page<ShipmentConfirmation> shipmentPage = shipmentConfirmationRepository.findByFilters(
                shipmentNumber, dispatchNumber, soNumber, packageNumber,
                trackingNumber, transporter, shippingMethod, vehicleNumber,
                status, confirmedBy,
                startDate, endDate,
                startDispatchDate, endDispatchDate,
                startExpectedDelivery, endExpectedDelivery,
                startActualDelivery, endActualDelivery,
                pageable);

        return shipmentPage.map(this::buildShipmentConfirmationResponse);
    }

    // ====== SEARCH SHIPMENT CONFIRMATIONS ======

    @Override
    public Page<ShipmentConfirmationResponse> searchShipmentConfirmations(String search, Pageable pageable) {
        log.info("Searching shipment confirmations with keyword: {}", search);
        return shipmentConfirmationRepository.searchShipments(search, pageable)
                .map(this::buildShipmentConfirmationResponse);
    }
    

    @Override
    public ShipmentConfirmationResponse getShipmentByNumber(String shipmentNumber) {
        ShipmentConfirmation shipment = shipmentConfirmationRepository.findByShipmentNumber(shipmentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + shipmentNumber));
        return buildShipmentConfirmationResponse(shipment);
    }

    @Override
    public ShipmentConfirmationResponse updateShipmentStatus(String shipmentNumber, String status) {
        ShipmentConfirmation shipment = shipmentConfirmationRepository.findByShipmentNumber(shipmentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + shipmentNumber));

        if ("DELIVERED".equals(status)) {
            shipment.setActualDeliveryDate(LocalDateTime.now());
            shipment.setStatus(status);
            shipmentConfirmationRepository.save(shipment);

            DeliveryRequest deliveryRequest = DeliveryRequest.builder()
                    .shipmentNumber(shipmentNumber)
                    .soNumber(shipment.getSoNumber())
                    .packageNumber(shipment.getPackageNumber())
                    .receivedBy("CUSTOMER")
                    .deliveredQuantity(getPackedQuantity(shipment.getPackageNumber()))
                    .createdBy("SYSTEM")
                    .build();
            confirmDelivery(deliveryRequest);

            SalesOrder salesOrder = salesOrderRepository.findBySoNumber(shipment.getSoNumber()).orElse(null);
            if (salesOrder != null) {
                salesOrder.setStatus("DELIVERED");
                salesOrderRepository.save(salesOrder);
            }
        } else {
            shipment.setStatus(status);
            shipmentConfirmationRepository.save(shipment);
        }

        log.info("Shipment status updated: {} -> {}", shipmentNumber, status);
        return buildShipmentConfirmationResponse(shipment);
    }

    @Override
    public void deleteShipment(String shipmentNumber) {
        ShipmentConfirmation shipment = shipmentConfirmationRepository.findByShipmentNumber(shipmentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + shipmentNumber));

        if (!shipment.getStatus().equals("IN_TRANSIT")) {
            throw new BusinessException("Cannot delete shipment in status: " + shipment.getStatus());
        }

        shipmentConfirmationRepository.delete(shipment);
        log.info("Shipment deleted successfully: {}", shipmentNumber);
    }

    // ============================================================
    // ===================== DELIVERY ==============================
    // ============================================================

    @Override
    public DeliveryResponse confirmDelivery(DeliveryRequest request) {
        log.info("Confirming Delivery for Shipment: {}", request.getShipmentNumber());

        ShipmentConfirmation shipment = shipmentConfirmationRepository.findByShipmentNumber(request.getShipmentNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + request.getShipmentNumber()));

        SalesOrder salesOrder = salesOrderRepository.findBySoNumber(request.getSoNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found: " + request.getSoNumber()));

        String deliveryNumber = generateDeliveryNumber();

        Delivery delivery = Delivery.builder()
                .deliveryNumber(deliveryNumber)
                .shipmentNumber(request.getShipmentNumber())
                .soNumber(request.getSoNumber())
                .packageNumber(request.getPackageNumber() != null ? request.getPackageNumber() : shipment.getPackageNumber())
                .customerCode(salesOrder.getCustomerCode())
                .customerName(salesOrder.getCustomerName())
                .trackingNumber(shipment.getTrackingNumber())
                .deliveryDate(LocalDateTime.now())
                .receivedBy(request.getReceivedBy())
                .deliveredQuantity(request.getDeliveredQuantity())
                .deliveryStatus("DELIVERED")
                .signature(request.getSignature())
                .deliveryProofUrl(request.getDeliveryProofUrl())
                .remarks(request.getRemarks())
                .createdBy(request.getCreatedBy())
                .build();

        Delivery saved = deliveryRepository.save(delivery);

        shipment.setStatus("DELIVERED");
        shipment.setActualDeliveryDate(LocalDateTime.now());
        shipmentConfirmationRepository.save(shipment);

        Dispatch dispatch = dispatchRepository.findByShipmentNumber(request.getShipmentNumber()).orElse(null);
        if (dispatch != null) {
            dispatch.setStatus("DELIVERED");
            dispatchRepository.save(dispatch);
        }

        PackageInfo packageInfo = packageInfoRepository.findByPackageNumber(delivery.getPackageNumber()).orElse(null);
        if (packageInfo != null) {
            packageInfo.setStatus("DELIVERED");
            packageInfoRepository.save(packageInfo);
        }

        salesOrder.setStatus("DELIVERED");
        salesOrderRepository.save(salesOrder);

        log.info("Delivery confirmed: {}", deliveryNumber);
        return buildDeliveryResponse(saved);
    }

    @Override
    public DeliveryResponse getDeliveryByNumber(String deliveryNumber) {
        Delivery delivery = deliveryRepository.findByDeliveryNumber(deliveryNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found: " + deliveryNumber));
        return buildDeliveryResponse(delivery);
    }
    
    
    @Override
    public Page<DeliveryResponse> getAllDeliveriesWithFilters(
            String deliveryNumber,
            String shipmentNumber,
            String soNumber,
            String packageNumber,
            String customerCode,
            String customerName,
            String trackingNumber,
            String deliveryStatus,
            String receivedBy,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime startDeliveryDate,
            LocalDateTime endDeliveryDate,
            Integer minQuantity,
            Integer maxQuantity,
            Pageable pageable) {

        log.info("Fetching deliveries with filters");

        Page<Delivery> deliveryPage = deliveryRepository.findByFilters(
                deliveryNumber, shipmentNumber, soNumber, packageNumber,
                customerCode, customerName, trackingNumber, deliveryStatus,
                receivedBy, startDate, endDate,
                startDeliveryDate, endDeliveryDate,
                minQuantity, maxQuantity, pageable);

        return deliveryPage.map(this::buildDeliveryResponse);
    }

    // ====== SEARCH DELIVERIES ======

    @Override
    public Page<DeliveryResponse> searchDeliveries(String search, Pageable pageable) {
        log.info("Searching deliveries with keyword: {}", search);
        return deliveryRepository.searchDeliveries(search, pageable)
                .map(this::buildDeliveryResponse);
    }
    
    
    

    @Override
    public DeliveryResponse updateDeliveryStatus(String deliveryNumber, String status) {
        Delivery delivery = deliveryRepository.findByDeliveryNumber(deliveryNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found: " + deliveryNumber));

        delivery.setDeliveryStatus(status);
        Delivery updated = deliveryRepository.save(delivery);
        log.info("Delivery status updated: {} -> {}", deliveryNumber, status);
        return buildDeliveryResponse(updated);
    }

    @Override
    public void deleteDelivery(String deliveryNumber) {
        Delivery delivery = deliveryRepository.findByDeliveryNumber(deliveryNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found: " + deliveryNumber));

        if (!delivery.getDeliveryStatus().equals("DELIVERED")) {
            throw new BusinessException("Cannot delete delivery in status: " + delivery.getDeliveryStatus());
        }

        deliveryRepository.delete(delivery);
        log.info("Delivery deleted successfully: {}", deliveryNumber);
    }

    // ============================================================
    // =================== HELPER METHODS ==========================
    // ============================================================

    // -------- Status Validation --------



    private void validateStatusTransition(String soNumber, String currentStatus, String newStatus) {
        if (currentStatus.equals(newStatus)) {
            return;
        }

        Map<String, List<String>> validTransitions = new HashMap<>();
        validTransitions.put("DRAFT", List.of("PENDING", "CONFIRMED", "CANCELLED"));
        validTransitions.put("PENDING", List.of("APPROVED", "CONFIRMED", "CANCELLED"));
        validTransitions.put("APPROVED", List.of("PROCESSING", "CANCELLED"));
        validTransitions.put("CONFIRMED", List.of("PROCESSING", "CANCELLED"));
        validTransitions.put("PROCESSING", List.of("PICKING", "CANCELLED"));
        validTransitions.put("PICKING", List.of("PACKING", "CANCELLED"));
        validTransitions.put("PACKING", List.of("DISPATCHED", "CANCELLED"));
        validTransitions.put("DISPATCHED", List.of("IN_TRANSIT", "DELIVERED", "CANCELLED"));
        validTransitions.put("IN_TRANSIT", List.of("DELIVERED", "CANCELLED"));
        validTransitions.put("DELIVERED", new ArrayList<>());
        validTransitions.put("CANCELLED", new ArrayList<>());

        List<String> allowed = validTransitions.get(currentStatus);
        if (allowed == null) {
            throw new BusinessException("Invalid current status: " + currentStatus);
        }

        if (!allowed.contains(newStatus) && !allowed.isEmpty()) {
            throw new BusinessException("Cannot transition from " + currentStatus + " to " + newStatus +
                    ". Allowed transitions: " + allowed);
        }

        // Pass SO number for specific validation rules
        validateStatusSpecificRules(soNumber, currentStatus, newStatus);
    }

private void validateStatusSpecificRules(String soNumber, String currentStatus, String newStatus) {
    
    // ====== PENDING VALIDATION ======
    if ("PENDING".equals(newStatus)) {
        log.info("Validating PENDING status for SO: {}", soNumber);
        
        // First check: Find items by SO number
        List<SalesOrderItem> items = salesOrderItemRepository.findBySoNumber(soNumber);
        log.info("Items found by SO number: {}", items.size());
        
        if (items.isEmpty()) {
            // Second check: Check if order has total quantity > 0
            SalesOrder salesOrder = salesOrderRepository.findBySoNumber(soNumber).orElse(null);
            log.info("SalesOrder found: {}, Total Quantity: {}", 
                     salesOrder != null ? "Yes" : "No", 
                     salesOrder != null ? salesOrder.getTotalQuantity() : "null");
            
            if (salesOrder != null && salesOrder.getTotalQuantity() != null && salesOrder.getTotalQuantity() > 0) {
                log.info("Order {} has total quantity {} but no items. Allowing PENDING status.", 
                         soNumber, salesOrder.getTotalQuantity());
                return; // Allow - items might not be saved yet
            }
            
            log.error("Order {} has no items and totalQuantity is null or 0. Throwing exception.", soNumber);
            throw new BusinessException("Cannot move to PENDING. Order has no items.");
        }
        
        log.info("Order {} has {} items. Proceeding with PENDING status.", soNumber, items.size());
        
        // Check if any item is already processed
        boolean hasProcessed = items.stream().anyMatch(item -> 
                item.getOrderedQuantity() == 0 || 
                (item.getReservedQuantity() != null && item.getReservedQuantity() > 0));
        if (hasProcessed) {
            throw new BusinessException("Cannot move to PENDING. Some items are already processed.");
        }
    }

 // ====== APPROVED VALIDATION AND RESERVATION ======
    if ("APPROVED".equals(newStatus)) {
        log.info("Validating APPROVED status for SO: {}", soNumber);
        
        // Check if pending reservations exist
        List<StockReservation> pendingReservations = stockReservationRepository.findBySoNumber(soNumber)
                .stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .collect(Collectors.toList());

        if (!pendingReservations.isEmpty()) {
            // Check if stock is available for pending reservations
            for (StockReservation reservation : pendingReservations) {
                List<InventoryStock> stocks = inventoryStockRepository.findByItemCode(reservation.getItemCode());
                int totalAvailable = stocks.stream()
                        .mapToInt(s -> s.getAvailableQuantity() != null ? s.getAvailableQuantity() : 0)
                        .sum();

                if (totalAvailable < reservation.getRequiredQuantity()) {
                    throw new BusinessException("Cannot approve. Insufficient stock for item: " +
                            reservation.getItemCode() + ". Available: " + totalAvailable +
                            ", Required: " + reservation.getRequiredQuantity());
                }
            }
            
            // Convert pending reservations to actual reservations
            log.info("Converting pending reservations to actual for SO: {}", soNumber);
            convertPendingToActual(soNumber);
            
        } else {
            // No pending reservations - create new ones directly
            log.info("No pending reservations found for SO: {}. Creating new reservations.", soNumber);
            
            // Get Sales Order items
            List<SalesOrderItem> items = salesOrderItemRepository.findBySoNumber(soNumber);
            
            if (items.isEmpty()) {
                throw new BusinessException("Cannot approve. Order has no items.");
            }
            
            // Create reservations for each item
            for (SalesOrderItem item : items) {
                // Check stock availability
                List<InventoryStock> stocks = inventoryStockRepository.findByItemCode(item.getItemCode());
                int totalAvailable = stocks.stream()
                        .mapToInt(s -> s.getAvailableQuantity() != null ? s.getAvailableQuantity() : 0)
                        .sum();
                
                if (totalAvailable < item.getOrderedQuantity()) {
                    throw new BusinessException("Insufficient stock for item: " + item.getItemCode() +
                            ". Available: " + totalAvailable + ", Required: " + item.getOrderedQuantity());
                }
                
                // Reserve from available stocks (FIFO)
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
                        // Reserve stock
                        stock.reserveQuantity(toReserve);
                        inventoryStockRepository.save(stock);
                        
                        // Create reservation record
                        String reservationNumber = generateReservationNumber();
                        StockReservation reservation = StockReservation.builder()
                                .reservationNumber(reservationNumber)
                                .soNumber(soNumber)
                                .itemCode(item.getItemCode())
                                .itemName(item.getItemName())
                                .uom(item.getUom())
                                .requiredQuantity(item.getOrderedQuantity())
                                .availableQuantity(available - toReserve)
                                .pysicalQuantity(stock.getQuantity() != null ? stock.getQuantity() : 0)
                                .reservedQuantity(toReserve)
                                .warehouseId(stock.getWarehouseId())
                                .zoneId(stock.getZone())
                                .aisleId(stock.getAisle())
                                .rackId(stock.getRack())
                                .levelId(stock.getLevel())
                                .binId(stock.getBinId())
                                .batchNumber(stock.getBatchNumber())
                                .salesOrderItemId(item.getId())
                                .status("RESERVED")
                                .reservationDate(LocalDateTime.now())
                                .createdBy("SYSTEM")
                                .build();
                        stockReservationRepository.save(reservation);
                        
                        remainingToReserve -= toReserve;
                    }
                }
                
                // Update SalesOrderItem reserved quantity
                salesOrderItemRepository.updateReservedQuantity(item.getId(), item.getOrderedQuantity());
            }
        }
    }

    // ====== PROCESSING VALIDATION ======
    if ("PROCESSING".equals(newStatus)) {
        log.info("Validating PROCESSING status for SO: {}", soNumber);
        
        if (!"APPROVED".equals(currentStatus) && !"CONFIRMED".equals(currentStatus)) {
            throw new BusinessException("Cannot move to PROCESSING. Order must be APPROVED or CONFIRMED first.");
        }
        List<StockReservation> reservations = stockReservationRepository.findBySoNumber(soNumber);
        if (reservations.isEmpty()) {
            throw new BusinessException("Cannot move to PROCESSING. No stock reservations found.");
        }
    }

    // ====== PICKING VALIDATION ======
    if ("PICKING".equals(newStatus)) {
        log.info("Validating PICKING status for SO: {}", soNumber);
        
        // Check if stock is reserved
        List<StockReservation> reservations = stockReservationRepository.findBySoNumber(soNumber);
        boolean hasReserved = reservations.stream().anyMatch(r -> 
                "RESERVED".equals(r.getStatus()) || "APPROVED".equals(r.getStatus()));
        
        if (!hasReserved) {
            throw new BusinessException("Cannot move to PICKING. Stock not reserved. Please approve first.");
        }
        
        // Check if all items have reservations
        List<SalesOrderItem> items = salesOrderItemRepository.findBySoNumber(soNumber);
        for (SalesOrderItem item : items) {
            boolean itemReserved = reservations.stream()
                    .anyMatch(r -> r.getItemCode().equals(item.getItemCode()) && 
                                  ("RESERVED".equals(r.getStatus()) || "APPROVED".equals(r.getStatus())));
            if (!itemReserved) {
                throw new BusinessException("Cannot move to PICKING. Item " + item.getItemCode() + " is not reserved.");
            }
        }
    }

    // ====== PACKING VALIDATION ======
    if ("PACKING".equals(newStatus)) {
        log.info("Validating PACKING status for SO: {}", soNumber);
        
        // Ensure all items are picked
        List<SalesOrderItem> items = salesOrderItemRepository.findBySoNumber(soNumber);
        boolean allPicked = items.stream().allMatch(item -> 
                item.getPickedQuantity() != null && 
                item.getPickedQuantity().equals(item.getOrderedQuantity()));
        if (!allPicked) {
            throw new BusinessException("Cannot move to PACKING. Not all items are picked.");
        }
    }

    // ====== DELIVERED VALIDATION ======
    if ("DELIVERED".equals(newStatus)) {
        log.info("Validating DELIVERED status for SO: {}", soNumber);
        
        List<SalesOrderItem> items = salesOrderItemRepository.findBySoNumber(soNumber);
        if (items.isEmpty()) {
            throw new BusinessException("Cannot deliver order. No items found.");
        }
        boolean allShipped = items.stream().allMatch(item ->
                item.getShippedQuantity() != null &&
                item.getShippedQuantity().equals(item.getOrderedQuantity()));
        if (!allShipped) {
            throw new BusinessException("Cannot deliver order. Not all items are shipped.");
        }
    }

    // ====== CANCELLED VALIDATION ======
    if ("CANCELLED".equals(newStatus)) {
        log.info("Validating CANCELLED status for SO: {}", soNumber);
        
        if ("DELIVERED".equals(currentStatus)) {
            throw new BusinessException("Cannot cancel a delivered order");
        }
        
        // Check if any item is already shipped
        List<SalesOrderItem> items = salesOrderItemRepository.findBySoNumber(soNumber);
        boolean anyShipped = items.stream().anyMatch(item ->
                item.getShippedQuantity() != null && item.getShippedQuantity() > 0);
        if (anyShipped && !"DISPATCHED".equals(currentStatus)) {
            throw new BusinessException("Cannot cancel order. Some items are already shipped.");
        }
    }

    // ====== IN_TRANSIT VALIDATION ======
    if ("IN_TRANSIT".equals(newStatus)) {
        log.info("Validating IN_TRANSIT status for SO: {}", soNumber);
        
        if (!"DISPATCHED".equals(currentStatus)) {
            throw new BusinessException("Cannot move to IN_TRANSIT. Order must be DISPATCHED first.");
        }
        
        List<Dispatch> dispatches = dispatchRepository.findBySoNumber(soNumber);
        if (dispatches.isEmpty()) {
            throw new BusinessException("Cannot move to IN_TRANSIT. No dispatch record found.");
        }
    }
}


    // -------- Status Actions --------

    private void handleStatusActions(String soNumber, String status) {
        try {
            switch (status) {
                case "PENDING":
                    createTemporaryReservations(soNumber);
                    break;
                case "APPROVED":
                    convertPendingToActual(soNumber);
                    break;
                case "PROCESSING":
                    ensureStockReserved(soNumber);
                    break;
                case "CANCELLED":
                    releaseAllReservations(soNumber);
                    break;
                default:
                    log.info("No specific action for status: {}", status);
            }
        } catch (Exception e) {
            log.error("Error handling status action for SO: {}", soNumber, e);
        }
    }

    private void createTemporaryReservations(String soNumber) {
        log.info("Creating temporary reservations for SO: {}", soNumber);

        // Get Sales Order
        SalesOrder salesOrder = salesOrderRepository.findBySoNumber(soNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found: " + soNumber));

        // Get Sales Order Items
        List<SalesOrderItem> items = salesOrderItemRepository.findBySoNumber(soNumber);

        if (items.isEmpty()) {
            log.warn("No items found for SO: {}", soNumber);
            throw new BusinessException("Cannot create temporary reservations. Order has no items.");
        }

        // Check if temporary reservations already exist
        List<StockReservation> existingReservations = stockReservationRepository.findBySoNumber(soNumber);
        if (!existingReservations.isEmpty()) {
            log.info("Temporary reservations already exist for SO: {}. Skipping creation.", soNumber);
            return;
        }

        for (SalesOrderItem item : items) {
            // Get inventory stock for this item
            List<InventoryStock> stocks = inventoryStockRepository.findByItemCode(item.getItemCode());
            
            if (stocks.isEmpty()) {
                log.warn("No stock found for item: {} in SO: {}", item.getItemCode(), soNumber);
                continue;
            }

            // Calculate total available quantity
            int totalAvailable = stocks.stream()
                    .mapToInt(s -> s.getAvailableQuantity() != null ? s.getAvailableQuantity() : 0)
                    .sum();

            if (totalAvailable < item.getOrderedQuantity()) {
                log.warn("Insufficient stock for item: {}. Available: {}, Required: {}", 
                         item.getItemCode(), totalAvailable, item.getOrderedQuantity());
                // Continue to create temporary reservation anyway
            }

            // Find best location for reservation (prefer FIFO)
            InventoryStock bestStock = findBestStockForReservation(stocks);
            
            // Create temporary reservation (no inventory update)
            String reservationNumber = generateReservationNumber();
            
            // Create temporary reservation with PENDING status
            StockReservation reservation = StockReservation.builder()
                    .reservationNumber(reservationNumber)
                    .soNumber(soNumber)
                    .itemCode(item.getItemCode())
                    .itemName(item.getItemName())
                    .uom(item.getUom())
                    .requiredQuantity(item.getOrderedQuantity())
                    .availableQuantity(totalAvailable)
                    .pysicalQuantity(stocks.stream().mapToInt(s -> s.getQuantity() != null ? s.getQuantity() : 0).sum())
                    .reservedQuantity(0)  // No actual reservation yet
                    .warehouseId(bestStock != null ? bestStock.getWarehouseId() : null)
                    .zoneId(bestStock != null ? bestStock.getZone() : null)
                    .aisleId(bestStock != null ? bestStock.getAisle() : null)
                    .rackId(bestStock != null ? bestStock.getRack() : null)
                    .levelId(bestStock != null ? bestStock.getLevel() : null)
                    .binId(bestStock != null ? bestStock.getBinId() : null)
                    .batchNumber(bestStock != null ? bestStock.getBatchNumber() : "")
                    .salesOrderItemId(item.getId())
                    .status("PENDING")  // PENDING status
                    .reservationDate(LocalDateTime.now())
                    .createdBy("SYSTEM")
                    .remarks("Temporary reservation created from PENDING status")
                    .build();
            
            stockReservationRepository.save(reservation);
            log.info("Temporary reservation created for item: {} with reservation number: {}", 
                     item.getItemCode(), reservationNumber);
        }

        log.info("Temporary reservations created successfully for SO: {}", soNumber);
    }

    /**
     * Find the best stock location for reservation (FIFO strategy)
     */
    private InventoryStock findBestStockForReservation(List<InventoryStock> stocks) {
        if (stocks == null || stocks.isEmpty()) {
            return null;
        }
        
        // Sort by received date (FIFO - oldest first)
        return stocks.stream()
                .filter(s -> s.getAvailableQuantity() != null && s.getAvailableQuantity() > 0)
                .filter(s -> "ACTIVE".equals(s.getStatus()))
                .filter(s -> Boolean.TRUE.equals(s.getIsAvailable()))
                .sorted(Comparator.comparing(InventoryStock::getReceivedDate, 
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .findFirst()
                .orElse(stocks.get(0)); // Fallback to first stock if none available
    }

    private void convertPendingToActual(String soNumber) {
        log.info("Converting pending reservations to actual for SO: {}", soNumber);
        // Implementation
    }

    private void ensureStockReserved(String soNumber) {
        log.info("Ensuring stock is reserved for SO: {}", soNumber);
        List<StockReservation> reservations = stockReservationRepository.findBySoNumber(soNumber);
        if (reservations.isEmpty()) {
            reserveStock(soNumber);
        }
    }

    // -------- Utility Methods --------

    private String getItemNameFromPickList(String pickListNumber, String itemCode) {
        List<PickListItem> items = pickListItemRepository.findByPickListNumber(pickListNumber);
        return items.stream()
                .filter(item -> item.getItemCode().equals(itemCode))
                .map(PickListItem::getItemName)
                .findFirst()
                .orElse(itemCode);
    }

    private Integer getPackedQuantity(String packageNumber) {
        PackageInfo packageInfo = packageInfoRepository.findByPackageNumber(packageNumber).orElse(null);
        return packageInfo != null ? packageInfo.getPackedQuantity() : 0;
    }

    // -------- Generation Methods --------

    private String generateReservationNumber() {
        return "RES-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String generatePickListNumber() {
        return "PL-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String generatePickTaskNumber() {
        return "PK-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String generateConfirmationNumber() {
        return "PC-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String generatePackageNumber() {
        return "PKG-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String generatePackageBarcode() {
        return "PKG" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private String generateLabelNumber() {
        return "SL-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String generateDispatchNumber() {
        return "DSP-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String generateShipmentNumber() {
        return "SHP-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String generateDeliveryNumber() {
        return "DEL-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    // -------- Response Builders --------

 private SalesOrderResponse buildSalesOrderResponse(SalesOrder order, List<SalesOrderItem> items) {
    
    // Get reservations for this SO
    List<StockReservation> reservations = stockReservationRepository.findBySoNumber(order.getSoNumber());
    
    // Build reservation responses
    List<StockReservationResponse> reservationResponses = reservations.stream()
            .map(reservation -> buildStockReservationResponse(reservation))
            .collect(Collectors.toList());

    // Build item responses with their reservations
    List<SalesOrderItemResponse> itemResponses = items.stream()
            .map(item -> {
                // Get reservations for this specific item
                List<StockReservationResponse> itemReservations = reservations.stream()
                        .filter(r -> r.getItemCode().equals(item.getItemCode()))
                        .map(reservation -> buildStockReservationResponse(reservation))
                        .collect(Collectors.toList());

                return SalesOrderItemResponse.builder()
                        .id(item.getId())
                        .soNumber(item.getSoNumber())
                        .itemCode(item.getItemCode())
                        .itemName(item.getItemName())
                        .uom(item.getUom())
                        .orderedQuantity(item.getOrderedQuantity())
                        .reservedQuantity(item.getReservedQuantity())
                        .pickedQuantity(item.getPickedQuantity())
                        .shippedQuantity(item.getShippedQuantity())
                        .batchNumber(item.getBatchNumber())
                        .sourceLocation(item.getSourceLocation())
                        .reservations(itemReservations)
                        .build();
            })
            .collect(Collectors.toList());

    return SalesOrderResponse.builder()
            .id(order.getId())
            .soNumber(order.getSoNumber())
            .soDate(order.getOrderDate())
            .customerCode(order.getCustomerCode())
            .customerName(order.getCustomerName())
            .warehouseId(order.getWarehouseId())
            .deliveryDate(order.getDeliveryDate())
            .priority(order.getPriority())
            .deliveryAddress(order.getDeliveryAddress())
            .totalQuantity(order.getTotalQuantity())
            .totalWeight(order.getTotalWeight())
            .shippingMethod(order.getShippingMethod())
            .status(order.getStatus())
            .remarks(order.getRemarks())
            .createdBy(order.getCreatedBy())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .items(itemResponses)
            .reservations(reservationResponses)
            .build();
}

    private SalesOrderItemResponse buildSalesOrderItemResponse(SalesOrderItem item) {
        List<StockReservation> reservations = stockReservationRepository.findBySalesOrderItemId(item.getId());
        List<StockReservationResponse> reservationResponses = reservations.stream()
                .map(this::buildStockReservationResponse)
                .collect(Collectors.toList());

        return SalesOrderItemResponse.builder()
                .id(item.getId())
                .soNumber(item.getSoNumber())
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .uom(item.getUom())
                .orderedQuantity(item.getOrderedQuantity())
                .reservedQuantity(item.getReservedQuantity())
                .pickedQuantity(item.getPickedQuantity())
                .shippedQuantity(item.getShippedQuantity())
                .batchNumber(item.getBatchNumber())
                .sourceLocation(item.getSourceLocation())
                .reservations(reservationResponses)
                .build();
    }

    private StockReservationResponse buildStockReservationResponse(StockReservation reservation) {
        return StockReservationResponse.builder()
                .id(reservation.getId())
                .reservationNumber(reservation.getReservationNumber())
                .soNumber(reservation.getSoNumber())
                .itemCode(reservation.getItemCode())
                .itemName(reservation.getItemName())
                .uom(reservation.getUom())
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

    private PickListResponse buildPickListResponse(PickList pickList, List<PickListItem> items) {
        return PickListResponse.builder()
                .pickListNumber(pickList.getPickListNumber())
                .soNumber(pickList.getSoNumber())
                .warehouseId(pickList.getWarehouseId())
                .priority(pickList.getPriority())
                .totalItems(pickList.getTotalItems())
                .totalQuantity(pickList.getTotalQuantity())
                .status(pickList.getStatus())
                .createdBy(pickList.getCreatedBy())
                .assignedTo(pickList.getAssignedTo())
                .completedDate(pickList.getCompletedDate())
                .remarks(pickList.getRemarks())
                .createdAt(pickList.getCreatedAt())
                .items(items.stream().map(this::buildPickListItemResponse).collect(Collectors.toList()))
                .build();
    }

    private PickListItemResponse buildPickListItemResponse(PickListItem item) {
        return PickListItemResponse.builder()
                .id(item.getId())
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .uom(item.getUom())
                .requiredQuantity(item.getRequiredQuantity())
                .pickedQuantity(item.getPickedQuantity())
                .shortQuantity(item.getShortQuantity())
                .sourceLocation(item.getSourceLocation())
                .batchNumber(item.getBatchNumber())
                .status(item.getStatus())
                .priority(item.getPriority())
                .build();
    }

    private PickTaskResponse buildPickTaskResponse(PickTask task) {
        return PickTaskResponse.builder()
                .pickTaskNumber(task.getPickTaskNumber())
                .pickListNumber(task.getPickListNumber())
                .soNumber(task.getSoNumber())
                .itemCode(task.getItemCode())
                .itemName(task.getItemName())
                .uom(task.getUom())
                .requiredQuantity(task.getRequiredQuantity())
                .quantityToPick(task.getQuantityToPick())
                .pickedQuantity(task.getPickedQuantity())
                .locationBarcode(task.getLocationBarcode())
                .itemBarcode(task.getItemBarcode())
                .binId(task.getBinId())
                .inventoryId(task.getInventoryId())
                .salesOrderLineId(task.getSalesOrderLineId())
                .batchNumber(task.getBatchNumber())
                .pickerId(task.getPickerId())
                .pickerName(task.getPickerName())
                .scanTime(task.getScanTime())
                .status(task.getStatus())
                .isScanned(task.getIsScanned())
                .remarks(task.getRemarks())
                .createdAt(task.getCreatedAt())
                .build();
    }

    private PickConfirmationResponse buildConfirmationResponse(PickConfirmation confirmation) {
        return PickConfirmationResponse.builder()
                .confirmationNumber(confirmation.getConfirmationNumber())
                .pickTaskNumber(confirmation.getPickTaskNumber())
                .pickListNumber(confirmation.getPickListNumber())
                .soNumber(confirmation.getSoNumber())
                .itemCode(confirmation.getItemCode())
                .itemName(confirmation.getItemName())
                .requiredQuantity(confirmation.getRequiredQuantity())
                .pickedQuantity(confirmation.getPickedQuantity())
                .shortQuantity(confirmation.getShortQuantity())
                .barcode(confirmation.getBarcode())
                .confirmedBy(confirmation.getConfirmedBy())
                .confirmedDate(confirmation.getConfirmedDate())
                .status(confirmation.getStatus())
                .remarks(confirmation.getRemarks())
                .build();
    }

    private PackageResponse buildPackageResponse(PackageInfo packageInfo) {
        return PackageResponse.builder()
                .packageNumber(packageInfo.getPackageNumber())
                .packageBarcode(packageInfo.getPackageBarcode())
                .soNumber(packageInfo.getSoNumber())
                .pickListNumber(packageInfo.getPickListNumber())
                .itemCode(packageInfo.getItemCode())
                .itemName(packageInfo.getItemName())
                .packedQuantity(packageInfo.getPackedQuantity())
                .packageType(packageInfo.getPackageType())
                .weight(packageInfo.getWeight())
                .length(packageInfo.getLength())
                .width(packageInfo.getWidth())
                .height(packageInfo.getHeight())
                .volume(packageInfo.getVolume())
                .packedBy(packageInfo.getPackedBy())
                .packedDate(packageInfo.getPackedDate())
                .status(packageInfo.getStatus())
                .remarks(packageInfo.getRemarks())
                .createdAt(packageInfo.getCreatedAt())
                .build();
    }

    private ShippingLabelResponse buildShippingLabelResponse(ShippingLabel label) {
        return ShippingLabelResponse.builder()
                .labelNumber(label.getLabelNumber())
                .packageNumber(label.getPackageNumber())
                .packageBarcode(label.getPackageBarcode())
                .soNumber(label.getSoNumber())
                .customerCode(label.getCustomerCode())
                .customerName(label.getCustomerName())
                .customerAddress(label.getCustomerAddress())
                .itemCode(label.getItemCode())
                .itemName(label.getItemName())
                .quantity(label.getQuantity())
                .weight(label.getWeight())
                .shippingMethod(label.getShippingMethod())
                .trackingNumber(label.getTrackingNumber())
                .labelStatus(label.getLabelStatus())
                .printedBy(label.getPrintedBy())
                .printedDate(label.getPrintedDate())
                .labelUrl(label.getLabelUrl())
                .remarks(label.getRemarks())
                .createdAt(label.getCreatedAt())
                .qrImage(label.getQrImage())
                .labelImage(label.getLabelImage())
                .barcode(label.getBarcode())
                .build();
    }

    private DispatchResponse buildDispatchResponse(Dispatch dispatch) {
        return DispatchResponse.builder()
                .dispatchNumber(dispatch.getDispatchNumber())
                .shipmentNumber(dispatch.getShipmentNumber())
                .soNumber(dispatch.getSoNumber())
                .packageNumber(dispatch.getPackageNumber())
                .customerCode(dispatch.getCustomerCode())
                .customerName(dispatch.getCustomerName())
                .transporter(dispatch.getTransporter())
                .vehicleNumber(dispatch.getVehicleNumber())
                .driverName(dispatch.getDriverName())
                .driverMobile(dispatch.getDriverMobile())
                .invoiceNumber(dispatch.getInvoiceNumber())
                .deliveryChallan(dispatch.getDeliveryChallan())
                .dispatchDate(dispatch.getDispatchDate())
                .status(dispatch.getStatus())
                .dispatchedBy(dispatch.getDispatchedBy())
                .remarks(dispatch.getRemarks())
                .createdAt(dispatch.getCreatedAt())
                .build();
    }

    private ShipmentConfirmationResponse buildShipmentConfirmationResponse(ShipmentConfirmation shipment) {
        return ShipmentConfirmationResponse.builder()
                .shipmentNumber(shipment.getShipmentNumber())
                .dispatchNumber(shipment.getDispatchNumber())
                .soNumber(shipment.getSoNumber())
                .packageNumber(shipment.getPackageNumber())
                .trackingNumber(shipment.getTrackingNumber())
                .transporter(shipment.getTransporter())
                .shippingMethod(shipment.getShippingMethod())
                .vehicleNumber(shipment.getVehicleNumber())
                .dispatchDate(shipment.getDispatchDate())
                .expectedDeliveryDate(shipment.getExpectedDeliveryDate())
                .actualDeliveryDate(shipment.getActualDeliveryDate())
                .status(shipment.getStatus())
                .confirmedBy(shipment.getConfirmedBy())
                .remarks(shipment.getRemarks())
                .createdAt(shipment.getCreatedAt())
                .build();
    }

    private DeliveryResponse buildDeliveryResponse(Delivery delivery) {
        return DeliveryResponse.builder()
                .deliveryNumber(delivery.getDeliveryNumber())
                .shipmentNumber(delivery.getShipmentNumber())
                .soNumber(delivery.getSoNumber())
                .packageNumber(delivery.getPackageNumber())
                .customerCode(delivery.getCustomerCode())
                .customerName(delivery.getCustomerName())
                .trackingNumber(delivery.getTrackingNumber())
                .deliveryDate(delivery.getDeliveryDate())
                .receivedBy(delivery.getReceivedBy())
                .deliveredQuantity(delivery.getDeliveredQuantity())
                .deliveryStatus(delivery.getDeliveryStatus())
                .signature(delivery.getSignature())
                .deliveryProofUrl(delivery.getDeliveryProofUrl())
                .remarks(delivery.getRemarks())
                .createdAt(delivery.getCreatedAt())
                .build();
    }
    
    
    private PickConfirmationResponse buildPickConfirmationResponse(PickConfirmation confirmation) {
        return PickConfirmationResponse.builder()
                .confirmationNumber(confirmation.getConfirmationNumber())
                .pickTaskNumber(confirmation.getPickTaskNumber())
                .pickListNumber(confirmation.getPickListNumber())
                .soNumber(confirmation.getSoNumber())
                .itemCode(confirmation.getItemCode())
                .itemName(confirmation.getItemName())
                .requiredQuantity(confirmation.getRequiredQuantity())
                .pickedQuantity(confirmation.getPickedQuantity())
                .shortQuantity(confirmation.getShortQuantity())
                .barcode(confirmation.getBarcode())
                .confirmedBy(confirmation.getConfirmedBy())
                .confirmedDate(confirmation.getConfirmedDate())
                .status(confirmation.getStatus())
                .remarks(confirmation.getRemarks())
                .createdAt(confirmation.getCreatedAt())
                .build();
    }
    
    
    
    
    private String generateLabelImages(ShippingLabel label) {
        // Generate label image with all details
        String labelData = String.format(
            "SHIPPING LABEL\n" +
            "Label: %s\n" +
            "Package: %s\n" +
            "SO: %s\n" +
            "Customer: %s\n" +
            "Address: %s\n" +
            "Item: %s\n" +
            "Qty: %d\n" +
            "Weight: %.2f kg\n" +
            "Tracking: %s\n" +
            "Status: %s",
            label.getLabelNumber(),
            label.getPackageNumber(),
            label.getSoNumber(),
            label.getCustomerName(),
            label.getCustomerAddress(),
            label.getItemName(),
            label.getQuantity(),
            label.getWeight(),
            label.getTrackingNumber() != null ? label.getTrackingNumber() : "N/A",
            label.getLabelStatus()
        );
        
        // Convert to Base64 (simplified - in production use actual image generation)
        return Base64.getEncoder().encodeToString(labelData.getBytes());
    }

    private String generateQrCode(ShippingLabel label) {
        // Generate QR code data as Base64
        String qrData = buildQrData(label);
        return Base64.getEncoder().encodeToString(qrData.getBytes());
    }

    private String buildQrData(ShippingLabel label) {
        return String.format(
            "{\"labelNumber\":\"%s\",\"packageNumber\":\"%s\",\"soNumber\":\"%s\",\"trackingNumber\":\"%s\"}",
            label.getLabelNumber(),
            label.getPackageNumber(),
            label.getSoNumber(),
            label.getTrackingNumber() != null ? label.getTrackingNumber() : ""
        );
    }

    private String buildLabelData(ShippingLabel label) {
        return String.format(
            "Label: %s | Package: %s | SO: %s | Customer: %s | Item: %s | Qty: %d | Tracking: %s",
            label.getLabelNumber(),
            label.getPackageNumber(),
            label.getSoNumber(),
            label.getCustomerName(),
            label.getItemName(),
            label.getQuantity(),
            label.getTrackingNumber() != null ? label.getTrackingNumber() : "N/A"
        );
    }
    
    private Color getStatusColor(String status) {
        if (status == null) return Color.BLACK;
        switch (status) {
            case "PRINTED": return Color.BLUE;
            case "SCANNED": return Color.ORANGE;
            case "SHIPPED": return Color.GREEN;
            case "CANCELLED": return Color.RED;
            default: return Color.BLACK;
        }
    }
}