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

    // ============================================================
    // ===================== CREATE ================================
    // ============================================================

    @PostMapping
    public ResponseEntity<DeliveryChallanResponse> createDeliveryChallan(
            @Valid @RequestBody DeliveryChallanRequest request) {
        log.info("POST /api/outbound/delivery-challan - Creating Delivery Challan");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deliveryChallanService.createDeliveryChallan(request));
    }

    
    
    @PutMapping("/{challanNumber}")
    public ResponseEntity<DeliveryChallanResponse> updateDeliveryChallan(
            @PathVariable String challanNumber,
            @Valid @RequestBody DeliveryChallanRequest request) {
        log.info("PUT /api/outbound/delivery-challan/{} - Updating Delivery Challan", challanNumber);
        return ResponseEntity.ok(deliveryChallanService.updateDeliveryChallan(challanNumber, request));
    }
    // ============================================================
    // ===================== GET BY NUMBER =========================
    // ============================================================

    @GetMapping("/{challanNumber}")
    public ResponseEntity<DeliveryChallanResponse> getDeliveryChallan(
            @PathVariable String challanNumber) {
        log.info("GET /api/outbound/delivery-challan/{} - Getting Delivery Challan", challanNumber);
        return ResponseEntity.ok(deliveryChallanService.getDeliveryChallanByNumber(challanNumber));
    }

    // ============================================================
    // ===================== GET ALL WITH FILTERS ==================
    // ============================================================

    @GetMapping
    public ResponseEntity<Page<DeliveryChallanResponse>> getAllDeliveryChallans(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String challanNumber,
            @RequestParam(required = false) String shipmentNumber,
            @RequestParam(required = false) String transporter,
            @RequestParam(required = false) String vehicleNumber,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("GET /api/outbound/delivery-challan - Getting all Delivery Challans with filters");

        // Handle search parameter
        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(deliveryChallanService.searchDeliveryChallans(search, pageable));
        }

        Page<DeliveryChallanResponse> response = deliveryChallanService.getAllDeliveryChallansWithFilters(
                challanNumber, shipmentNumber, transporter,
                vehicleNumber, status, startDate, endDate, pageable);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // ===================== GET BY SO NUMBER ======================
    // ============================================================

  

    // ============================================================
    // ===================== GET BY STATUS ========================
    // ============================================================

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DeliveryChallanResponse>> getDeliveryChallansByStatus(
            @PathVariable String status) {
        log.info("GET /api/outbound/delivery-challan/status/{} - Getting by Status", status);
        return ResponseEntity.ok(deliveryChallanService.getDeliveryChallansByStatus(status));
    }

    // ============================================================
    // ===================== GET BY TRANSPORTER ===================
    // ============================================================

    @GetMapping("/transporter/{transporter}")
    public ResponseEntity<List<DeliveryChallanResponse>> getDeliveryChallansByTransporter(
            @PathVariable String transporter) {
        log.info("GET /api/outbound/delivery-challan/transporter/{} - Getting by Transporter", transporter);
        return ResponseEntity.ok(deliveryChallanService.getDeliveryChallansByStatus(transporter));
    }

    // ============================================================
    // ===================== UPDATE STATUS ========================
    // ============================================================

    @PatchMapping("/{challanNumber}/status")
    public ResponseEntity<DeliveryChallanResponse> updateStatus(
            @PathVariable String challanNumber,
            @RequestParam String status) {
        log.info("PATCH /api/outbound/delivery-challan/{}/status - Updating to {}", challanNumber, status);
        return ResponseEntity.ok(deliveryChallanService.updateDeliveryChallanStatus(challanNumber, status));
    }

    // ============================================================
    // ===================== PRINT ================================
    // ============================================================

    @PatchMapping("/{challanNumber}/print")
    public ResponseEntity<DeliveryChallanResponse> printDeliveryChallan(
            @PathVariable String challanNumber) {
        log.info("PATCH /api/outbound/delivery-challan/{}/print - Printing", challanNumber);
        return ResponseEntity.ok(deliveryChallanService.printDeliveryChallan(challanNumber));
    }

    // ============================================================
    // ===================== MARK AS DISPATCHED ===================
    // ============================================================

    @PatchMapping("/{challanNumber}/dispatch")
    public ResponseEntity<DeliveryChallanResponse> markAsDispatched(
            @PathVariable String challanNumber) {
        log.info("PATCH /api/outbound/delivery-challan/{}/dispatch - Marking as Dispatched", challanNumber);
        return ResponseEntity.ok(deliveryChallanService.markAsDispatched(challanNumber));
    }

    // ============================================================
    // ===================== MARK AS DELIVERED ====================
    // ============================================================

    @PatchMapping("/{challanNumber}/deliver")
    public ResponseEntity<DeliveryChallanResponse> markAsDelivered(
            @PathVariable String challanNumber) {
        log.info("PATCH /api/outbound/delivery-challan/{}/deliver - Marking as Delivered", challanNumber);
        return ResponseEntity.ok(deliveryChallanService.markAsDelivered(challanNumber));
    }

    // ============================================================
    // ===================== CANCEL ===============================
    // ============================================================

    @PatchMapping("/{challanNumber}/cancel")
    public ResponseEntity<DeliveryChallanResponse> cancelDeliveryChallan(
            @PathVariable String challanNumber) {
        log.info("PATCH /api/outbound/delivery-challan/{}/cancel - Cancelling", challanNumber);
        return ResponseEntity.ok(deliveryChallanService.cancelDeliveryChallan(challanNumber));
    }

    // ============================================================
    // ===================== GENERATE PDF =========================
    // ============================================================

//    @GetMapping(value = "/{challanNumber}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
//    public ResponseEntity<byte[]> generatePdf(
//            @PathVariable String challanNumber) {
//        log.info("GET /api/outbound/delivery-challan/{}/pdf - Generating PDF", challanNumber);
//        
//        byte[] pdfBytes = deliveryChallanService.generateDeliveryChallanPdf(challanNumber);
//        
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_PDF);
//        headers.setContentDispositionFormData("attachment", "delivery-challan-" + challanNumber + ".pdf");
//        headers.setContentLength(pdfBytes.length);
//        
//        return ResponseEntity.ok()
//                .headers(headers)
//                .body(pdfBytes);
//    }
//
//    // ============================================================
//    // ===================== GENERATE HTML ========================
//    // ============================================================
//
//    @GetMapping(value = "/{challanNumber}/html", produces = MediaType.TEXT_HTML_VALUE)
//    public ResponseEntity<String> generateHtml(
//            @PathVariable String challanNumber) {
//        log.info("GET /api/outbound/delivery-challan/{}/html - Generating HTML", challanNumber);
//        return ResponseEntity.ok(deliveryChallanService.generateDeliveryChallanHtml(challanNumber));
//    }

    // ============================================================
    // ===================== GET SUMMARY ==========================
    // ============================================================

    @GetMapping("/summary")
    public ResponseEntity<DeliveryChallanSummaryResponse> getSummary() {
        log.info("GET /api/outbound/delivery-challan/summary - Getting Summary");
        return ResponseEntity.ok(deliveryChallanService.getDeliveryChallanSummary());
    }

   

   

    // ============================================================
    // ===================== DELETE ===============================
    // ============================================================

    @DeleteMapping("/{challanNumber}")
    public ResponseEntity<Void> deleteDeliveryChallan(
            @PathVariable String challanNumber) {
        log.info("DELETE /api/outbound/delivery-challan/{} - Deleting", challanNumber);
        deliveryChallanService.deleteDeliveryChallan(challanNumber);
        return ResponseEntity.noContent().build();
    }
}