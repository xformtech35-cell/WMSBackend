// ====== FILE: src/main/java/com/warehouse/wms/controller/BinLocationController.java ======
package com.warehouse.wms.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import com.warehouse.wms.dto.request.BinLocationRequest;
import com.warehouse.wms.dto.response.BinLocationResponse;
import com.warehouse.wms.dto.response.BinLocationStatistics;
import com.warehouse.wms.dto.response.StandardResponse;
import com.warehouse.wms.service.BinLocationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/bin-locations")
@RequiredArgsConstructor
@Tag(name = "Bin Location Management", description = "APIs for managing bin locations")
public class BinLocationController {

    private final BinLocationService binLocationService;

    // ====== Create ======

    @PostMapping
    @Operation(summary = "Create bin location")
    public ResponseEntity<StandardResponse<BinLocationResponse>> createBinLocation(
            @Valid @RequestBody BinLocationRequest request) {
        log.info("📦 Creating bin location: {}", request.getBinId());
        BinLocationResponse response = binLocationService.createBinLocation(request);
        return ResponseEntity.ok(StandardResponse.success("Bin location created successfully", response));
    }

    @PostMapping("/batch")
    @Operation(summary = "Create multiple bin locations")
    public ResponseEntity<StandardResponse<List<BinLocationResponse>>> createBatchBinLocations(
            @Valid @RequestBody List<BinLocationRequest> requests) {
        log.info("📦 Creating {} bin locations", requests.size());
        List<BinLocationResponse> responses = binLocationService.createBatchBinLocations(requests);
        return ResponseEntity.ok(StandardResponse.success("Bin locations created successfully", responses));
    }

    // ====== Read ======

    @GetMapping("/{id}")
    @Operation(summary = "Get bin location by ID")
    public ResponseEntity<StandardResponse<BinLocationResponse>> getBinLocationById(@PathVariable Long id) {
        BinLocationResponse response = binLocationService.getBinLocationById(id);
        return ResponseEntity.ok(StandardResponse.success("Bin location fetched successfully", response));
    }

    @GetMapping("/bin/{binId}")
    @Operation(summary = "Get bin location by bin ID")
    public ResponseEntity<StandardResponse<BinLocationResponse>> getBinLocationByBinId(@PathVariable String binId) {
        BinLocationResponse response = binLocationService.getBinLocationByBinId(binId);
        return ResponseEntity.ok(StandardResponse.success("Bin location fetched successfully", response));
    }

