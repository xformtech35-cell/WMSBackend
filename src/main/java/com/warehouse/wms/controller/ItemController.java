package com.warehouse.wms.controller;

import com.warehouse.wms.dto.ApiResponse;
import com.warehouse.wms.dto.CreateItemDTO;
import com.warehouse.wms.dto.ItemDTO;
import com.warehouse.wms.dto.ItemFilterDTO;
import com.warehouse.wms.dto.UpdateItemDTO;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ItemController {
    
    private final ItemService itemService;
    
    // ============ CREATE ============
    
    @PostMapping
    public ResponseEntity<ApiResponse<ItemDTO>> createItem(@Valid @RequestBody CreateItemDTO createItemDTO) {
        try {
            ItemDTO created = itemService.createItem(createItemDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item created successfully", created));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating item", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error creating item: " + e.getMessage()));
        }
    }
    
    // ============ READ ============
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemDTO>> getItemById(@PathVariable Long id) {
        try {
            ItemDTO item = itemService.getItemById(id);
            return ResponseEntity.ok(ApiResponse.success(item));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting item", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error retrieving item"));
        }
    }
    
    @GetMapping("/code/{itemCode}")
    public ResponseEntity<ApiResponse<ItemDTO>> getItemByCode(@PathVariable String itemCode) {
        try {
            ItemDTO item = itemService.getItemByCode(itemCode);
            return ResponseEntity.ok(ApiResponse.success(item));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting item by code", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error retrieving item"));
        }
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ItemDTO>>> getAllItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "itemName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<ItemDTO> items = itemService.getAllItems(pageable);
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            log.error("Error getting items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error retrieving items"));
        }
    }
    
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Page<ItemDTO>>> searchItems(
            @RequestBody ItemFilterDTO filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "itemName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<ItemDTO> items = itemService.searchItems(filter, pageable);
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            log.error("Error searching items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error searching items"));
        }
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ItemDTO>>> getItemsByCategory(@PathVariable String category) {
        try {
            List<ItemDTO> items = itemService.getItemsByCategory(category);
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            log.error("Error getting items by category", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error retrieving items"));
        }
    }
    
    @GetMapping("/brand/{brand}")
    public ResponseEntity<ApiResponse<List<ItemDTO>>> getItemsByBrand(@PathVariable String brand) {
        try {
            List<ItemDTO> items = itemService.getItemsByBrand(brand);
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            log.error("Error getting items by brand", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error retrieving items"));
        }
    }
    
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ItemDTO>>> getActiveItems() {
        try {
            List<ItemDTO> items = itemService.getActiveItems();
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            log.error("Error getting active items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error retrieving active items"));
        }
    }
    
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<ItemDTO>>> getLowStockItems() {
        try {
            List<ItemDTO> items = itemService.getLowStockItems();
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            log.error("Error getting low stock items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error retrieving low stock items"));
        }
    }
    
    @GetMapping("/reorder")
    public ResponseEntity<ApiResponse<List<ItemDTO>>> getItemsNeedingReorder() {
        try {
            List<ItemDTO> items = itemService.getItemsNeedingReorder();
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            log.error("Error getting reorder items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error retrieving reorder items"));
        }
    }
    
    @GetMapping("/gst-applicable")
    public ResponseEntity<ApiResponse<List<ItemDTO>>> getGstApplicableItems() {
        try {
            List<ItemDTO> items = itemService.getGstApplicableItems();
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            log.error("Error getting GST applicable items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error retrieving GST applicable items"));
        }
    }
    
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<ApiResponse<List<ItemDTO>>> getItemsBySupplier(@PathVariable Long supplierId) {
        try {
            List<ItemDTO> items = itemService.getItemsBySupplier(supplierId);
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            log.error("Error getting items by supplier", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error retrieving items by supplier"));
        }
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Object>> getStatistics() {
        try {
            Object stats = itemService.getStatistics();
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            log.error("Error getting statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error retrieving statistics"));
        }
    }
    
    // ============ UPDATE ============
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemDTO>> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateItemDTO updateItemDTO) {
        try {
            updateItemDTO.setId(id);
            ItemDTO updated = itemService.updateItem(id, updateItemDTO);
            return ResponseEntity.ok(ApiResponse.success("Item updated successfully", updated));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating item", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error updating item: " + e.getMessage()));
        }
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemDTO>> patchItem(
            @PathVariable Long id,
            @RequestBody CreateItemDTO patchDTO) {
        try {
            ItemDTO patched = itemService.patchItem(id, patchDTO);
            return ResponseEntity.ok(ApiResponse.success("Item patched successfully", patched));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error patching item", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error patching item: " + e.getMessage()));
        }
    }
    
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<ItemDTO>> updateStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        try {
            ItemDTO item = itemService.updateStock(id, quantity);
            return ResponseEntity.ok(ApiResponse.success("Stock updated successfully", item));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating stock", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error updating stock"));
        }
    }
    
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<ItemDTO>> toggleItemStatus(@PathVariable Long id) {
        try {
            ItemDTO item = itemService.toggleItemStatus(id);
            return ResponseEntity.ok(ApiResponse.success("Item status toggled successfully", item));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error toggling item status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error toggling item status"));
        }
    }
    
    // ============ DELETE ============
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        try {
            itemService.deleteItem(id);
            return ResponseEntity.ok(ApiResponse.success("Item deleted successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting item", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error deleting item"));
        }
    }
    
    @PatchMapping("/{id}/soft-delete")
    public ResponseEntity<ApiResponse<ItemDTO>> softDeleteItem(@PathVariable Long id) {
        try {
            ItemDTO item = itemService.softDeleteItem(id);
            return ResponseEntity.ok(ApiResponse.success("Item soft deleted successfully", item));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error soft deleting item", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error soft deleting item"));
        }
    }
}