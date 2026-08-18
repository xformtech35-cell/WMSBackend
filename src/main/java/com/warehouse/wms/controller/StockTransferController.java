// ====== FILE: src/main/java/com/warehouse/wms/controller/StockTransferController.java ======
package com.warehouse.wms.controller;

import com.warehouse.wms.dto.request.StockTransferRequest;
import com.warehouse.wms.dto.response.StockTransferResponse;
import com.warehouse.wms.service.StockTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Sort;


import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/stock-transfers")
@RequiredArgsConstructor
@Slf4j
public class StockTransferController {
    
    private final StockTransferService transferService;
    
    @PostMapping("/transfer")
    public ResponseEntity<StockTransferResponse> transferStock(
            @Valid @RequestBody StockTransferRequest request) {
        log.info("Received stock transfer request: {}", request);
        StockTransferResponse response = transferService.transferStock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{transferNumber}")
    public ResponseEntity<StockTransferResponse> getTransferByNumber(
            @PathVariable String transferNumber) {
        StockTransferResponse response = transferService.getTransferByNumber(transferNumber);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<Page<StockTransferResponse>> getAllTransfers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<StockTransferResponse> transfers = transferService.getAllTransfers(pageable);
        return ResponseEntity.ok(transfers);
    }
    
    @GetMapping("/filter")
    public ResponseEntity<Page<StockTransferResponse>> getTransfersWithFilter(
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String sourceLocation,
            @RequestParam(required = false) String targetLocation,
            @RequestParam(required = false) String batchNumber,
            @RequestParam(required = false) String grnNumber,
            @RequestParam(required = false) String inventoryNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String transferStatus,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<StockTransferResponse> transfers = transferService.getTransfersWithFilter(
            itemCode, sourceLocation, targetLocation, batchNumber,
            grnNumber, inventoryNumber, search,
            startDate, endDate, transferStatus, pageable);
        
        return ResponseEntity.ok(transfers);
    }
    @GetMapping("/item/{itemCode}")
    public ResponseEntity<List<StockTransferResponse>> getTransfersByItemCode(
            @PathVariable String itemCode) {
        List<StockTransferResponse> transfers = transferService.getTransfersByItemCode(itemCode);
        return ResponseEntity.ok(transfers);
    }
    
    @GetMapping("/source/{locationPath}")
    public ResponseEntity<List<StockTransferResponse>> getTransfersBySourceLocation(
            @PathVariable String locationPath) {
        List<StockTransferResponse> transfers = transferService.getTransfersBySourceLocation(locationPath);
        return ResponseEntity.ok(transfers);
    }
    
    @GetMapping("/target/{locationPath}")
    public ResponseEntity<List<StockTransferResponse>> getTransfersByTargetLocation(
            @PathVariable String locationPath) {
        List<StockTransferResponse> transfers = transferService.getTransfersByTargetLocation(locationPath);
        return ResponseEntity.ok(transfers);
    }
    
    @PutMapping("/{transferNumber}/cancel")
    public ResponseEntity<StockTransferResponse> cancelTransfer(
            @PathVariable String transferNumber) {
        StockTransferResponse response = transferService.cancelTransfer(transferNumber);
        return ResponseEntity.ok(response);
    }
}