package com.warehouse.wms.controller;

import com.warehouse.wms.entity.CountLine;
import com.warehouse.wms.entity.CountTask;
import com.warehouse.wms.entity.StockAdjustment;
import com.warehouse.wms.repository.CountLineRepository;
import com.warehouse.wms.repository.CountTaskRepository;
import com.warehouse.wms.repository.StockAdjustmentRepository;
import com.warehouse.wms.service.CycleCountService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.warehouse.wms.repository.UserRepository;

@RestController
@RequestMapping("/api/cycle-count")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('CYCLE_COUNT_VIEW')")
public class CycleCountController {

    private final CycleCountService cycleCountService;
    private final CountTaskRepository countTaskRepository;
    private final CountLineRepository countLineRepository;
    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<CountTask>> listTasks() {
        return ResponseEntity.ok(countTaskRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CountTask> getTask(@PathVariable Long id) {
        return ResponseEntity.ok(countTaskRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Count task not found")));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CYCLE_COUNT_EXECUTE')")
    public ResponseEntity<CountTask> createAdHoc(@RequestBody AdHocRequest req) {
        return ResponseEntity.ok(cycleCountService.createAdHocTask(
                req.getZoneId(), req.getBinId(), req.getSkuId(), req.getAssignedToId()
        ));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAuthority('CYCLE_COUNT_EXECUTE')")
    public ResponseEntity<CountTask> startTask(@PathVariable Long id) {
        return ResponseEntity.ok(cycleCountService.startCountTask(id));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('CYCLE_COUNT_EXECUTE')")
    public ResponseEntity<CountTask> submitCounts(@PathVariable Long id, @RequestBody List<CycleCountService.CountSubmitRequest> counts) {
        return ResponseEntity.ok(cycleCountService.submitCount(id, counts));
    }

    @GetMapping("/variance-review")
    public ResponseEntity<List<CountLine>> getVarianceReview() {
        // Return lines that have non-zero variance and are pending
        List<CountLine> pendingLines = countLineRepository.findAll().stream()
                .filter(l -> l.getStatus() == CountLine.LineStatus.PENDING && l.getVariance() != 0)
                .toList();
        return ResponseEntity.ok(pendingLines);
    }

    @PostMapping("/lines/{lineId}/approve")
    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
    public ResponseEntity<CountLine> approveLine(
            @PathVariable Long lineId,
            @RequestParam(required = false) Long userId) {
        Long finalUserId = userId;
        if (finalUserId == null) {
            String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            finalUserId = userRepository.findByUsername(username)
                    .map(com.warehouse.wms.entity.User::getId)
                    .orElse(1L);
        }
        return ResponseEntity.ok(cycleCountService.approveLine(lineId, finalUserId));
    }

    @PostMapping("/lines/{lineId}/reject")
    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
    public ResponseEntity<CountLine> rejectLine(
            @PathVariable Long lineId,
            @RequestParam(required = false) Long userId) {
        Long finalUserId = userId;
        if (finalUserId == null) {
            String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            finalUserId = userRepository.findByUsername(username)
                    .map(com.warehouse.wms.entity.User::getId)
                    .orElse(1L);
        }
        return ResponseEntity.ok(cycleCountService.rejectLine(lineId, supervisorId(finalUserId)));
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        List<CountLine> allLines = countLineRepository.findAll();
        long totalLines = allLines.size();
        long accurateLines = allLines.stream().filter(l -> l.getVariance() == 0).count();
        double accuracyPct = totalLines > 0 ? ((double) accurateLines / totalLines) * 100 : 100.0;

        List<StockAdjustment> adjustments = stockAdjustmentRepository.findAll();

        Map<String, Object> data = new HashMap<>();
        data.put("accuracyPct", accuracyPct);
        data.put("totalCountsCounted", totalLines);
        data.put("totalAdjustmentsCount", adjustments.size());
        data.put("recentAdjustments", adjustments.stream().limit(10).toList());

        return ResponseEntity.ok(data);
    }

    private Long supervisorId(Long id) {
        return id != null ? id : 1L; // Fallback
    }

    @Data
    public static class AdHocRequest {
        private Long zoneId;
        private Long binId;
        private Long skuId;
        private Long assignedToId;
    }
}
