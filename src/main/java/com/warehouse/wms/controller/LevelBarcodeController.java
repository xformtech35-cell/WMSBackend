// ====== FILE: src/main/java/com/warehouse/wms/controller/LevelBarcodeController.java ======
package com.warehouse.wms.controller;

import com.warehouse.wms.service.LevelBarcodeService;
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
@RequestMapping("/api/barcodes/levels")
@RequiredArgsConstructor
@Slf4j
public class LevelBarcodeController {

    private final LevelBarcodeService levelBarcodeService;

    @PostMapping("/{levelId}")
    @Operation(summary = "Generate barcode for a level by ID")
    public ResponseEntity<String> generateLevelBarcodeById(
            @Parameter(description = "Level ID", required = true)
            @PathVariable Long levelId) {
        
        log.info("POST /api/barcodes/levels/{} - Generate level barcode", levelId);
        String barcodeBase64 = levelBarcodeService.generateLevelBarcode(levelId);
        
        if (barcodeBase64 != null) {
            return ResponseEntity.ok(barcodeBase64);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate barcode");
        }
    }

    @PostMapping("/{warehouseId}/{zoneId}/{aisleId}/{rackId}/{levelId}")
    @Operation(summary = "Generate barcode for a level by full path")
    public ResponseEntity<String> generateLevelBarcode(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable String warehouseId,
            @Parameter(description = "Zone ID", required = true)
            @PathVariable String zoneId,
            @Parameter(description = "Aisle ID", required = true)
            @PathVariable String aisleId,
            @Parameter(description = "Rack ID", required = true)
            @PathVariable String rackId,
            @Parameter(description = "Level ID", required = true)
            @PathVariable String levelId) {
        
        log.info("POST /api/barcodes/levels/{}/{}/{}/{}/{} - Generate level barcode", 
                warehouseId, zoneId, aisleId, rackId, levelId);
        String barcodeBase64 = levelBarcodeService.generateLevelBarcode(warehouseId, zoneId, aisleId, rackId, levelId);
        
        if (barcodeBase64 != null) {
            return ResponseEntity.ok(barcodeBase64);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate barcode");
        }
    }

    @PostMapping("/{levelId}/with-label")
    @Operation(summary = "Generate barcode with custom label for a level")
    public ResponseEntity<String> generateLevelBarcodeWithLabel(
            @Parameter(description = "Level ID", required = true)
            @PathVariable Long levelId,
            @RequestParam(defaultValue = "") String label) {
        
        log.info("POST /api/barcodes/levels/{}/with-label - Generate level barcode with label", levelId);
        String barcodeBase64 = levelBarcodeService.generateLevelBarcodeWithLabel(levelId, 
                label.isEmpty() ? null : label);
        
        if (barcodeBase64 != null) {
            return ResponseEntity.ok(barcodeBase64);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate barcode");
        }
    }

    @GetMapping("/{levelId}/base64")
    @Operation(summary = "Get level barcode as Base64")
    public ResponseEntity<String> getLevelBarcodeBase64(
            @Parameter(description = "Level ID", required = true)
            @PathVariable Long levelId) {
        
        log.info("GET /api/barcodes/levels/{}/base64 - Get level barcode as Base64", levelId);
        String barcodeBase64 = levelBarcodeService.getLevelBarcodeBase64(levelId);
        return ResponseEntity.ok(barcodeBase64);
    }

    @GetMapping("/{levelId}/datauri")
    @Operation(summary = "Get level barcode as Data URI")
    public ResponseEntity<String> getLevelBarcodeDataURI(
            @Parameter(description = "Level ID", required = true)
            @PathVariable Long levelId) {
        
        log.info("GET /api/barcodes/levels/{}/datauri - Get level barcode as Data URI", levelId);
        String dataUri = levelBarcodeService.getLevelBarcodeDataURI(levelId);
        return ResponseEntity.ok(dataUri);
    }