    @GetMapping("/barcode/{binBarcode}")
    @Operation(summary = "Get bin location by barcode")
    public ResponseEntity<StandardResponse<BinLocationResponse>> getBinLocationByBarcode(@PathVariable String binBarcode) {
        BinLocationResponse response = binLocationService.getBinLocationByBarcode(binBarcode);
        return ResponseEntity.ok(StandardResponse.success("Bin location fetched successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all bin locations with pagination")
    public ResponseEntity<StandardResponse<Page<BinLocationResponse>>> getAllBinLocations(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String warehouseId) {
        Page<BinLocationResponse> responses = binLocationService.getAllBinLocations(pageable, warehouseId);
        return ResponseEntity.ok(StandardResponse.success("Bin locations fetched successfully", responses));
    }

    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "Get bin locations by warehouse")
    public ResponseEntity<StandardResponse<List<BinLocationResponse>>> getBinLocationsByWarehouse(
            @PathVariable String warehouseId) {
        List<BinLocationResponse> responses = binLocationService.getBinLocationsByWarehouse(warehouseId);
        return ResponseEntity.ok(StandardResponse.success("Bin locations fetched successfully", responses));
    }

    @GetMapping("/warehouse/{warehouseId}/zone/{zone}")
    @Operation(summary = "Get bin locations by warehouse and zone")
    public ResponseEntity<StandardResponse<List<BinLocationResponse>>> getBinLocationsByWarehouseAndZone(
            @PathVariable String warehouseId,
            @PathVariable String zone) {
        List<BinLocationResponse> responses = binLocationService.getBinLocationsByWarehouseAndZone(warehouseId, zone);
        return ResponseEntity.ok(StandardResponse.success("Bin locations fetched successfully", responses));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available bin locations")
    public ResponseEntity<StandardResponse<List<BinLocationResponse>>> getAvailableBinLocations(
            @RequestParam String warehouseId,
            @RequestParam(required = false) String zone) {
        List<BinLocationResponse> responses = binLocationService.getAvailableBinLocations(warehouseId, zone);
        return ResponseEntity.ok(StandardResponse.success("Available bin locations fetched successfully", responses));
    }

    @GetMapping("/occupied")
    @Operation(summary = "Get occupied bin locations")
    public ResponseEntity<StandardResponse<List<BinLocationResponse>>> getOccupiedBinLocations(
            @RequestParam(required = false) String warehouseId) {
        List<BinLocationResponse> responses = binLocationService.getOccupiedBinLocations(warehouseId);
        return ResponseEntity.ok(StandardResponse.success("Occupied bin locations fetched successfully", responses));
    }

    // ====== Update ======

    @PutMapping("/{id}")
    @Operation(summary = "Update bin location")
    public ResponseEntity<StandardResponse<BinLocationResponse>> updateBinLocation(
            @PathVariable Long id,
            @Valid @RequestBody BinLocationRequest request) {
        BinLocationResponse response = binLocationService.updateBinLocation(id, request);
        return ResponseEntity.ok(StandardResponse.success("Bin location updated successfully", response));
    }

    @PostMapping("/{id}/allocate")
    @Operation(summary = "Allocate bin capacity")
    public ResponseEntity<StandardResponse<BinLocationResponse>> allocateBinCapacity(
            @PathVariable Long id,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) String uom) {
        BinLocationResponse response = binLocationService.allocateBinCapacity(id, quantity, itemCode, itemName, uom);
        return ResponseEntity.ok(StandardResponse.success("Bin capacity allocated successfully", response));
    }

    @PostMapping("/{id}/release")
    @Operation(summary = "Release bin capacity")
    public ResponseEntity<StandardResponse<BinLocationResponse>> releaseBinCapacity(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        BinLocationResponse response = binLocationService.releaseBinCapacity(id, quantity);
        return ResponseEntity.ok(StandardResponse.success("Bin capacity released successfully", response));
    }

    @PatchMapping("/{id}/active")
    @Operation(summary = "Toggle bin active status")
    public ResponseEntity<StandardResponse<BinLocationResponse>> toggleActiveStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        BinLocationResponse response = binLocationService.toggleActiveStatus(id, isActive);
        return ResponseEntity.ok(StandardResponse.success("Bin active status updated successfully", response));
    }

    // ====== Delete ======

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete bin location (soft delete)")
    public ResponseEntity<StandardResponse<Void>> deleteBinLocation(@PathVariable Long id) {
        binLocationService.deleteBinLocation(id);
        return ResponseEntity.ok(StandardResponse.success("Bin location deleted successfully"));
    }

    @DeleteMapping("/bin/{binId}")
    @Operation(summary = "Delete bin location by bin ID (soft delete)")
    public ResponseEntity<StandardResponse<Void>> deleteBinLocationByBinId(@PathVariable String binId) {
        binLocationService.deleteBinLocationByBinId(binId);
        return ResponseEntity.ok(StandardResponse.success("Bin location deleted successfully"));
    }

    // ====== Statistics ======

    @GetMapping("/statistics")
    @Operation(summary = "Get bin location statistics")
    public ResponseEntity<StandardResponse<BinLocationStatistics>> getBinLocationStatistics(
            @RequestParam String warehouseId) {
        BinLocationStatistics statistics = binLocationService.getBinLocationStatistics(warehouseId);
        return ResponseEntity.ok(StandardResponse.success("Bin location statistics fetched successfully", statistics));
    }
}