// ====== FILE: src/main/java/com/warehouse/wms/controller/BinBarcodeController.java ======
package com.warehouse.wms.controller;

import com.warehouse.wms.service.BinBarcodeService;
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
@RequestMapping("/api/barcodes/bins")
@RequiredArgsConstructor
@Slf4j
public class BinBarcodeController {

    private final BinBarcodeService binBarcodeService;

    @PostMapping("/{binId}")
    @Operation(summary = "Generate barcode for a bin by ID")
    public ResponseEntity<String> generateBinBarcodeById(
            @Parameter(description = "Bin ID", required = true)
            @PathVariable Long binId) {
        
        log.info("POST /api/barcodes/bins/{} - Generate bin barcode", binId);
        String barcodeBase64 = binBarcodeService.generateBinBarcode(binId);
        
        if (barcodeBase64 != null) {
            return ResponseEntity.ok(barcodeBase64);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate barcode");
        }
    }

    @PostMapping("/{warehouseId}/{zoneId}/{aisleId}/{rackId}/{levelId}/{binBarcode}")
    @Operation(summary = "Generate barcode for a bin by full path")
    public ResponseEntity<String> generateBinBarcode(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable String warehouseId,
            @Parameter(description = "Zone ID", required = true)
            @PathVariable String zoneId,
            @Parameter(description = "Aisle ID", required = true)
            @PathVariable String aisleId,
            @Parameter(description = "Rack ID", required = true)
            @PathVariable String rackId,
            @Parameter(description = "Level ID", required = true)
            @PathVariable String levelId,
            @Parameter(description = "Bin Barcode", required = true)
            @PathVariable String binBarcode) {
        
        log.info("POST /api/barcodes/bins/{}/{}/{}/{}/{}/{} - Generate bin barcode", 
                warehouseId, zoneId, aisleId, rackId, levelId, binBarcode);
        String barcodeBase64 = binBarcodeService.generateBinBarcode(
                warehouseId, zoneId, aisleId, rackId, levelId, binBarcode);
        
        if (barcodeBase64 != null) {
            return ResponseEntity.ok(barcodeBase64);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate barcode");
        }
    }

    @PostMapping("/{binId}/with-label")
    @Operation(summary = "Generate barcode with custom label for a bin")
    public ResponseEntity<String> generateBinBarcodeWithLabel(
            @Parameter(description = "Bin ID", required = true)
            @PathVariable Long binId,
            @RequestParam(defaultValue = "") String label) {
        
        log.info("POST /api/barcodes/bins/{}/with-label - Generate bin barcode with label", binId);
        String barcodeBase64 = binBarcodeService.generateBinBarcodeWithLabel(binId, 
                label.isEmpty() ? null : label);
        
        if (barcodeBase64 != null) {
            return ResponseEntity.ok(barcodeBase64);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate barcode");
        }
    }

    @GetMapping("/{binId}/base64")
    @Operation(summary = "Get bin barcode as Base64")
    public ResponseEntity<String> getBinBarcodeBase64(
            @Parameter(description = "Bin ID", required = true)
            @PathVariable Long binId) {
        
        log.info("GET /api/barcodes/bins/{}/base64 - Get bin barcode as Base64", binId);
        String barcodeBase64 = binBarcodeService.getBinBarcodeBase64(binId);
        return ResponseEntity.ok(barcodeBase64);
    }

    @GetMapping("/{binId}/datauri")
    @Operation(summary = "Get bin barcode as Data URI")
    public ResponseEntity<String> getBinBarcodeDataURI(
            @Parameter(description = "Bin ID", required = true)
            @PathVariable Long binId) {
        
        log.info("GET /api/barcodes/bins/{}/datauri - Get bin barcode as Data URI", binId);
        String dataUri = binBarcodeService.getBinBarcodeDataURI(binId);
        return ResponseEntity.ok(dataUri);
    }

