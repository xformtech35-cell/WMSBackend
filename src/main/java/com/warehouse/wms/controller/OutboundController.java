package com.warehouse.wms.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import com.warehouse.wms.dto.request.SalesOrderRequest;
import com.warehouse.wms.dto.request.ShipmentConfirmationRequest;
import com.warehouse.wms.dto.response.DeliveryResponse;
import com.warehouse.wms.dto.response.DispatchResponse;
import com.warehouse.wms.dto.response.PackageResponse;
import com.warehouse.wms.dto.response.PickConfirmationResponse;
import com.warehouse.wms.dto.response.PickListResponse;
import com.warehouse.wms.dto.response.PickTaskResponse;
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

    // ====== SALES ORDER ======

    @PostMapping("/sales-order")
    public ResponseEntity<SalesOrderResponse> createSalesOrder(@Valid @RequestBody SalesOrderRequest request) {
        log.info("POST /api/outbound/sales-order - Creating Sales Order");
        return ResponseEntity.ok(outboundService.createSalesOrder(request));
    }

    @GetMapping("/sales-order/{soNumber}")
    public ResponseEntity<SalesOrderResponse> getSalesOrder(@PathVariable String soNumber) {
        log.info("GET /api/outbound/sales-order/{} - Getting Sales Order", soNumber);
        return ResponseEntity.ok(outboundService.getSalesOrderByNumber(soNumber));
    }

