// ====== FILE: src/main/java/com/warehouse/wms/controller/ZoneBarcodeController.java ======
package com.warehouse.wms.controller;

import com.warehouse.wms.service.ZoneBarcodeService;
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
@RequestMapping("/api/barcodes/zones")
@RequiredArgsConstructor
@Slf4j
public class ZoneBarcodeController {

    private final ZoneBarcodeService zoneBarcodeService;

    @PostMapping("/{zoneId}")
    @Operation(summary = "Generate barcode for a zone by ID")
    public ResponseEntity<String> generateZoneBarcodeById(
            @Parameter(description = "Zone ID", required = true)
            @PathVariable Long zoneId) {
        
        log.info("POST /api/barcodes/zones/{} - Generate zone barcode", zoneId);
        String barcodeBase64 = zoneBarcodeService.generateZoneBarcode(zoneId);
        
        if (barcodeBase64 != null) {
            return ResponseEntity.ok(barcodeBase64);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate barcode");
        }
    }

    @PostMapping("/{warehouseId}/{zoneId}")
    @Operation(summary = "Generate barcode for a zone by warehouse and zone IDs")
    public ResponseEntity<String> generateZoneBarcode(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable String warehouseId,
            @Parameter(description = "Zone ID", required = true)
            @PathVariable String zoneId) {
        
        log.info("POST /api/barcodes/zones/{}/{} - Generate zone barcode", warehouseId, zoneId);
        String barcodeBase64 = zoneBarcodeService.generateZoneBarcode(warehouseId, zoneId);
        
        if (barcodeBase64 != null) {
            return ResponseEntity.ok(barcodeBase64);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate barcode");
        }
    }

    @PostMapping("/{zoneId}/with-label")
    @Operation(summary = "Generate barcode with custom label for a zone")
    public ResponseEntity<String> generateZoneBarcodeWithLabel(
            @Parameter(description = "Zone ID", required = true)
            @PathVariable Long zoneId,
            @RequestParam(defaultValue = "") String label) {
        
        log.info("POST /api/barcodes/zones/{}/with-label - Generate zone barcode with label", zoneId);
        String barcodeBase64 = zoneBarcodeService.generateZoneBarcodeWithLabel(zoneId, 
                label.isEmpty() ? null : label);
        
        if (barcodeBase64 != null) {
            return ResponseEntity.ok(barcodeBase64);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate barcode");
        }
    }

    @GetMapping("/{zoneId}/base64")
    @Operation(summary = "Get zone barcode as Base64")
    public ResponseEntity<String> getZoneBarcodeBase64(
            @Parameter(description = "Zone ID", required = true)
            @PathVariable Long zoneId) {
        
        log.info("GET /api/barcodes/zones/{}/base64 - Get zone barcode as Base64", zoneId);
        String barcodeBase64 = zoneBarcodeService.getZoneBarcodeBase64(zoneId);
        return ResponseEntity.ok(barcodeBase64);
    }

    @GetMapping("/{zoneId}/datauri")
    @Operation(summary = "Get zone barcode as Data URI")
    public ResponseEntity<String> getZoneBarcodeDataURI(
            @Parameter(description = "Zone ID", required = true)
            @PathVariable Long zoneId) {
        
        log.info("GET /api/barcodes/zones/{}/datauri - Get zone barcode as Data URI", zoneId);
        String dataUri = zoneBarcodeService.getZoneBarcodeDataURI(zoneId);
        return ResponseEntity.ok(dataUri);
    }

    @GetMapping(value = "/{zoneId}/image", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Get zone barcode as image")
    public ResponseEntity<byte[]> getZoneBarcodeImage(
            @Parameter(description = "Zone ID", required = true)
            @PathVariable Long zoneId) {
        
        log.info("GET /api/barcodes/zones/{}/image - Get zone barcode image", zoneId);
        byte[] barcodeBytes = zoneBarcodeService.getZoneBarcodeBytes(zoneId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDispositionFormData("attachment", "zone-" + zoneId + "-barcode.png");
        
        return new ResponseEntity<>(barcodeBytes, headers, HttpStatus.OK);
    }

    @PutMapping("/{zoneId}/regenerate")
    @Operation(summary = "Regenerate zone barcode")
    public ResponseEntity<String> regenerateZoneBarcode(
            @Parameter(description = "Zone ID", required = true)
            @PathVariable Long zoneId) {
        
        log.info("PUT /api/barcodes/zones/{}/regenerate - Regenerate zone barcode", zoneId);
        boolean success = zoneBarcodeService.regenerateZoneBarcode(zoneId);
        
        if (success) {
            return ResponseEntity.ok("Barcode regenerated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to regenerate barcode");
        }
    }

    @PostMapping("/batch/all")
    @Operation(summary = "Generate barcodes for all zones")
    public ResponseEntity<String> generateBarcodesForAllZones() {
        log.info("POST /api/barcodes/zones/batch/all - Generate barcodes for all zones");
        
        int count = zoneBarcodeService.generateBarcodesForAllZones();
        return ResponseEntity.ok("Successfully generated barcodes for " + count + " zones");
    }

    @PostMapping("/batch/warehouse/{warehouseId}")
    @Operation(summary = "Generate barcodes for all zones in a warehouse")
    public ResponseEntity<String> generateBarcodesForWarehouseZones(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable String warehouseId) {
        
        log.info("POST /api/barcodes/zones/batch/warehouse/{} - Generate barcodes for warehouse zones", warehouseId);
        
        int count = zoneBarcodeService.generateBarcodesForWarehouseZones(warehouseId);
        return ResponseEntity.ok("Successfully generated barcodes for " + count + " zones in warehouse " + warehouseId);
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate custom barcode for zone")
    public ResponseEntity<String> generateCustomBarcode(
            @RequestParam String data,
            @RequestParam(required = false, defaultValue = "") String label,
            @RequestParam(required = false, defaultValue = "CODE128") String format) {
        
        log.info("POST /api/barcodes/zones/generate - Generate custom zone barcode");
        String barcodeBase64 = zoneBarcodeService.generateZoneBarcodeWithFormat(data, label, format);
        
        if (barcodeBase64 != null) {
            return ResponseEntity.ok(barcodeBase64);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate barcode");
        }
    }
}