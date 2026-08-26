package com.warehouse.wms.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.warehouse.wms.dto.request.DeliveryRequest;
import com.warehouse.wms.dto.request.DispatchRequest;
import com.warehouse.wms.dto.request.PackageRequest;
import com.warehouse.wms.dto.request.PickConfirmationRequest;
import com.warehouse.wms.dto.request.PickListRequest;
import com.warehouse.wms.dto.request.PickTaskRequest;
import com.warehouse.wms.dto.request.SalesOrderItemUpdateRequest;
import com.warehouse.wms.dto.request.SalesOrderRequest;
import com.warehouse.wms.dto.request.ShipmentConfirmationRequest;
import com.warehouse.wms.dto.response.DeliveryResponse;
import com.warehouse.wms.dto.response.DispatchResponse;
import com.warehouse.wms.dto.response.PackageResponse;
import com.warehouse.wms.dto.response.PickConfirmationResponse;
import com.warehouse.wms.dto.response.PickListResponse;
import com.warehouse.wms.dto.response.PickTaskResponse;
import com.warehouse.wms.dto.response.SalesOrderItemResponse;
import com.warehouse.wms.dto.response.SalesOrderResponse;
import com.warehouse.wms.dto.response.ShipmentConfirmationResponse;
import com.warehouse.wms.dto.response.ShippingLabelResponse;
import com.warehouse.wms.dto.response.StockReservationResponse;
import com.warehouse.wms.service.OutboundService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/outbound")
@RequiredArgsConstructor
@Slf4j
public class OutboundController {

    private final OutboundService outboundService;

    // ============================================================
    // ===================== SALES ORDER ===========================
    // ============================================================

    // CREATE Sales Order
    @PostMapping("/sales-order")
    public ResponseEntity<SalesOrderResponse> createSalesOrder(@Valid @RequestBody SalesOrderRequest request) {
        log.info("POST /api/outbound/sales-order - Creating Sales Order");
        return ResponseEntity.status(HttpStatus.CREATED).body(outboundService.createSalesOrder(request));
    }

    // GET Sales Order by Number
    @GetMapping("/sales-order/{soNumber}")
    public ResponseEntity<SalesOrderResponse> getSalesOrder(@PathVariable String soNumber) {
        log.info("GET /api/outbound/sales-order/{} - Getting Sales Order", soNumber);
        return ResponseEntity.ok(outboundService.getSalesOrderByNumber(soNumber));
    }

    // GET All Sales Orders with Filters & Search
    @GetMapping("/sales-orders")
    public ResponseEntity<Page<SalesOrderResponse>> getAllSalesOrders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String soNumber,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String warehouseId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startCreatedDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endCreatedDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDeliveryDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDeliveryDate,
            @RequestParam(required = false) Integer minQuantity,
            @RequestParam(required = false) Integer maxQuantity,
            @RequestParam(required = false) String shippingMethod,
            @RequestParam(required = false) String createdBy,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("GET /api/outbound/sales-orders - Getting all Sales Orders with filters");
        
        Page<SalesOrderResponse> response = outboundService.getAllSalesOrdersWithFilters(
                search, soNumber, customerCode, customerName, warehouseId, 
                status, priority, startDate, endDate, 
                startCreatedDate, endCreatedDate,
                startDeliveryDate, endDeliveryDate,
                minQuantity, maxQuantity, shippingMethod, createdBy, pageable);
        