    @GetMapping(value = "/{binId}/image", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Get bin barcode as image")
    public ResponseEntity<byte[]> getBinBarcodeImage(
            @Parameter(description = "Bin ID", required = true)
            @PathVariable Long binId) {
        
        log.info("GET /api/barcodes/bins/{}/image - Get bin barcode image", binId);
        byte[] barcodeBytes = binBarcodeService.getBinBarcodeBytes(binId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDispositionFormData("attachment", "bin-" + binId + "-barcode.png");
        
        return new ResponseEntity<>(barcodeBytes, headers, HttpStatus.OK);
    }

    @PutMapping("/{binId}/regenerate")
    @Operation(summary = "Regenerate bin barcode")
    public ResponseEntity<String> regenerateBinBarcode(
            @Parameter(description = "Bin ID", required = true)
            @PathVariable Long binId) {
        
        log.info("PUT /api/barcodes/bins/{}/regenerate - Regenerate bin barcode", binId);
        boolean success = binBarcodeService.regenerateBinBarcode(binId);
        
        if (success) {
            return ResponseEntity.ok("Barcode regenerated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to regenerate barcode");
        }
    }

    @PostMapping("/batch/all")
    @Operation(summary = "Generate barcodes for all bins")
    public ResponseEntity<String> generateBarcodesForAllBins() {
        log.info("POST /api/barcodes/bins/batch/all - Generate barcodes for all bins");
        
        int count = binBarcodeService.generateBarcodesForAllBins();
        return ResponseEntity.ok("Successfully generated barcodes for " + count + " bins");
    }

    @PostMapping("/batch/level/{warehouseId}/{zoneId}/{aisleId}/{rackId}/{levelId}")
    @Operation(summary = "Generate barcodes for all bins in a level")
    public ResponseEntity<String> generateBarcodesForLevelBins(
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
        
        log.info("POST /api/barcodes/bins/batch/level/{}/{}/{}/{}/{} - Generate barcodes for level bins", 
                warehouseId, zoneId, aisleId, rackId, levelId);
        
        int count = binBarcodeService.generateBarcodesForLevelBins(warehouseId, zoneId, aisleId, rackId, levelId);
        return ResponseEntity.ok("Successfully generated barcodes for " + count + 
                " bins in level " + warehouseId + "-" + zoneId + "-" + aisleId + "-" + rackId + "-" + levelId);
    }

    @PostMapping("/batch/rack/{warehouseId}/{zoneId}/{aisleId}/{rackId}")
    @Operation(summary = "Generate barcodes for all bins in a rack")
    public ResponseEntity<String> generateBarcodesForRackBins(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable String warehouseId,
            @Parameter(description = "Zone ID", required = true)
            @PathVariable String zoneId,
            @Parameter(description = "Aisle ID", required = true)
            @PathVariable String aisleId,
            @Parameter(description = "Rack ID", required = true)
            @PathVariable String rackId) {
        
        log.info("POST /api/barcodes/bins/batch/rack/{}/{}/{}/{} - Generate barcodes for rack bins", 
                warehouseId, zoneId, aisleId, rackId);
        
        int count = binBarcodeService.generateBarcodesForRackBins(warehouseId, zoneId, aisleId, rackId);
        return ResponseEntity.ok("Successfully generated barcodes for " + count + 
                " bins in rack " + warehouseId + "-" + zoneId + "-" + aisleId + "-" + rackId);
    }

    @PostMapping("/batch/aisle/{warehouseId}/{zoneId}/{aisleId}")
    @Operation(summary = "Generate barcodes for all bins in an aisle")
    public ResponseEntity<String> generateBarcodesForAisleBins(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable String warehouseId,
            @Parameter(description = "Zone ID", required = true)
            @PathVariable String zoneId,
            @Parameter(description = "Aisle ID", required = true)
            @PathVariable String aisleId) {
        
        log.info("POST /api/barcodes/bins/batch/aisle/{}/{}/{} - Generate barcodes for aisle bins", 
                warehouseId, zoneId, aisleId);
        
        int count = binBarcodeService.generateBarcodesForAisleBins(warehouseId, zoneId, aisleId);
        return ResponseEntity.ok("Successfully generated barcodes for " + count + 
                " bins in aisle " + warehouseId + "-" + zoneId + "-" + aisleId);
    }

    @PostMapping("/batch/zone/{warehouseId}/{zoneId}")
    @Operation(summary = "Generate barcodes for all bins in a zone")
    public ResponseEntity<String> generateBarcodesForZoneBins(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable String warehouseId,
            @Parameter(description = "Zone ID", required = true)
            @PathVariable String zoneId) {
        
        log.info("POST /api/barcodes/bins/batch/zone/{}/{} - Generate barcodes for zone bins", 
                warehouseId, zoneId);
        
        int count = binBarcodeService.generateBarcodesForZoneBins(warehouseId, zoneId);
        return ResponseEntity.ok("Successfully generated barcodes for " + count + 
                " bins in zone " + warehouseId + "-" + zoneId);
    }

    @PostMapping("/batch/warehouse/{warehouseId}")
    @Operation(summary = "Generate barcodes for all bins in a warehouse")
    public ResponseEntity<String> generateBarcodesForWarehouseBins(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable String warehouseId) {
        
        log.info("POST /api/barcodes/bins/batch/warehouse/{} - Generate barcodes for warehouse bins", 
                warehouseId);
        
        int count = binBarcodeService.generateBarcodesForWarehouseBins(warehouseId);
        return ResponseEntity.ok("Successfully generated barcodes for " + count + 
                " bins in warehouse " + warehouseId);
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate custom barcode for bin")
    public ResponseEntity<String> generateCustomBarcode(
            @RequestParam String data,
            @RequestParam(required = false, defaultValue = "") String label,
            @RequestParam(required = false, defaultValue = "CODE128") String format) {
        
        log.info("POST /api/barcodes/bins/generate - Generate custom bin barcode");
        String barcodeBase64 = binBarcodeService.generateBinBarcodeWithFormat(data, label, format);
        
        if (barcodeBase64 != null) {
            return ResponseEntity.ok(barcodeBase64);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate barcode");
        }
    }
}