// ====== FILE: src/main/java/com/warehouse/wms/controller/BinController.java ======
package com.warehouse.wms.controller;

import com.warehouse.wms.dto.BinCreateRequest;
import com.warehouse.wms.dto.response.BinResponse;
import com.warehouse.wms.dto.response.StandardResponse;
import com.warehouse.wms.service.BinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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
    @Operation(summary = "Create a new bin", description = "Creates a new bin in the specified rack")
    public ResponseEntity<StandardResponse<BinResponse>> createBin(
            @Valid @RequestBody BinCreateRequest request) {
        log.info("📦 Creating bin with barcode: {}", request.getBarcode());
        BinResponse response = binService.createBin(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StandardResponse.success("Bin created successfully", response));
    }

    // ====== READ ======

    @GetMapping("/{id}")
    @Operation(summary = "Get bin by ID", description = "Retrieves a bin by its unique ID")
    public ResponseEntity<StandardResponse<BinResponse>> getBinById(
            @Parameter(description = "Bin ID", required = true)
            @PathVariable Long id) {
        log.info("📦 Getting bin by ID: {}", id);
        BinResponse response = binService.getBinById(id);
        return ResponseEntity.ok(StandardResponse.success("Bin fetched successfully", response));
    }

    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "Get bin by barcode", description = "Retrieves a bin by its barcode")
    public ResponseEntity<StandardResponse<BinResponse>> getBinByBarcode(
            @Parameter(description = "Bin barcode", required = true)
            @PathVariable String barcode) {
        log.info("📦 Getting bin by barcode: {}", barcode);
        BinResponse response = binService.getBinByBarcode(barcode);
        return ResponseEntity.ok(StandardResponse.success("Bin fetched successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all bins with pagination", description = "Retrieves all bins with pagination, search, and filtering")
    public ResponseEntity<StandardResponse<Page<BinResponse>>> getAllBins(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) @Parameter(description = "Search by barcode") String search,
            @RequestParam(required = false) @Parameter(description = "Filter by rack ID") Long rackId) {
        log.info("📦 Getting all bins - page: {}, size: {}, search: {}, rackId: {}", 
                 pageable.getPageNumber(), pageable.getPageSize(), search, rackId);
        Page<BinResponse> responses = binService.getAllBins(pageable, search, rackId);
        return ResponseEntity.ok(StandardResponse.success("Bins fetched successfully", responses));
    }

    @GetMapping("/rack/{rackId}")
    @Operation(summary = "Get bins by rack", description = "Retrieves all bins in a specific rack")
    public ResponseEntity<StandardResponse<List<BinResponse>>> getBinsByRack(
            @Parameter(description = "Rack ID", required = true)
            @PathVariable Long rackId) {
        log.info("📦 Getting bins by rack: {}", rackId);
        List<BinResponse> responses = binService.getBinsByRack(rackId);
        return ResponseEntity.ok(StandardResponse.success("Bins fetched successfully", responses));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available bins", description = "Retrieves available bins based on required volume and weight")
    public ResponseEntity<StandardResponse<List<BinResponse>>> getAvailableBins(
            @RequestParam @Parameter(description = "Rack ID", required = true) Long rackId,
            @RequestParam @Parameter(description = "Required volume in cm³", required = true) BigDecimal requiredVolume,
            @RequestParam @Parameter(description = "Required weight in grams", required = true) BigDecimal requiredWeight) {
        log.info("📦 Getting available bins - rack: {}, volume: {}, weight: {}", rackId, requiredVolume, requiredWeight);
        List<BinResponse> responses = binService.getAvailableBins(rackId, requiredVolume, requiredWeight);
        return ResponseEntity.ok(StandardResponse.success("Available bins fetched successfully", responses));
    }

    // ====== UPDATE ======

    @PutMapping("/{id}")
    @Operation(summary = "Update bin", description = "Updates an existing bin")
    public ResponseEntity<StandardResponse<BinResponse>> updateBin(
            @Parameter(description = "Bin ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody BinCreateRequest request) {
        log.info("📦 Updating bin: {}", id);
        BinResponse response = binService.updateBin(id, request);
        return ResponseEntity.ok(StandardResponse.success("Bin updated successfully", response));
    }

    @PostMapping("/{id}/occupy")
    @Operation(summary = "Occupy bin space", description = "Occupies space in a bin with given volume and weight")
    public ResponseEntity<StandardResponse<BinResponse>> occupyBinSpace(
            @Parameter(description = "Bin ID", required = true)
            @PathVariable Long id,
            @RequestParam @Parameter(description = "Volume to occupy in cm³", required = true) BigDecimal volume,
            @RequestParam @Parameter(description = "Weight to occupy in grams", required = true) BigDecimal weight) {
        log.info("📦 Occupying bin space - id: {}, volume: {}, weight: {}", id, volume, weight);
        BinResponse response = binService.occupyBinSpace(id, volume, weight);
        return ResponseEntity.ok(StandardResponse.success("Bin space occupied successfully", response));
    }

    @PostMapping("/{id}/release")
    @Operation(summary = "Release bin space", description = "Releases occupied space from a bin")
    public ResponseEntity<StandardResponse<BinResponse>> releaseBinSpace(
            @Parameter(description = "Bin ID", required = true)
            @PathVariable Long id,
            @RequestParam @Parameter(description = "Volume to release in cm³", required = true) BigDecimal volume,
            @RequestParam @Parameter(description = "Weight to release in grams", required = true) BigDecimal weight) {
        log.info("📦 Releasing bin space - id: {}, volume: {}, weight: {}", id, volume, weight);
        BinResponse response = binService.releaseBinSpace(id, volume, weight);
        return ResponseEntity.ok(StandardResponse.success("Bin space released successfully", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update bin status", description = "Updates the status of a bin (AVAILABLE, FULL, BLOCKED)")
    public ResponseEntity<StandardResponse<BinResponse>> updateBinStatus(
            @Parameter(description = "Bin ID", required = true)
            @PathVariable Long id,
            @RequestParam @Parameter(description = "New status (AVAILABLE, FULL, BLOCKED)", required = true) String status) {
        log.info("📦 Updating bin status - id: {}, status: {}", id, status);
        BinResponse response = binService.updateBinStatus(id, status);
        return ResponseEntity.ok(StandardResponse.success("Bin status updated successfully", response));
    }

    // ====== DELETE ======

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete bin (soft delete)", description = "Soft deletes a bin by setting isActive to false")
    public ResponseEntity<StandardResponse<Void>> deleteBin(
            @Parameter(description = "Bin ID", required = true)
            @PathVariable Long id) {
        log.info("📦 Deleting bin: {}", id);
        binService.deleteBin(id);
        return ResponseEntity.ok(StandardResponse.success("Bin deleted successfully"));
    }

    @DeleteMapping("/barcode/{barcode}")
    @Operation(summary = "Delete bin by barcode (soft delete)", description = "Soft deletes a bin by barcode")
    public ResponseEntity<StandardResponse<Void>> deleteBinByBarcode(
            @Parameter(description = "Bin barcode", required = true)
            @PathVariable String barcode) {
        log.info("📦 Deleting bin by barcode: {}", barcode);
        binService.deleteBinByBarcode(barcode);
        return ResponseEntity.ok(StandardResponse.success("Bin deleted successfully"));
    }
}