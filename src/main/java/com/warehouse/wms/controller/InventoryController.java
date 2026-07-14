//package com.warehouse.wms.controller;
//
//import com.warehouse.wms.entity.Bin;
//import com.warehouse.wms.entity.Inventory;
//import com.warehouse.wms.entity.PurchaseRequestItem;
//import com.warehouse.wms.entity.Sku;
//import com.warehouse.wms.repository.*;
//import com.warehouse.wms.service.InventorySpecification;
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Collections;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Locale;
//
//@RestController
//@RequestMapping("/api/inventory")
//@RequiredArgsConstructor
//@PreAuthorize("hasAuthority('INVENTORY_VIEW')")
//public class InventoryController {
//
//    private final InventoryRepository inventoryRepository;
//    private final SkuRepository skuRepository;
//    private final BinRepository binRepository;
//    private final WarehouseRepository warehouseRepository;
//    private final PurchaseRequestItemRepository purchaseRequestItemRepository;
//
//    @GetMapping
//    public Map<String, Object> list(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @RequestParam(defaultValue = "") String search,
//            @RequestParam(defaultValue = "") String state,
//            @RequestParam(defaultValue = "") String warehouse) {
//
//        Specification<Inventory> spec = Specification.where(InventorySpecification.withDynamicQuery(search, state, warehouse));
//        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt", "createdAt"));
//
//        Page<Inventory> inventoryPage = inventoryRepository.findAll(spec, pageable);
//
//        List<Map<String, Object>> items = inventoryPage.getContent().stream()
//                .map(this::toInventoryView)
//                .toList();
//
//        Map<String, Object> result = new LinkedHashMap<>();
//        result.put("content", items);
//        result.put("totalElements", inventoryPage.getTotalElements());
//        result.put("totalPages", inventoryPage.getTotalPages());
//        result.put("number", inventoryPage.getNumber());
//        result.put("hasNext", inventoryPage.hasNext());
//        result.put("page", page);
//        result.put("size", size);
//        return result;
//    }
//
//    @GetMapping("/{id}")
//    public Map<String, Object> getById(@PathVariable Long id) {
//        Inventory item = inventoryRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Inventory not found: " + id));
//        return toInventoryView(item);
//    }
//
//    @GetMapping("/meta")
//    public Map<String, Object> meta() {
//        List<Map<String, Object>> skus = skuRepository.findAll().stream()
//                .map(s -> {
//                    Map<String, Object> m = new LinkedHashMap<>();
//                    m.put("id", s.getId());
//                    m.put("skuCode", s.getSkuCode());
//                    m.put("description", s.getDescription());
//                    m.put("isPerishable", s.getIsPerishable() != null ? s.getIsPerishable() : false);
//                    return m;
//                })
//                .toList();
//
//        List<Map<String, Object>> bins = binRepository.findAll().stream()
//                .map(b -> {
//                    Map<String, Object> m = new LinkedHashMap<>();
//                    m.put("id", b.getId());
//                    m.put("barcode", b.getBarcode());
//                    return m;
//                })
//                .toList();
//
//        List<Map<String, Object>> warehouses = warehouseRepository.findAll().stream()
//                .map(w -> {
//                    Map<String, Object> m = new LinkedHashMap<>();
//                    m.put("id", w.getId());
//                    m.put("name", w.getName());
//                    return m;
//                })
//                .toList();
//
//        Map<String, Object> result = new LinkedHashMap<>();
//        result.put("skus", skus);
//        result.put("bins", bins);
//        result.put("warehouses", warehouses);
//        result.put("states", java.util.Arrays.stream(Inventory.InventoryState.values()).map(Enum::name).toList());
//        return result;
//    }
//
//    @PostMapping
//    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
//    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
//        Inventory inv = new Inventory();
//        applyInventoryFromBody(inv, body);
//        Inventory saved = inventoryRepository.save(inv);
//        return ResponseEntity.ok(toInventoryView(saved));
//    }
//
//    @PutMapping("/{id}")
//    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
//    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
//        Inventory inv = inventoryRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Inventory not found: " + id));
//        applyInventoryFromBody(inv, body);
//        Inventory saved = inventoryRepository.save(inv);
//        return ResponseEntity.ok(toInventoryView(saved));
//    }
//
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
//    public ResponseEntity<Void> delete(@PathVariable Long id) {
//        if (!inventoryRepository.existsById(id)) {
//            throw new EntityNotFoundException("Inventory not found: " + id);
//        }
//        inventoryRepository.deleteById(id);
//        return ResponseEntity.noContent().build();
//    }
//
//    @GetMapping("/summary")
//    public Map<String, Object> summary(
//            @RequestParam(defaultValue = "") String search,
//            @RequestParam(defaultValue = "") String state,
//            @RequestParam(defaultValue = "") String warehouse) {
//
//        Specification<Inventory> spec = Specification.where(InventorySpecification.withDynamicQuery(search, state, warehouse));
//
//        long totalItems = inventoryRepository.count(spec);
//        long totalQuantity = inventoryRepository.findAll(spec).stream().mapToLong(i -> i.getQuantity() != null ? i.getQuantity() : 0).sum(); // Full sum still requires loading
//        long availableQuantity = inventoryRepository.sumAvailableQuantity(spec);
//
//        Map<String, Object> result = new LinkedHashMap<>();
//        result.put("totalItems", totalItems);
//        result.put("totalQuantity", totalQuantity);
//        result.put("availableQuantity", availableQuantity);
//        return result;
//    }
//
//    @PostMapping("/adjust")
//    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
//    public ResponseEntity<Map<String, Object>> adjust(@RequestBody Map<String, Object> body) {
//        Long inventoryId = Long.valueOf(body.get("inventoryId").toString());
//        int quantity = Integer.parseInt(body.get("quantity").toString());
//        String reason = body.getOrDefault("reason", "MANUAL").toString();
//
//        Inventory inv = inventoryRepository.findById(inventoryId)
//                .orElseThrow(() -> new EntityNotFoundException("Inventory not found: " + inventoryId));
//        inv.setQuantity(quantity);
//        inventoryRepository.save(inv);
//
//        Map<String, Object> resp = new LinkedHashMap<>();
//        resp.put("inventoryId", inv.getId());
//        resp.put("newQuantity", inv.getQuantity());
//        resp.put("reason", reason);
//        return ResponseEntity.ok(resp);
//    }
//
//    @PostMapping("/bulk-state")
//    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
//    public ResponseEntity<Map<String, Object>> bulkUpdateState(@RequestBody Map<String, Object> body) {
//        List<Long> ids = ((List<?>) body.get("ids")).stream()
//                .map(id -> Long.valueOf(id.toString()))
//                .toList();
//        String stateStr = body.get("state").toString();
//        Inventory.InventoryState toState = Inventory.InventoryState.valueOf(stateStr.toUpperCase(Locale.ROOT));
//
//        int updatedCount = inventoryRepository.bulkUpdateState(ids, toState);
//
//        // Optionally, add movement logs here for each updated item
//
//        Map<String, Object> result = new LinkedHashMap<>();
//        result.put("updatedCount", updatedCount);
//        result.put("newState", toState.name());
//        return ResponseEntity.ok(result);
//    }
//
//    private void applyInventoryFromBody(Inventory inv, Map<String, Object> body) {
//        if (!body.containsKey("skuId")) {
//            throw new IllegalArgumentException("skuId is required");
//        }
//
//        Long skuId = Long.valueOf(body.get("skuId").toString());
//        Sku sku = skuRepository.findById(skuId)
//                .orElseThrow(() -> new EntityNotFoundException("SKU not found: " + skuId));
//        inv.setSku(sku);
//
//        Object binRaw = body.get("binId");
//        if (binRaw != null && !binRaw.toString().isBlank()) {
//            Long binId = Long.valueOf(binRaw.toString());
//            Bin bin = binRepository.findById(binId)
//                    .orElseThrow(() -> new EntityNotFoundException("Bin not found: " + binId));
//            inv.setBin(bin);
//        } else {
//            inv.setBin(null);
//        }
//
//        inv.setBatchNo(body.getOrDefault("batchNo", "").toString().trim());
//
//        String serialNo = body.getOrDefault("serialNo", body.getOrDefault("barcode", "")).toString().trim();
//        if (serialNo.isBlank()) {
//            throw new IllegalArgumentException("barcode is required");
//        }
//        inv.setSerialNo(serialNo);
//        applyPurchaseRequestItemDetails(inv, serialNo, body);
//
//        int quantity = Integer.parseInt(body.getOrDefault("quantity", "0").toString());
//        if (quantity < 0) {
//            throw new IllegalArgumentException("quantity must be 0 or greater");
//        }
//        inv.setQuantity(quantity);
//
//        String rawState = body.getOrDefault("state", Inventory.InventoryState.AVAILABLE.name()).toString();
//        inv.setState(Inventory.InventoryState.valueOf(rawState.toUpperCase(Locale.ROOT)));
//    }
//
//    private Map<String, Object> toInventoryView(Inventory i) {
//        PurchaseRequestItem requestItem = resolvePurchaseRequestItem(i);
//        String itemCode = resolveItemCode(i, requestItem);
//        String itemName = resolveItemName(i, requestItem);
//
//        Map<String, Object> m = new LinkedHashMap<>();
//        m.put("id", i.getId());
//        m.put("skuId", i.getSku() != null ? i.getSku().getId() : null);
//        m.put("skuCode", itemCode);
//        m.put("skuName", itemName);
//        m.put("itemCode", itemCode);
//        m.put("itemName", itemName);
//        m.put("barcode", i.getSerialNo());
//        m.put("serialNo", i.getSerialNo());
//        m.put("binId", i.getBin() != null ? i.getBin().getId() : null);
//        m.put("binBarcode", i.getBin() != null ? i.getBin().getBarcode() : null);
//        m.put("warehouseName", resolveWarehouseName(i));
//        m.put("batchNo", i.getBatchNo());
//        m.put("quantity", i.getQuantity());
//        m.put("state", i.getState() != null ? i.getState().name() : null);
//        m.put("createdAt", i.getCreatedAt());
//        m.put("updatedAt", i.getUpdatedAt());
//        return m;
//    }
//
//    private void applyPurchaseRequestItemDetails(Inventory inv, String itemBarcode, Map<String, Object> body) {
//        PurchaseRequestItem requestItem = null;
//        if (itemBarcode != null && !itemBarcode.isBlank()) {
//            requestItem = purchaseRequestItemRepository.findByItemBarcode(itemBarcode).orElse(null);
//        }
//
//        if (requestItem != null) {
//            inv.setPurchaseRequestItem(requestItem);
//            inv.setItemCode(requestItem.getItemCode());
//            inv.setItemName(requestItem.getItemName());
//            if (inv.getBatchNo() == null || inv.getBatchNo().isBlank()) {
//                inv.setBatchNo(requestItem.getBatchNo());
//            }
//            return;
//        }
//
//        Object itemCode = body.get("itemCode");
//        if (itemCode != null && !itemCode.toString().isBlank()) {
//            inv.setItemCode(itemCode.toString().trim());
//        }
//
//        Object itemName = body.get("itemName");
//        if (itemName != null && !itemName.toString().isBlank()) {
//            inv.setItemName(itemName.toString().trim());
//        } else if (inv.getItemName() == null || inv.getItemName().isBlank()) {
//            inv.setItemName(inv.getSku() != null ? inv.getSku().getDescription() : "");
//        }
//    }
//
//    private PurchaseRequestItem resolvePurchaseRequestItem(Inventory i) {
//        if (i.getPurchaseRequestItem() != null) {
//            return i.getPurchaseRequestItem();
//        }
//        if (hasText(i.getSerialNo())) {
//            return purchaseRequestItemRepository.findByItemBarcode(i.getSerialNo()).orElse(null);
//        }
//        return null;
//    }
//
//    private String resolveItemCode(Inventory i) {
//        return resolveItemCode(i, resolvePurchaseRequestItem(i));
//    }
//
//    private String resolveItemCode(Inventory i, PurchaseRequestItem requestItem) {
//        if (requestItem != null && hasText(requestItem.getItemCode())) {
//            return requestItem.getItemCode();
//        }
//        if (hasText(i.getItemCode())) {
//            return i.getItemCode();
//        }
//        return i.getSku() != null ? i.getSku().getSkuCode() : null;
//    }
//
//    private String resolveItemName(Inventory i) {
//        return resolveItemName(i, resolvePurchaseRequestItem(i));
//    }
//
//    private String resolveItemName(Inventory i, PurchaseRequestItem requestItem) {
//        if (requestItem != null && hasText(requestItem.getItemName())) {
//            return requestItem.getItemName();
//        }
//        if (hasText(i.getItemName())) {
//            return i.getItemName();
//        }
//        return i.getSku() != null ? i.getSku().getDescription() : null;
//    }
//
//    private boolean hasText(String value) {
//        return value != null && !value.isBlank();
//    }
//
//    private String resolveWarehouseName(Inventory i) {
//        if (i == null
//                || i.getBin() == null
//                || i.getBin().getRack() == null
//                || i.getBin().getRack().getAisle() == null
//                || i.getBin().getRack().getAisle().getZone() == null
//                || i.getBin().getRack().getAisle().getZone().getWarehouse() == null
//                || i.getBin().getRack().getAisle().getZone().getWarehouse().getName() == null) {
//            return "";
//        }
//        return i.getBin().getRack().getAisle().getZone().getWarehouse().getName();
//    }
//}
