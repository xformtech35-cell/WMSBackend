package com.warehouse.wms.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import com.warehouse.wms.dto.ApiResponse;
import com.warehouse.wms.dto.request.DispatchDTO;
import com.warehouse.wms.dto.request.PackingDTO;
import com.warehouse.wms.dto.request.PickingDTO;
import com.warehouse.wms.dto.request.QCDTO;
import com.warehouse.wms.dto.request.SettlementDTO;
import com.warehouse.wms.dto.request.VendorReceiptDTO;
import com.warehouse.wms.dto.request.VendorReturnOrderDTO;
import com.warehouse.wms.dto.request.VendorReturnRequestDTO;
import com.warehouse.wms.dto.response.DispatchResponseDTO;
import com.warehouse.wms.dto.response.SettlementResponseDTO;
import com.warehouse.wms.dto.response.VendorReceiptResponseDTO;
import com.warehouse.wms.dto.response.VendorReturnOrderResponseDTO;
import com.warehouse.wms.dto.response.VendorReturnResponseDTO;
import com.warehouse.wms.service.VendorReturnService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/vendor-returns")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vendor Return Management", description = "Complete vendor return management APIs")
public class VendorReturnController {

    private final VendorReturnService vendorReturnService;

    // ========== RETURN REQUEST APIs ==========

    @PostMapping("/requests")
    @Operation(summary = "Create a return request")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<VendorReturnResponseDTO>> createReturnRequest(
            @Valid @RequestBody VendorReturnRequestDTO request) {
        log.info("REST request to create return request");
        VendorReturnResponseDTO response = vendorReturnService.createReturnRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Return request created successfully", response));
    }

    @PutMapping("/requests/{id}")
    @Operation(summary = "Update a return request")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<VendorReturnResponseDTO>> updateReturnRequest(
            @PathVariable Long id,
            @Valid @RequestBody VendorReturnRequestDTO request) {
        log.info("REST request to update return request with ID: {}", id);
        VendorReturnResponseDTO response = vendorReturnService.updateReturnRequest(id, request);
        return ResponseEntity.ok(ApiResponse.success("Return request updated successfully", response));
    }

