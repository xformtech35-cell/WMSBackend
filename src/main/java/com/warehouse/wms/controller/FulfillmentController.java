package com.warehouse.wms.controller;

import com.warehouse.wms.dto.FulfillmentPendingSummary;
import com.warehouse.wms.repository.InventoryRepository;
import com.warehouse.wms.repository.PickTaskRepository;
import com.warehouse.wms.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fulfillment")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
public class FulfillmentController {

    private final PickTaskRepository pickTaskRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final InventoryRepository inventoryRepository;

    @GetMapping("/pending")
    public ResponseEntity<FulfillmentPendingSummary> pending() {
        long pendingPickTasks = pickTaskRepository.countByStatus("PENDING");
        long ordersCreated = salesOrderRepository.countByStatus("CREATED");
        long ordersReserved = salesOrderRepository.countByStatus("RESERVED");
        long ordersPacked = salesOrderRepository.countByStatus("PACKED");
        long inventoryReserved = inventoryRepository.countByState(com.warehouse.wms.entity.Inventory.InventoryState.RESERVED);
        long inventoryPacked = inventoryRepository.countByState(com.warehouse.wms.entity.Inventory.InventoryState.PACKED);

        FulfillmentPendingSummary summary = FulfillmentPendingSummary.builder()
                .pendingPickTasks(pendingPickTasks)
                .ordersCreatedOrReserved(ordersCreated + ordersReserved)
                .ordersPackedNotShipped(ordersPacked)
                .inventoryReserved(inventoryReserved)
                .inventoryPacked(inventoryPacked)
                .build();

        return ResponseEntity.ok(summary);
    }
}
