package com.warehouse.wms.controller;

import com.warehouse.wms.dto.request.DeliveryChallanRequest;
import com.warehouse.wms.dto.response.DeliveryChallanResponse;
import com.warehouse.wms.dto.response.DeliveryChallanSummaryResponse;
import com.warehouse.wms.service.DeliveryChallanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/outbound/delivery-challan")
@RequiredArgsConstructor
@Slf4j
public class DeliveryChallanController {

    private final DeliveryChallanService deliveryChallanService;

    // ====== CREATE ======
    @PostMapping
    public ResponseEntity<DeliveryChallanResponse> createDeliveryChallan(
            @Valid @RequestBody DeliveryChallanRequest request) {
        log.info("POST /api/outbound/delivery-challan - Creating Delivery Challan");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deliveryChallanService.createDeliveryChallan(request));
    }

    // ====== GET BY NUMBER ======
    @GetMapping("/{challanNumber}")
    public ResponseEntity<DeliveryChallanResponse> getDeliveryChallan(@PathVariable String challanNumber) {
        log.info("GET /api/outbound/delivery-challan/{} - Getting Delivery Challan", challanNumber);
        return ResponseEntity.ok(deliveryChallanService.getDeliveryChallanByNumber(challanNumber));
    }

    @GetMapping("/deliverychallans")
    public ResponseEntity<Page<DeliveryChallanResponse>> getAllDeliveryChallans(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String challanNumber,
            @RequestParam(required = false) String soNumber,
            @RequestParam(required = false) String packageNumber,
            @RequestParam(required = false) String shipmentNumber,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String transporter,
            @RequestParam(required = false) String vehicleNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDispatchDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDispatchDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("GET /api/outbound/delivery-challan - Getting all Delivery Challans with filters");

        // Handle search parameter
        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(deliveryChallanService.searchDeliveryChallans(search, pageable));
        }

        // FIX: Pass all 14 parameters including vehicleNumber
        Page<DeliveryChallanResponse> response = deliveryChallanService.getAllDeliveryChallansWithFilters(
                challanNumber,           // String
                soNumber,                // String
                packageNumber,           // String
                shipmentNumber,          // String
                customerCode,            // String
                customerName,            // String
                status,                  // String
                transporter,             // String
                vehicleNumber,           // String - THIS WAS MISSING
                startDate,               // LocalDateTime
                endDate,                 // LocalDateTime
                startDispatchDate,       // LocalDateTime
                endDispatchDate,         // LocalDateTime
                pageable);               // Pageable

        return ResponseEntity.ok(response);
    }

    // ====== GET BY SO NUMBER ======
    @GetMapping("/so/{soNumber}")
    public ResponseEntity<List<DeliveryChallanResponse>> getDeliveryChallansBySoNumber(
            @PathVariable String soNumber) {
        log.info("GET /api/outbound/delivery-challan/so/{} - Getting by SO", soNumber);
        return ResponseEntity.ok(deliveryChallanService.getDeliveryChallansBySoNumber(soNumber));
    }

    // ====== GET BY STATUS ======
    @GetMapping("/status/{status}")
    public ResponseEntity<List<DeliveryChallanResponse>> getDeliveryChallansByStatus(
            @PathVariable String status) {
        log.info("GET /api/outbound/delivery-challan/status/{} - Getting by Status", status);
        return ResponseEntity.ok(deliveryChallanService.getDeliveryChallansByStatus(status));
    }

    // ====== UPDATE STATUS ======
    @PatchMapping("/{challanNumber}/status")
    public ResponseEntity<DeliveryChallanResponse> updateStatus(
            @PathVariable String challanNumber,
            @RequestParam String status) {
        log.info("PATCH /api/outbound/delivery-challan/{}/status - Updating to {}", challanNumber, status);
        return ResponseEntity.ok(deliveryChallanService.updateDeliveryChallanStatus(challanNumber, status));
    }

    // ====== PRINT ======
    @PatchMapping("/{challanNumber}/print")
    public ResponseEntity<DeliveryChallanResponse> printDeliveryChallan(@PathVariable String challanNumber) {
        log.info("PATCH /api/outbound/delivery-challan/{}/print - Printing", challanNumber);
        return ResponseEntity.ok(deliveryChallanService.printDeliveryChallan(challanNumber));
    }

    // ====== MARK AS DELIVERED ======
    @PatchMapping("/{challanNumber}/deliver")
    public ResponseEntity<DeliveryChallanResponse> markAsDelivered(@PathVariable String challanNumber) {
        log.info("PATCH /api/outbound/delivery-challan/{}/deliver - Marking as Delivered", challanNumber);
        return ResponseEntity.ok(deliveryChallanService.markAsDelivered(challanNumber));
    }

    // ====== CANCEL ======
    @PatchMapping("/{challanNumber}/cancel")
    public ResponseEntity<DeliveryChallanResponse> cancelDeliveryChallan(@PathVariable String challanNumber) {
        log.info("PATCH /api/outbound/delivery-challan/{}/cancel - Cancelling", challanNumber);
        return ResponseEntity.ok(deliveryChallanService.cancelDeliveryChallan(challanNumber));
    }

  
    // ====== DELETE ======
    @DeleteMapping("/{challanNumber}")
    public ResponseEntity<Void> deleteDeliveryChallan(@PathVariable String challanNumber) {
        log.info("DELETE /api/outbound/delivery-challan/{} - Deleting", challanNumber);
        deliveryChallanService.deleteDeliveryChallan(challanNumber);
        return ResponseEntity.noContent().build();
    }
}