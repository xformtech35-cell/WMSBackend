// ====== FILE: src/main/java/com/warehouse/wms/controller/BinController.java ======
package com.warehouse.wms.controller;

import com.warehouse.wms.dto.BinCreateRequest;
import com.warehouse.wms.dto.BinResponse;
import com.warehouse.wms.dto.response.StandardResponse;
import com.warehouse.wms.service.BinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/bins")
@RequiredArgsConstructor
@Tag(name = "Bin Management", description = "APIs for managing warehouse bins")
public class BinController {

    private final BinService binService;

    // ====== CREATE ======

    @PostMapping
    @Operation(summary = "Create bin")
    public ResponseEntity<StandardResponse<BinResponse>> createBin(
            @Valid @RequestBody BinCreateRequest request) {
        log.info("📦 Creating bin: {}", request.getBarcode());
        BinResponse response = binService.createBin(request);
        return ResponseEntity.ok(StandardResponse.success("Bin created successfully", response));
    }

    // ====== READ ======

    @GetMapping("/{id}")
    @Operation(summary = "Get bin by ID")
    public ResponseEntity<StandardResponse<BinResponse>> getBinById(@PathVariable Long id) {
        BinResponse response = binService.getBinById(id);
        return ResponseEntity.ok(StandardResponse.success("Bin fetched successfully", response));
    }

    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "Get bin by barcode")
    public ResponseEntity<StandardResponse<BinResponse>> getBinByBarcode(@PathVariable String barcode) {
        BinResponse response = binService.getBinByBarcode(barcode);
        return ResponseEntity.ok(StandardResponse.success("Bin fetched successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all bins with pagination")
    public ResponseEntity<StandardResponse<Page<BinResponse>>> getAllBins(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long rackId) {
        Page<BinResponse> responses = binService.getAllBins(pageable, search, rackId);
        return ResponseEntity.ok(StandardResponse.success("Bins fetched successfully", responses));
    }

    @GetMapping("/rack/{rackId}")
    @Operation(summary = "Get bins by rack")
    public ResponseEntity<StandardResponse<List<BinResponse>>> getBinsByRack(
            @PathVariable Long rackId) {
        List<BinResponse> responses = binService.getBinsByRack(rackId);
        return ResponseEntity.ok(StandardResponse.success("Bins fetched successfully", responses));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available bins")
    public ResponseEntity<StandardResponse<List<BinResponse>>> getAvailableBins(
            @RequestParam Long rackId,
            @RequestParam BigDecimal requiredVolume,
            @RequestParam BigDecimal requiredWeight) {
        List<BinResponse> responses = binService.getAvailableBins(rackId, requiredVolume, requiredWeight);
        return ResponseEntity.ok(StandardResponse.success("Available bins fetched successfully", responses));
    }

    // ====== UPDATE ======

    @PutMapping("/{id}")
    @Operation(summary = "Update bin")
    public ResponseEntity<StandardResponse<BinResponse>> updateBin(
            @PathVariable Long id,
            @Valid @RequestBody BinCreateRequest request) {
        BinResponse response = binService.updateBin(id, request);
        return ResponseEntity.ok(StandardResponse.success("Bin updated successfully", response));
    }

    @PostMapping("/{id}/occupy")
    @Operation(summary = "Occupy bin space")
    public ResponseEntity<StandardResponse<BinResponse>> occupyBinSpace(
            @PathVariable Long id,
            @RequestParam BigDecimal volume,
            @RequestParam BigDecimal weight) {
        BinResponse response = binService.occupyBinSpace(id, volume, weight);
        return ResponseEntity.ok(StandardResponse.success("Bin space occupied successfully", response));
    }

    @PostMapping("/{id}/release")
    @Operation(summary = "Release bin space")
    public ResponseEntity<StandardResponse<BinResponse>> releaseBinSpace(
            @PathVariable Long id,
            @RequestParam BigDecimal volume,
            @RequestParam BigDecimal weight) {
        BinResponse response = binService.releaseBinSpace(id, volume, weight);
        return ResponseEntity.ok(StandardResponse.success("Bin space released successfully", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update bin status")
    public ResponseEntity<StandardResponse<BinResponse>> updateBinStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        BinResponse response = binService.updateBinStatus(id, status);
        return ResponseEntity.ok(StandardResponse.success("Bin status updated successfully", response));
    }

    // ====== DELETE ======

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete bin (soft delete)")
    public ResponseEntity<StandardResponse<Void>> deleteBin(@PathVariable Long id) {
        binService.deleteBin(id);
        return ResponseEntity.ok(StandardResponse.success("Bin deleted successfully"));
    }

    @DeleteMapping("/barcode/{barcode}")
    @Operation(summary = "Delete bin by barcode (soft delete)")
    public ResponseEntity<StandardResponse<Void>> deleteBinByBarcode(@PathVariable String barcode) {
        binService.deleteBinByBarcode(barcode);
        return ResponseEntity.ok(StandardResponse.success("Bin deleted successfully"));
    }
}