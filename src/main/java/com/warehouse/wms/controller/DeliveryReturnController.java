package com.warehouse.wms.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.warehouse.wms.dto.request.CompleteReturnDto;
import com.warehouse.wms.dto.request.ReturnApprovalDto;
import com.warehouse.wms.dto.request.ReturnFilterRequest;
import com.warehouse.wms.dto.request.ReturnQcDto;
import com.warehouse.wms.dto.request.ReturnReceiveDto;
import com.warehouse.wms.dto.request.ReturnRequestDto;
import com.warehouse.wms.dto.response.ReturnResponseDto;
import com.warehouse.wms.dto.response.StandardResponse;
import com.warehouse.wms.service.DeliveryReturnService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/delivery/returns")
@RequiredArgsConstructor
public class DeliveryReturnController {

    private final DeliveryReturnService returnService;

    /** 1. Customer initiates a multi-item return */
    @PostMapping("/initiate")
    public ResponseEntity<ReturnResponseDto> initiate(
            @Valid @RequestBody ReturnRequestDto req) {
        return ResponseEntity.ok(returnService.initiateReturn(req));
    }

    /** 2. Admin approves / rejects (optionally per item) */
    @PostMapping("/approve")
    public ResponseEntity<ReturnResponseDto> approve(
            @Valid @RequestBody ReturnApprovalDto req) {
        return ResponseEntity.ok(returnService.approveReturn(req));
    }

    /** 3. Warehouse receives returned items */
    @PostMapping("/receive")
    public ResponseEntity<ReturnResponseDto> receive(
            @Valid @RequestBody ReturnReceiveDto req) {
        return ResponseEntity.ok(returnService.receiveReturn(req));
    }

    /** 4. Quality check (per item accepted / rejected / restock) */
    @PostMapping("/qc")
    public ResponseEntity<ReturnResponseDto> qc(
            @Valid @RequestBody ReturnQcDto req) {
        return ResponseEntity.ok(returnService.runQualityCheck(req));
    }

    /** 5. Complete / close the return */
    @PostMapping("/{returnId}/complete")
    public ResponseEntity<ReturnResponseDto> complete(
            @PathVariable Long returnId,
            @RequestBody(required = false) CompleteReturnDto req) {
        return ResponseEntity.ok(returnService.completeReturn(returnId, req));
    }

    /** 6. Cancel return */
    @PostMapping("/{returnId}/cancel")
    public ResponseEntity<ReturnResponseDto> cancel(@PathVariable Long returnId) {
        return ResponseEntity.ok(returnService.cancelReturn(returnId));
    }

    
    
   @GetMapping
@Operation(
    summary = "Get all delivery returns with pagination",
    description = "Retrieves all delivery returns with pagination, search and filters"
)
public ResponseEntity<StandardResponse<Page<ReturnResponseDto>>> getAllReturns(
        @PageableDefault(size = 20, sort = "returnRequestedAt", direction = Sort.Direction.DESC) Pageable pageable,

        @RequestParam(required = false)
        String search,

        @RequestParam(required = false)
        String returnStatus,

        @RequestParam(required = false)
        String itemReturnStatus,

        @RequestParam(required = false)
        Long deliveryId,

        @RequestParam(required = false)
        String deliveryNumber,

        @RequestParam(required = false)
        String soNumber,

        @RequestParam(required = false)
        String shipmentNumber,

        @RequestParam(required = false)
        String customerCode,

        @RequestParam(required = false)
        String customerName,

        @RequestParam(required = false)
        String itemCode,

        @RequestParam(required = false)
        String requestedBy,

        @RequestParam(required = false)
        String approvedBy,

        @RequestParam(required = false)
        String receivedBy,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate fromDate,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate toDate) {

    log.info("📦 Getting all delivery returns - page: {}, size: {}, search: {}, returnStatus: {}, deliveryId: {}",
            pageable.getPageNumber(), pageable.getPageSize(),
            search, returnStatus, deliveryId);

    ReturnFilterRequest filter = new ReturnFilterRequest();
    filter.setSearch(search);
    filter.setReturnStatus(returnStatus != null ? returnStatus.toUpperCase() : null);
    filter.setItemReturnStatus(itemReturnStatus != null ? itemReturnStatus.toUpperCase() : null);
    filter.setDeliveryId(deliveryId);
    filter.setDeliveryNumber(deliveryNumber);
    filter.setSoNumber(soNumber);
    filter.setShipmentNumber(shipmentNumber);
    filter.setCustomerCode(customerCode);
    filter.setCustomerName(customerName);
    filter.setItemCode(itemCode);
    filter.setRequestedBy(requestedBy);
    filter.setApprovedBy(approvedBy);
    filter.setReceivedBy(receivedBy);
    filter.setFromDate(fromDate);
    filter.setToDate(toDate);

    Page<ReturnResponseDto> responses = returnService.search(filter, pageable);

    return ResponseEntity.ok(
            StandardResponse.success("Returns fetched successfully", responses));
}
    
    
    /** 7. Get return by ID */
    @GetMapping("/{returnId}")
    public ResponseEntity<ReturnResponseDto> getById(@PathVariable Long returnId) {
        return ResponseEntity.ok(returnService.getById(returnId));
    }

    /** 8. List returns for a delivery */
    @GetMapping("/by-delivery/{deliveryId}")
    public ResponseEntity<List<ReturnResponseDto>> listByDelivery(
            @PathVariable Long deliveryId) {
        return ResponseEntity.ok(returnService.listByDelivery(deliveryId));
    }

    /** 9. List returns by status */
    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<ReturnResponseDto>> listByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(returnService.listByStatus(status.toUpperCase()));
    }
}