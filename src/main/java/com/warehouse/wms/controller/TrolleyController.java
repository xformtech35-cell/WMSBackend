// ====== FILE: src/main/java/com/warehouse/wms/controller/TrolleyController.java ======
package com.warehouse.wms.controller;

import com.warehouse.wms.dto.request.TrolleyRequest;
import com.warehouse.wms.dto.response.TrolleyResponse;
import com.warehouse.wms.dto.response.StandardResponse;
import com.warehouse.wms.service.TrolleyService;
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
@RequestMapping("/api/trolleys")
@RequiredArgsConstructor
@Tag(name = "Trolley Management", description = "APIs for managing warehouse trolleys")
public class TrolleyController {

    private final TrolleyService trolleyService;

    // ====== CREATE ======

    @PostMapping
    @Operation(summary = "Create trolley")
    public ResponseEntity<StandardResponse<TrolleyResponse>> createTrolley(
            @Valid @RequestBody TrolleyRequest request) {
        log.info("📦 Creating trolley: {}", request.getTrolleyIdentifier());
        TrolleyResponse response = trolleyService.createTrolley(request);
        return ResponseEntity.ok(StandardResponse.success("Trolley created successfully", response));
    }

    // ====== READ ======

    @GetMapping("/{id}")
    @Operation(summary = "Get trolley by ID")
    public ResponseEntity<StandardResponse<TrolleyResponse>> getTrolleyById(@PathVariable Long id) {
        TrolleyResponse response = trolleyService.getTrolleyById(id);
        return ResponseEntity.ok(StandardResponse.success("Trolley fetched successfully", response));
    }

    @GetMapping("/identifier/{trolleyIdentifier}")
    @Operation(summary = "Get trolley by identifier")
    public ResponseEntity<StandardResponse<TrolleyResponse>> getTrolleyByIdentifier(
            @PathVariable String trolleyIdentifier) {
        TrolleyResponse response = trolleyService.getTrolleyByIdentifier(trolleyIdentifier);
        return ResponseEntity.ok(StandardResponse.success("Trolley fetched successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all trolleys with pagination")
    public ResponseEntity<StandardResponse<Page<TrolleyResponse>>> getAllTrolleys(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        Page<TrolleyResponse> responses = trolleyService.getAllTrolleys(pageable, search, status);
        return ResponseEntity.ok(StandardResponse.success("Trolleys fetched successfully", responses));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get trolleys by status")
    public ResponseEntity<StandardResponse<List<TrolleyResponse>>> getTrolleysByStatus(
            @PathVariable String status) {
        List<TrolleyResponse> responses = trolleyService.getTrolleysByStatus(status);
        return ResponseEntity.ok(StandardResponse.success("Trolleys fetched successfully", responses));
    }

    @GetMapping("/type/{trolleyType}")
    @Operation(summary = "Get trolleys by type")
    public ResponseEntity<StandardResponse<List<TrolleyResponse>>> getTrolleysByType(
            @PathVariable String trolleyType) {
        List<TrolleyResponse> responses = trolleyService.getTrolleysByType(trolleyType);
        return ResponseEntity.ok(StandardResponse.success("Trolleys fetched successfully", responses));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available trolleys")
    public ResponseEntity<StandardResponse<List<TrolleyResponse>>> getAvailableTrolleys(
            @RequestParam(required = false) Integer requiredWeight) {
        List<TrolleyResponse> responses = trolleyService.getAvailableTrolleys(requiredWeight);
        return ResponseEntity.ok(StandardResponse.success("Available trolleys fetched successfully", responses));
    }

    @GetMapping("/maintenance/due")
    @Operation(summary = "Get trolleys due for maintenance")
    public ResponseEntity<StandardResponse<List<TrolleyResponse>>> getTrolleysDueForMaintenance() {
        List<TrolleyResponse> responses = trolleyService.getTrolleysDueForMaintenance();
        return ResponseEntity.ok(StandardResponse.success("Trolleys due for maintenance fetched successfully", responses));
    }

    // ====== UPDATE ======

    @PutMapping("/{id}")
    @Operation(summary = "Update trolley")
    public ResponseEntity<StandardResponse<TrolleyResponse>> updateTrolley(
            @PathVariable Long id,
            @Valid @RequestBody TrolleyRequest request) {
        TrolleyResponse response = trolleyService.updateTrolley(id, request);
        return ResponseEntity.ok(StandardResponse.success("Trolley updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update trolley status")
    public ResponseEntity<StandardResponse<TrolleyResponse>> updateTrolleyStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        TrolleyResponse response = trolleyService.updateTrolleyStatus(id, status);
        return ResponseEntity.ok(StandardResponse.success("Trolley status updated successfully", response));
    }

    @PostMapping("/{id}/load/add")
    @Operation(summary = "Add load to trolley")
    public ResponseEntity<StandardResponse<TrolleyResponse>> addTrolleyLoad(
            @PathVariable Long id,
            @RequestParam Integer weight) {
        TrolleyResponse response = trolleyService.addTrolleyLoad(id, weight);
        return ResponseEntity.ok(StandardResponse.success("Load added to trolley successfully", response));
    }

    @PostMapping("/{id}/load/remove")
    @Operation(summary = "Remove load from trolley")
    public ResponseEntity<StandardResponse<TrolleyResponse>> removeTrolleyLoad(
            @PathVariable Long id,
            @RequestParam Integer weight) {
        TrolleyResponse response = trolleyService.removeTrolleyLoad(id, weight);
        return ResponseEntity.ok(StandardResponse.success("Load removed from trolley successfully", response));
    }

    @PatchMapping("/{id}/active")
    @Operation(summary = "Toggle trolley active status")
    public ResponseEntity<StandardResponse<TrolleyResponse>> toggleTrolleyStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        TrolleyResponse response = trolleyService.toggleTrolleyStatus(id, isActive);
        return ResponseEntity.ok(StandardResponse.success("Trolley status updated successfully", response));
    }

    // ====== DELETE ======

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete trolley (soft delete)")
    public ResponseEntity<StandardResponse<Void>> deleteTrolley(@PathVariable Long id) {
        trolleyService.deleteTrolley(id);
        return ResponseEntity.ok(StandardResponse.success("Trolley deleted successfully"));
    }

    @DeleteMapping("/identifier/{trolleyIdentifier}")
    @Operation(summary = "Delete trolley by identifier (soft delete)")
    public ResponseEntity<StandardResponse<Void>> deleteTrolleyByIdentifier(
            @PathVariable String trolleyIdentifier) {
        trolleyService.deleteTrolleyByIdentifier(trolleyIdentifier);
        return ResponseEntity.ok(StandardResponse.success("Trolley deleted successfully"));
    }
}