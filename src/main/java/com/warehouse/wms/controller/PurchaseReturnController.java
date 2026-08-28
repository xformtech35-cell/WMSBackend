package com.warehouse.wms.controller;

import java.util.List;

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
import com.warehouse.wms.dto.request.PurchaseReturnRequestDTO;
import com.warehouse.wms.dto.response.PurchaseReturnResponseDTO;
import com.warehouse.wms.service.PurchaseReturnService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/purchase-returns")
@RequiredArgsConstructor
@Slf4j
public class PurchaseReturnController {

    private final PurchaseReturnService purchaseReturnService;

    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseReturnResponseDTO>> createPurchaseReturn(
            @Valid @RequestBody PurchaseReturnRequestDTO request) {
        log.info("REST request to create Purchase Return");
        PurchaseReturnResponseDTO response = purchaseReturnService.createPurchaseReturn(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase Return created successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseReturnResponseDTO>> updatePurchaseReturn(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseReturnRequestDTO request) {
        log.info("REST request to update Purchase Return with ID: {}", id);
        PurchaseReturnResponseDTO response = purchaseReturnService.updatePurchaseReturn(id, request);
        return ResponseEntity.ok(ApiResponse.success("Purchase Return updated successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseReturnResponseDTO>> getPurchaseReturnById(@PathVariable Long id) {
        log.info("REST request to get Purchase Return by ID: {}", id);
        PurchaseReturnResponseDTO response = purchaseReturnService.getPurchaseReturnById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/number/{returnNumber}")
    public ResponseEntity<ApiResponse<PurchaseReturnResponseDTO>> getPurchaseReturnByNumber(@PathVariable String returnNumber) {
        log.info("REST request to get Purchase Return by Number: {}", returnNumber);
        PurchaseReturnResponseDTO response = purchaseReturnService.getPurchaseReturnByNumber(returnNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PurchaseReturnResponseDTO>>> getAllPurchaseReturns(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("REST request to get all Purchase Returns");
        Page<PurchaseReturnResponseDTO> response = purchaseReturnService.getAllPurchaseReturns(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<PurchaseReturnResponseDTO>>> searchPurchaseReturns(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String searchTerm,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("REST request to search Purchase Returns");
        Page<PurchaseReturnResponseDTO> response = purchaseReturnService.searchPurchaseReturns(
                status, supplierName, searchTerm, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<ApiResponse<List<PurchaseReturnResponseDTO>>> getPurchaseReturnsBySupplier(
            @PathVariable Long supplierId) {
        log.info("REST request to get Purchase Returns by Supplier ID: {}", supplierId);
        List<PurchaseReturnResponseDTO> response = purchaseReturnService.getPurchaseReturnsBySupplier(supplierId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<PurchaseReturnResponseDTO>>> getPurchaseReturnsByStatus(
            @PathVariable String status) {
        log.info("REST request to get Purchase Returns by Status: {}", status);
        List<PurchaseReturnResponseDTO> response = purchaseReturnService.getPurchaseReturnsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('APPROVE_PURCHASE_RETURN')")
    public ResponseEntity<ApiResponse<PurchaseReturnResponseDTO>> approvePurchaseReturn(
            @PathVariable Long id,
            @RequestParam Long approvedBy) {
        log.info("REST request to approve Purchase Return: {}", id);
        PurchaseReturnResponseDTO response = purchaseReturnService.approvePurchaseReturn(id, approvedBy);
        return ResponseEntity.ok(ApiResponse.success("Purchase Return approved successfully", response));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<PurchaseReturnResponseDTO>> rejectPurchaseReturn(
            @PathVariable Long id,
            @RequestParam Long rejectedBy,
            @RequestParam String rejectionReason) {
        log.info("REST request to reject Purchase Return: {}", id);
        PurchaseReturnResponseDTO response = purchaseReturnService.rejectPurchaseReturn(id, rejectedBy, rejectionReason);
        return ResponseEntity.ok(ApiResponse.success("Purchase Return rejected successfully", response));
    }

    @PatchMapping("/{id}/ship")
    public ResponseEntity<ApiResponse<PurchaseReturnResponseDTO>> shipPurchaseReturn(
            @PathVariable Long id,
            @RequestParam Long shippedBy,
            @RequestParam String trackingNumber) {
        log.info("REST request to ship Purchase Return: {}", id);
        PurchaseReturnResponseDTO response = purchaseReturnService.shipPurchaseReturn(id, shippedBy, trackingNumber);
        return ResponseEntity.ok(ApiResponse.success("Purchase Return shipped successfully", response));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<PurchaseReturnResponseDTO>> completePurchaseReturn(@PathVariable Long id) {
        log.info("REST request to complete Purchase Return: {}", id);
        PurchaseReturnResponseDTO response = purchaseReturnService.completePurchaseReturn(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase Return completed successfully", response));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<PurchaseReturnResponseDTO>> cancelPurchaseReturn(@PathVariable Long id) {
        log.info("REST request to cancel Purchase Return: {}", id);
        PurchaseReturnResponseDTO response = purchaseReturnService.cancelPurchaseReturn(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase Return cancelled successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePurchaseReturn(@PathVariable Long id) {
        log.info("REST request to delete Purchase Return: {}", id);
        purchaseReturnService.deletePurchaseReturn(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase Return deleted successfully", null));
    }

    @GetMapping("/status-count/{status}")
    public ResponseEntity<ApiResponse<Long>> getCountByStatus(@PathVariable String status) {
        log.info("REST request to get count by status: {}", status);
        long count = purchaseReturnService.getCountByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}