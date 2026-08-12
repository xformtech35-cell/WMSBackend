// ====== FILE: src/main/java/com/warehouse/wms/service/impl/AisleBarcodeServiceImpl.java ======
package com.warehouse.wms.service.impl;

import com.google.zxing.BarcodeFormat;
import com.warehouse.wms.entity.Aisle;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.repository.AisleRepository;
import com.warehouse.wms.service.AisleBarcodeService;
import com.warehouse.wms.util.BarcodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AisleBarcodeServiceImpl implements AisleBarcodeService {

    private final BarcodeGenerator barcodeGenerator;
    private final AisleRepository aisleRepository;

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 100;

    @Override
    public String generateAisleBarcode(String warehouseId, String zoneId, String aisleId) {
        String fullIdentifier = warehouseId + "-" + zoneId + "-" + aisleId;
        return generateAisleBarcodeWithLabel(warehouseId, zoneId, aisleId, "Aisle: " + fullIdentifier);
    }

    @Override
    public String generateAisleBarcode(Long aisleId) {
        Aisle aisle = aisleRepository.findById(aisleId)
                .orElseThrow(() -> new ResourceNotFoundException("Aisle not found with ID: " + aisleId));
        return generateAisleBarcode(
                aisle.getZone().getWarehouse().getWarehouseId(),
                aisle.getZone().getZoneId(),
                aisle.getAisleId()
        );
    }

    @Override
    public String generateAisleBarcodeWithLabel(String warehouseId, String zoneId, String aisleId, String label) {
        try {
            String fullIdentifier = warehouseId + "-" + zoneId + "-" + aisleId;
            log.info("Generating barcode for aisle: {}", fullIdentifier);
            
            // Find aisle by warehouseId, zoneId and aisleId
            Aisle aisle = aisleRepository.findByZone_Warehouse_WarehouseIdAndZone_ZoneIdAndAisleId(
                    warehouseId, zoneId, aisleId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Aisle not found: " + fullIdentifier));
            
            // Generate barcode
            String barcodeBase64 = barcodeGenerator.generateBarcodeBase64(
                    fullIdentifier, 
                    
                    DEFAULT_WIDTH, 
                    DEFAULT_HEIGHT
            );
            
            if (barcodeBase64 != null) {
                // Update aisle with barcode data
                aisle.setBarcodeData(fullIdentifier);
                aisle.setBarcodeImage(barcodeBase64);
                aisle.setBarcodeFormat("CODE128");
                aisleRepository.save(aisle);
                
                log.info("✅ Barcode generated and saved for aisle: {}", fullIdentifier);
                return barcodeBase64;
            } else {
                log.error("Failed to generate barcode for aisle: {}", fullIdentifier);
                return null;
            }
        } catch (Exception e) {
            log.error("Error generating aisle barcode: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate barcode: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateAisleBarcodeWithLabel(Long aisleId, String label) {
        Aisle aisle = aisleRepository.findById(aisleId)
                .orElseThrow(() -> new ResourceNotFoundException("Aisle not found with ID: " + aisleId));
        return generateAisleBarcodeWithLabel(
                aisle.getZone().getWarehouse().getWarehouseId(),
                aisle.getZone().getZoneId(),
                aisle.getAisleId(),
                label
        );
    }

    @Override
    public byte[] getAisleBarcodeBytes(Long aisleId) {
        Aisle aisle = aisleRepository.findById(aisleId)
                .orElseThrow(() -> new ResourceNotFoundException("Aisle not found with ID: " + aisleId));
        
        if (aisle.getBarcodeImage() == null) {
            // Generate barcode if not exists
            generateAisleBarcode(aisleId);
            aisle = aisleRepository.findById(aisleId).orElseThrow();
        }
        
        // Convert base64 to bytes
        return java.util.Base64.getDecoder().decode(aisle.getBarcodeImage());
    }

    @Override
    public String getAisleBarcodeBase64(Long aisleId) {
        Aisle aisle = aisleRepository.findById(aisleId)
                .orElseThrow(() -> new ResourceNotFoundException("Aisle not found with ID: " + aisleId));
        
        if (aisle.getBarcodeImage() == null) {
            // Generate barcode if not exists
            generateAisleBarcode(aisleId);
            aisle = aisleRepository.findById(aisleId).orElseThrow();
        }
        
        return aisle.getBarcodeImage();
    }

    @Override
    public String getAisleBarcodeDataURI(Long aisleId) {
        String base64 = getAisleBarcodeBase64(aisleId);
        return "data:image/png;base64," + base64;
    }

    @Override
    public boolean regenerateAisleBarcode(Long aisleId) {
        try {
            Aisle aisle = aisleRepository.findById(aisleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Aisle not found with ID: " + aisleId));
            
            String fullIdentifier = aisle.getFullAisleIdentifier();
            
            // Generate new barcode
            String barcodeBase64 = barcodeGenerator.generateBarcodeBase64(
                    fullIdentifier,
                    
                    DEFAULT_WIDTH,
                    DEFAULT_HEIGHT
            );
            
            if (barcodeBase64 != null) {
                aisle.setBarcodeImage(barcodeBase64);
                aisle.setBarcodeData(fullIdentifier);
                aisle.setBarcodeFormat("CODE128");
                aisleRepository.save(aisle);
                log.info("✅ Barcode regenerated for aisle: {}", fullIdentifier);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error regenerating barcode: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public int generateBarcodesForAllAisles() {
        log.info("Generating barcodes for all aisles");
        
        List<Aisle> aisles = aisleRepository.findAll();
        int successCount = 0;
        
        for (Aisle aisle : aisles) {
            try {
                generateAisleBarcode(aisle.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to generate barcode for aisle {}: {}", 
                         aisle.getFullAisleIdentifier(), e.getMessage());
            }
        }
        
        log.info("✅ Generated barcodes for {} out of {} aisles", successCount, aisles.size());
        return successCount;
    }

    @Override
    public int generateBarcodesForZoneAisles(String warehouseId, String zoneId) {
        log.info("Generating barcodes for aisles in zone: {}-{}", warehouseId, zoneId);
        
        List<Aisle> aisles = aisleRepository.findByZone_Warehouse_WarehouseIdAndZone_ZoneId(
                warehouseId, zoneId);
        int successCount = 0;
        
        for (Aisle aisle : aisles) {
            try {
                generateAisleBarcode(aisle.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to generate barcode for aisle {}: {}", 
                         aisle.getFullAisleIdentifier(), e.getMessage());
            }
        }
        
        log.info("✅ Generated barcodes for {} out of {} aisles in zone {}-{}", 
                successCount, aisles.size(), warehouseId, zoneId);
        return successCount;
    }

    @Override
    public int generateBarcodesForWarehouseAisles(String warehouseId) {
        log.info("Generating barcodes for aisles in warehouse: {}", warehouseId);
        
        List<Aisle> aisles = aisleRepository.findByZone_Warehouse_WarehouseId(warehouseId);
        int successCount = 0;
        
        for (Aisle aisle : aisles) {
            try {
                generateAisleBarcode(aisle.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to generate barcode for aisle {}: {}", 
                         aisle.getFullAisleIdentifier(), e.getMessage());
            }
        }
        
        log.info("✅ Generated barcodes for {} out of {} aisles in warehouse {}", 
                successCount, aisles.size(), warehouseId);
        return successCount;
    }

    @Override
    public String generateAisleBarcodeWithFormat(String data, String label, String format) {
        try {
            BarcodeFormat barcodeFormat;
            switch (format.toUpperCase()) {
                case "CODE128":
                    barcodeFormat = BarcodeFormat.CODE_128;
                    break;
                case "CODE39":
                    barcodeFormat = BarcodeFormat.CODE_39;
                    break;
                case "EAN13":
                    barcodeFormat = BarcodeFormat.EAN_13;
                    break;
                case "UPC_A":
                    barcodeFormat = BarcodeFormat.UPC_A;
                    break;
                default:
                    barcodeFormat = BarcodeFormat.CODE_128;
            }
            
            return barcodeGenerator.generateBarcodeBase64(data, barcodeFormat);
        } catch (Exception e) {
            log.error("Error generating barcode with format {}: {}", format, e.getMessage(), e);
            return null;
        }
    }
}