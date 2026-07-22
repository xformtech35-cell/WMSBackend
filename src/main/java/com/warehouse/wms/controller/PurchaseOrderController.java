package com.warehouse.wms.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.warehouse.wms.dto.ApiResponse;
import com.warehouse.wms.dto.CreatePurchaseOrderDTO;
import com.warehouse.wms.dto.PurchaseOrderDTO;
import com.warehouse.wms.dto.PurchaseOrderFilterDTO;
import com.warehouse.wms.dto.PurchaseOrderFilterRequestDTO;
import com.warehouse.wms.dto.StatusUpdateRequestDTOPO;
import com.warehouse.wms.service.PurchaseOrderService;
import com.warehouse.wms.util.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    // ============ CREATE ============
    
    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> createPurchaseOrder(
            @Valid @RequestBody CreatePurchaseOrderDTO requestDTO) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            PurchaseOrderDTO created = purchaseOrderService.createPurchaseOrder(requestDTO, userId);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase order created successfully", created));
        } catch (Exception e) {
            log.error("Error creating purchase order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ============ READ ============
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> getPurchaseOrderById(@PathVariable Long id) {
        try {
            PurchaseOrderDTO order = purchaseOrderService.getPurchaseOrderById(id);
            return ResponseEntity.ok(ApiResponse.success(order));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/number/{poNumber}")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> getPurchaseOrderByNumber(@PathVariable String poNumber) {
        try {
            PurchaseOrderDTO order = purchaseOrderService.getPurchaseOrderByPoNumber(poNumber);
            return ResponseEntity.ok(ApiResponse.success(order));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/filter")
    public ResponseEntity<ApiResponse<Page<PurchaseOrderDTO>>> filterPurchaseOrders(
            @RequestBody(required = false) PurchaseOrderFilterRequestDTO filterRequest) {
        try {
            if (filterRequest == null) {
                filterRequest = PurchaseOrderFilterRequestDTO.builder()
                    .filters(PurchaseOrderFilterDTO.builder().build())
                    .build();
            }
            
            if (filterRequest.getFilters() == null) {
                filterRequest.setFilters(PurchaseOrderFilterDTO.builder().build());
            }
            
            Sort.Direction direction = filterRequest.getSortDir().equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(
                filterRequest.getPage(), 
                filterRequest.getSize(), 
                Sort.by(direction, filterRequest.getSortBy())
            );
            
            Page<PurchaseOrderDTO> result = purchaseOrderService.filterPurchaseOrders(
                filterRequest.getFilters(), pageable);
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("Error filtering purchase orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error filtering purchase orders: " + e.getMessage()));
        }
    }
    
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<ApiResponse<List<PurchaseOrderDTO>>> getOrdersBySupplier(@PathVariable Long supplierId) {
        try {
            List<PurchaseOrderDTO> orders = purchaseOrderService.getPurchaseOrdersBySupplier(supplierId);
            return ResponseEntity.ok(ApiResponse.success(orders));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/pr/{prId}")
    public ResponseEntity<ApiResponse<List<PurchaseOrderDTO>>> getOrdersByPR(@PathVariable Long prId) {
        try {
            List<PurchaseOrderDTO> orders = purchaseOrderService.getPurchaseOrdersByPR(prId);
            return ResponseEntity.ok(ApiResponse.success(orders));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ============ UPDATE ============
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> updatePurchaseOrder(
            @PathVariable Long id,
            @Valid @RequestBody CreatePurchaseOrderDTO requestDTO) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            PurchaseOrderDTO updated = purchaseOrderService.updatePurchaseOrder(id, requestDTO, userId);
            return ResponseEntity.ok(ApiResponse.success("Purchase order updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ============ STATUS MANAGEMENT ============
    
    @PostMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequestDTOPO statusUpdateRequest) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            PurchaseOrderDTO updated = purchaseOrderService.updateStatus(id, statusUpdateRequest, userId);
            return ResponseEntity.ok(ApiResponse.success(
                "Status updated to: " + statusUpdateRequest.getStatus(), updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
   

    // ============ DELETE ============
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePurchaseOrder(@PathVariable Long id) {
        try {
            purchaseOrderService.deletePurchaseOrder(id);
            return ResponseEntity.ok(ApiResponse.success("Purchase order deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ============ STATISTICS ============
//    
//    @GetMapping("/statistics")
//    public ResponseEntity<ApiResponse<Object>> getStatistics() {
//        try {
//            Object stats = purchaseOrderService.getStatistics();
//            return ResponseEntity.ok(ApiResponse.success(stats));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error("Error retrieving statistics"));
//        }
//    }
}