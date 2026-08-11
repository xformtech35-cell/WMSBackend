// ====== FILE: src/main/java/com/warehouse/wms/controller/AisleBarcodeController.java ======
package com.warehouse.wms.controller;

import com.warehouse.wms.service.AisleBarcodeService;
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
@RequestMapping("/api/barcodes/aisles")
@RequiredArgsConstructor
@Slf4j
public class AisleBarcodeController {

    private final AisleBarcodeService aisleBarcodeService;

    @PostMapping("/{aisleId}")
    @Operation(summary = "Generate barcode for an aisle by ID")
    public ResponseEntity<String> generateAisleBarcodeById(
            @Parameter(description = "Aisle ID", required = true)
            @PathVariable Long aisleId) {
        
        log.info("POST /api/barcodes/aisles/{} - Generate aisle barcode", aisleId);
        String barcodeBase64 = aisleBarcodeService.generateAisleBarcode(aisleId);
        
        if (barcodeBase64 != null) {
            return ResponseEntity.ok(barcodeBase64);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate barcode");
        }
    }

    @PostMapping("/{warehouseId}/{zoneId}/{aisleId}")
    @Operation(summary = "Generate barcode for an aisle by warehouse, zone, and aisle IDs")
    public ResponseEntity<String> generateAisleBarcode(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable String warehouseId,
            @Parameter(description = "Zone ID", required = true)
            @PathVariable String zoneId,
            @Parameter(description = "Aisle ID", required = true)
            @PathVariable String aisleId) {
        
        log.info("POST /api/barcodes/aisles/{}/{}/{} - Generate aisle barcode", 
                warehouseId, zoneId, aisleId);
        String barcodeBase64 = aisleBarcodeService.generateAisleBarcode(warehouseId, zoneId, aisleId);
        
        if (barcodeBase64 != null) {
            return ResponseEntity.ok(barcodeBase64);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate barcode");
        }
    }

    @PostMapping("/{aisleId}/with-label")
    @Operation(summary = "Generate barcode with custom label for an aisle")
    public ResponseEntity<String> generateAisleBarcodeWithLabel(
            @Parameter(description = "Aisle ID", required = true)
            @PathVariable Long aisleId,
            @RequestParam(defaultValue = "") String label) {
        
        log.info("POST /api/barcodes/aisles/{}/with-label - Generate aisle barcode with label", aisleId);
        String barcodeBase64 = aisleBarcodeService.generateAisleBarcodeWithLabel(aisleId, 
                label.isEmpty() ? null : label);
        
        if (barcodeBase64 != null) {
            return ResponseEntity.ok(barcodeBase64);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate barcode");
        }
    }

    @GetMapping("/{aisleId}/base64")
    @Operation(summary = "Get aisle barcode as Base64")
    public ResponseEntity<String> getAisleBarcodeBase64(
            @Parameter(description = "Aisle ID", required = true)
            @PathVariable Long aisleId) {
        
        log.info("GET /api/barcodes/aisles/{}/base64 - Get aisle barcode as Base64", aisleId);
        String barcodeBase64 = aisleBarcodeService.getAisleBarcodeBase64(aisleId);
        return ResponseEntity.ok(barcodeBase64);
    }

    @GetMapping("/{aisleId}/datauri")
    @Operation(summary = "Get aisle barcode as Data URI")
    public ResponseEntity<String> getAisleBarcodeDataURI(
            @Parameter(description = "Aisle ID", required = true)
            @PathVariable Long aisleId) {
        
        log.info("GET /api/barcodes/aisles/{}/datauri - Get aisle barcode as Data URI", aisleId);
        String dataUri = aisleBarcodeService.getAisleBarcodeDataURI(aisleId);
        return ResponseEntity.ok(dataUri);
    }

    @GetMapping(value = "/{aisleId}/image", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Get aisle barcode as image")
    public ResponseEntity<byte[]> getAisleBarcodeImage(
            @Parameter(description = "Aisle ID", required = true)
            @PathVariable Long aisleId) {
        
        log.info("GET /api/barcodes/aisles/{}/image - Get aisle barcode image", aisleId);
        byte[] barcodeBytes = aisleBarcodeService.getAisleBarcodeBytes(aisleId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDispositionFormData("attachment", "aisle-" + aisleId + "-barcode.png");
        
        return new ResponseEntity<>(barcodeBytes, headers, HttpStatus.OK);
    }

    @PutMapping("/{aisleId}/regenerate")
    @Operation(summary = "Regenerate aisle barcode")
    public ResponseEntity<String> regenerateAisleBarcode(
            @Parameter(description = "Aisle ID", required = true)
            @PathVariable Long aisleId) {
        
        log.info("PUT /api/barcodes/aisles/{}/regenerate - Regenerate aisle barcode", aisleId);
        boolean success = aisleBarcodeService.regenerateAisleBarcode(aisleId);
        
        if (success) {
            return ResponseEntity.ok("Barcode regenerated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to regenerate barcode");
        }
    }

    @PostMapping("/batch/all")
    @Operation(summary = "Generate barcodes for all aisles")
    public ResponseEntity<String> generateBarcodesForAllAisles() {
        log.info("POST /api/barcodes/aisles/batch/all - Generate barcodes for all aisles");
        
        int count = aisleBarcodeService.generateBarcodesForAllAisles();
        return ResponseEntity.ok("Successfully generated barcodes for " + count + " aisles");
    }

    @PostMapping("/batch/zone/{warehouseId}/{zoneId}")
    @Operation(summary = "Generate barcodes for all aisles in a zone")
    public ResponseEntity<String> generateBarcodesForZoneAisles(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable String warehouseId,
            @Parameter(description = "Zone ID", required = true)
            @PathVariable String zoneId) {
        
        log.info("POST /api/barcodes/aisles/batch/zone/{}/{} - Generate barcodes for zone aisles", 
                warehouseId, zoneId);
        
        int count = aisleBarcodeService.generateBarcodesForZoneAisles(warehouseId, zoneId);
        return ResponseEntity.ok("Successfully generated barcodes for " + count + 
                " aisles in zone " + warehouseId + "-" + zoneId);
    }

    @PostMapping("/batch/warehouse/{warehouseId}")
    @Operation(summary = "Generate barcodes for all aisles in a warehouse")
    public ResponseEntity<String> generateBarcodesForWarehouseAisles(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable String warehouseId) {
        
        log.info("POST /api/barcodes/aisles/batch/warehouse/{} - Generate barcodes for warehouse aisles", 
                warehouseId);
        
        int count = aisleBarcodeService.generateBarcodesForWarehouseAisles(warehouseId);
        return ResponseEntity.ok("Successfully generated barcodes for " + count + 
                " aisles in warehouse " + warehouseId);
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate custom barcode for aisle")
    public ResponseEntity<String> generateCustomBarcode(
            @RequestParam String data,
            @RequestParam(required = false, defaultValue = "") String label,
            @RequestParam(required = false, defaultValue = "CODE128") String format) {
        
        log.info("POST /api/barcodes/aisles/generate - Generate custom aisle barcode");
        String barcodeBase64 = aisleBarcodeService.generateAisleBarcodeWithFormat(data, label, format);
        
        if (barcodeBase64 != null) {
            return ResponseEntity.ok(barcodeBase64);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate barcode");
        }
    }
}