        return ResponseEntity.ok(response);
    }

    // UPDATE Sales Order - Full Update
    @PutMapping("/sales-order/{soNumber}")
    public ResponseEntity<SalesOrderResponse> updateSalesOrder(
            @PathVariable String soNumber,
            @Valid @RequestBody SalesOrderRequest request) {
        log.info("PUT /api/outbound/sales-order/{} - Updating Sales Order", soNumber);
        return ResponseEntity.ok(outboundService.updateSalesOrder(soNumber, request));
    }

    // UPDATE Sales Order Status
    @PutMapping("/sales-order/{soNumber}/status")
    public ResponseEntity<SalesOrderResponse> updateSalesOrderStatus(
            @PathVariable String soNumber,
            @RequestParam String status) {
        log.info("PUT /api/outbound/sales-order/{}/status - Updating status to {}", soNumber, status);
        return ResponseEntity.ok(outboundService.updateSalesOrderStatus(soNumber, status));
    }

    // DELETE Sales Order (Hard Delete)
    @DeleteMapping("/sales-order/{soNumber}")
    public ResponseEntity<Void> deleteSalesOrder(@PathVariable String soNumber) {
        log.info("DELETE /api/outbound/sales-order/{} - Deleting Sales Order", soNumber);
        outboundService.deleteSalesOrder(soNumber);
        return ResponseEntity.noContent().build();
    }

    // CANCEL Sales Order (Soft Delete)
    @DeleteMapping("/sales-order/{soNumber}/cancel")
    public ResponseEntity<Void> cancelSalesOrder(@PathVariable String soNumber) {
        log.info("DELETE /api/outbound/sales-order/{}/cancel - Cancelling Sales Order", soNumber);
        outboundService.cancelSalesOrder(soNumber);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // =================== SALES ORDER ITEM ========================
    // ============================================================

    // GET Sales Order Item by ID with Reservations
    @GetMapping("/sales-order-item/{itemId}")
    public ResponseEntity<SalesOrderItemResponse> getSalesOrderItemById(@PathVariable Long itemId) {
        log.info("GET /api/outbound/sales-order-item/{} - Getting Sales Order Item", itemId);
        return ResponseEntity.ok(outboundService.getSalesOrderItemById(itemId));
    }

    // GET All Items by SO Number
    @GetMapping("/sales-order-items/so/{soNumber}")
    public ResponseEntity<List<SalesOrderItemResponse>> getSalesOrderItemsBySoNumber(
            @PathVariable String soNumber) {
        log.info("GET /api/outbound/sales-order-items/so/{} - Getting items by SO", soNumber);
        return ResponseEntity.ok(outboundService.getSalesOrderItemsBySoNumber(soNumber));
    }

    // UPDATE Sales Order Item - Full Update
    @PutMapping("/sales-order-item/{itemId}")
    public ResponseEntity<SalesOrderItemResponse> updateSalesOrderItem(
            @PathVariable Long itemId,
            @Valid @RequestBody SalesOrderItemUpdateRequest request) {
        log.info("PUT /api/outbound/sales-order-item/{} - Updating Sales Order Item", itemId);
        return ResponseEntity.ok(outboundService.updateSalesOrderItem(itemId, request));
    }

    // UPDATE Item Quantity
    @PatchMapping("/sales-order-item/{itemId}/quantity")
    public ResponseEntity<SalesOrderItemResponse> updateSalesOrderItemQuantity(
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        log.info("PATCH /api/outbound/sales-order-item/{}/quantity - Updating to {}", itemId, quantity);
        return ResponseEntity.ok(outboundService.updateSalesOrderItemQuantity(itemId, quantity));
    }

    // UPDATE Item Reserved Quantity
    @PatchMapping("/sales-order-item/{itemId}/reserved-quantity")
    public ResponseEntity<SalesOrderItemResponse> updateSalesOrderItemReservedQuantity(
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        log.info("PATCH /api/outbound/sales-order-item/{}/reserved-quantity - Updating to {}", itemId, quantity);
        return ResponseEntity.ok(outboundService.updateSalesOrderItemReservedQuantity(itemId, quantity));
    }

    // UPDATE Item Picked Quantity
    @PatchMapping("/sales-order-item/{itemId}/picked-quantity")
    public ResponseEntity<SalesOrderItemResponse> updateSalesOrderItemPickedQuantity(
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        log.info("PATCH /api/outbound/sales-order-item/{}/picked-quantity - Updating to {}", itemId, quantity);
        return ResponseEntity.ok(outboundService.updateSalesOrderItemPickedQuantity(itemId, quantity));
    }

    // UPDATE Item Shipped Quantity
    @PatchMapping("/sales-order-item/{itemId}/shipped-quantity")
    public ResponseEntity<SalesOrderItemResponse> updateSalesOrderItemShippedQuantity(
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        log.info("PATCH /api/outbound/sales-order-item/{}/shipped-quantity - Updating to {}", itemId, quantity);
        return ResponseEntity.ok(outboundService.updateSalesOrderItemShippedQuantity(itemId, quantity));
    }

    // UPDATE Item Location
    @PatchMapping("/sales-order-item/{itemId}/location")
    public ResponseEntity<SalesOrderItemResponse> updateSalesOrderItemLocation(
            @PathVariable Long itemId,
            @RequestParam String sourceLocation) {
        log.info("PATCH /api/outbound/sales-order-item/{}/location - Updating to {}", itemId, sourceLocation);
        return ResponseEntity.ok(outboundService.updateSalesOrderItemLocation(itemId, sourceLocation));
    }

    // UPDATE Item Batch
    @PatchMapping("/sales-order-item/{itemId}/batch")
    public ResponseEntity<SalesOrderItemResponse> updateSalesOrderItemBatch(
            @PathVariable Long itemId,
            @RequestParam String batchNumber) {
        log.info("PATCH /api/outbound/sales-order-item/{}/batch - Updating to {}", itemId, batchNumber);
        return ResponseEntity.ok(outboundService.updateSalesOrderItemBatch(itemId, batchNumber));
    }

    // UPDATE Item Name
    @PatchMapping("/sales-order-item/{itemId}/name")
    public ResponseEntity<SalesOrderItemResponse> updateSalesOrderItemName(
            @PathVariable Long itemId,
            @RequestParam String itemName) {
        log.info("PATCH /api/outbound/sales-order-item/{}/name - Updating to {}", itemId, itemName);
        return ResponseEntity.ok(outboundService.updateSalesOrderItemName(itemId, itemName));
    }

    // UPDATE Item UOM
    @PatchMapping("/sales-order-item/{itemId}/uom")
    public ResponseEntity<SalesOrderItemResponse> updateSalesOrderItemUom(
            @PathVariable Long itemId,
            @RequestParam String uom) {
        log.info("PATCH /api/outbound/sales-order-item/{}/uom - Updating to {}", itemId, uom);
        return ResponseEntity.ok(outboundService.updateSalesOrderItemUom(itemId, uom));
    }

    // UPDATE Item Code
    @PatchMapping("/sales-order-item/{itemId}/code")
    public ResponseEntity<SalesOrderItemResponse> updateSalesOrderItemCode(
            @PathVariable Long itemId,
            @RequestParam String itemCode) {
        log.info("PATCH /api/outbound/sales-order-item/{}/code - Updating to {}", itemId, itemCode);
        return ResponseEntity.ok(outboundService.updateSalesOrderItemCode(itemId, itemCode));
    }

    // DELETE Sales Order Item
    @DeleteMapping("/sales-order-item/{itemId}")
    public ResponseEntity<Void> deleteSalesOrderItem(@PathVariable Long itemId) {
        log.info("DELETE /api/outbound/sales-order-item/{} - Deleting Sales Order Item", itemId);
        outboundService.deleteSalesOrderItem(itemId);
        return ResponseEntity.noContent().build();
    }

    // DELETE All Items for a Sales Order
    @DeleteMapping("/sales-order-items/so/{soNumber}")
    public ResponseEntity<Void> deleteSalesOrderItemsBySoNumber(@PathVariable String soNumber) {
        log.info("DELETE /api/outbound/sales-order-items/so/{} - Deleting all items for SO", soNumber);
        outboundService.deleteSalesOrderItemsBySoNumber(soNumber);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // =================== STOCK RESERVATION =======================
    // ============================================================

    // GET Reservation by Number
    @GetMapping("/reservation/{reservationNumber}")
    public ResponseEntity<StockReservationResponse> getReservation(@PathVariable String reservationNumber) {
        log.info("GET /api/outbound/reservation/{} - Getting Reservation", reservationNumber);
        return ResponseEntity.ok(outboundService.getReservationByNumber(reservationNumber));
    }

    // GET Reservations by SO Number
    @GetMapping("/reservations/so/{soNumber}")
    public ResponseEntity<List<StockReservationResponse>> getReservationsBySoNumber(@PathVariable String soNumber) {
        log.info("GET /api/outbound/reservations/so/{} - Getting Reservations by SO", soNumber);
        return ResponseEntity.ok(outboundService.getReservationsBySoNumber(soNumber));
    }

    // RELEASE Reservation
    @DeleteMapping("/reservation/{reservationNumber}")
    public ResponseEntity<Void> releaseReservation(@PathVariable String reservationNumber) {
        log.info("DELETE /api/outbound/reservation/{} - Releasing Reservation", reservationNumber);
        outboundService.releaseReservation(reservationNumber);
        return ResponseEntity.noContent().build();
    }

    // RELEASE All Reservations for SO
    @DeleteMapping("/reservations/so/{soNumber}")
    public ResponseEntity<Void> releaseAllReservations(@PathVariable String soNumber) {
        log.info("DELETE /api/outbound/reservations/so/{} - Releasing all Reservations", soNumber);
        outboundService.releaseAllReservations(soNumber);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // ===================== PICK LIST =============================
    // ============================================================

    // CREATE Pick List
    @PostMapping("/pick-list")
    public ResponseEntity<PickListResponse> createPickList(@Valid @RequestBody PickListRequest request) {
        log.info("POST /api/outbound/pick-list - Creating Pick List");
        return ResponseEntity.status(HttpStatus.CREATED).body(outboundService.createPickList(request));
    }

    // GET Pick List by Number
    @GetMapping("/pick-list/{pickListNumber}")
    public ResponseEntity<PickListResponse> getPickList(@PathVariable String pickListNumber) {
        log.info("GET /api/outbound/pick-list/{} - Getting Pick List", pickListNumber);
        return ResponseEntity.ok(outboundService.getPickListByNumber(pickListNumber));
    }

    // GET All Pick Lists with Filters & Search
    @GetMapping("/pick-lists")
    public ResponseEntity<Page<PickListResponse>> getAllPickLists(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String pickListNumber,
            @RequestParam(required = false) String soNumber,
            @RequestParam(required = false) String warehouseId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String assignedTo,
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startCreatedDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endCreatedDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startCompletedDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endCompletedDate,
            @RequestParam(required = false) Integer minTotalItems,
            @RequestParam(required = false) Integer maxTotalItems,
            @RequestParam(required = false) Integer minTotalQuantity,
            @RequestParam(required = false) Integer maxTotalQuantity,
            @RequestParam(required = false) String itemCode,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("GET /api/outbound/pick-lists - Getting all Pick Lists with filters");

        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(outboundService.searchPickLists(search, pageable));
        }

        Page<PickListResponse> response = outboundService.getAllPickListsWithFilters(
                pickListNumber, soNumber, warehouseId, status, priority,
                assignedTo, createdBy, startDate, endDate,
                startCreatedDate, endCreatedDate,
                startCompletedDate, endCompletedDate,
                minTotalItems, maxTotalItems,
                minTotalQuantity, maxTotalQuantity,
                itemCode, pageable);

        return ResponseEntity.ok(response);
    }

    // UPDATE Pick List Status
    @PatchMapping("/pick-list/{pickListNumber}/status")
    public ResponseEntity<PickListResponse> updatePickListStatus(
            @PathVariable String pickListNumber,
            @RequestParam String status) {
        log.info("PATCH /api/outbound/pick-list/{}/status - Updating status to {}", pickListNumber, status);
        return ResponseEntity.ok(outboundService.updatePickListStatus(pickListNumber, status));
    }

    // DELETE Pick List
    @DeleteMapping("/pick-list/{pickListNumber}")
    public ResponseEntity<Void> deletePickList(@PathVariable String pickListNumber) {
        log.info("DELETE /api/outbound/pick-list/{} - Deleting Pick List", pickListNumber);
        outboundService.deletePickList(pickListNumber);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // ===================== PICK TASK =============================
    // ============================================================

    // CREATE Pick Task
    @PostMapping("/pick-task")
    public ResponseEntity<PickTaskResponse> createPickTask(@Valid @RequestBody PickTaskRequest request) {
        log.info("POST /api/outbound/pick-task - Creating Pick Task");
        return ResponseEntity.status(HttpStatus.CREATED).body(outboundService.createPickTask(request));
    }

    // GET Pick Task by Number
    @GetMapping("/pick-task/{pickTaskNumber}")
    public ResponseEntity<PickTaskResponse> getPickTask(@PathVariable String pickTaskNumber) {
        log.info("GET /api/outbound/pick-task/{} - Getting Pick Task", pickTaskNumber);
        return ResponseEntity.ok(outboundService.getPickTaskByNumber(pickTaskNumber));
    }

    // GET Pick Tasks by Pick List
    @GetMapping("/pick-tasks/pick-list/{pickListNumber}")
    public ResponseEntity<List<PickTaskResponse>> getPickTasksByPickList(@PathVariable String pickListNumber) {
        log.info("GET /api/outbound/pick-tasks/pick-list/{} - Getting Pick Tasks for Pick List", pickListNumber);
        return ResponseEntity.ok(outboundService.getPickTasksByPickList(pickListNumber));
    }

    // GET All Pick Tasks with Filters & Search
    @GetMapping("/pick-tasks")
    public ResponseEntity<Page<PickTaskResponse>> getAllPickTasks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String pickTaskNumber,
            @RequestParam(required = false) String pickListNumber,
            @RequestParam(required = false) String soNumber,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String pickerId,
            @RequestParam(required = false) String pickerName,
            @RequestParam(required = false) String binId,
            @RequestParam(required = false) String locationBarcode,
            @RequestParam(required = false) String batchNumber,
            @RequestParam(required = false) Boolean isScanned,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startScanDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endScanDate,
            @RequestParam(required = false) Integer minRequiredQuantity,
            @RequestParam(required = false) Integer maxRequiredQuantity,
            @RequestParam(required = false) Integer minPickedQuantity,
            @RequestParam(required = false) Integer maxPickedQuantity,
            @RequestParam(required = false) String createdBy,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("GET /api/outbound/pick-tasks - Getting all Pick Tasks with filters");

        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(outboundService.searchPickTasks(search, pageable));
        }

        Page<PickTaskResponse> response = outboundService.getAllPickTasksWithFilters(
                pickTaskNumber, pickListNumber, soNumber, itemCode, itemName,
                status, pickerId, pickerName, binId, locationBarcode,
                batchNumber, isScanned, startDate, endDate,
                startScanDate, endScanDate,
                minRequiredQuantity, maxRequiredQuantity,
                minPickedQuantity, maxPickedQuantity,
                createdBy, pageable);

        return ResponseEntity.ok(response);
    }

    // SCAN Pick Task
    @PatchMapping("/pick-task/{pickTaskNumber}/scan")
    public ResponseEntity<PickTaskResponse> scanPickTask(
            @PathVariable String pickTaskNumber,
            @RequestParam String pickerId,
            @RequestParam String pickerName) {
        log.info("PATCH /api/outbound/pick-task/{}/scan - Scanning Pick Task", pickTaskNumber);
        return ResponseEntity.ok(outboundService.scanPickTask(pickTaskNumber, pickerId, pickerName));
    }

    // UPDATE Pick Task Status
    @PatchMapping("/pick-task/{pickTaskNumber}/status")
    public ResponseEntity<PickTaskResponse> updatePickTaskStatus(
            @PathVariable String pickTaskNumber,
            @RequestParam String status) {
        log.info("PATCH /api/outbound/pick-task/{}/status - Updating status to {}", pickTaskNumber, status);
        return ResponseEntity.ok(outboundService.updatePickTaskStatus(pickTaskNumber, status));
    }

    // DELETE Pick Task
    @DeleteMapping("/pick-task/{pickTaskNumber}")
    public ResponseEntity<Void> deletePickTask(@PathVariable String pickTaskNumber) {
        log.info("DELETE /api/outbound/pick-task/{} - Deleting Pick Task", pickTaskNumber);
        outboundService.deletePickTask(pickTaskNumber);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // ================== PICK CONFIRMATION ========================
    // ============================================================

    // CONFIRM Pick
    @PostMapping("/pick-confirmation")
    public ResponseEntity<PickConfirmationResponse> confirmPick(@Valid @RequestBody PickConfirmationRequest request) {
        log.info("POST /api/outbound/pick-confirmation - Confirming Pick");
        return ResponseEntity.status(HttpStatus.CREATED).body(outboundService.confirmPick(request));
    }

    // GET Pick Confirmation by Number
    @GetMapping("/pick-confirmation/{confirmationNumber}")
    public ResponseEntity<PickConfirmationResponse> getPickConfirmation(@PathVariable String confirmationNumber) {
        log.info("GET /api/outbound/pick-confirmation/{} - Getting Pick Confirmation", confirmationNumber);
        return ResponseEntity.ok(outboundService.getConfirmationByNumber(confirmationNumber));
    }
    
    
    
    
 // ====== GET ALL WITH FILTERS AND SEARCH ======
    @GetMapping("/pick-confirmations")
    public ResponseEntity<Page<PickConfirmationResponse>> getAllPickConfirmations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String confirmationNumber,
            @RequestParam(required = false) String pickTaskNumber,
            @RequestParam(required = false) String pickListNumber,
            @RequestParam(required = false) String soNumber,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) String confirmedBy,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startConfirmedDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endConfirmedDate,
            @RequestParam(required = false) Integer minPickedQuantity,
            @RequestParam(required = false) Integer maxPickedQuantity,
            @RequestParam(required = false) Integer minShortQuantity,
            @RequestParam(required = false) Integer maxShortQuantity,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("GET /api/outbound/pick-confirmations - Getting all Pick Confirmations with filters");

        // Handle search parameter
        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(outboundService.searchPickConfirmations(search, pageable));
        }

        Page<PickConfirmationResponse> response = outboundService.getAllPickConfirmationsWithFilters(
                confirmationNumber, pickTaskNumber, pickListNumber, soNumber,
                itemCode, itemName, confirmedBy, status, barcode,
                startDate, endDate, startConfirmedDate, endConfirmedDate,
                minPickedQuantity, maxPickedQuantity,
                minShortQuantity, maxShortQuantity, pageable);

        return ResponseEntity.ok(response);
    }


    // ============================================================
    // ===================== PACKAGE ===============================
    // ============================================================

    // CREATE Package
    @PostMapping("/package")
    public ResponseEntity<PackageResponse> createPackage(@Valid @RequestBody PackageRequest request) {
        log.info("POST /api/outbound/package - Creating Package");
        return ResponseEntity.status(HttpStatus.CREATED).body(outboundService.createPackage(request));
    }

    // GET Package by Number
    @GetMapping("/package/{packageNumber}")
    public ResponseEntity<PackageResponse> getPackage(@PathVariable String packageNumber) {
        log.info("GET /api/outbound/package/{} - Getting Package", packageNumber);
        return ResponseEntity.ok(outboundService.getPackageByNumber(packageNumber));
    }
    
    
    
    @GetMapping("/packages")
    public ResponseEntity<Page<PackageResponse>> getAllPackages(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String packageNumber,
            @RequestParam(required = false) String packageBarcode,
            @RequestParam(required = false) String soNumber,
            @RequestParam(required = false) String pickListNumber,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) String packageType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String packedBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startPackedDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endPackedDate,
            @RequestParam(required = false) Double minWeight,
            @RequestParam(required = false) Double maxWeight,
            @RequestParam(required = false) Integer minQuantity,
            @RequestParam(required = false) Integer maxQuantity,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("GET /api/outbound/packages - Getting all Packages with filters");

        // Handle search parameter
        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(outboundService.searchPackages(search, pageable));
        }

        Page<PackageResponse> response = outboundService.getAllPackagesWithFilters(
                packageNumber, packageBarcode, soNumber, pickListNumber,
                itemCode, itemName, packageType, status, packedBy,
                startDate, endDate, startPackedDate, endPackedDate,
                minWeight, maxWeight, minQuantity, maxQuantity, pageable);

        return ResponseEntity.ok(response);
    }
    

    // GET Package by Barcode
    @GetMapping("/package/barcode/{packageBarcode}")
    public ResponseEntity<PackageResponse> getPackageByBarcode(@PathVariable String packageBarcode) {
        log.info("GET /api/outbound/package/barcode/{} - Getting Package by Barcode", packageBarcode);
        return ResponseEntity.ok(outboundService.getPackageByBarcode(packageBarcode));
    }

    // UPDATE Package Status
    @PatchMapping("/package/{packageNumber}/status")
    public ResponseEntity<Void> updatePackageStatus(
            @PathVariable String packageNumber,
            @RequestParam String status) {
        log.info("PATCH /api/outbound/package/{}/status - Updating status to {}", packageNumber, status);
        outboundService.updatePackageStatus(packageNumber, status);
        return ResponseEntity.ok().build();
    }

    // DELETE Package
    @DeleteMapping("/package/{packageNumber}")
    public ResponseEntity<Void> deletePackage(@PathVariable String packageNumber) {
        log.info("DELETE /api/outbound/package/{} - Deleting Package", packageNumber);
        outboundService.deletePackage(packageNumber);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // ================== SHIPPING LABEL ===========================
    // ============================================================

    // GENERATE Shipping Label
    @PostMapping("/shipping-label/{packageNumber}")
    public ResponseEntity<ShippingLabelResponse> generateShippingLabel(@PathVariable String packageNumber) {
        log.info("POST /api/outbound/shipping-label/{} - Generating Shipping Label", packageNumber);
        return ResponseEntity.status(HttpStatus.CREATED).body(outboundService.generateShippingLabel(packageNumber));
    }

    // GET Shipping Label by Number
    @GetMapping("/shipping-label/{labelNumber}")
    public ResponseEntity<ShippingLabelResponse> getShippingLabel(@PathVariable String labelNumber) {
        log.info("GET /api/outbound/shipping-label/{} - Getting Shipping Label", labelNumber);
        return ResponseEntity.ok(outboundService.getShippingLabelByNumber(labelNumber));
    }

    // UPDATE Shipping Label Status
    @PatchMapping("/shipping-label/{labelNumber}/status")
    public ResponseEntity<Void> updateShippingLabelStatus(
            @PathVariable String labelNumber,
            @RequestParam String status) {
        log.info("PATCH /api/outbound/shipping-label/{}/status - Updating status to {}", labelNumber, status);
        outboundService.updateShippingLabelStatus(labelNumber, status);
        return ResponseEntity.ok().build();
    }

    // ============================================================
    // ===================== DISPATCH ==============================
    // ============================================================

    // CREATE Dispatch
    @PostMapping("/dispatch")
    public ResponseEntity<DispatchResponse> createDispatch(@Valid @RequestBody DispatchRequest request) {
        log.info("POST /api/outbound/dispatch - Creating Dispatch");
        return ResponseEntity.status(HttpStatus.CREATED).body(outboundService.createDispatch(request));
    }

    // GET Dispatch by Number
    @GetMapping("/dispatch/{dispatchNumber}")
    public ResponseEntity<DispatchResponse> getDispatch(@PathVariable String dispatchNumber) {
        log.info("GET /api/outbound/dispatch/{} - Getting Dispatch", dispatchNumber);
        return ResponseEntity.ok(outboundService.getDispatchByNumber(dispatchNumber));
    }

    // UPDATE Dispatch Status
    @PatchMapping("/dispatch/{dispatchNumber}/status")
    public ResponseEntity<DispatchResponse> updateDispatchStatus(
            @PathVariable String dispatchNumber,
            @RequestParam String status) {
        log.info("PATCH /api/outbound/dispatch/{}/status - Updating status to {}", dispatchNumber, status);
        return ResponseEntity.ok(outboundService.updateDispatchStatus(dispatchNumber, status));
    }

    // DELETE Dispatch
    @DeleteMapping("/dispatch/{dispatchNumber}")
    public ResponseEntity<Void> deleteDispatch(@PathVariable String dispatchNumber) {
        log.info("DELETE /api/outbound/dispatch/{} - Deleting Dispatch", dispatchNumber);
        outboundService.deleteDispatch(dispatchNumber);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // ================ SHIPMENT CONFIRMATION ======================
    // ============================================================

    // CONFIRM Shipment
    @PostMapping("/shipment-confirmation")
    public ResponseEntity<ShipmentConfirmationResponse> confirmShipment(@Valid @RequestBody ShipmentConfirmationRequest request) {
        log.info("POST /api/outbound/shipment-confirmation - Confirming Shipment");
        return ResponseEntity.status(HttpStatus.CREATED).body(outboundService.confirmShipment(request));
    }

    // GET Shipment by Number
    @GetMapping("/shipment-confirmation/{shipmentNumber}")
    public ResponseEntity<ShipmentConfirmationResponse> getShipment(@PathVariable String shipmentNumber) {
        log.info("GET /api/outbound/shipment-confirmation/{} - Getting Shipment", shipmentNumber);
        return ResponseEntity.ok(outboundService.getShipmentByNumber(shipmentNumber));
    }

    // UPDATE Shipment Status
    @PatchMapping("/shipment-confirmation/{shipmentNumber}/status")
    public ResponseEntity<ShipmentConfirmationResponse> updateShipmentStatus(
            @PathVariable String shipmentNumber,
            @RequestParam String status) {
        log.info("PATCH /api/outbound/shipment-confirmation/{}/status - Updating status to {}", shipmentNumber, status);
        return ResponseEntity.ok(outboundService.updateShipmentStatus(shipmentNumber, status));
    }

    // DELETE Shipment
    @DeleteMapping("/shipment-confirmation/{shipmentNumber}")
    public ResponseEntity<Void> deleteShipment(@PathVariable String shipmentNumber) {
        log.info("DELETE /api/outbound/shipment-confirmation/{} - Deleting Shipment", shipmentNumber);
        outboundService.deleteShipment(shipmentNumber);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // ===================== DELIVERY ==============================
    // ============================================================

    // CONFIRM Delivery
    @PostMapping("/delivery")
    public ResponseEntity<DeliveryResponse> confirmDelivery(@Valid @RequestBody DeliveryRequest request) {
        log.info("POST /api/outbound/delivery - Confirming Delivery");
        return ResponseEntity.status(HttpStatus.CREATED).body(outboundService.confirmDelivery(request));
    }

    // GET Delivery by Number
    @GetMapping("/delivery/{deliveryNumber}")
    public ResponseEntity<DeliveryResponse> getDelivery(@PathVariable String deliveryNumber) {
        log.info("GET /api/outbound/delivery/{} - Getting Delivery", deliveryNumber);
        return ResponseEntity.ok(outboundService.getDeliveryByNumber(deliveryNumber));
    }

    // UPDATE Delivery Status
    @PatchMapping("/delivery/{deliveryNumber}/status")
    public ResponseEntity<DeliveryResponse> updateDeliveryStatus(
            @PathVariable String deliveryNumber,
            @RequestParam String status) {
        log.info("PATCH /api/outbound/delivery/{}/status - Updating status to {}", deliveryNumber, status);
        return ResponseEntity.ok(outboundService.updateDeliveryStatus(deliveryNumber, status));
    }

    // DELETE Delivery
    @DeleteMapping("/delivery/{deliveryNumber}")
    public ResponseEntity<Void> deleteDelivery(@PathVariable String deliveryNumber) {
        log.info("DELETE /api/outbound/delivery/{} - Deleting Delivery", deliveryNumber);
        outboundService.deleteDelivery(deliveryNumber);
        return ResponseEntity.noContent().build();
    }
}