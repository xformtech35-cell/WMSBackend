// ====== FILE: src/main/java/com/warehouse/wms/controller/ZoneController.java ======
package com.warehouse.wms.controller;

import com.warehouse.wms.dto.request.ZoneRequest;
import com.warehouse.wms.dto.response.ZoneResponse;
import com.warehouse.wms.dto.response.StandardResponse;
import com.warehouse.wms.service.ZoneService;
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
@RequestMapping("/api/zones")
@RequiredArgsConstructor
@Tag(name = "Zone Management", description = "APIs for managing warehouse zones")
public class ZoneController {

    private final ZoneService zoneService;

    // ====== CREATE ======

    @PostMapping
    @Operation(summary = "Create zone")
    public ResponseEntity<StandardResponse<ZoneResponse>> createZone(
            @Valid @RequestBody ZoneRequest request) {
        log.info("📦 Creating zone: {}", request.getZoneId());
        ZoneResponse response = zoneService.createZone(request);
        return ResponseEntity.ok(StandardResponse.success("Zone created successfully", response));
    }

    // ====== READ ======

    @GetMapping("/{id}")
    @Operation(summary = "Get zone by ID")
    public ResponseEntity<StandardResponse<ZoneResponse>> getZoneById(@PathVariable Long id) {
        ZoneResponse response = zoneService.getZoneById(id);
        return ResponseEntity.ok(StandardResponse.success("Zone fetched successfully", response));
    }

    @GetMapping("/code/{zoneId}")
    @Operation(summary = "Get zone by zone ID")
    public ResponseEntity<StandardResponse<ZoneResponse>> getZoneByZoneId(@PathVariable String zoneId) {
        ZoneResponse response = zoneService.getZoneByZoneId(zoneId);
        return ResponseEntity.ok(StandardResponse.success("Zone fetched successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all zones with pagination")
    public ResponseEntity<StandardResponse<Page<ZoneResponse>>> getAllZones(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long warehouseId) {
        Page<ZoneResponse> responses = zoneService.getAllZones(pageable, search, warehouseId);
        return ResponseEntity.ok(StandardResponse.success("Zones fetched successfully", responses));
    }

    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "Get zones by warehouse")
    public ResponseEntity<StandardResponse<List<ZoneResponse>>> getZonesByWarehouse(
            @PathVariable Long warehouseId) {
        List<ZoneResponse> responses = zoneService.getZonesByWarehouse(warehouseId);
        return ResponseEntity.ok(StandardResponse.success("Zones fetched successfully", responses));
    }

    @GetMapping("/warehouse/{warehouseId}/active")
    @Operation(summary = "Get active zones by warehouse")
    public ResponseEntity<StandardResponse<List<ZoneResponse>>> getActiveZonesByWarehouse(
            @PathVariable Long warehouseId) {
        List<ZoneResponse> responses = zoneService.getActiveZonesByWarehouse(warehouseId);
        return ResponseEntity.ok(StandardResponse.success("Active zones fetched successfully", responses));
    }

    @GetMapping("/type/{zoneType}")
    @Operation(summary = "Get zones by type")
    public ResponseEntity<StandardResponse<List<ZoneResponse>>> getZonesByType(
            @PathVariable String zoneType) {
        List<ZoneResponse> responses = zoneService.getZonesByType(zoneType);
        return ResponseEntity.ok(StandardResponse.success("Zones fetched successfully", responses));
    }

    // ====== UPDATE ======

    @PutMapping("/{id}")
    @Operation(summary = "Update zone")
    public ResponseEntity<StandardResponse<ZoneResponse>> updateZone(
            @PathVariable Long id,
            @Valid @RequestBody ZoneRequest request) {
        ZoneResponse response = zoneService.updateZone(id, request);
        return ResponseEntity.ok(StandardResponse.success("Zone updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Toggle zone status")
    public ResponseEntity<StandardResponse<ZoneResponse>> toggleZoneStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        ZoneResponse response = zoneService.toggleZoneStatus(id, isActive);
        return ResponseEntity.ok(StandardResponse.success("Zone status updated successfully", response));
    }

    // ====== DELETE ======

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete zone (soft delete)")
    public ResponseEntity<StandardResponse<Void>> deleteZone(@PathVariable Long id) {
        zoneService.deleteZone(id);
        return ResponseEntity.ok(StandardResponse.success("Zone deleted successfully"));
    }

    @DeleteMapping("/code/{zoneId}")
    @Operation(summary = "Delete zone by zone ID (soft delete)")
    public ResponseEntity<StandardResponse<Void>> deleteZoneByZoneId(@PathVariable String zoneId) {
        zoneService.deleteZoneByZoneId(zoneId);
        return ResponseEntity.ok(StandardResponse.success("Zone deleted successfully"));
    }
}