package com.warehouse.wms.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.warehouse.wms.dto.InventoryResponse;
import com.warehouse.wms.dto.request.InventoryRequest;
import com.warehouse.wms.dto.request.InventorySearchRequest;
import com.warehouse.wms.service.InventoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(@Valid @RequestBody InventoryRequest request) {
        log.info("POST /api/inventory - Create inventory");
        InventoryResponse response = inventoryService.createInventory(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryResponse> updateInventory(
            @PathVariable Long id,
            @Valid @RequestBody InventoryRequest request) {
        log.info("PUT /api/inventory/{} - Update inventory", id);
        InventoryResponse response = inventoryService.updateInventory(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponse> getInventoryById(@PathVariable Long id) {
        log.info("GET /api/inventory/{} - Get inventory by ID", id);
        InventoryResponse response = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/serial/{serialNo}")
    public ResponseEntity<InventoryResponse> getInventoryBySerialNo(@PathVariable String serialNo) {
        log.info("GET /api/inventory/serial/{} - Get inventory by serial number", serialNo);
        InventoryResponse response = inventoryService.getInventoryBySerialNo(serialNo);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<InventoryResponse>> getAllInventories(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/inventory - Get all inventories");
        Page<InventoryResponse> responses = inventoryService.getAllInventories(pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/item/{itemCode}")
    public ResponseEntity<List<InventoryResponse>> getInventoriesByItemCode(@PathVariable String itemCode) {
        log.info("GET /api/inventory/item/{} - Get inventories by item code", itemCode);
        List<InventoryResponse> responses = inventoryService.getInventoriesByItemCode(itemCode);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/sku/{skuId}")
    public ResponseEntity<List<InventoryResponse>> getInventoriesBySkuId(@PathVariable Long skuId) {
        log.info("GET /api/inventory/sku/{} - Get inventories by SKU ID", skuId);
        List<InventoryResponse> responses = inventoryService.getInventoriesBySkuId(skuId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/bin/{binId}")
    public ResponseEntity<List<InventoryResponse>> getInventoriesByBinId(@PathVariable Long binId) {
        log.info("GET /api/inventory/bin/{} - Get inventories by bin ID", binId);
        List<InventoryResponse> responses = inventoryService.getInventoriesByBinId(binId);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/search")
    public ResponseEntity<Page<InventoryResponse>> searchInventories(
            @RequestBody InventorySearchRequest searchRequest,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("POST /api/inventory/search - Search inventories");
        Page<InventoryResponse> responses = inventoryService.searchInventories(searchRequest, pageable);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        log.info("DELETE /api/inventory/{} - Delete inventory", id);
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/state")
    public ResponseEntity<Void> updateInventoryState(
            @PathVariable Long id,
            @RequestParam String state) {
        log.info("PATCH /api/inventory/{}/state - Update inventory state to: {}", id, state);
        inventoryService.updateInventoryState(id, state);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/reserve")
    public ResponseEntity<Void> reserveInventory(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        log.info("PATCH /api/inventory/{}/reserve - Reserve quantity: {}", id, quantity);
        inventoryService.reserveInventory(id, quantity);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/unreserve")
    public ResponseEntity<Void> unreserveInventory(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        log.info("PATCH /api/inventory/{}/unreserve - Unreserve quantity: {}", id, quantity);
        inventoryService.unreserveInventory(id, quantity);
        return ResponseEntity.ok().build();
    }
}