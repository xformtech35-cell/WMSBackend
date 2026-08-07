// ====== FILE: src/main/java/com/warehouse/wms/controller/RockController.java ======
package com.warehouse.wms.controller;

import com.warehouse.wms.dto.request.RockRequest;
import com.warehouse.wms.dto.response.RockResponse;
import com.warehouse.wms.dto.response.StandardResponse;
import com.warehouse.wms.service.RockService;
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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/rocks")
@RequiredArgsConstructor
@Tag(name = "Rock Management", description = "APIs for managing warehouse rocks")
public class RockController {

    private final RockService rockService;

    // ====== CREATE ======

    @PostMapping
    @Operation(summary = "Create a new rock", description = "Creates a new rock in the warehouse")
    public ResponseEntity<StandardResponse<RockResponse>> createRock(
            @Valid @RequestBody RockRequest request) {
        log.info("📦 Creating rock: {}", request.getRockId());
        RockResponse response = rockService.createRock(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StandardResponse.success("Rock created successfully", response));
    }

    // ====== READ ======

    @GetMapping("/{id}")
    @Operation(summary = "Get rock by ID", description = "Retrieves a rock by its unique ID")
    public ResponseEntity<StandardResponse<RockResponse>> getRockById(
            @Parameter(description = "Rock ID", required = true)
            @PathVariable Long id) {
        log.info("📦 Getting rock by ID: {}", id);
        RockResponse response = rockService.getRockById(id);
        return ResponseEntity.ok(StandardResponse.success("Rock fetched successfully", response));
    }

    @GetMapping("/code/{rockId}")
    @Operation(summary = "Get rock by rock ID", description = "Retrieves a rock by its rock ID (e.g., RCK-001)")
    public ResponseEntity<StandardResponse<RockResponse>> getRockByRockId(
            @Parameter(description = "Rock ID (e.g., RCK-001)", required = true)
            @PathVariable String rockId) {
        log.info("📦 Getting rock by rockId: {}", rockId);
        RockResponse response = rockService.getRockByRockId(rockId);
        return ResponseEntity.ok(StandardResponse.success("Rock fetched successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all rocks with pagination", description = "Retrieves all rocks with pagination and filtering")
    public ResponseEntity<StandardResponse<Page<RockResponse>>> getAllRocks(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) @Parameter(description = "Search by name, rock ID, or type") String search,
            @RequestParam(required = false) @Parameter(description = "Filter by warehouse ID") Long warehouseId) {
        log.info("📦 Getting all rocks - page: {}, size: {}, search: {}, warehouseId: {}", 
                 pageable.getPageNumber(), pageable.getPageSize(), search, warehouseId);
        Page<RockResponse> responses = rockService.getAllRocks(pageable, search, warehouseId);
        return ResponseEntity.ok(StandardResponse.success("Rocks fetched successfully", responses));
    }

    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "Get rocks by warehouse", description = "Retrieves all rocks in a specific warehouse")
    public ResponseEntity<StandardResponse<List<RockResponse>>> getRocksByWarehouse(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable Long warehouseId) {
        log.info("📦 Getting rocks by warehouse: {}", warehouseId);
        List<RockResponse> responses = rockService.getRocksByWarehouse(warehouseId);
        return ResponseEntity.ok(StandardResponse.success("Rocks fetched successfully", responses));
    }

    @GetMapping("/warehouse/{warehouseId}/active")
    @Operation(summary = "Get active rocks by warehouse", description = "Retrieves only active rocks in a specific warehouse")
    public ResponseEntity<StandardResponse<List<RockResponse>>> getActiveRocksByWarehouse(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable Long warehouseId) {
        log.info("📦 Getting active rocks by warehouse: {}", warehouseId);
        List<RockResponse> responses = rockService.getActiveRocksByWarehouse(warehouseId);
        return ResponseEntity.ok(StandardResponse.success("Active rocks fetched successfully", responses));
    }

