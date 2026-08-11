// ====== FILE: src/main/java/com/warehouse/wms/controller/BarcodeController.java ======
package com.warehouse.wms.controller;

import com.warehouse.wms.service.BarcodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/barcodes")
@RequiredArgsConstructor
@Slf4j
public class BarcodeController {

    private final BarcodeService barcodeService;

    @PostMapping("/warehouse/{warehouseId}")
    @Operation(summary = "Generate barcode for a warehouse")
    public ResponseEntity<String> generateWarehouseBarcode(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable String warehouseId) {
        
        log.info("POST /api/barcodes/warehouse/{} - Generate warehouse barcode", warehouseId);
        String barcodeBase64 = barcodeService.generateWarehouseBarcode(warehouseId);
        
        if (barcodeBase64 != null) {
            return ResponseEntity.ok(barcodeBase64);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate barcode");
        }
    }

    @PostMapping("/warehouse/{warehouseId}/with-label")
    @Operation(summary = "Generate barcode with custom label for a warehouse")
    public ResponseEntity<String> generateWarehouseBarcodeWithLabel(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable String warehouseId,
            @RequestParam(defaultValue = "") String label) {
        
        log.info("POST /api/barcodes/warehouse/{}/with-label - Generate warehouse barcode with label", warehouseId);
        String barcodeBase64 = barcodeService.generateWarehouseBarcode(warehouseId, 
                label.isEmpty() ? "Warehouse: " + warehouseId : label);
        
        if (barcodeBase64 != null) {
            return ResponseEntity.ok(barcodeBase64);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate barcode");
        }
    }

    @GetMapping("/warehouse/{warehouseId}/base64")
    @Operation(summary = "Get warehouse barcode as Base64")
    public ResponseEntity<String> getWarehouseBarcodeBase64(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable String warehouseId) {
        
        log.info("GET /api/barcodes/warehouse/{}/base64 - Get warehouse barcode as Base64", warehouseId);
        String barcodeBase64 = barcodeService.getWarehouseBarcodeBase64(warehouseId);
        return ResponseEntity.ok(barcodeBase64);
    }

    @GetMapping("/warehouse/{warehouseId}/datauri")
    @Operation(summary = "Get warehouse barcode as Data URI")
    public ResponseEntity<String> getWarehouseBarcodeDataURI(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable String warehouseId) {
        
        log.info("GET /api/barcodes/warehouse/{}/datauri - Get warehouse barcode as Data URI", warehouseId);
        String dataUri = barcodeService.getWarehouseBarcodeDataURI(warehouseId);
        return ResponseEntity.ok(dataUri);
    }

    @GetMapping(value = "/warehouse/{warehouseId}/image", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Get warehouse barcode as image")
    public ResponseEntity<byte[]> getWarehouseBarcodeImage(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable String warehouseId) {
        
        log.info("GET /api/barcodes/warehouse/{}/image - Get warehouse barcode image", warehouseId);
        byte[] barcodeBytes = barcodeService.getWarehouseBarcodeBytes(warehouseId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDispositionFormData("attachment", "warehouse-" + warehouseId + "-barcode.png");
        
        return new ResponseEntity<>(barcodeBytes, headers, HttpStatus.OK);
    }

    @PutMapping("/warehouse/{warehouseId}/regenerate")
    @Operation(summary = "Regenerate warehouse barcode")
    public ResponseEntity<String> regenerateWarehouseBarcode(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable Long warehouseId) {
        
        log.info("PUT /api/barcodes/warehouse/{}/regenerate - Regenerate warehouse barcode", warehouseId);
        boolean success = barcodeService.regenerateWarehouseBarcode(warehouseId);
        
        if (success) {
            return ResponseEntity.ok("Barcode regenerated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to regenerate barcode");
        }
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate barcode with custom format")
    public ResponseEntity<String> generateCustomBarcode(
            @RequestParam String data,
            @RequestParam(required = false, defaultValue = "") String label,
            @RequestParam(required = false, defaultValue = "CODE128") String format) {
        
        log.info("POST /api/barcodes/generate - Generate custom barcode");
        String barcodeBase64 = barcodeService.generateBarcodeWithFormat(data, label, format);
        
        if (barcodeBase64 != null) {
            return ResponseEntity.ok(barcodeBase64);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate barcode");
        }
    }

    @PostMapping("/batch/warehouse")
    @Operation(summary = "Generate barcodes for all warehouses")
    public ResponseEntity<String> generateBarcodesForAllWarehouses() {
        log.info("POST /api/barcodes/batch/warehouse - Generate barcodes for all warehouses");
        
        // Implementation to generate barcodes for all warehouses
        // You can add this method to BarcodeService
        return ResponseEntity.ok("Batch generation initiated");
    }
}