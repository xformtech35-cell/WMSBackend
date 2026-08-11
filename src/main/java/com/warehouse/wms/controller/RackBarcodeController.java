// ====== FILE: src/main/java/com/warehouse/wms/controller/RackBarcodeController.java ======
package com.warehouse.wms.controller;

import com.warehouse.wms.service.RackBarcodeService;
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
@RequestMapping("/api/barcodes/racks")
@RequiredArgsConstructor
@Slf4j
public class RackBarcodeController {

    private final RackBarcodeService rackBarcodeService;

    @PostMapping("/{rackId}")
    @Operation(summary = "Generate barcode for a rack by ID")
    public ResponseEntity<String> generateRackBarcodeById(
            @Parameter(description = "Rack ID", required = true)
            @PathVariable Long rackId) {
        
        log.info("POST /api/barcodes/racks/{} - Generate rack barcode", rackId);
        String barcodeBase64 = rackBarcodeService.generateRackBarcode(rackId);
        
        if (barcodeBase64 != null) {
            return ResponseEntity.ok(barcodeBase64);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate barcode");
        }
    }

    @PostMapping("/{warehouseId}/{zoneId}/{aisleId}/{rackId}")
    @Operation(summary = "Generate barcode for a rack by warehouse, zone, aisle, and rack IDs")
    public ResponseEntity<String> generateRackBarcode(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable String warehouseId,
            @Parameter(description = "Zone ID", required = true)
            @PathVariable String zoneId,
            @Parameter(description = "Aisle ID", required = true)
            @PathVariable String aisleId,
            @Parameter(description = "Rack ID", required = true)
            @PathVariable String rackId) {
        
        log.info("POST /api/barcodes/racks/{}/{}/{}/{} - Generate rack barcode", 
                warehouseId, zoneId, aisleId, rackId);
        String barcodeBase64 = rackBarcodeService.generateRackBarcode(warehouseId, zoneId, aisleId, rackId);
        
        if (barcodeBase64 != null) {
            return ResponseEntity.ok(barcodeBase64);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate barcode");
        }
    }

    @PostMapping("/{rackId}/with-label")
    @Operation(summary = "Generate barcode with custom label for a rack")
    public ResponseEntity<String> generateRackBarcodeWithLabel(
            @Parameter(description = "Rack ID", required = true)
            @PathVariable Long rackId,
            @RequestParam(defaultValue = "") String label) {
        
        log.info("POST /api/barcodes/racks/{}/with-label - Generate rack barcode with label", rackId);
        String barcodeBase64 = rackBarcodeService.generateRackBarcodeWithLabel(rackId, 
                label.isEmpty() ? null : label);
        
        if (barcodeBase64 != null) {
            return ResponseEntity.ok(barcodeBase64);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate barcode");
        }
    }

    @GetMapping("/{rackId}/base64")
    @Operation(summary = "Get rack barcode as Base64")
    public ResponseEntity<String> getRackBarcodeBase64(
            @Parameter(description = "Rack ID", required = true)
            @PathVariable Long rackId) {
        
        log.info("GET /api/barcodes/racks/{}/base64 - Get rack barcode as Base64", rackId);
        String barcodeBase64 = rackBarcodeService.getRackBarcodeBase64(rackId);
        return ResponseEntity.ok(barcodeBase64);
    }

    @GetMapping("/{rackId}/datauri")
    @Operation(summary = "Get rack barcode as Data URI")
    public ResponseEntity<String> getRackBarcodeDataURI(
            @Parameter(description = "Rack ID", required = true)
            @PathVariable Long rackId) {
        
        log.info("GET /api/barcodes/racks/{}/datauri - Get rack barcode as Data URI", rackId);
        String dataUri = rackBarcodeService.getRackBarcodeDataURI(rackId);
        return ResponseEntity.ok(dataUri);
    }

