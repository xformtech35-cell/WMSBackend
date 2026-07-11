package com.warehouse.wms.controller;

import com.warehouse.wms.repository.PurchaseOrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.warehouse.wms.repository.SkuRepository;
import com.warehouse.wms.entity.PurchaseOrder;
import com.warehouse.wms.entity.PurchaseOrderLine;
import com.warehouse.wms.entity.Sku;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PURCHASE_VIEW')")
public class PurchaseOrderController {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SkuRepository skuRepository;

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
    public ResponseEntity<Map<String, Object>> createDraft(@RequestBody Map<String, Object> req) {
        String supplier = (String) req.getOrDefault("supplier", "AI Suggested Supplier");
        List<Map<String, Object>> linesReq = (List<Map<String, Object>>) req.get("lines");

        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber("PO-" + System.currentTimeMillis());
        po.setSupplier(supplier);
        po.setStatus("OPEN");
        po.setExpectedArrivalDate(LocalDate.now().plusDays(7));

        List<PurchaseOrderLine> lines = new java.util.ArrayList<>();
        for (var lineMap : linesReq) {
            String skuCode = (String) lineMap.get("skuCode");
            Integer qty = (Integer) lineMap.get("quantity");

            Sku sku = skuRepository.findBySkuCode(skuCode)
                    .orElseThrow(() -> new EntityNotFoundException("Sku not found: " + skuCode));

            PurchaseOrderLine line = new PurchaseOrderLine();
            line.setPurchaseOrder(po);
            line.setSku(sku);
            line.setQuantity(qty);
            lines.add(line);
        }
        po.setLines(lines);

        PurchaseOrder saved = purchaseOrderRepository.save(po);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", saved.getId());
        result.put("poNumber", saved.getPoNumber());
        result.put("supplier", saved.getSupplier());
        result.put("status", saved.getStatus());
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return purchaseOrderRepository.findAllSummary().stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", row[0]);
            m.put("poNumber", row[1]);
            m.put("supplier", row[2]);
            m.put("status", row[3]);
            m.put("lineCount", row[4]);
            m.put("expectedArrivalDate", row[5]);
            return m;
        }).toList();
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        var po = purchaseOrderRepository.findByIdWithLines(id)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found: " + id));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", po.getId());
        result.put("poNumber", po.getPoNumber());
        result.put("supplier", po.getSupplier());
        result.put("status", po.getStatus());
        result.put("expectedArrivalDate", po.getExpectedArrivalDate());
        result.put("lines", po.getLines().stream().map(line -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", line.getId());
            m.put("skuId", line.getSku().getId());
            m.put("skuCode", line.getSku().getSkuCode());
            m.put("orderedQuantity", line.getQuantity());
            return m;
        }).toList());
        return result;
    }
}