    @GetMapping("/type/{rockType}")
    @Operation(summary = "Get rocks by type", description = "Retrieves rocks by their type")
    public ResponseEntity<StandardResponse<List<RockResponse>>> getRocksByType(
            @Parameter(description = "Rock type (GRANITE, LIMESTONE, etc.)", required = true)
            @PathVariable String rockType) {
        log.info("📦 Getting rocks by type: {}", rockType);
        List<RockResponse> responses = rockService.getRocksByType(rockType);
        return ResponseEntity.ok(StandardResponse.success("Rocks fetched successfully", responses));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock rocks", description = "Retrieves rocks with quantity below minimum threshold")
    public ResponseEntity<StandardResponse<List<RockResponse>>> getLowStockRocks() {
        log.info("📦 Getting low stock rocks");
        List<RockResponse> responses = rockService.getLowStockRocks();
        return ResponseEntity.ok(StandardResponse.success("Low stock rocks fetched successfully", responses));
    }

    @GetMapping("/over-stock")
    @Operation(summary = "Get over stock rocks", description = "Retrieves rocks with quantity above maximum threshold")
    public ResponseEntity<StandardResponse<List<RockResponse>>> getOverStockRocks() {
        log.info("📦 Getting over stock rocks");
        List<RockResponse> responses = rockService.getOverStockRocks();
        return ResponseEntity.ok(StandardResponse.success("Over stock rocks fetched successfully", responses));
    }

    // ====== UPDATE ======

    @PutMapping("/{id}")
    @Operation(summary = "Update rock", description = "Updates an existing rock")
    public ResponseEntity<StandardResponse<RockResponse>> updateRock(
            @Parameter(description = "Rock ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody RockRequest request) {
        log.info("📦 Updating rock: {}", id);
        RockResponse response = rockService.updateRock(id, request);
        return ResponseEntity.ok(StandardResponse.success("Rock updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Toggle rock status", description = "Activates or deactivates a rock")
    public ResponseEntity<StandardResponse<RockResponse>> toggleRockStatus(
            @Parameter(description = "Rock ID", required = true)
            @PathVariable Long id,
            @RequestParam @Parameter(description = "Active status (true/false)", required = true) Boolean isActive) {
        log.info("📦 Toggling rock status: {} to {}", id, isActive);
        RockResponse response = rockService.toggleRockStatus(id, isActive);
        return ResponseEntity.ok(StandardResponse.success("Rock status updated successfully", response));
    }

    @PostMapping("/{id}/quantity/add")
    @Operation(summary = "Add rock quantity", description = "Adds quantity to a rock")
    public ResponseEntity<StandardResponse<RockResponse>> addRockQuantity(
            @Parameter(description = "Rock ID", required = true)
            @PathVariable Long id,
            @RequestParam @Parameter(description = "Quantity to add", required = true) Integer quantity) {
        log.info("📦 Adding quantity to rock: {} - {}", id, quantity);
        RockResponse response = rockService.addRockQuantity(id, quantity);
        return ResponseEntity.ok(StandardResponse.success("Quantity added successfully", response));
    }

    @PostMapping("/{id}/quantity/deduct")
    @Operation(summary = "Deduct rock quantity", description = "Deducts quantity from a rock")
    public ResponseEntity<StandardResponse<RockResponse>> deductRockQuantity(
            @Parameter(description = "Rock ID", required = true)
            @PathVariable Long id,
            @RequestParam @Parameter(description = "Quantity to deduct", required = true) Integer quantity) {
        log.info("📦 Deducting quantity from rock: {} - {}", id, quantity);
        RockResponse response = rockService.deductRockQuantity(id, quantity);
        return ResponseEntity.ok(StandardResponse.success("Quantity deducted successfully", response));
    }

    // ====== DELETE ======

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete rock (soft delete)", description = "Soft deletes a rock by setting isActive to false")
    public ResponseEntity<StandardResponse<Void>> deleteRock(
            @Parameter(description = "Rock ID", required = true)
            @PathVariable Long id) {
        log.info("📦 Deleting rock: {}", id);
        rockService.deleteRock(id);
        return ResponseEntity.ok(StandardResponse.success("Rock deleted successfully"));
    }

    @DeleteMapping("/code/{rockId}")
    @Operation(summary = "Delete rock by rock ID (soft delete)", description = "Soft deletes a rock by its rock ID")
    public ResponseEntity<StandardResponse<Void>> deleteRockByRockId(
            @Parameter(description = "Rock ID (e.g., RCK-001)", required = true)
            @PathVariable String rockId) {
        log.info("📦 Deleting rock by rockId: {}", rockId);
        rockService.deleteRockByRockId(rockId);
        return ResponseEntity.ok(StandardResponse.success("Rock deleted successfully"));
    }
}