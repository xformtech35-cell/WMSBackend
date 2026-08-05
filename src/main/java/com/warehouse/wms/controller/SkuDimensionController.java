// ====== FILE: src/main/java/com/warehouse/wms/controller/SkuDimensionController.java ======
package com.warehouse.wms.controller;

import com.warehouse.wms.dto.request.SkuDimensionRequest;
import com.warehouse.wms.dto.response.SkuDimensionResponse;
import com.warehouse.wms.dto.response.StandardResponse;
import com.warehouse.wms.service.SkuDimensionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/sku-dimensions")
@RequiredArgsConstructor
@Tag(name = "SKU Dimension Management", description = "APIs for managing SKU dimensions")
public class SkuDimensionController {

    private final SkuDimensionService skuDimensionService;

    @PostMapping("/sku/{skuId}")
    @Operation(summary = "Create dimension for SKU")
    public ResponseEntity<StandardResponse<SkuDimensionResponse>> createDimension(
            @PathVariable Long skuId,
            @Valid @RequestBody SkuDimensionRequest request) {
        log.info("📦 Creating dimension for SKU: {}", skuId);
        SkuDimensionResponse response = skuDimensionService.createDimension(skuId, request);
        return ResponseEntity.ok(StandardResponse.success("Dimension created successfully", response));
    }

    @GetMapping("/sku/{skuId}")
    @Operation(summary = "Get dimension by SKU ID")
    public ResponseEntity<StandardResponse<SkuDimensionResponse>> getDimensionBySkuId(
            @PathVariable Long skuId) {
        SkuDimensionResponse response = skuDimensionService.getDimensionBySkuId(skuId);
        return ResponseEntity.ok(StandardResponse.success("Dimension fetched successfully", response));
    }

    @PutMapping("/sku/{skuId}")
    @Operation(summary = "Update dimension for SKU")
    public ResponseEntity<StandardResponse<SkuDimensionResponse>> updateDimension(
            @PathVariable Long skuId,
            @Valid @RequestBody SkuDimensionRequest request) {
        SkuDimensionResponse response = skuDimensionService.updateDimension(skuId, request);
        return ResponseEntity.ok(StandardResponse.success("Dimension updated successfully", response));
    }

    @DeleteMapping("/sku/{skuId}")
    @Operation(summary = "Delete dimension by SKU ID")
    public ResponseEntity<StandardResponse<Void>> deleteDimensionBySkuId(
            @PathVariable Long skuId) {
        skuDimensionService.deleteDimensionBySkuId(skuId);
        return ResponseEntity.ok(StandardResponse.success("Dimension deleted successfully"));
    }
}