    @GetMapping(value = "/{levelId}/image", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Get level barcode as image")
    public ResponseEntity<byte[]> getLevelBarcodeImage(
            @Parameter(description = "Level ID", required = true)
            @PathVariable Long levelId) {
        
        log.info("GET /api/barcodes/levels/{}/image - Get level barcode image", levelId);
        byte[] barcodeBytes = levelBarcodeService.getLevelBarcodeBytes(levelId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDispositionFormData("attachment", "level-" + levelId + "-barcode.png");
        
        return new ResponseEntity<>(barcodeBytes, headers, HttpStatus.OK);
    }

    @PutMapping("/{levelId}/regenerate")
    @Operation(summary = "Regenerate level barcode")
    public ResponseEntity<String> regenerateLevelBarcode(
            @Parameter(description = "Level ID", required = true)
            @PathVariable Long levelId) {
        
        log.info("PUT /api/barcodes/levels/{}/regenerate - Regenerate level barcode", levelId);
        boolean success = levelBarcodeService.regenerateLevelBarcode(levelId);
        
        if (success) {
            return ResponseEntity.ok("Barcode regenerated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to regenerate barcode");
        }
    }

    @PostMapping("/batch/all")
    @Operation(summary = "Generate barcodes for all levels")
    public ResponseEntity<String> generateBarcodesForAllLevels() {
        log.info("POST /api/barcodes/levels/batch/all - Generate barcodes for all levels");
        
        int count = levelBarcodeService.generateBarcodesForAllLevels();
        return ResponseEntity.ok("Successfully generated barcodes for " + count + " levels");
    }

    @PostMapping("/batch/rack/{warehouseId}/{zoneId}/{aisleId}/{rackId}")
    @Operation(summary = "Generate barcodes for all levels in a rack")
    public ResponseEntity<String> generateBarcodesForRackLevels(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable String warehouseId,
            @Parameter(description = "Zone ID", required = true)
            @PathVariable String zoneId,
            @Parameter(description = "Aisle ID", required = true)
            @PathVariable String aisleId,
            @Parameter(description = "Rack ID", required = true)
            @PathVariable String rackId) {
        
        log.info("POST /api/barcodes/levels/batch/rack/{}/{}/{}/{} - Generate barcodes for rack levels", 
                warehouseId, zoneId, aisleId, rackId);
        
        int count = levelBarcodeService.generateBarcodesForRackLevels(warehouseId, zoneId, aisleId, rackId);
        return ResponseEntity.ok("Successfully generated barcodes for " + count + 
                " levels in rack " + warehouseId + "-" + zoneId + "-" + aisleId + "-" + rackId);
    }

    @PostMapping("/batch/aisle/{warehouseId}/{zoneId}/{aisleId}")
    @Operation(summary = "Generate barcodes for all levels in an aisle")
    public ResponseEntity<String> generateBarcodesForAisleLevels(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable String warehouseId,
            @Parameter(description = "Zone ID", required = true)
            @PathVariable String zoneId,
            @Parameter(description = "Aisle ID", required = true)
            @PathVariable String aisleId) {
        
        log.info("POST /api/barcodes/levels/batch/aisle/{}/{}/{} - Generate barcodes for aisle levels", 
                warehouseId, zoneId, aisleId);
        
        int count = levelBarcodeService.generateBarcodesForAisleLevels(warehouseId, zoneId, aisleId);
        return ResponseEntity.ok("Successfully generated barcodes for " + count + 
                " levels in aisle " + warehouseId + "-" + zoneId + "-" + aisleId);
    }

    @PostMapping("/batch/zone/{warehouseId}/{zoneId}")
    @Operation(summary = "Generate barcodes for all levels in a zone")
    public ResponseEntity<String> generateBarcodesForZoneLevels(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable String warehouseId,
            @Parameter(description = "Zone ID", required = true)
            @PathVariable String zoneId) {
        
        log.info("POST /api/barcodes/levels/batch/zone/{}/{} - Generate barcodes for zone levels", 
                warehouseId, zoneId);
        
        int count = levelBarcodeService.generateBarcodesForZoneLevels(warehouseId, zoneId);
        return ResponseEntity.ok("Successfully generated barcodes for " + count + 
                " levels in zone " + warehouseId + "-" + zoneId);
    }

    @PostMapping("/batch/warehouse/{warehouseId}")
    @Operation(summary = "Generate barcodes for all levels in a warehouse")
    public ResponseEntity<String> generateBarcodesForWarehouseLevels(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable String warehouseId) {
        
        log.info("POST /api/barcodes/levels/batch/warehouse/{} - Generate barcodes for warehouse levels", 
                warehouseId);
        
        int count = levelBarcodeService.generateBarcodesForWarehouseLevels(warehouseId);
        return ResponseEntity.ok("Successfully generated barcodes for " + count + 
                " levels in warehouse " + warehouseId);
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate custom barcode for level")
    public ResponseEntity<String> generateCustomBarcode(
            @RequestParam String data,
            @RequestParam(required = false, defaultValue = "") String label,
            @RequestParam(required = false, defaultValue = "CODE128") String format) {
        
        log.info("POST /api/barcodes/levels/generate - Generate custom level barcode");
        String barcodeBase64 = levelBarcodeService.generateLevelBarcodeWithFormat(data, label, format);
        
        if (barcodeBase64 != null) {
            return ResponseEntity.ok(barcodeBase64);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate barcode");
        }
    }
}