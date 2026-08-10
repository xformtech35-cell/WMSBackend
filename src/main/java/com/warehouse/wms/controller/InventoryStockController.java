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

import com.warehouse.wms.dto.request.InventorySearchRequest;
import com.warehouse.wms.dto.request.InventoryStockRequest;
import com.warehouse.wms.dto.response.InventoryStockResponse;
import com.warehouse.wms.service.InventoryStockService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/inventory-stock")
@RequiredArgsConstructor
@Slf4j
public class InventoryStockController {

    private final InventoryStockService inventoryStockService;

    @PostMapping
    public ResponseEntity<InventoryStockResponse> createStock(@Valid @RequestBody InventoryStockRequest request) {
        log.info("POST /api/inventory-stock - Create inventory stock");
        InventoryStockResponse response = inventoryStockService.createStock(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryStockResponse> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody InventoryStockRequest request) {
        log.info("PUT /api/inventory-stock/{} - Update inventory stock", id);
        InventoryStockResponse response = inventoryStockService.updateStock(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryStockResponse> getStockById(@PathVariable Long id) {
        log.info("GET /api/inventory-stock/{} - Get inventory stock by ID", id);
        InventoryStockResponse response = inventoryStockService.getStockById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{inventoryNumber}")
    public ResponseEntity<InventoryStockResponse> getStockByInventoryNumber(@PathVariable String inventoryNumber) {
        log.info("GET /api/inventory-stock/number/{} - Get inventory stock by number", inventoryNumber);
        InventoryStockResponse response = inventoryStockService.getStockByInventoryNumber(inventoryNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/item/{itemCode}/bin/{binId}")
    public ResponseEntity<InventoryStockResponse> getStockByItemAndBin(
            @PathVariable String itemCode,
            @PathVariable String binId) {
        log.info("GET /api/inventory-stock/item/{}/bin/{} - Get stock by item and bin", itemCode, binId);
        InventoryStockResponse response = inventoryStockService.getStockByItemAndBin(itemCode, binId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<InventoryStockResponse>> getAllStocks(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/inventory-stock - Get all inventory stocks");
        Page<InventoryStockResponse> responses = inventoryStockService.getAllStocks(pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/item/{itemCode}")
    public ResponseEntity<List<InventoryStockResponse>> getStocksByItemCode(@PathVariable String itemCode) {
        log.info("GET /api/inventory-stock/item/{} - Get stocks by item code", itemCode);
        List<InventoryStockResponse> responses = inventoryStockService.getStocksByItemCode(itemCode);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/bin/{binId}")
    public ResponseEntity<List<InventoryStockResponse>> getStocksByBinId(@PathVariable String binId) {
        log.info("GET /api/inventory-stock/bin/{} - Get stocks by bin ID", binId);
        List<InventoryStockResponse> responses = inventoryStockService.getStocksByBinId(binId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<InventoryStockResponse>> getStocksByWarehouseId(@PathVariable String warehouseId) {
        log.info("GET /api/inventory-stock/warehouse/{} - Get stocks by warehouse ID", warehouseId);
        List<InventoryStockResponse> responses = inventoryStockService.getStocksByWarehouseId(warehouseId);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/search")
    public ResponseEntity<Page<InventoryStockResponse>> searchStocks(
            @RequestBody InventorySearchRequest searchRequest,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("POST /api/inventory-stock/search - Search inventory stocks");
        Page<InventoryStockResponse> responses = inventoryStockService.searchStocks(searchRequest, pageable);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStock(@PathVariable Long id) {
        log.info("DELETE /api/inventory-stock/{} - Delete inventory stock", id);
        inventoryStockService.deleteStock(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/add-quantity")
    public ResponseEntity<Void> addQuantity(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        log.info("PATCH /api/inventory-stock/{}/add-quantity - Add quantity: {}", id, quantity);
        inventoryStockService.addQuantity(id, quantity);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/remove-quantity")
    public ResponseEntity<Void> removeQuantity(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        log.info("PATCH /api/inventory-stock/{}/remove-quantity - Remove quantity: {}", id, quantity);
        inventoryStockService.removeQuantity(id, quantity);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/reserve")
    public ResponseEntity<Void> reserveStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        log.info("PATCH /api/inventory-stock/{}/reserve - Reserve quantity: {}", id, quantity);
        inventoryStockService.reserveStock(id, quantity);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/unreserve")
    public ResponseEntity<Void> unreserveStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        log.info("PATCH /api/inventory-stock/{}/unreserve - Unreserve quantity: {}", id, quantity);
        inventoryStockService.unreserveStock(id, quantity);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStockStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        log.info("PATCH /api/inventory-stock/{}/status - Update status to: {}", id, status);
        inventoryStockService.updateStockStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/freeze")
    public ResponseEntity<Void> freezeStock(@PathVariable Long id) {
        log.info("PATCH /api/inventory-stock/{}/freeze - Freeze stock", id);
        inventoryStockService.freezeStock(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/unfreeze")
    public ResponseEntity<Void> unfreezeStock(@PathVariable Long id) {
        log.info("PATCH /api/inventory-stock/{}/unfreeze - Unfreeze stock", id);
        inventoryStockService.unfreezeStock(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryStockResponse>> getLowStockItems() {
        log.info("GET /api/inventory-stock/low-stock - Get low stock items");
        List<InventoryStockResponse> responses = inventoryStockService.getLowStockItems();
        return ResponseEntity.ok(responses);
    }
}