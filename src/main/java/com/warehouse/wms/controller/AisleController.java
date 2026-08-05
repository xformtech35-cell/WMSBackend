// ====== FILE: src/main/java/com/warehouse/wms/controller/AisleController.java ======
package com.warehouse.wms.controller;

import com.warehouse.wms.dto.request.AisleRequest;
import com.warehouse.wms.dto.response.AisleResponse;
import com.warehouse.wms.dto.response.StandardResponse;
import com.warehouse.wms.service.AisleService;
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
@RequestMapping("/api/aisles")
@RequiredArgsConstructor
@Tag(name = "Aisle Management", description = "APIs for managing warehouse aisles")
public class AisleController {

    private final AisleService aisleService;

    // ====== CREATE ======

    @PostMapping
    @Operation(summary = "Create aisle")
    public ResponseEntity<StandardResponse<AisleResponse>> createAisle(
            @Valid @RequestBody AisleRequest request) {
        log.info("📦 Creating aisle: {}", request.getAisleId());
        AisleResponse response = aisleService.createAisle(request);
        return ResponseEntity.ok(StandardResponse.success("Aisle created successfully", response));
    }

    // ====== READ ======

    @GetMapping("/{id}")
    @Operation(summary = "Get aisle by ID")
    public ResponseEntity<StandardResponse<AisleResponse>> getAisleById(@PathVariable Long id) {
        AisleResponse response = aisleService.getAisleById(id);
        return ResponseEntity.ok(StandardResponse.success("Aisle fetched successfully", response));
    }

    @GetMapping("/code/{aisleId}")
    @Operation(summary = "Get aisle by aisle ID")
    public ResponseEntity<StandardResponse<AisleResponse>> getAisleByAisleId(@PathVariable String aisleId) {
        AisleResponse response = aisleService.getAisleByAisleId(aisleId);
        return ResponseEntity.ok(StandardResponse.success("Aisle fetched successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all aisles with pagination")
    public ResponseEntity<StandardResponse<Page<AisleResponse>>> getAllAisles(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long zoneId) {
        Page<AisleResponse> responses = aisleService.getAllAisles(pageable, search, zoneId);
        return ResponseEntity.ok(StandardResponse.success("Aisles fetched successfully", responses));
    }

    @GetMapping("/zone/{zoneId}")
    @Operation(summary = "Get aisles by zone")
    public ResponseEntity<StandardResponse<List<AisleResponse>>> getAislesByZone(
            @PathVariable Long zoneId) {
        List<AisleResponse> responses = aisleService.getAislesByZone(zoneId);
        return ResponseEntity.ok(StandardResponse.success("Aisles fetched successfully", responses));
    }

    @GetMapping("/zone/{zoneId}/active")
    @Operation(summary = "Get active aisles by zone")
    public ResponseEntity<StandardResponse<List<AisleResponse>>> getActiveAislesByZone(
            @PathVariable Long zoneId) {
        List<AisleResponse> responses = aisleService.getActiveAislesByZone(zoneId);
        return ResponseEntity.ok(StandardResponse.success("Active aisles fetched successfully", responses));
    }

    @GetMapping("/zone-code/{zoneId}")
    @Operation(summary = "Get aisles by zone ID (string)")
    public ResponseEntity<StandardResponse<List<AisleResponse>>> getAislesByZoneId(
            @PathVariable String zoneId) {
        List<AisleResponse> responses = aisleService.getAislesByZoneId(zoneId);
        return ResponseEntity.ok(StandardResponse.success("Aisles fetched successfully", responses));
    }

    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "Get aisles by warehouse")
    public ResponseEntity<StandardResponse<List<AisleResponse>>> getAislesByWarehouse(
            @PathVariable Long warehouseId) {
        List<AisleResponse> responses = aisleService.getAislesByWarehouse(warehouseId);
        return ResponseEntity.ok(StandardResponse.success("Aisles fetched successfully", responses));
    }

    // ====== UPDATE ======

    @PutMapping("/{id}")
    @Operation(summary = "Update aisle")
    public ResponseEntity<StandardResponse<AisleResponse>> updateAisle(
            @PathVariable Long id,
            @Valid @RequestBody AisleRequest request) {
        AisleResponse response = aisleService.updateAisle(id, request);
        return ResponseEntity.ok(StandardResponse.success("Aisle updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Toggle aisle status")
    public ResponseEntity<StandardResponse<AisleResponse>> toggleAisleStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        AisleResponse response = aisleService.toggleAisleStatus(id, isActive);
        return ResponseEntity.ok(StandardResponse.success("Aisle status updated successfully", response));
    }

    // ====== DELETE ======

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete aisle (soft delete)")
    public ResponseEntity<StandardResponse<Void>> deleteAisle(@PathVariable Long id) {
        aisleService.deleteAisle(id);
        return ResponseEntity.ok(StandardResponse.success("Aisle deleted successfully"));
    }

    @DeleteMapping("/code/{aisleId}")
    @Operation(summary = "Delete aisle by aisle ID (soft delete)")
    public ResponseEntity<StandardResponse<Void>> deleteAisleByAisleId(@PathVariable String aisleId) {
        aisleService.deleteAisleByAisleId(aisleId);
        return ResponseEntity.ok(StandardResponse.success("Aisle deleted successfully"));
    }
}