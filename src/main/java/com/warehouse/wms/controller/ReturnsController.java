package com.warehouse.wms.controller;

import com.warehouse.wms.dto.*;
import com.warehouse.wms.entity.ReturnOrder;
import com.warehouse.wms.service.ReturnsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.warehouse.wms.repository.UserRepository;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('INVENTORY_VIEW')")
public class ReturnsController {

    private final ReturnsService returnsService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
    public ResponseEntity<ReturnOrderResponse> create(@Valid @RequestBody ReturnOrderRequest request) {
        return ResponseEntity.ok(returnsService.createReturnOrder(request));
    }

    @GetMapping
    public ResponseEntity<List<ReturnOrderResponse>> list() {
        return ResponseEntity.ok(returnsService.listReturnOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReturnOrderResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(returnsService.getReturnOrder(id));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
    public ResponseEntity<ReturnOrderResponse> updateStatus(@PathVariable Long id, @RequestParam ReturnOrder.ReturnStatus status) {
        return ResponseEntity.ok(returnsService.updateStatus(id, status));
    }

    @PutMapping("/{id}/lines/{lineId}/grade")
    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
    public ResponseEntity<ReturnOrderResponse> gradeLine(
            @PathVariable Long id,
            @PathVariable Long lineId,
            @Valid @RequestBody GradeLineRequest request) {
        return ResponseEntity.ok(returnsService.gradeLine(id, lineId, request));
    }

    @PostMapping("/{id}/lines/{lineId}/restock")
    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
    public ResponseEntity<ReturnOrderResponse> restockLine(
            @PathVariable Long id,
            @PathVariable Long lineId,
            @Valid @RequestBody RestockLineRequest request,
            @RequestParam(required = false) Long userId) {
        Long finalUserId = userId;
        if (finalUserId == null) {
            String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            finalUserId = userRepository.findByUsername(username)
                    .map(com.warehouse.wms.entity.User::getId)
                    .orElse(1L);
        }
        return ResponseEntity.ok(returnsService.restockLine(id, lineId, request, finalUserId));
    }
}
