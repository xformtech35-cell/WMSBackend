// ====== FILE: src/main/java/com/warehouse/wms/controller/LevelController.java ======
package com.warehouse.wms.controller;

import com.warehouse.wms.dto.request.LevelRequest;
import com.warehouse.wms.dto.response.LevelResponse;
import com.warehouse.wms.dto.response.StandardResponse;
import com.warehouse.wms.service.LevelService;
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
@RequestMapping("/api/levels")
@RequiredArgsConstructor
@Tag(name = "Level Management", description = "APIs for managing rack levels/shelves")
public class LevelController {

    private final LevelService levelService;

    // ====== CREATE ======

    @PostMapping
    @Operation(summary = "Create a new level", description = "Creates a new level/shelf in a rack")
    public ResponseEntity<StandardResponse<LevelResponse>> createLevel(
            @Valid @RequestBody LevelRequest request) {
        log.info("📦 Creating level: {}", request.getLevelId());
        LevelResponse response = levelService.createLevel(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StandardResponse.success("Level created successfully", response));
    }

    // ====== READ ======

    @GetMapping("/{id}")
    @Operation(summary = "Get level by ID", description = "Retrieves a level by its unique ID")
    public ResponseEntity<StandardResponse<LevelResponse>> getLevelById(
            @Parameter(description = "Level ID", required = true)
            @PathVariable Long id) {
        log.info("📦 Getting level by ID: {}", id);
        LevelResponse response = levelService.getLevelById(id);
        return ResponseEntity.ok(StandardResponse.success("Level fetched successfully", response));
    }

    @GetMapping("/code/{levelId}")
    @Operation(summary = "Get level by level ID", description = "Retrieves a level by its level ID (e.g., L-01)")
    public ResponseEntity<StandardResponse<LevelResponse>> getLevelByLevelId(
            @Parameter(description = "Level ID (e.g., L-01)", required = true)
            @PathVariable String levelId) {
        log.info("📦 Getting level by levelId: {}", levelId);
        LevelResponse response = levelService.getLevelByLevelId(levelId);
        return ResponseEntity.ok(StandardResponse.success("Level fetched successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all levels with pagination", description = "Retrieves all levels with pagination and filtering")
    public ResponseEntity<StandardResponse<Page<LevelResponse>>> getAllLevels(
            @PageableDefault(size = 20, sort = "levelNumber", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) @Parameter(description = "Search by level ID or name") String search,
            @RequestParam(required = false) @Parameter(description = "Filter by rack ID") Long rackId) {
        log.info("📦 Getting all levels - page: {}, size: {}, search: {}, rackId: {}", 
                 pageable.getPageNumber(), pageable.getPageSize(), search, rackId);
        Page<LevelResponse> responses = levelService.getAllLevels(pageable, search, rackId);
        return ResponseEntity.ok(StandardResponse.success("Levels fetched successfully", responses));
    }

    @GetMapping("/rack/{rackId}")
    @Operation(summary = "Get levels by rack", description = "Retrieves all levels in a specific rack")
    public ResponseEntity<StandardResponse<List<LevelResponse>>> getLevelsByRack(
            @Parameter(description = "Rack ID", required = true)
            @PathVariable Long rackId) {
        log.info("📦 Getting levels by rack: {}", rackId);
        List<LevelResponse> responses = levelService.getLevelsByRack(rackId);
        return ResponseEntity.ok(StandardResponse.success("Levels fetched successfully", responses));
    }

    @GetMapping("/rack/{rackId}/active")
    @Operation(summary = "Get active levels by rack", description = "Retrieves only active levels in a specific rack")
    public ResponseEntity<StandardResponse<List<LevelResponse>>> getActiveLevelsByRack(
            @Parameter(description = "Rack ID", required = true)
            @PathVariable Long rackId) {
        log.info("📦 Getting active levels by rack: {}", rackId);
        List<LevelResponse> responses = levelService.getActiveLevelsByRack(rackId);
        return ResponseEntity.ok(StandardResponse.success("Active levels fetched successfully", responses));
    }

    @GetMapping("/rack/{rackId}/ordered")
    @Operation(summary = "Get levels by rack ordered", description = "Retrieves levels in a rack ordered by level number")
    public ResponseEntity<StandardResponse<List<LevelResponse>>> getLevelsByRackOrdered(
            @Parameter(description = "Rack ID", required = true)
            @PathVariable Long rackId) {
        log.info("📦 Getting levels by rack ordered: {}", rackId);
        List<LevelResponse> responses = levelService.getLevelsByRackOrdered(rackId);
        return ResponseEntity.ok(StandardResponse.success("Levels fetched successfully", responses));
    }

    // ====== UPDATE ======

    @PutMapping("/{id}")
    @Operation(summary = "Update level", description = "Updates an existing level")
    public ResponseEntity<StandardResponse<LevelResponse>> updateLevel(
            @Parameter(description = "Level ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody LevelRequest request) {
        log.info("📦 Updating level: {}", id);
        LevelResponse response = levelService.updateLevel(id, request);
        return ResponseEntity.ok(StandardResponse.success("Level updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Toggle level status", description = "Activates or deactivates a level")
    public ResponseEntity<StandardResponse<LevelResponse>> toggleLevelStatus(
            @Parameter(description = "Level ID", required = true)
            @PathVariable Long id,
            @RequestParam @Parameter(description = "Active status (true/false)", required = true) Boolean isActive) {
        log.info("📦 Toggling level status: {} to {}", id, isActive);
        LevelResponse response = levelService.toggleLevelStatus(id, isActive);
        return ResponseEntity.ok(StandardResponse.success("Level status updated successfully", response));
    }

    // ====== DELETE ======

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete level (soft delete)", description = "Soft deletes a level by setting isActive to false")
    public ResponseEntity<StandardResponse<Void>> deleteLevel(
            @Parameter(description = "Level ID", required = true)
            @PathVariable Long id) {
        log.info("📦 Deleting level: {}", id);
        levelService.deleteLevel(id);
        return ResponseEntity.ok(StandardResponse.success("Level deleted successfully"));
    }

    @DeleteMapping("/code/{levelId}")
    @Operation(summary = "Delete level by level ID (soft delete)", description = "Soft deletes a level by its level ID")
    public ResponseEntity<StandardResponse<Void>> deleteLevelByLevelId(
            @Parameter(description = "Level ID (e.g., L-01)", required = true)
            @PathVariable String levelId) {
        log.info("📦 Deleting level by levelId: {}", levelId);
        levelService.deleteLevelByLevelId(levelId);
        return ResponseEntity.ok(StandardResponse.success("Level deleted successfully"));
    }
}