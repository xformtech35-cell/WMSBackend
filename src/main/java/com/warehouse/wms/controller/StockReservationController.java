package com.warehouse.wms.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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

import com.warehouse.wms.dto.request.CustomReservationRequest;
import com.warehouse.wms.dto.request.EditReservationRequest;
import com.warehouse.wms.dto.response.CustomReservationResponse;
import com.warehouse.wms.dto.response.StockAvailabilityResponse;
import com.warehouse.wms.dto.response.StockReservationResponse;
import com.warehouse.wms.entity.StockReservation;
import com.warehouse.wms.service.StockReservationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/stock-reservations")
@RequiredArgsConstructor
@Slf4j
public class StockReservationController {

    private final StockReservationService stockReservationService;

    // ====== AUTO RESERVE ======
    @PostMapping("/auto/{soNumber}")
    public ResponseEntity<Void> autoReserveStock(@PathVariable String soNumber) {
        log.info("POST /api/stock-reservations/auto/{} - Auto reserving stock", soNumber);
        stockReservationService.reserveStock(soNumber);
        return ResponseEntity.ok().build();
    }

    // ====== CUSTOM RESERVE ======
    @PostMapping("/custom")
    public ResponseEntity<CustomReservationResponse> customReserveStock(
            @Valid @RequestBody CustomReservationRequest request) {
        log.info("POST /api/stock-reservations/custom - Custom reserving stock");
        return ResponseEntity.ok(stockReservationService.customReserveStock(request));
    }

    // ====== RESERVE FROM BIN ======
    @PostMapping("/reserve-from-bin")
    public ResponseEntity<StockReservationResponse> reserveFromBin(
            @RequestParam String soNumber,
            @RequestParam String itemCode,
            @RequestParam String binId,
            @RequestParam Integer quantity) {
        log.info("POST /api/stock-reservations/reserve-from-bin - Reserving from bin");
        StockReservation reservation = stockReservationService.reserveFromBin(soNumber, itemCode, binId, quantity);
        return ResponseEntity.ok(buildStockReservationResponse(reservation));
    }

    // ====== RESERVE FROM INVENTORY ======
    @PostMapping("/reserve-from-inventory")
    public ResponseEntity<StockReservationResponse> reserveFromInventory(
            @RequestParam String soNumber,
            @RequestParam Long inventoryStockId,
            @RequestParam Integer quantity) {
        log.info("POST /api/stock-reservations/reserve-from-inventory - Reserving from inventory");
        StockReservation reservation = stockReservationService.reserveFromInventory(soNumber, inventoryStockId, quantity);
        return ResponseEntity.ok(buildStockReservationResponse(reservation));
    }

    // ====== RELEASE RESERVATION ======
    @DeleteMapping("/{reservationNumber}")
    public ResponseEntity<Void> releaseReservation(@PathVariable String reservationNumber) {
        log.info("DELETE /api/stock-reservations/{} - Releasing reservation", reservationNumber);
        stockReservationService.releaseReservation(reservationNumber);
        return ResponseEntity.noContent().build();
    }

    // ====== RELEASE ALL RESERVATIONS ======
    @DeleteMapping("/so/{soNumber}")
    public ResponseEntity<Void> releaseAllReservations(@PathVariable String soNumber) {
        log.info("DELETE /api/stock-reservations/so/{} - Releasing all reservations", soNumber);
        stockReservationService.releaseAllReservations(soNumber);
        return ResponseEntity.noContent().build();
    }

    // ====== GET RESERVATIONS BY SO ======
    @GetMapping("/so/{soNumber}")
    public ResponseEntity<List<StockReservationResponse>> getReservationsBySoNumber(@PathVariable String soNumber) {
        log.info("GET /api/stock-reservations/so/{} - Getting reservations by SO", soNumber);
        List<StockReservation> reservations = stockReservationService.getReservationsBySoNumber(soNumber);
        return ResponseEntity.ok(reservations.stream()
                .map(this::buildStockReservationResponse)
                .collect(Collectors.toList()));
    }

