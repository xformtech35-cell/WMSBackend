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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.warehouse.wms.dto.ApiResponse;
import com.warehouse.wms.dto.CreateRfqDTO;
import com.warehouse.wms.dto.PurchaseOrderDTO;
import com.warehouse.wms.dto.RfqDTO;
import com.warehouse.wms.dto.RfqFilterDTO;
import com.warehouse.wms.dto.RfqFilterRequestDTO;
import com.warehouse.wms.dto.RfqItemDTO;
import com.warehouse.wms.dto.StatusUpdateRequestDTORfq;
import com.warehouse.wms.dto.VendorQuotationDTO;
import com.warehouse.wms.service.RfqService;
import com.warehouse.wms.util.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/rfqs")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RfqController {

    private final RfqService rfqService;

    // ============ CREATE RFQ ============
    
    @PostMapping("/create-from-pr")
    public ResponseEntity<ApiResponse<RfqDTO>> createRfqFromPR(
            @Valid @RequestBody CreateRfqDTO requestDTO) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            RfqDTO created = rfqService.createRfqFromPR(requestDTO, userId);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("RFQ created successfully from PR", created));
        } catch (Exception e) {
            log.error("Error creating RFQ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error creating RFQ: " + e.getMessage()));
        }
    }

    // ============ ADD VENDOR QUOTATION ============
    
    @PostMapping("/{rfqId}/vendor-quotations")
    public ResponseEntity<ApiResponse<VendorQuotationDTO>> addVendorQuotation(
            @PathVariable Long rfqId,
            @Valid @RequestBody VendorQuotationDTO quotationDTO) {
        try {
            VendorQuotationDTO created = rfqService.addVendorQuotation(rfqId, quotationDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vendor quotation added successfully", created));
        } catch (Exception e) {
            log.error("Error adding vendor quotation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error adding vendor quotation: " + e.getMessage()));
        }
    }

    // ============ UPDATE VENDOR QUOTATION ============
    
    @PutMapping("/vendor-quotations/{quotationId}")
    public ResponseEntity<ApiResponse<VendorQuotationDTO>> updateVendorQuotation(
            @PathVariable Long quotationId,
            @Valid @RequestBody VendorQuotationDTO quotationDTO) {
        try {
            VendorQuotationDTO updated = rfqService.updateVendorQuotation(quotationId, quotationDTO);
            return ResponseEntity.ok(ApiResponse.success("Vendor quotation updated successfully", updated));
        } catch (Exception e) {
            log.error("Error updating vendor quotation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error updating vendor quotation: " + e.getMessage()));
        }
    }

    // ============ COMPARE QUOTATIONS ============
    
    @PostMapping("/{rfqId}/compare")
    public ResponseEntity<ApiResponse<List<VendorQuotationDTO>>> compareQuotations(
            @PathVariable Long rfqId) {
        try {
            List<VendorQuotationDTO> compared = rfqService.compareQuotations(rfqId);
            return ResponseEntity.ok(ApiResponse.success("Quotations compared successfully", compared));
        } catch (Exception e) {
            log.error("Error comparing quotations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error comparing quotations: " + e.getMessage()));
        }
    }

    // ============ CONVERT TO PO ============
    
 // In RfqController.java
    @PostMapping("/vendor-quotations/{quotationId}/convert-to-po")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> convertToPO(
            @PathVariable Long quotationId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            PurchaseOrderDTO po = rfqService.convertToPO(quotationId, userId);
            return ResponseEntity.ok(ApiResponse.success("Quotation converted to PO successfully: " + po.getPoNumber(), po));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error converting to PO", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error converting to PO: " + e.getMessage()));
        }
    }

    // ============ GET RFQ ============
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RfqDTO>> getRfqById(@PathVariable Long id) {
        try {
            RfqDTO rfq = rfqService.getRfqById(id);
            return ResponseEntity.ok(ApiResponse.success(rfq));
        } catch (Exception e) {
            log.error("Error getting RFQ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("RFQ not found: " + e.getMessage()));
        }
    }

    @GetMapping("/number/{rfqNumber}")
    public ResponseEntity<ApiResponse<RfqDTO>> getRfqByNumber(@PathVariable String rfqNumber) {
        try {
            RfqDTO rfq = rfqService.getRfqByNumber(rfqNumber);
            return ResponseEntity.ok(ApiResponse.success(rfq));
        } catch (Exception e) {
            log.error("Error getting RFQ by number", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("RFQ not found: " + e.getMessage()));
        }
    }

    // ============ FILTER RFQS ============
    
    @PostMapping("/filter")
    public ResponseEntity<ApiResponse<Page<RfqDTO>>> filterRfqs(
            @RequestBody(required = false) RfqFilterRequestDTO filterRequest) {
        try {
            if (filterRequest == null) {
                filterRequest = RfqFilterRequestDTO.builder()
                    .filters(RfqFilterDTO.builder().build())
                    .build();
            }
            
            if (filterRequest.getFilters() == null) {
                filterRequest.setFilters(RfqFilterDTO.builder().build());
            }
            
            Sort.Direction direction = filterRequest.getSortDir().equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(
                filterRequest.getPage(), 
                filterRequest.getSize(), 
                Sort.by(direction, filterRequest.getSortBy())
            );
            
            Page<RfqDTO> result = rfqService.filterRfqs(filterRequest.getFilters(), pageable);
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            log.error("Error filtering RFQs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error filtering RFQs: " + e.getMessage()));
        }
    }

    // ============ GET RFQ BY PR ============
    
    @GetMapping("/pr/{prId}")
    public ResponseEntity<ApiResponse<List<RfqDTO>>> getRfqsByPR(@PathVariable Long prId) {
        try {
            List<RfqDTO> rfqs = rfqService.getRfqsByPR(prId);
            return ResponseEntity.ok(ApiResponse.success(rfqs));
        } catch (Exception e) {
            log.error("Error getting RFQs by PR", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error getting RFQs: " + e.getMessage()));
        }
    }

    // ============ GET RFQ ITEMS ============
    
    @GetMapping("/{rfqId}/items")
    public ResponseEntity<ApiResponse<List<RfqItemDTO>>> getRfqItems(@PathVariable Long rfqId) {
        try {
            List<RfqItemDTO> items = rfqService.getRfqItems(rfqId);
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            log.error("Error getting RFQ items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error getting RFQ items: " + e.getMessage()));
        }
    }

    // ============ STATUS MANAGEMENT ============
    
    @PostMapping("/{id}/status")
    public ResponseEntity<ApiResponse<RfqDTO>> updateRfqStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequestDTORfq statusRequest) {
        try {
            RfqDTO updated = rfqService.updateRfqStatus(id, statusRequest);
            return ResponseEntity.ok(ApiResponse.success("RFQ status updated successfully", updated));
        } catch (Exception e) {
            log.error("Error updating RFQ status", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Error updating RFQ status: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<RfqDTO>> submitRfq(@PathVariable Long id) {
        try {
            RfqDTO updated = rfqService.submitRfq(id);
            return ResponseEntity.ok(ApiResponse.success("RFQ submitted successfully", updated));
        } catch (Exception e) {
            log.error("Error submitting RFQ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Error submitting RFQ: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<RfqDTO>> closeRfq(@PathVariable Long id) {
        try {
            RfqDTO updated = rfqService.closeRfq(id);
            return ResponseEntity.ok(ApiResponse.success("RFQ closed successfully", updated));
        } catch (Exception e) {
            log.error("Error closing RFQ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Error closing RFQ: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<RfqDTO>> cancelRfq(@PathVariable Long id) {
        try {
            RfqDTO updated = rfqService.cancelRfq(id);
            return ResponseEntity.ok(ApiResponse.success("RFQ cancelled successfully", updated));
        } catch (Exception e) {
            log.error("Error cancelling RFQ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Error cancelling RFQ: " + e.getMessage()));
        }
    }

    // ============ DELETE ============
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRfq(@PathVariable Long id) {
        try {
            rfqService.deleteRfq(id);
            return ResponseEntity.ok(ApiResponse.success("RFQ deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting RFQ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Error deleting RFQ: " + e.getMessage()));
        }
    }

    // ============ STATISTICS ============
    
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Object>> getRfqStatistics() {
        try {
            Object stats = rfqService.getRfqStatistics();
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            log.error("Error getting RFQ statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error getting RFQ statistics: " + e.getMessage()));
        }
    }

    // ============ VENDOR QUOTATION ACTIONS ============
    
    @PostMapping("/vendor-quotations/{quotationId}/approve")
    public ResponseEntity<ApiResponse<VendorQuotationDTO>> approveQuotation(
            @PathVariable Long quotationId) {
        try {
            VendorQuotationDTO approved = rfqService.approveQuotation(quotationId);
            return ResponseEntity.ok(ApiResponse.success("Quotation approved successfully", approved));
        } catch (Exception e) {
            log.error("Error approving quotation", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Error approving quotation: " + e.getMessage()));
        }
    }

    @PostMapping("/vendor-quotations/{quotationId}/reject")
    public ResponseEntity<ApiResponse<VendorQuotationDTO>> rejectQuotation(
            @PathVariable Long quotationId,
            @RequestParam(required = false) String reason) {
        try {
            VendorQuotationDTO rejected = rfqService.rejectQuotation(quotationId, reason);
            return ResponseEntity.ok(ApiResponse.success("Quotation rejected successfully", rejected));
        } catch (Exception e) {
            log.error("Error rejecting quotation", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Error rejecting quotation: " + e.getMessage()));
        }
    }
}