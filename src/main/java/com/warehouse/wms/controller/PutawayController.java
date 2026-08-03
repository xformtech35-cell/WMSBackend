// ====== FILE: src/main/java/com/warehouse/wms/controller/PutawayController.java ======
package com.warehouse.wms.controller;

import com.warehouse.wms.dto.request.PutawayInitiateRequest;
import com.warehouse.wms.dto.request.PutawayExecuteRequest;
import com.warehouse.wms.dto.request.PutawayConfirmRequest;
import com.warehouse.wms.dto.request.LocationSuggestionRequest;
import com.warehouse.wms.dto.response.PutawayTaskResponse;
import com.warehouse.wms.dto.response.LocationSuggestionResponse;
import com.warehouse.wms.service.PutawayService;
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
@RequestMapping("/api/v1/putaway")
@RequiredArgsConstructor
@Tag(name = "Putaway Management", description = "APIs for Putaway process management")
public class PutawayController {

    private final PutawayService putawayService;

    @PostMapping("/initiate")
    @Operation(summary = "Initiate Putaway task")
    public ResponseEntity<PutawayTaskResponse> initiatePutaway(@Valid @RequestBody PutawayInitiateRequest request) {
        log.info("Received request to initiate putaway");
        PutawayTaskResponse response = putawayService.initiatePutaway(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/execute")
    @Operation(summary = "Execute putaway stage")
    public ResponseEntity<PutawayTaskResponse> executePutawayStage(@Valid @RequestBody PutawayExecuteRequest request) {
        log.info("Received request to execute putaway stage: {}", request.getStage());
        PutawayTaskResponse response = putawayService.executePutawayStage(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    @Operation(summary = "Confirm putaway")
    public ResponseEntity<PutawayTaskResponse> confirmPutaway(@Valid @RequestBody PutawayConfirmRequest request) {
        log.info("Received request to confirm putaway");
        PutawayTaskResponse response = putawayService.confirmPutaway(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/suggest-location")
    @Operation(summary = "Suggest location for putaway")
    public ResponseEntity<LocationSuggestionResponse> suggestLocation(@Valid @RequestBody LocationSuggestionRequest request) {
        log.info("Received request to suggest location for item: {}", request.getItemCode());
        LocationSuggestionResponse response = putawayService.suggestLocation(
                request.getItemCode(), 
                request.getQuantity(), 
                request.getWarehouseId()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/task/{taskNumber}")
    @Operation(summary = "Get Putaway task by number")
    public ResponseEntity<PutawayTaskResponse> getPutawayTaskByNumber(@PathVariable String taskNumber) {
        PutawayTaskResponse response = putawayService.getPutawayTaskByNumber(taskNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/grn/{grnNumber}")
    @Operation(summary = "Get Putaway task by GRN number")
    public ResponseEntity<PutawayTaskResponse> getPutawayTaskByGrnNumber(@PathVariable String grnNumber) {
        PutawayTaskResponse response = putawayService.getPutawayTaskByGrnNumber(grnNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get Putaway tasks by status")
    public ResponseEntity<List<PutawayTaskResponse>> getPutawayTasksByStatus(@PathVariable String status) {
        List<PutawayTaskResponse> responses = putawayService.getPutawayTasksByStatus(status);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/assigned-to/{assignedTo}")
    @Operation(summary = "Get Putaway tasks assigned to operator")
    public ResponseEntity<List<PutawayTaskResponse>> getPutawayTasksByAssignedTo(@PathVariable String assignedTo) {
        List<PutawayTaskResponse> responses = putawayService.getPutawayTasksByAssignedTo(assignedTo);
        return ResponseEntity.ok(responses);
    }

    @GetMapping
    @Operation(summary = "Get all Putaway tasks with pagination")
    public ResponseEntity<Page<PutawayTaskResponse>> getAllPutawayTasks(@PageableDefault(size = 20) Pageable pageable) {
        Page<PutawayTaskResponse> responses = putawayService.getAllPutawayTasks(pageable);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/cancel/{taskNumber}")
    @Operation(summary = "Cancel Putaway task")
    public ResponseEntity<Void> cancelPutawayTask(@PathVariable String taskNumber, @RequestParam String reason) {
        log.info("Received request to cancel task: {}", taskNumber);
        putawayService.cancelPutawayTask(taskNumber, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/update-inventory/{confirmationNumber}")
    @Operation(summary = "Update inventory after putaway")
    public ResponseEntity<Void> updateInventoryAfterPutaway(@PathVariable String confirmationNumber) {
        log.info("Received request to update inventory for confirmation: {}", confirmationNumber);
        putawayService.updateInventoryAfterPutaway(confirmationNumber);
        return ResponseEntity.ok().build();
    }
}