//    @GetMapping("/sales-orders")
//    public ResponseEntity<Page<SalesOrderResponse>> getAllSalesOrders(Pageable pageable) {
//        log.info("GET /api/outbound/sales-orders - Getting all Sales Orders");
//        return ResponseEntity.ok(outboundService.getAllSalesOrders(pageable));
//    }

    
    
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
    
    
    @PatchMapping("/sales-order/{soNumber}/status")
    public ResponseEntity<SalesOrderResponse> updateSalesOrderStatus(
            @PathVariable String soNumber,
            @RequestParam String status) {
        log.info("PATCH /api/outbound/sales-order/{}/status - Updating status to {}", soNumber, status);
        return ResponseEntity.ok(outboundService.updateSalesOrderStatus(soNumber, status));
    }

    @DeleteMapping("/sales-order/{soNumber}")
    public ResponseEntity<Void> cancelSalesOrder(@PathVariable String soNumber) {
        log.info("DELETE /api/outbound/sales-order/{} - Cancelling Sales Order", soNumber);
        outboundService.cancelSalesOrder(soNumber);
        return ResponseEntity.noContent().build();
    }

    // ====== STOCK RESERVATION ======

    @PostMapping("/reserve/{soNumber}")
    public ResponseEntity<Void> reserveStock(@PathVariable String soNumber) {
        log.info("POST /api/outbound/reserve/{} - Reserving Stock", soNumber);
        outboundService.reserveStock(soNumber);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reservation/{reservationNumber}")
    public ResponseEntity<StockReservationResponse> getReservation(@PathVariable String reservationNumber) {
        log.info("GET /api/outbound/reservation/{} - Getting Reservation", reservationNumber);
        return ResponseEntity.ok(outboundService.getReservationByNumber(reservationNumber));
    }

    @DeleteMapping("/reservation/{reservationNumber}")
    public ResponseEntity<Void> releaseReservation(@PathVariable String reservationNumber) {
        log.info("DELETE /api/outbound/reservation/{} - Releasing Reservation", reservationNumber);
        outboundService.releaseReservation(reservationNumber);
        return ResponseEntity.noContent().build();
    }

    // ====== PICK LIST ======

    @PostMapping("/pick-list")
    public ResponseEntity<PickListResponse> createPickList(@Valid @RequestBody PickListRequest request) {
        log.info("POST /api/outbound/pick-list - Creating Pick List");
        return ResponseEntity.ok(outboundService.createPickList(request));
    }

    @GetMapping("/pick-list/{pickListNumber}")
    public ResponseEntity<PickListResponse> getPickList(@PathVariable String pickListNumber) {
        log.info("GET /api/outbound/pick-list/{} - Getting Pick List", pickListNumber);
        return ResponseEntity.ok(outboundService.getPickListByNumber(pickListNumber));
    }

    // ====== GET ALL WITH FILTERS AND SEARCH ======
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

        // Handle search parameter
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


    @PatchMapping("/pick-list/{pickListNumber}/status")
    public ResponseEntity<PickListResponse> updatePickListStatus(
            @PathVariable String pickListNumber,
            @RequestParam String status) {
        log.info("PATCH /api/outbound/pick-list/{}/status - Updating status to {}", pickListNumber, status);
        return ResponseEntity.ok(outboundService.updatePickListStatus(pickListNumber, status));
    }

    // ====== PICK TASK ======

    @PostMapping("/pick-task")
    public ResponseEntity<PickTaskResponse> createPickTask(@Valid @RequestBody PickTaskRequest request) {
        log.info("POST /api/outbound/pick-task - Creating Pick Task");
        return ResponseEntity.ok(outboundService.createPickTask(request));
    }

    @GetMapping("/pick-task/{pickTaskNumber}")
    public ResponseEntity<PickTaskResponse> getPickTask(@PathVariable String pickTaskNumber) {
        log.info("GET /api/outbound/pick-task/{} - Getting Pick Task", pickTaskNumber);
        return ResponseEntity.ok(outboundService.getPickTaskByNumber(pickTaskNumber));
    }

    @GetMapping("/pick-tasks/pick-list/{pickListNumber}")
    public ResponseEntity<List<PickTaskResponse>> getPickTasksByPickList(@PathVariable String pickListNumber) {
        log.info("GET /api/outbound/pick-tasks/pick-list/{} - Getting Pick Tasks for Pick List", pickListNumber);
        return ResponseEntity.ok(outboundService.getPickTasksByPickList(pickListNumber));
    }

    @PatchMapping("/pick-task/{pickTaskNumber}/scan")
    public ResponseEntity<PickTaskResponse> scanPickTask(
            @PathVariable String pickTaskNumber,
            @RequestParam String pickerId,
            @RequestParam String pickerName) {
        log.info("PATCH /api/outbound/pick-task/{}/scan - Scanning Pick Task", pickTaskNumber);
        return ResponseEntity.ok(outboundService.scanPickTask(pickTaskNumber, pickerId, pickerName));
    }

    // ====== PICK CONFIRMATION ======

    @PostMapping("/pick-confirmation")
    public ResponseEntity<PickConfirmationResponse> confirmPick(@Valid @RequestBody PickConfirmationRequest request) {
        log.info("POST /api/outbound/pick-confirmation - Confirming Pick");
        return ResponseEntity.ok(outboundService.confirmPick(request));
    }

    @GetMapping("/pick-confirmation/{confirmationNumber}")
    public ResponseEntity<PickConfirmationResponse> getPickConfirmation(@PathVariable String confirmationNumber) {
        log.info("GET /api/outbound/pick-confirmation/{} - Getting Pick Confirmation", confirmationNumber);
        return ResponseEntity.ok(outboundService.getConfirmationByNumber(confirmationNumber));
    }

    // ====== PACKAGE ======

    @PostMapping("/package")
    public ResponseEntity<PackageResponse> createPackage(@Valid @RequestBody PackageRequest request) {
        log.info("POST /api/outbound/package - Creating Package");
        return ResponseEntity.ok(outboundService.createPackage(request));
    }

    @GetMapping("/package/{packageNumber}")
    public ResponseEntity<PackageResponse> getPackage(@PathVariable String packageNumber) {
        log.info("GET /api/outbound/package/{} - Getting Package", packageNumber);
        return ResponseEntity.ok(outboundService.getPackageByNumber(packageNumber));
    }

    @GetMapping("/package/barcode/{packageBarcode}")
    public ResponseEntity<PackageResponse> getPackageByBarcode(@PathVariable String packageBarcode) {
        log.info("GET /api/outbound/package/barcode/{} - Getting Package by Barcode", packageBarcode);
        return ResponseEntity.ok(outboundService.getPackageByBarcode(packageBarcode));
    }

    @PatchMapping("/package/{packageNumber}/status")
    public ResponseEntity<Void> updatePackageStatus(
            @PathVariable String packageNumber,
            @RequestParam String status) {
        log.info("PATCH /api/outbound/package/{}/status - Updating status to {}", packageNumber, status);
        outboundService.updatePackageStatus(packageNumber, status);
        return ResponseEntity.ok().build();
    }

    // ====== SHIPPING LABEL ======

    @PostMapping("/shipping-label/{packageNumber}")
    public ResponseEntity<ShippingLabelResponse> generateShippingLabel(@PathVariable String packageNumber) {
        log.info("POST /api/outbound/shipping-label/{} - Generating Shipping Label", packageNumber);
        return ResponseEntity.ok(outboundService.generateShippingLabel(packageNumber));
    }

    @GetMapping("/shipping-label/{labelNumber}")
    public ResponseEntity<ShippingLabelResponse> getShippingLabel(@PathVariable String labelNumber) {
        log.info("GET /api/outbound/shipping-label/{} - Getting Shipping Label", labelNumber);
        return ResponseEntity.ok(outboundService.getShippingLabelByNumber(labelNumber));
    }

    @PatchMapping("/shipping-label/{labelNumber}/status")
    public ResponseEntity<Void> updateShippingLabelStatus(
            @PathVariable String labelNumber,
            @RequestParam String status) {
        log.info("PATCH /api/outbound/shipping-label/{}/status - Updating status to {}", labelNumber, status);
        outboundService.updateShippingLabelStatus(labelNumber, status);
        return ResponseEntity.ok().build();
    }

    // ====== DISPATCH ======

    @PostMapping("/dispatch")
    public ResponseEntity<DispatchResponse> createDispatch(@Valid @RequestBody DispatchRequest request) {
        log.info("POST /api/outbound/dispatch - Creating Dispatch");
        return ResponseEntity.ok(outboundService.createDispatch(request));
    }

    @GetMapping("/dispatch/{dispatchNumber}")
    public ResponseEntity<DispatchResponse> getDispatch(@PathVariable String dispatchNumber) {
        log.info("GET /api/outbound/dispatch/{} - Getting Dispatch", dispatchNumber);
        return ResponseEntity.ok(outboundService.getDispatchByNumber(dispatchNumber));
    }

    @PatchMapping("/dispatch/{dispatchNumber}/status")
    public ResponseEntity<DispatchResponse> updateDispatchStatus(
            @PathVariable String dispatchNumber,
            @RequestParam String status) {
        log.info("PATCH /api/outbound/dispatch/{}/status - Updating status to {}", dispatchNumber, status);
        return ResponseEntity.ok(outboundService.updateDispatchStatus(dispatchNumber, status));
    }

    // ====== SHIPMENT CONFIRMATION ======

    @PostMapping("/shipment-confirmation")
    public ResponseEntity<ShipmentConfirmationResponse> confirmShipment(@Valid @RequestBody ShipmentConfirmationRequest request) {
        log.info("POST /api/outbound/shipment-confirmation - Confirming Shipment");
        return ResponseEntity.ok(outboundService.confirmShipment(request));
    }

    @GetMapping("/shipment-confirmation/{shipmentNumber}")
    public ResponseEntity<ShipmentConfirmationResponse> getShipment(@PathVariable String shipmentNumber) {
        log.info("GET /api/outbound/shipment-confirmation/{} - Getting Shipment", shipmentNumber);
        return ResponseEntity.ok(outboundService.getShipmentByNumber(shipmentNumber));
    }

    @PatchMapping("/shipment-confirmation/{shipmentNumber}/status")
    public ResponseEntity<ShipmentConfirmationResponse> updateShipmentStatus(
            @PathVariable String shipmentNumber,
            @RequestParam String status) {
        log.info("PATCH /api/outbound/shipment-confirmation/{}/status - Updating status to {}", shipmentNumber, status);
        return ResponseEntity.ok(outboundService.updateShipmentStatus(shipmentNumber, status));
    }

    // ====== DELIVERY ======

    @PostMapping("/delivery")
    public ResponseEntity<DeliveryResponse> confirmDelivery(@Valid @RequestBody DeliveryRequest request) {
        log.info("POST /api/outbound/delivery - Confirming Delivery");
        return ResponseEntity.ok(outboundService.confirmDelivery(request));
    }

    @GetMapping("/delivery/{deliveryNumber}")
    public ResponseEntity<DeliveryResponse> getDelivery(@PathVariable String deliveryNumber) {
        log.info("GET /api/outbound/delivery/{} - Getting Delivery", deliveryNumber);
        return ResponseEntity.ok(outboundService.getDeliveryByNumber(deliveryNumber));
    }

    @PatchMapping("/delivery/{deliveryNumber}/status")
    public ResponseEntity<DeliveryResponse> updateDeliveryStatus(
            @PathVariable String deliveryNumber,
            @RequestParam String status) {
        log.info("PATCH /api/outbound/delivery/{}/status - Updating status to {}", deliveryNumber, status);
        return ResponseEntity.ok(outboundService.updateDeliveryStatus(deliveryNumber, status));
    }
}