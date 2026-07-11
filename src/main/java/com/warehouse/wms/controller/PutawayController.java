package com.warehouse.wms.controller;

import com.warehouse.wms.dto.ExecutionResult;
import com.warehouse.wms.dto.PutawayExecutionRequest;
import com.warehouse.wms.dto.PutawayHistoryEntry;
import com.warehouse.wms.dto.PutawayTaskResponse;
import com.warehouse.wms.entity.MovementLog;
import com.warehouse.wms.entity.PutawayTask;
import com.warehouse.wms.entity.User;
import com.warehouse.wms.repository.MovementLogRepository;
import com.warehouse.wms.repository.PutawayTaskRepository;
import com.warehouse.wms.repository.UserRepository;
import com.warehouse.wms.service.PutawayEngineService;
import com.warehouse.wms.service.PutawayExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/putaway")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PUTAWAY_VIEW')")
public class PutawayController {

    private final PutawayEngineService putawayEngineService;
    private final PutawayExecutionService putawayExecutionService;
    private final PutawayTaskRepository putawayTaskRepository;
    private final MovementLogRepository movementLogRepository;
    private final UserRepository userRepository;

    @Operation(summary = "Generate putaway tasks from a GRN")
    @PostMapping("/tasks/generate/{grnId}")
    public ResponseEntity<List<PutawayTaskResponse>> generate(@PathVariable Long grnId) {
        return ResponseEntity.ok(putawayEngineService.generatePutawayTasks(grnId));
    }

    @Operation(summary = "Execute putaway scan")
    @PostMapping("/execute")
    public ResponseEntity<ExecutionResult> execute(
            @Valid @RequestBody PutawayExecutionRequest request,
            Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + authentication.getName()));
        return ResponseEntity.ok(putawayExecutionService.executeScan(
                request.getItemBarcode(), request.getBinBarcode(), user.getId()));
    }

    @Operation(summary = "Get putaway history by item barcode/serial number")
    @GetMapping("/history")
    public ResponseEntity<List<PutawayHistoryEntry>> history(@RequestParam String serialNo) {
        List<MovementLog> logs = movementLogRepository.findByInventorySerialNo(serialNo);
        Set<Long> userIds = logs.stream()
                .map(MovementLog::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> users = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        List<PutawayHistoryEntry> history = logs.stream().map(log -> {
            User actor = log.getUserId() != null ? users.get(log.getUserId()) : null;
            PutawayTask task = putawayTaskRepository.findByInventoryIdAndStatus(
                    log.getInventory() != null ? log.getInventory().getId() : null,
                    PutawayTask.PutawayTaskStatus.COMPLETED
            ).orElse(null);
            return PutawayHistoryEntry.builder()
                    .id(log.getId())
                    .serialNo(log.getInventory() != null ? log.getInventory().getSerialNo() : null)
                    .fromState(log.getFromState() != null ? log.getFromState().name() : null)
                    .toState(log.getToState() != null ? log.getToState().name() : null)
                    .binBarcode(log.getBin() != null ? log.getBin().getBarcode() : null)
                    .suggestedBinBarcode(task != null && task.getSuggestedBin() != null ? task.getSuggestedBin().getBarcode() : null)
                    .action(log.getAction())
                    .userId(log.getUserId())
                    .userName(actor != null ? actor.getUsername() : null)
                    .userRole(actor != null && actor.getRole() != null ? actor.getRole().getName() : null)
                    .createdAt(log.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "List pending putaway tasks")
    @GetMapping("/tasks/pending")
    public ResponseEntity<List<PutawayTaskResponse>> pending() {
        List<PutawayTask> tasks = putawayTaskRepository.findByStatusOrderByPriorityAscIdAsc(PutawayTask.PutawayTaskStatus.PENDING);
        List<PutawayTaskResponse> responses = tasks.stream().map(t -> PutawayTaskResponse.builder()
                .taskId(t.getId())
                .inventoryId(t.getInventory() != null ? t.getInventory().getId() : null)
                .itemBarcode(t.getInventory() != null ? t.getInventory().getSerialNo() : null)
                .suggestedBinBarcode(t.getSuggestedBin() != null ? t.getSuggestedBin().getBarcode() : null)
                .priority(t.getPriority())
                .state(t.getStatus().name())
                .build()
        ).toList();
        return ResponseEntity.ok(responses);
    }
}
