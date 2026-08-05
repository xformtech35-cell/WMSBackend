// ====== FILE: src/main/java/com/warehouse/wms/controller/WarehouseController.java ======
package com.warehouse.wms.controller;

import com.warehouse.wms.dto.request.WarehouseRequest;
import com.warehouse.wms.dto.response.WarehouseResponse;
import com.warehouse.wms.dto.response.StandardResponse;
import com.warehouse.wms.service.WarehouseService;
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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@Tag(name = "Warehouse Management", description = "APIs for managing warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    // ====== CREATE ======
    @PostMapping
    @Operation(summary = "Create warehouse")
    public ResponseEntity<StandardResponse<WarehouseResponse>> createWarehouse(
            @Valid @RequestBody WarehouseRequest request) {
        WarehouseResponse response = warehouseService.createWarehouse(request);
        return ResponseEntity.ok(StandardResponse.success("Warehouse created successfully", response));
    }

    // ====== READ ======
    @GetMapping("/{id}")
    @Operation(summary = "Get warehouse by ID")
    public ResponseEntity<StandardResponse<WarehouseResponse>> getWarehouseById(@PathVariable Long id) {
        WarehouseResponse response = warehouseService.getWarehouseById(id);
        return ResponseEntity.ok(StandardResponse.success("Warehouse fetched successfully", response));
    }

    @GetMapping("/code/{warehouseId}")
    @Operation(summary = "Get warehouse by warehouse ID")
    public ResponseEntity<StandardResponse<WarehouseResponse>> getWarehouseByWarehouseId(
            @PathVariable String warehouseId) {
        WarehouseResponse response = warehouseService.getWarehouseByWarehouseId(warehouseId);
        return ResponseEntity.ok(StandardResponse.success("Warehouse fetched successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all warehouses with pagination")
    public ResponseEntity<StandardResponse<Page<WarehouseResponse>>> getAllWarehouses(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String search) {
        Page<WarehouseResponse> responses = warehouseService.getAllWarehouses(pageable, search);
        return ResponseEntity.ok(StandardResponse.success("Warehouses fetched successfully", responses));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active warehouses")
    public ResponseEntity<StandardResponse<List<WarehouseResponse>>> getActiveWarehouses() {
        List<WarehouseResponse> responses = warehouseService.getActiveWarehouses();
        return ResponseEntity.ok(StandardResponse.success("Active warehouses fetched successfully", responses));
    }

    // ====== UPDATE ======
    @PutMapping("/{id}")
    @Operation(summary = "Update warehouse")
    public ResponseEntity<StandardResponse<WarehouseResponse>> updateWarehouse(
            @PathVariable Long id,
            @Valid @RequestBody WarehouseRequest request) {
        WarehouseResponse response = warehouseService.updateWarehouse(id, request);
        return ResponseEntity.ok(StandardResponse.success("Warehouse updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Toggle warehouse status")
    public ResponseEntity<StandardResponse<Void>> toggleWarehouseStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        warehouseService.toggleWarehouseStatus(id, isActive);
        return ResponseEntity.ok(StandardResponse.success("Warehouse status updated successfully"));
    }

    // ====== DELETE ======
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete warehouse (soft delete)")
    public ResponseEntity<StandardResponse<Void>> deleteWarehouse(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.ok(StandardResponse.success("Warehouse deleted successfully"));
    }
}