    @GetMapping("/requests/{id}")
    @Operation(summary = "Get return request by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<ApiResponse<VendorReturnResponseDTO>> getReturnRequestById(@PathVariable Long id) {
        log.info("REST request to get return request by ID: {}", id);
        VendorReturnResponseDTO response = vendorReturnService.getReturnRequestById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/requests")
    @Operation(summary = "Get all return requests with pagination")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<ApiResponse<Page<VendorReturnResponseDTO>>> getAllReturnRequests(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("REST request to get all return requests");
        Page<VendorReturnResponseDTO> response = vendorReturnService.getAllReturnRequests(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/requests/search")
    @Operation(summary = "Search return requests")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<ApiResponse<Page<VendorReturnResponseDTO>>> searchReturnRequests(
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String searchTerm,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("REST request to search return requests");
        Page<VendorReturnResponseDTO> response = vendorReturnService.searchReturnRequests(
                supplierName, status, searchTerm, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/requests/{id}/submit")
    @Operation(summary = "Submit return request for approval")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<VendorReturnResponseDTO>> submitReturnRequest(@PathVariable Long id) {
        log.info("REST request to submit return request: {}", id);
        VendorReturnResponseDTO response = vendorReturnService.submitReturnRequest(id);
        return ResponseEntity.ok(ApiResponse.success("Return request submitted successfully", response));
    }

    @PatchMapping("/requests/{id}/approve")
    @Operation(summary = "Approve return request")
    public ResponseEntity<ApiResponse<VendorReturnResponseDTO>> approveReturnRequest(
            @PathVariable Long id,
            @RequestParam Long approvedBy) {
        log.info("REST request to approve return request: {}", id);
        VendorReturnResponseDTO response = vendorReturnService.approveReturnRequest(id, approvedBy);
        return ResponseEntity.ok(ApiResponse.success("Return request approved successfully", response));
    }

    @PatchMapping("/requests/{id}/reject")
    @Operation(summary = "Reject return request")
    public ResponseEntity<ApiResponse<VendorReturnResponseDTO>> rejectReturnRequest(
            @PathVariable Long id,
            @RequestParam Long rejectedBy,
            @RequestParam String rejectionReason) {
        log.info("REST request to reject return request: {}", id);
        VendorReturnResponseDTO response = vendorReturnService.rejectReturnRequest(id, rejectedBy, rejectionReason);
        return ResponseEntity.ok(ApiResponse.success("Return request rejected successfully", response));
    }

    @DeleteMapping("/requests/{id}")
    @Operation(summary = "Delete return request")
    public ResponseEntity<ApiResponse<Void>> deleteReturnRequest(@PathVariable Long id) {
        log.info("REST request to delete return request: {}", id);
        vendorReturnService.deleteReturnRequest(id);
        return ResponseEntity.ok(ApiResponse.success("Return request deleted successfully", null));
    }

    // ========== RETURN ORDER APIs ==========

    @PostMapping("/orders")
    @Operation(summary = "Create a return order")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<VendorReturnOrderResponseDTO>> createReturnOrder(
            @Valid @RequestBody VendorReturnOrderDTO request) {
        log.info("REST request to create return order");
        VendorReturnOrderResponseDTO response = vendorReturnService.createReturnOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Return order created successfully", response));
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "Get return order by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<ApiResponse<VendorReturnOrderResponseDTO>> getReturnOrderById(@PathVariable Long id) {
        log.info("REST request to get return order by ID: {}", id);
        VendorReturnOrderResponseDTO response = vendorReturnService.getReturnOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/orders")
    @Operation(summary = "Get all return orders with pagination")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<ApiResponse<Page<VendorReturnOrderResponseDTO>>> getAllReturnOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("REST request to get all return orders");
        Page<VendorReturnOrderResponseDTO> response = vendorReturnService.getAllReturnOrders(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/orders/{id}/generate-picklist")
    @Operation(summary = "Generate pick list for return order")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'WAREHOUSE')")
    public ResponseEntity<ApiResponse<VendorReturnOrderResponseDTO>> generatePickList(@PathVariable Long id, @RequestParam  String assignTo) 
    {
    	
    	
        log.info("REST request to generate pick list for order: {}", id);
        VendorReturnOrderResponseDTO response = vendorReturnService.generatePickList(id,assignTo);
        return ResponseEntity.ok(ApiResponse.success("Pick list generated successfully", response));
    }
    
    // ========== WAREHOUSE EXECUTION APIs ==========

    @PatchMapping("/orders/{id}/pick")
    @Operation(summary = "Perform picking for return order")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'WAREHOUSE')")
    public ResponseEntity<ApiResponse<VendorReturnOrderResponseDTO>> performPicking(
            @PathVariable Long id,
            @RequestBody List<PickingDTO> pickingDetails) {
        log.info("REST request to perform picking for order: {}", id);
        VendorReturnOrderResponseDTO response = vendorReturnService.performPicking(id, pickingDetails);
        return ResponseEntity.ok(ApiResponse.success("Picking completed successfully", response));
    }

    @PatchMapping("/orders/{id}/qc")
    @Operation(summary = "Perform QC for return order")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'WAREHOUSE')")
    public ResponseEntity<ApiResponse<VendorReturnOrderResponseDTO>> performQC(
            @PathVariable Long id,
            @RequestBody List<QCDTO> qcDetails) {
        log.info("REST request to perform QC for order: {}", id);
        VendorReturnOrderResponseDTO response = vendorReturnService.performQC(id, qcDetails);
        return ResponseEntity.ok(ApiResponse.success("QC completed successfully", response));
    }
    
    
    
    @PatchMapping("/orders/{orderId}/pack")
    @Operation(summary = "Perform packing for return order")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'WAREHOUSE')")
    public ResponseEntity<ApiResponse<VendorReturnOrderResponseDTO>> performPacking(
            @PathVariable Long orderId,
            @Valid @RequestBody List<PackingDTO> packingDetails) {
        log.info("REST request to perform packing for order ID: {}", orderId);
        VendorReturnOrderResponseDTO response = vendorReturnService.performPacking(orderId, packingDetails);
        return ResponseEntity.ok(ApiResponse.success("Packing completed successfully", response));
    }
    

    // ========== DISPATCH APIs ==========

    @PostMapping("/dispatches")
    @Operation(summary = "Create a dispatch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'WAREHOUSE')")
    public ResponseEntity<ApiResponse<DispatchResponseDTO>> createDispatch(
            @Valid @RequestBody DispatchDTO dispatchDTO) {
        log.info("REST request to create dispatch");
        DispatchResponseDTO response = vendorReturnService.createDispatch(dispatchDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Dispatch created successfully", response));
    }

    @PatchMapping("/dispatches/{id}/confirm")
    @Operation(summary = "Confirm dispatch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'WAREHOUSE')")
    public ResponseEntity<ApiResponse<DispatchResponseDTO>> confirmDispatch(@PathVariable Long id) {
        log.info("REST request to confirm dispatch: {}", id);
        DispatchResponseDTO response = vendorReturnService.confirmDispatch(id);
        return ResponseEntity.ok(ApiResponse.success("Dispatch confirmed successfully", response));
    }

    // ========== RECEIPT APIs ==========

    @PostMapping("/receipts")
    @Operation(summary = "Create a vendor receipt")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'WAREHOUSE')")
    public ResponseEntity<ApiResponse<VendorReceiptResponseDTO>> createReceipt(
            @Valid @RequestBody VendorReceiptDTO receiptDTO) {
        log.info("REST request to create receipt");
        VendorReceiptResponseDTO response = vendorReturnService.createReceipt(receiptDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Receipt created successfully", response));
    }

    // ========== SETTLEMENT APIs ==========

    @PostMapping("/settlements")
    @Operation(summary = "Create a settlement")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FINANCE')")
    public ResponseEntity<ApiResponse<SettlementResponseDTO>> createSettlement(
            @Valid @RequestBody SettlementDTO settlementDTO) {
        log.info("REST request to create settlement");
        SettlementResponseDTO response = vendorReturnService.createSettlement(settlementDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Settlement created successfully", response));
    }

    // ========== STATISTICS APIs ==========

    @GetMapping("/statistics/status-counts")
    @Operation(summary = "Get status counts for orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStatusCounts() {
        log.info("REST request to get status counts");
        Map<String, Long> counts = vendorReturnService.getStatusCounts();
        return ResponseEntity.ok(ApiResponse.success(counts));
    }

    @GetMapping("/statistics/request-status-counts")
    @Operation(summary = "Get request status counts")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getRequestStatusCounts() {
        log.info("REST request to get request status counts");
        Map<String, Long> counts = vendorReturnService.getRequestStatusCounts();
        return ResponseEntity.ok(ApiResponse.success(counts));
    }
}