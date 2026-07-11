package com.warehouse.wms.controller;

import com.warehouse.wms.dto.ApiResponse;
import com.warehouse.wms.dto.CreatePurchaseRequestDTO;
import com.warehouse.wms.dto.PurchaseRequestDTO;
import com.warehouse.wms.dto.PurchaseRequestItemDTO;
import com.warehouse.wms.dto.ReceiveItemDTO;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.service.PurchaseRequestService;
import com.warehouse.wms.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/purchase-requests")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PurchaseRequestController {
    
    private final PurchaseRequestService purchaseRequestService;
    
    // Create new purchase request
    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseRequestDTO>> createPurchaseRequest(
            @Valid @RequestBody CreatePurchaseRequestDTO requestDTO) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            PurchaseRequestDTO created = purchaseRequestService.createPurchaseRequest(requestDTO, userId);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase request created successfully", created));
        } catch (Exception e) {
            log.error("Error creating purchase request", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Submit purchase request
    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<PurchaseRequestDTO>> submitPurchaseRequest(@PathVariable Long id) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            PurchaseRequestDTO submitted = purchaseRequestService.submitPurchaseRequest(id, userId);
            return ResponseEntity.ok(ApiResponse.success("Purchase request submitted successfully", submitted));
        } catch (Exception e) {
            log.error("Error submitting purchase request", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Approve purchase request
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<PurchaseRequestDTO>> approvePurchaseRequest(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestParam(required = false) String rejectionReason) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            PurchaseRequestDTO result = purchaseRequestService.approvePurchaseRequest(id, userId, approved, rejectionReason);
            String message = approved ? "Purchase request approved" : "Purchase request rejected";
            return ResponseEntity.ok(ApiResponse.success(message, result));
        } catch (Exception e) {
            log.error("Error approving purchase request", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Get all purchase requests with pagination and filters
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PurchaseRequestDTO>>> getAllPurchaseRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<PurchaseRequestDTO> requests = purchaseRequestService.getAllPurchaseRequests(
            status, priority, startDate, endDate, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(requests));
    }
    
    // Get purchase request by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseRequestDTO>> getPurchaseRequestById(@PathVariable Long id) {
        try {
            PurchaseRequestDTO request = purchaseRequestService.getPurchaseRequestById(id);
            return ResponseEntity.ok(ApiResponse.success(request));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Update purchase request
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseRequestDTO>> updatePurchaseRequest(
            @PathVariable Long id,
            @Valid @RequestBody CreatePurchaseRequestDTO requestDTO) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            PurchaseRequestDTO updated = purchaseRequestService.updatePurchaseRequest(id, requestDTO, userId);
            return ResponseEntity.ok(ApiResponse.success("Purchase request updated successfully", updated));
        } catch (Exception e) {
            log.error("Error updating purchase request", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Delete purchase request
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePurchaseRequest(@PathVariable Long id) {
        try {
            purchaseRequestService.deletePurchaseRequest(id);
            return ResponseEntity.ok(ApiResponse.success("Purchase request deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting purchase request", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Get purchase requests by current user
    @GetMapping("/my-requests")
    public ResponseEntity<ApiResponse<List<PurchaseRequestDTO>>> getMyPurchaseRequests() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<PurchaseRequestDTO> requests = purchaseRequestService.getPurchaseRequestsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }
    
    // Get statistics
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Object>> getStatistics() {
        Object stats = purchaseRequestService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // ✅ Receive items with quality check
   @PostMapping("/items/{itemId}/receive")
public ResponseEntity<ApiResponse<PurchaseRequestItemDTO>> receiveItem(
        @PathVariable Long itemId,
        @Valid @RequestBody ReceiveItemDTO receiveDTO) {
    try {
        log.info("=== RECEIVE ITEM DEBUG ===");
        log.info("itemId: {}", itemId);
        log.info("receiveDTO: {}", receiveDTO);
        log.info("receivedQuantity: {}", receiveDTO.getReceivedQuantity());
        log.info("qualityStatus: {}", receiveDTO.getQualityStatus());
        
        PurchaseRequestItemDTO updatedItem = purchaseRequestService.receiveItem(itemId, receiveDTO);
        return ResponseEntity.ok(ApiResponse.success("Item received successfully", updatedItem));
    } catch (Exception e) {
        log.error("Error receiving item", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(e.getMessage()));
    }
}
}