// ====== FILE: src/main/java/com/warehouse/wms/controller/RackController.java ======
package com.warehouse.wms.controller;

import com.warehouse.wms.dto.request.RackRequest;
import com.warehouse.wms.dto.response.RackResponse;
import com.warehouse.wms.dto.response.StandardResponse;
import com.warehouse.wms.service.RackService;
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
@RequestMapping("/api/racks")
@RequiredArgsConstructor
@Tag(name = "Rack Management", description = "APIs for managing warehouse racks")
public class RackController {

    private final RackService rackService;

    // ====== CREATE ======

    @PostMapping
    @Operation(summary = "Create rack")
    public ResponseEntity<StandardResponse<RackResponse>> createRack(
            @Valid @RequestBody RackRequest request) {
        log.info("📦 Creating rack: {}", request.getRackId());
        RackResponse response = rackService.createRack(request);
        return ResponseEntity.ok(StandardResponse.success("Rack created successfully", response));
    }

    // ====== READ ======

    @GetMapping("/{id}")
    @Operation(summary = "Get rack by ID")
    public ResponseEntity<StandardResponse<RackResponse>> getRackById(@PathVariable Long id) {
        RackResponse response = rackService.getRackById(id);
        return ResponseEntity.ok(StandardResponse.success("Rack fetched successfully", response));
    }

    @GetMapping("/code/{rackId}")
    @Operation(summary = "Get rack by rack ID")
    public ResponseEntity<StandardResponse<RackResponse>> getRackByRackId(@PathVariable String rackId) {
        RackResponse response = rackService.getRackByRackId(rackId);
        return ResponseEntity.ok(StandardResponse.success("Rack fetched successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all racks with pagination")
    public ResponseEntity<StandardResponse<Page<RackResponse>>> getAllRacks(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long aisleId) {
        Page<RackResponse> responses = rackService.getAllRacks(pageable, search, aisleId);
        return ResponseEntity.ok(StandardResponse.success("Racks fetched successfully", responses));
    }

    @GetMapping("/aisle/{aisleId}")
    @Operation(summary = "Get racks by aisle")
    public ResponseEntity<StandardResponse<List<RackResponse>>> getRacksByAisle(
            @PathVariable Long aisleId) {
        List<RackResponse> responses = rackService.getRacksByAisle(aisleId);
        return ResponseEntity.ok(StandardResponse.success("Racks fetched successfully", responses));
    }

    @GetMapping("/aisle/{aisleId}/active")
    @Operation(summary = "Get active racks by aisle")
    public ResponseEntity<StandardResponse<List<RackResponse>>> getActiveRacksByAisle(
            @PathVariable Long aisleId) {
        List<RackResponse> responses = rackService.getActiveRacksByAisle(aisleId);
        return ResponseEntity.ok(StandardResponse.success("Active racks fetched successfully", responses));
    }

    @GetMapping("/aisle-code/{aisleId}")
    @Operation(summary = "Get racks by aisle ID (string)")
    public ResponseEntity<StandardResponse<List<RackResponse>>> getRacksByAisleId(
            @PathVariable String aisleId) {
        List<RackResponse> responses = rackService.getRacksByAisleId(aisleId);
        return ResponseEntity.ok(StandardResponse.success("Racks fetched successfully", responses));
    }

    @GetMapping("/zone/{zoneId}")
    @Operation(summary = "Get racks by zone")
    public ResponseEntity<StandardResponse<List<RackResponse>>> getRacksByZone(
            @PathVariable Long zoneId) {
        List<RackResponse> responses = rackService.getRacksByZone(zoneId);
        return ResponseEntity.ok(StandardResponse.success("Racks fetched successfully", responses));
    }

    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "Get racks by warehouse")
    public ResponseEntity<StandardResponse<List<RackResponse>>> getRacksByWarehouse(
            @PathVariable Long warehouseId) {
        List<RackResponse> responses = rackService.getRacksByWarehouse(warehouseId);
        return ResponseEntity.ok(StandardResponse.success("Racks fetched successfully", responses));
    }

    // ====== UPDATE ======

    @PutMapping("/{id}")
    @Operation(summary = "Update rack")
    public ResponseEntity<StandardResponse<RackResponse>> updateRack(
            @PathVariable Long id,
            @Valid @RequestBody RackRequest request) {
        RackResponse response = rackService.updateRack(id, request);
        return ResponseEntity.ok(StandardResponse.success("Rack updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Toggle rack status")
    public ResponseEntity<StandardResponse<RackResponse>> toggleRackStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        RackResponse response = rackService.toggleRackStatus(id, isActive);
        return ResponseEntity.ok(StandardResponse.success("Rack status updated successfully", response));
    }

    // ====== DELETE ======

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete rack (soft delete)")
    public ResponseEntity<StandardResponse<Void>> deleteRack(@PathVariable Long id) {
        rackService.deleteRack(id);
        return ResponseEntity.ok(StandardResponse.success("Rack deleted successfully"));
    }

    @DeleteMapping("/code/{rackId}")
    @Operation(summary = "Delete rack by rack ID (soft delete)")
    public ResponseEntity<StandardResponse<Void>> deleteRackByRackId(@PathVariable String rackId) {
        rackService.deleteRackByRackId(rackId);
        return ResponseEntity.ok(StandardResponse.success("Rack deleted successfully"));
    }
}