    @GetMapping(value = "/{rackId}/image", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Get rack barcode as image")
    public ResponseEntity<byte[]> getRackBarcodeImage(
            @Parameter(description = "Rack ID", required = true)
            @PathVariable Long rackId) {
        
        log.info("GET /api/barcodes/racks/{}/image - Get rack barcode image", rackId);
        byte[] barcodeBytes = rackBarcodeService.getRackBarcodeBytes(rackId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDispositionFormData("attachment", "rack-" + rackId + "-barcode.png");
        
        return new ResponseEntity<>(barcodeBytes, headers, HttpStatus.OK);
    }

    @PutMapping("/{rackId}/regenerate")
    @Operation(summary = "Regenerate rack barcode")
    public ResponseEntity<String> regenerateRackBarcode(
            @Parameter(description = "Rack ID", required = true)
            @PathVariable Long rackId) {
        
        log.info("PUT /api/barcodes/racks/{}/regenerate - Regenerate rack barcode", rackId);
        boolean success = rackBarcodeService.regenerateRackBarcode(rackId);
        
        if (success) {
            return ResponseEntity.ok("Barcode regenerated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to regenerate barcode");
        }
    }

    @PostMapping("/batch/all")
    @Operation(summary = "Generate barcodes for all racks")
    public ResponseEntity<String> generateBarcodesForAllRacks() {
        log.info("POST /api/barcodes/racks/batch/all - Generate barcodes for all racks");
        
        int count = rackBarcodeService.generateBarcodesForAllRacks();
        return ResponseEntity.ok("Successfully generated barcodes for " + count + " racks");
    }

    @PostMapping("/batch/aisle/{warehouseId}/{zoneId}/{aisleId}")
    @Operation(summary = "Generate barcodes for all racks in an aisle")
    public ResponseEntity<String> generateBarcodesForAisleRacks(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable String warehouseId,
            @Parameter(description = "Zone ID", required = true)
            @PathVariable String zoneId,
            @Parameter(description = "Aisle ID", required = true)
            @PathVariable String aisleId) {
        
        log.info("POST /api/barcodes/racks/batch/aisle/{}/{}/{} - Generate barcodes for aisle racks", 
                warehouseId, zoneId, aisleId);
        
        int count = rackBarcodeService.generateBarcodesForAisleRacks(warehouseId, zoneId, aisleId);
        return ResponseEntity.ok("Successfully generated barcodes for " + count + 
                " racks in aisle " + warehouseId + "-" + zoneId + "-" + aisleId);
    }

    @PostMapping("/batch/zone/{warehouseId}/{zoneId}")
    @Operation(summary = "Generate barcodes for all racks in a zone")
    public ResponseEntity<String> generateBarcodesForZoneRacks(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable String warehouseId,
            @Parameter(description = "Zone ID", required = true)
            @PathVariable String zoneId) {
        
        log.info("POST /api/barcodes/racks/batch/zone/{}/{} - Generate barcodes for zone racks", 
                warehouseId, zoneId);
        
        int count = rackBarcodeService.generateBarcodesForZoneRacks(warehouseId, zoneId);
        return ResponseEntity.ok("Successfully generated barcodes for " + count + 
                " racks in zone " + warehouseId + "-" + zoneId);
    }

    @PostMapping("/batch/warehouse/{warehouseId}")
    @Operation(summary = "Generate barcodes for all racks in a warehouse")
    public ResponseEntity<String> generateBarcodesForWarehouseRacks(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable String warehouseId) {
        
        log.info("POST /api/barcodes/racks/batch/warehouse/{} - Generate barcodes for warehouse racks", 
                warehouseId);
        
        int count = rackBarcodeService.generateBarcodesForWarehouseRacks(warehouseId);
        return ResponseEntity.ok("Successfully generated barcodes for " + count + 
                " racks in warehouse " + warehouseId);
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate custom barcode for rack")
    public ResponseEntity<String> generateCustomBarcode(
            @RequestParam String data,
            @RequestParam(required = false, defaultValue = "") String label,
            @RequestParam(required = false, defaultValue = "CODE128") String format) {
        
        log.info("POST /api/barcodes/racks/generate - Generate custom rack barcode");
        String barcodeBase64 = rackBarcodeService.generateRackBarcodeWithFormat(data, label, format);
        
        if (barcodeBase64 != null) {
            return ResponseEntity.ok(barcodeBase64);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate barcode");
        }
    }
}