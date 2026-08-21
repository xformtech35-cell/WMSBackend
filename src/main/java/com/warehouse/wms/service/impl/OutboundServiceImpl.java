package com.warehouse.wms.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.warehouse.wms.dto.request.DeliveryRequest;
import com.warehouse.wms.dto.request.DispatchRequest;
import com.warehouse.wms.dto.request.PackageRequest;
import com.warehouse.wms.dto.request.PickConfirmationRequest;
import com.warehouse.wms.dto.request.PickListItemRequest;
import com.warehouse.wms.dto.request.PickListRequest;
import com.warehouse.wms.dto.request.PickTaskRequest;
import com.warehouse.wms.dto.request.SalesOrderItemRequest;
import com.warehouse.wms.dto.request.SalesOrderRequest;
import com.warehouse.wms.dto.request.ShipmentConfirmationRequest;
import com.warehouse.wms.dto.response.DeliveryResponse;
import com.warehouse.wms.dto.response.DispatchResponse;
import com.warehouse.wms.dto.response.PackageResponse;
import com.warehouse.wms.dto.response.PickConfirmationResponse;
import com.warehouse.wms.dto.response.PickListItemResponse;
import com.warehouse.wms.dto.response.PickListResponse;
import com.warehouse.wms.dto.response.PickTaskResponse;
import com.warehouse.wms.dto.response.SalesOrderItemResponse;
import com.warehouse.wms.dto.response.SalesOrderResponse;
import com.warehouse.wms.dto.response.ShipmentConfirmationResponse;
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
    private final SoNumberGenerator soNumberGenerator;  // ADD THIS

    // ====== SALES ORDER ======

    @Override
    public SalesOrderResponse createSalesOrder(SalesOrderRequest request) {
        
        
        
        
        
        
        String soNumber;
            soNumber = soNumberGenerator.generateSoNumber();
            log.info("Auto-generated SO Number: {}", soNumber);
      
            // Check if custom SO number already exists
            if (salesOrderRepository.findBySoNumber(soNumber).isPresent()) {
                throw new BusinessException("Sales Order already exists: " + soNumber);
            }
        

      

        // Validate items
        int totalQuantity = 0;
        for (SalesOrderItemRequest itemReq : request.getItems()) {
            // Check if item exists in inventory
            List<InventoryStock> stocks = inventoryStockRepository.findByItemCode(itemReq.getItemCode());
            if (stocks.isEmpty()) {
                throw new BusinessException("Item not found in inventory: " + itemReq.getItemCode());
            }
            totalQuantity += itemReq.getOrderedQuantity();
        }

        // Create Sales Order
        SalesOrder salesOrder = SalesOrder.builder()
                .soNumber(soNumber)
                .orderDate(request.getSoDate() != null ? request.getSoDate() : LocalDateTime.now())
                .customerCode(request.getCustomerCode())
                .customerName(request.getCustomerName())
                .warehouseId(request.getWarehouseId())
                .deliveryDate(request.getDeliveryDate())
                .priority(request.getPriority() != null ? request.getPriority() : "MEDIUM")
                .deliveryAddress(request.getDeliveryAddress())
                .totalQuantity(totalQuantity)
                .shippingMethod(request.getShippingMethod())
                .status("DRAFT")
                .remarks(request.getRemarks())
                .createdBy(request.getCreatedBy())
                .build();

        SalesOrder savedOrder = salesOrderRepository.save(salesOrder);

        // Create Order Items
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

        // Auto-reserve stock
       // reserveStock(request.getSoNumber());

        return buildSalesOrderResponse(savedOrder, items);
    }

    @Override
    public SalesOrderResponse getSalesOrderByNumber(String soNumber) {
        SalesOrder salesOrder = salesOrderRepository.findBySoNumber(soNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found: " + soNumber));

        List<SalesOrderItem> items = salesOrderItemRepository.findBySoNumber(soNumber);
        return buildSalesOrderResponse(salesOrder, items);
    }

//    @Override
//    public Page<SalesOrderResponse> getAllSalesOrders(Pageable pageable) {
//        return salesOrderRepository.findAll(pageable)
//                .map(order -> buildSalesOrderResponse(order, salesOrderItemRepository.findBySoNumber(order.getSoNumber())));
//    }
    
    @Override
    public Page<SalesOrderResponse> getAllSalesOrdersWithFilters(
            String search,
            String soNumber,
            String customerCode,
            String customerName,
            String warehouseId,
            String status,
            String priority,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime startCreatedDate,
            LocalDateTime endCreatedDate,
            LocalDateTime startDeliveryDate,
            LocalDateTime endDeliveryDate,
            Integer minQuantity,
            Integer maxQuantity,
            String shippingMethod,
            String createdBy,
            Pageable pageable) {

        log.info("Fetching sales orders with filters");

        // Handle search parameter
        if (StringUtils.hasText(search)) {
            // Search across multiple fields
            return salesOrderRepository.searchSalesOrders(search, pageable)
                    .map(order -> buildSalesOrderResponse(order, 
                            salesOrderItemRepository.findBySoNumber(order.getSoNumber())));
        }

        // Build query with filters
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
    public SalesOrderResponse updateSalesOrderStatus(String soNumber, String status) {
        SalesOrder salesOrder = salesOrderRepository.findBySoNumber(soNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found: " + soNumber));

        salesOrder.setStatus(status);
        salesOrder.setUpdatedBy("SYSTEM");
        
        if(status=="PENDING") {
        	
                reserveStock(salesOrder.getSoNumber());
           }      
        SalesOrder updated = salesOrderRepository.save(salesOrder);


        return buildSalesOrderResponse(updated, salesOrderItemRepository.findBySoNumber(soNumber));
    }
    
    

    
    
    
    @Override
    public void cancelSalesOrder(String soNumber) {
        SalesOrder salesOrder = salesOrderRepository.findBySoNumber(soNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found: " + soNumber));

        if (!salesOrder.getStatus().equals("CONFIRMED") && !salesOrder.getStatus().equals("PROCESSING")) {
            throw new BusinessException("Cannot cancel order in status: " + salesOrder.getStatus());
        }

        // Release reservations
        List<StockReservation> reservations = stockReservationRepository.findBySoNumber(soNumber);
        for (StockReservation reservation : reservations) {
            releaseReservation(reservation.getReservationNumber());
        }

        salesOrder.setStatus("CANCELLED");
        salesOrder.setUpdatedBy("SYSTEM");
        salesOrderRepository.save(salesOrder);
    }

    // ====== STOCK RESERVATION ======

 @Override
public StockReservation reserveStock(String soNumber) {
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

            // Initialize null values
            if (stock.getReservedQuantity() == null) {
                stock.setReservedQuantity(0);
            }
            if (stock.getAvailableQuantity() == null) {
                stock.setAvailableQuantity(0);
            }

            int available = stock.getAvailableQuantity();
            
            int pysical=stock.getQuantity();
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
                        .pysicalQuantity(pysical)
                        .reservedQuantity(toReserve)
                        .warehouseId(stock.getWarehouseId())
                        .zoneId(stock.getZone())
                        .aisleId(stock.getAisle())
                        .rackId(stock.getRack())
                        .levelId(stock.getLevel())
                        .binId(stock.getBinId())
                        .batchNumber(stock.getBatchNumber())
                        .status("PENDING")
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
        return buildReservationResponse(reservation);
    }

    @Override
    public void releaseReservation(String reservationNumber) {
        StockReservation reservation = stockReservationRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + reservationNumber));

        if (reservation.getStatus().equals("RELEASED") || reservation.getStatus().equals("CANCELLED")) {
            return;
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

        reservation.setStatus("RELEASED");
        stockReservationRepository.save(reservation);
    }

    // ====== PICK LIST ======

    @Override
    public PickListResponse createPickList(PickListRequest request) {
        log.info("Creating Pick List for SO: {}", request.getSoNumber());

        // Verify SO exists
        SalesOrder salesOrder = salesOrderRepository.findBySoNumber(request.getSoNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found: " + request.getSoNumber()));

        // Generate pick list number
        String pickListNumber = generatePickListNumber();

        // Calculate totals
        int totalItems = request.getItems().size();
        int totalQuantity = request.getItems().stream().mapToInt(PickListItemRequest::getRequiredQuantity).sum();

        // Create Pick List
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

        // Create Pick List Items
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

        // Update order status
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
            String pickListNumber,
            String soNumber,
            String warehouseId,
            String status,
            String priority,
            String assignedTo,
            String createdBy,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime startCreatedDate,
            LocalDateTime endCreatedDate,
            LocalDateTime startCompletedDate,
            LocalDateTime endCompletedDate,
            Integer minTotalItems,
            Integer maxTotalItems,
            Integer minTotalQuantity,
            Integer maxTotalQuantity,
            String itemCode,
            Pageable pageable) {

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

    // ====== PICK TASK ======
@Override
public PickTaskResponse createPickTask(PickTaskRequest request) {
    log.info("Creating Pick Task for Pick List: {}", request.getPickListNumber());

    // Verify Pick List exists
    PickList pickList = pickListRepository.findByPickListNumber(request.getPickListNumber())
            .orElseThrow(() -> new ResourceNotFoundException("Pick List not found: " + request.getPickListNumber()));

    // Verify Pick List Item exists
    List<PickListItem> items = pickListItemRepository.findByPickListNumber(request.getPickListNumber());
    PickListItem pickItem = items.stream()
            .filter(item -> item.getItemCode().equals(request.getItemCode()))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Item not found in Pick List: " + request.getItemCode()));

    String pickTaskNumber = generatePickTaskNumber();

    // DON'T set inventoryId - keep it null
    // The foreign key constraint requires a valid ID from wms_inventory table
    Long inventoryId = null;  // Set to null to avoid foreign key violation

    // Find Sales Order Line ID
    Long salesOrderLineId = request.getSalesOrderLineId();
    if (salesOrderLineId == null) {
        List<SalesOrderItem> orderItems = salesOrderItemRepository.findByItemCode(request.getItemCode());
        if (!orderItems.isEmpty()) {
            salesOrderLineId = orderItems.get(0).getId();
        }
    }

    // Create Pick Task
    PickTask pickTask = PickTask.builder()
            .pickTaskNumber(pickTaskNumber)
            .pickListNumber(request.getPickListNumber())
            .soNumber(pickList.getSoNumber())
            .itemCode(request.getItemCode())
            .itemName(pickItem.getItemName())
            .uom(pickItem.getUom())
            .requiredQuantity(request.getRequiredQuantity())
            .quantityToPick(request.getRequiredQuantity())
            .inventoryId(null)  // Set to null
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

    // Update Pick List status
    pickList.setStatus("PICKING");
    pickList.setUpdatedBy(request.getCreatedBy());
    pickListRepository.save(pickList);

    // Update item status
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
    public PickTaskResponse scanPickTask(String pickTaskNumber, String pickerId, String pickerName) {
        PickTask pickTask = pickTaskRepository.findByPickTaskNumber(pickTaskNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Pick Task not found: " + pickTaskNumber));

        if (pickTask.getIsScanned()) {
            throw new BusinessException("Pick Task already scanned");
        }

        // Update pick task
        pickTask.setStatus("SCANNED");
        pickTask.setIsScanned(true);
        pickTask.setPickerId(pickerId);
        pickTask.setPickerName(pickerName);
        pickTask.setScanTime(LocalDateTime.now());
        pickTask.setUpdatedBy(pickerName);

        PickTask updated = pickTaskRepository.save(pickTask);

        // Update Pick List status
        PickList pickList = pickListRepository.findByPickListNumber(pickTask.getPickListNumber()).orElse(null);
        if (pickList != null) {
            pickList.setStatus("PICKING");
            pickListRepository.save(pickList);
        }

        // Update Pick List Item status
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
    public Page<PickTaskResponse> getAllPickTasksWithFilters(
            String pickTaskNumber,
            String pickListNumber,
            String soNumber,
            String itemCode,
            String itemName,
            String status,
            String pickerId,
            String pickerName,
            String binId,
            String locationBarcode,
            String batchNumber,
            Boolean isScanned,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime startScanDate,
            LocalDateTime endScanDate,
            Integer minRequiredQuantity,
            Integer maxRequiredQuantity,
            Integer minPickedQuantity,
            Integer maxPickedQuantity,
            String createdBy,
            Pageable pageable) {

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
    
    
    
    
    
    
    
    
    
    
    
    

    // ====== PICK CONFIRMATION ======

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

        // Update pick task
        pickTask.setPickedQuantity(request.getPickedQuantity());
        pickTask.setStatus("CONFIRMED");
        pickTask.setScanTime(LocalDateTime.now());
        pickTaskRepository.save(pickTask);

        // Create confirmation record
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

        // Update Pick List Item
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

        // Update Inventory Stock
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

        // Check if all items are picked
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

            // Update Sales Order status
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

    // ====== PACKAGE ======

    @Override
    public PackageResponse createPackage(PackageRequest request) {
        log.info("Creating Package for SO: {}", request.getSoNumber());

        // Verify Pick List
        PickList pickList = pickListRepository.findByPickListNumber(request.getPickListNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Pick List not found: " + request.getPickListNumber()));

        // Generate package details
        String packageNumber = generatePackageNumber();
        String packageBarcode = generatePackageBarcode();

        // Calculate volume if dimensions provided
        Double volume = null;
        if (request.getLength() != null && request.getWidth() != null && request.getHeight() != null) {
            volume = request.getLength() * request.getWidth() * request.getHeight();
        }

        // Create Package
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

        // Update Sales Order status
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
    }

    // ====== SHIPPING LABEL ======

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

        // Update Package status
        packageInfo.setStatus("LABELED");
        packageInfoRepository.save(packageInfo);

        log.info("Shipping Label generated: {}", labelNumber);
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
    }

    // ====== DISPATCH ======

    @Override
    public DispatchResponse createDispatch(DispatchRequest request) {
        log.info("Creating Dispatch for SO: {}", request.getSoNumber());

        // Verify SO and Package
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

        // Update Package status
        packageInfo.setStatus("DISPATCHED");
        packageInfoRepository.save(packageInfo);

        // Update Sales Order status
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

        // If status is IN_TRANSIT, create Shipment Confirmation
        if ("IN_TRANSIT".equals(status)) {
            // Auto create shipment confirmation
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

            // Update dispatch with shipment number
            dispatch.setShipmentNumber(shipmentNumber);
            dispatchRepository.save(dispatch);
        }

        return buildDispatchResponse(updated);
    }

    // ====== SHIPMENT CONFIRMATION ======

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

        // Update dispatch
        dispatch.setShipmentNumber(shipmentNumber);
        dispatch.setStatus("IN_TRANSIT");
        dispatchRepository.save(dispatch);

        // Update Shipping Label with tracking number
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

            // Auto create Delivery record
            DeliveryRequest deliveryRequest = DeliveryRequest.builder()
                    .shipmentNumber(shipmentNumber)
                    .soNumber(shipment.getSoNumber())
                    .packageNumber(shipment.getPackageNumber())
                    .receivedBy("CUSTOMER")
                    .deliveredQuantity(getPackedQuantity(shipment.getPackageNumber()))
                    .createdBy("SYSTEM")
                    .build();
            confirmDelivery(deliveryRequest);

            // Update Sales Order status
            SalesOrder salesOrder = salesOrderRepository.findBySoNumber(shipment.getSoNumber()).orElse(null);
            if (salesOrder != null) {
                salesOrder.setStatus("DELIVERED");
                salesOrderRepository.save(salesOrder);
            }
        } else {
            shipment.setStatus(status);
            shipmentConfirmationRepository.save(shipment);
        }

        return buildShipmentConfirmationResponse(shipment);
    }

    // ====== DELIVERY ======

    @Override
    public DeliveryResponse confirmDelivery(DeliveryRequest request) {
        log.info("Confirming Delivery for Shipment: {}", request.getShipmentNumber());

        ShipmentConfirmation shipment = shipmentConfirmationRepository.findByShipmentNumber(request.getShipmentNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + request.getShipmentNumber()));

        // Verify SO
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

        // Update Shipment status
        shipment.setStatus("DELIVERED");
        shipment.setActualDeliveryDate(LocalDateTime.now());
        shipmentConfirmationRepository.save(shipment);

        // Update Dispatch status
        Dispatch dispatch = dispatchRepository.findByShipmentNumber(request.getShipmentNumber()).orElse(null);
        if (dispatch != null) {
            dispatch.setStatus("DELIVERED");
            dispatchRepository.save(dispatch);
        }

        // Update Package status
        PackageInfo packageInfo = packageInfoRepository.findByPackageNumber(delivery.getPackageNumber()).orElse(null);
        if (packageInfo != null) {
            packageInfo.setStatus("DELIVERED");
            packageInfoRepository.save(packageInfo);
        }

        // Update Sales Order status
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
    public DeliveryResponse updateDeliveryStatus(String deliveryNumber, String status) {
        Delivery delivery = deliveryRepository.findByDeliveryNumber(deliveryNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found: " + deliveryNumber));

        delivery.setDeliveryStatus(status);
        Delivery updated = deliveryRepository.save(delivery);
        return buildDeliveryResponse(updated);
    }

    // ====== HELPER METHODS ======

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

    // ====== GENERATION METHODS ======

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

    // ====== RESPONSE BUILDERS ======

    private SalesOrderResponse buildSalesOrderResponse(SalesOrder order, List<SalesOrderItem> items) {
        
        // Get reservations for this SO
        List<StockReservation> reservations = stockReservationRepository.findBySoNumber(order.getSoNumber());
        List<StockReservationResponse> reservationResponses = reservations.stream()
                .map(this::buildStockReservationResponse)
                .collect(Collectors.toList());
        
        // Build item responses with their reservations
        List<SalesOrderItemResponse> itemResponses = items.stream()
                .map(item -> {
                    // Get reservations for this specific item
                    List<StockReservationResponse> itemReservations = reservations.stream()
                            .filter(r -> r.getItemCode().equals(item.getItemCode()))
                            .map(this::buildStockReservationResponse)
                            .collect(Collectors.toList());
                    
                    return SalesOrderItemResponse.builder()
                            .id(item.getId())
                            .itemCode(item.getItemCode())
                            .itemName(item.getItemName())
                            .uom(item.getUom())
                            .orderedQuantity(item.getOrderedQuantity())
                            .reservedQuantity(item.getReservedQuantity())
                            .pickedQuantity(item.getPickedQuantity())
                            .shippedQuantity(item.getShippedQuantity())
                            .batchNumber(item.getBatchNumber())
                            .sourceLocation(item.getSourceLocation())
                            .reservations(itemReservations)  // Add item-specific reservations
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
                .reservations(reservationResponses)  // Add all reservations
                .build();
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

    private SalesOrderItemResponse buildSalesOrderItemResponse(SalesOrderItem item) {
        return SalesOrderItemResponse.builder()
                .id(item.getId())
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .uom(item.getUom())
                .orderedQuantity(item.getOrderedQuantity())
                .reservedQuantity(item.getReservedQuantity())
                .pickedQuantity(item.getPickedQuantity())
                .shippedQuantity(item.getShippedQuantity())
                .batchNumber(item.getBatchNumber())
                .sourceLocation(item.getSourceLocation())
                .build();
    }

    private StockReservationResponse buildReservationResponse(StockReservation reservation) {
        return StockReservationResponse.builder()
                .reservationNumber(reservation.getReservationNumber())
                .soNumber(reservation.getSoNumber())
//                .itemCode(reservation.getItemCode())
//                .itemName(reservation.getItemName())
//                .uom(reservation.getUom())
                .requiredQuantity(reservation.getRequiredQuantity())
                .availableQuantity(reservation.getAvailableQuantity())
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
                .remarks(reservation.getRemarks())
                .createdAt(reservation.getCreatedAt())
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
                .pickedQuantity(task.getPickedQuantity())
                .locationBarcode(task.getLocationBarcode())
                .itemBarcode(task.getItemBarcode())
                .binId(task.getBinId())
                .inventoryId(task.getInventoryId())
                .quantityToPick(task.getQuantityToPick())
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
}