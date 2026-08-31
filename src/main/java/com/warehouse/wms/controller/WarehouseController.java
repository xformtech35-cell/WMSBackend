// ====== FILE: src/main/java/com/warehouse/wms/controller/WarehouseController.java ======
package com.warehouse.wms.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import com.warehouse.wms.dto.ApiResponse;
import com.warehouse.wms.dto.request.WarehouseFilterRequest;
import com.warehouse.wms.dto.request.WarehouseRequest;
import com.warehouse.wms.dto.response.StandardResponse;
import com.warehouse.wms.dto.response.WarehouseResponse;
import com.warehouse.wms.dto.response.ZoneResponse;
import com.warehouse.wms.service.WarehouseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    
    
    
    
    
    
    
    @PostMapping("/filter")
    @Operation(summary = "Get warehouses with filter as request body")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<ApiResponse<Page<WarehouseResponse>>> filterWarehouses(
            @RequestBody WarehouseFilterRequest filter) {
        
        log.debug("POST /api/warehouses/filter with body: {}", filter);
        
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? filter.getSize() : 20;
        String sortBy = filter.getSortBy() != null ? filter.getSortBy() : "id";
        String sortDirection = filter.getSortDirection() != null ? filter.getSortDirection() : "ASC";
        
        Pageable pageable = PageRequest.of(page, size, 
                Sort.Direction.fromString(sortDirection), sortBy);
        
        Page<WarehouseResponse> result = warehouseService.getWarehousesWithFullHierarchy(filter, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Filtered warehouses retrieved successfully", result));
    }



//    @GetMapping("/search")
//    @Operation(summary = "Search warehouses by term across multiple fields")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
//    public ResponseEntity<ApiResponse<Page<WarehouseResponse>>> searchWarehouses(
//            @RequestParam String term,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @RequestParam(defaultValue = "id") String sortBy,
//            @RequestParam(defaultValue = "ASC") String sortDirection) {
//        
//        log.debug("GET /api/warehouses/search with term: {}", term);
//        
//        Pageable pageable = PageRequest.of(page, size, 
//                Sort.Direction.fromString(sortDirection), sortBy);
//        
//        Page<WarehouseResponse> result = warehouseService.searchWarehouses(term, pageable);
//        
//        return ResponseEntity.ok(ApiResponse.success("Search results retrieved successfully", result));
//    }

    @GetMapping("/{id}/zones")
    @Operation(summary = "Get all zones of a warehouse with their hierarchy")
    public ResponseEntity<ApiResponse<List<ZoneResponse>>> getWarehouseZones(
            @PathVariable Long id) {
        
        log.debug("GET /api/warehouses/{}/zones", id);
        
        WarehouseResponse warehouse = warehouseService.getWarehouseWithFullHierarchy(id);
        
        return ResponseEntity.ok(ApiResponse.success("Zones retrieved successfully", warehouse.getZones()));
    }

    @GetMapping("/{id}/zones/{zoneId}")
    @Operation(summary = "Get a specific zone with full hierarchy")
    public ResponseEntity<ApiResponse<ZoneResponse>> getZoneDetails(
            @PathVariable Long id,
            @PathVariable Long zoneId) {
        
        log.debug("GET /api/warehouses/{}/zones/{}", id, zoneId);
        
        WarehouseResponse warehouse = warehouseService.getWarehouseWithFullHierarchy(id);
        
        ZoneResponse zone = warehouse.getZones().stream()
                .filter(z -> z.getId().equals(zoneId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Zone not found with id: " + zoneId));
        
        return ResponseEntity.ok(ApiResponse.success("Zone details retrieved successfully", zone));
    }
}