    // ====== CHECK AVAILABILITY ======
    @GetMapping("/availability")
    public ResponseEntity<StockAvailabilityResponse> checkAvailability(
            @RequestParam String itemCode,
            @RequestParam Integer quantity) {
        log.info("GET /api/stock-reservations/availability - Checking availability for item: {}", itemCode);
        return ResponseEntity.ok(stockReservationService.checkAvailability(itemCode, quantity));
    }

    private StockReservationResponse buildStockReservationResponse(StockReservation reservation) {
        return StockReservationResponse.builder()
                .id(reservation.getId())
                .reservationNumber(reservation.getReservationNumber())
                .soNumber(reservation.getSoNumber())
                .itemCode(reservation.getItemCode())
                .itemName(reservation.getItemName())
                .uom(reservation.getUom())
                .requiredQuantity(reservation.getRequiredQuantity())
                .availableQuantity(reservation.getAvailableQuantity())
                .reservedQuantity(reservation.getReservedQuantity())
                .warehouseId(reservation.getWarehouseId())
                .zoneId(reservation.getZoneId())
                .aisleId(reservation.getAisleId())
                .rackId(reservation.getRackId())
                .levelId(reservation.getLevelId())
                .binId(reservation.getBinId())
                .batchNumber(reservation.getBatchNumber())
                .status(reservation.getStatus())
                .reservationDate(reservation.getReservationDate())
                .expiryDate(reservation.getExpiryDate())
                .remarks(reservation.getRemarks())
                .createdBy(reservation.getCreatedBy())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }
    
    
    
    
    @PutMapping("/edit")
    public ResponseEntity<StockReservationResponse> editReservation(
            @Valid @RequestBody EditReservationRequest request) {
        log.info("PUT /api/stock-reservations/edit - Editing reservation");
        return ResponseEntity.ok(stockReservationService.editReservation(request));
    }

    // ====== EDIT QUANTITY ======
    @PatchMapping("/{reservationNumber}/quantity")
    public ResponseEntity<StockReservationResponse> editQuantity(
            @PathVariable String reservationNumber,
            @RequestParam Integer quantity) {
        log.info("PATCH /api/stock-reservations/{}/quantity - Editing quantity to {}", reservationNumber, quantity);
        return ResponseEntity.ok(stockReservationService.editReservationQuantity(reservationNumber, quantity));
    }

    // ====== EDIT LOCATION ======
    @PatchMapping("/{reservationNumber}/location")
    public ResponseEntity<StockReservationResponse> editLocation(
            @PathVariable String reservationNumber,
            @RequestParam String binId) {
        log.info("PATCH /api/stock-reservations/{}/location - Editing location to {}", reservationNumber, binId);
        return ResponseEntity.ok(stockReservationService.editReservationLocation(reservationNumber, binId));
    }

    // ====== EDIT BATCH ======
    @PatchMapping("/{reservationNumber}/batch")
    public ResponseEntity<StockReservationResponse> editBatch(
            @PathVariable String reservationNumber,
            @RequestParam String batchNumber) {
        log.info("PATCH /api/stock-reservations/{}/batch - Editing batch to {}", reservationNumber, batchNumber);
        return ResponseEntity.ok(stockReservationService.editReservationBatch(reservationNumber, batchNumber));
    }

    // ====== EDIT REMARKS ======
    @PatchMapping("/{reservationNumber}/remarks")
    public ResponseEntity<StockReservationResponse> editRemarks(
            @PathVariable String reservationNumber,
            @RequestParam String remarks) {
        log.info("PATCH /api/stock-reservations/{}/remarks - Editing remarks", reservationNumber);
        return ResponseEntity.ok(stockReservationService.editReservationRemarks(reservationNumber, remarks));
    }

    // ====== EXTEND EXPIRY ======
    @PatchMapping("/{reservationNumber}/expiry")
    public ResponseEntity<StockReservationResponse> extendExpiry(
            @PathVariable String reservationNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newExpiryDate) {
        log.info("PATCH /api/stock-reservations/{}/expiry - Extending expiry to {}", reservationNumber, newExpiryDate);
        return ResponseEntity.ok(stockReservationService.extendReservationExpiry(reservationNumber, newExpiryDate));
    }

    // ====== BULK EDIT ======
    
}