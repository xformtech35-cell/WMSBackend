// ====== FILE: src/main/java/com/warehouse/wms/service/impl/LevelBarcodeServiceImpl.java ======
package com.warehouse.wms.service.impl;

import com.google.zxing.BarcodeFormat;
import com.warehouse.wms.entity.Level;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.repository.LevelRepository;
import com.warehouse.wms.service.LevelBarcodeService;
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
public class LevelBarcodeServiceImpl implements LevelBarcodeService {

    private final BarcodeGenerator barcodeGenerator;
    private final LevelRepository levelRepository;

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 100;

    @Override
    public String generateLevelBarcode(String warehouseId, String zoneId, String aisleId, String rackId, String levelId) {
        String fullIdentifier = warehouseId + "-" + zoneId + "-" + aisleId + "-" + rackId + "-" + levelId;
        return generateLevelBarcodeWithLabel(warehouseId, zoneId, aisleId, rackId, levelId, "Level: " + fullIdentifier);
    }

    @Override
    public String generateLevelBarcode(Long levelId) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new ResourceNotFoundException("Level not found with ID: " + levelId));
        return generateLevelBarcode(
                level.getRack().getAisle().getZone().getWarehouse().getWarehouseId(),
                level.getRack().getAisle().getZone().getZoneId(),
                level.getRack().getAisle().getAisleId(),
                level.getRack().getRackId(),
                level.getLevelId()
        );
    }

    @Override
    public String generateLevelBarcodeWithLabel(String warehouseId, String zoneId, String aisleId, String rackId, String levelId, String label) {
        try {
            String fullIdentifier = warehouseId + "-" + zoneId + "-" + aisleId + "-" + rackId + "-" + levelId;
            log.info("Generating barcode for level: {}", fullIdentifier);
            
            // Find level by full path
            Level level = levelRepository.findByRack_Aisle_Zone_Warehouse_WarehouseIdAndRack_Aisle_Zone_ZoneIdAndRack_Aisle_AisleIdAndRack_RackIdAndLevelId(
                    warehouseId, zoneId, aisleId, rackId, levelId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Level not found: " + fullIdentifier));
            
            // Generate barcode
            String barcodeBase64 = barcodeGenerator.generateBarcodeBase64(
                    fullIdentifier, 
                    label, 
                    DEFAULT_WIDTH, 
                    DEFAULT_HEIGHT
            );
            
            if (barcodeBase64 != null) {
                // Update level with barcode data
                level.setBarcodeData(fullIdentifier);
                level.setBarcodeImage(barcodeBase64);
                level.setBarcodeFormat("CODE128");
                levelRepository.save(level);
                
                log.info("✅ Barcode generated and saved for level: {}", fullIdentifier);
                return barcodeBase64;
            } else {
                log.error("Failed to generate barcode for level: {}", fullIdentifier);
                return null;
            }
        } catch (Exception e) {
            log.error("Error generating level barcode: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate barcode: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateLevelBarcodeWithLabel(Long levelId, String label) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new ResourceNotFoundException("Level not found with ID: " + levelId));
        return generateLevelBarcodeWithLabel(
                level.getRack().getAisle().getZone().getWarehouse().getWarehouseId(),
                level.getRack().getAisle().getZone().getZoneId(),
                level.getRack().getAisle().getAisleId(),
                level.getRack().getRackId(),
                level.getLevelId(),
                label
        );
    }

    @Override
    public byte[] getLevelBarcodeBytes(Long levelId) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new ResourceNotFoundException("Level not found with ID: " + levelId));
        
        if (level.getBarcodeImage() == null) {
            // Generate barcode if not exists
            generateLevelBarcode(levelId);
            level = levelRepository.findById(levelId).orElseThrow();
        }
        
        // Convert base64 to bytes
        return java.util.Base64.getDecoder().decode(level.getBarcodeImage());
    }

    @Override
    public String getLevelBarcodeBase64(Long levelId) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new ResourceNotFoundException("Level not found with ID: " + levelId));
        
        if (level.getBarcodeImage() == null) {
            // Generate barcode if not exists
            generateLevelBarcode(levelId);
            level = levelRepository.findById(levelId).orElseThrow();
        }
        
        return level.getBarcodeImage();
    }

    @Override
    public String getLevelBarcodeDataURI(Long levelId) {
        String base64 = getLevelBarcodeBase64(levelId);
        return "data:image/png;base64," + base64;
    }

    @Override
    public boolean regenerateLevelBarcode(Long levelId) {
        try {
            Level level = levelRepository.findById(levelId)
                    .orElseThrow(() -> new ResourceNotFoundException("Level not found with ID: " + levelId));
            
            String fullIdentifier = level.getFullLevelLocation();
            
            // Generate new barcode
            String barcodeBase64 = barcodeGenerator.generateBarcodeBase64(
                    fullIdentifier,
                    "Level: " + fullIdentifier,
                    DEFAULT_WIDTH,
                    DEFAULT_HEIGHT
            );
            
            if (barcodeBase64 != null) {
                level.setBarcodeImage(barcodeBase64);
                level.setBarcodeData(fullIdentifier);
                level.setBarcodeFormat("CODE128");
                levelRepository.save(level);
                log.info("✅ Barcode regenerated for level: {}", fullIdentifier);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error regenerating barcode: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public int generateBarcodesForAllLevels() {
        log.info("Generating barcodes for all levels");
        
        List<Level> levels = levelRepository.findAll();
        int successCount = 0;
        
        for (Level level : levels) {
            try {
                generateLevelBarcode(level.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to generate barcode for level {}: {}", 
                         level.getFullLevelLocation(), e.getMessage());
            }
        }
        
        log.info("✅ Generated barcodes for {} out of {} levels", successCount, levels.size());
        return successCount;
    }

    @Override
    public int generateBarcodesForRackLevels(String warehouseId, String zoneId, String aisleId, String rackId) {
        log.info("Generating barcodes for levels in rack: {}-{}-{}-{}", warehouseId, zoneId, aisleId, rackId);
        
        List<Level> levels = levelRepository.findByRack_Aisle_Zone_Warehouse_WarehouseIdAndRack_Aisle_Zone_ZoneIdAndRack_Aisle_AisleIdAndRack_RackId(
                warehouseId, zoneId, aisleId, rackId);
        int successCount = 0;
        
        for (Level level : levels) {
            try {
                generateLevelBarcode(level.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to generate barcode for level {}: {}", 
                         level.getFullLevelLocation(), e.getMessage());
            }
        }
        
        log.info("✅ Generated barcodes for {} out of {} levels in rack {}-{}-{}-{}", 
                successCount, levels.size(), warehouseId, zoneId, aisleId, rackId);
        return successCount;
    }

    @Override
    public int generateBarcodesForAisleLevels(String warehouseId, String zoneId, String aisleId) {
        log.info("Generating barcodes for levels in aisle: {}-{}-{}", warehouseId, zoneId, aisleId);
        
        List<Level> levels = levelRepository.findByRack_Aisle_Zone_Warehouse_WarehouseIdAndRack_Aisle_Zone_ZoneIdAndRack_Aisle_AisleId(
                warehouseId, zoneId, aisleId);
        int successCount = 0;
        
        for (Level level : levels) {
            try {
                generateLevelBarcode(level.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to generate barcode for level {}: {}", 
                         level.getFullLevelLocation(), e.getMessage());
            }
        }
        
        log.info("✅ Generated barcodes for {} out of {} levels in aisle {}-{}-{}", 
                successCount, levels.size(), warehouseId, zoneId, aisleId);
        return successCount;
    }

    @Override
    public int generateBarcodesForZoneLevels(String warehouseId, String zoneId) {
        log.info("Generating barcodes for levels in zone: {}-{}", warehouseId, zoneId);
        
        List<Level> levels = levelRepository.findByRack_Aisle_Zone_Warehouse_WarehouseIdAndRack_Aisle_Zone_ZoneId(
                warehouseId, zoneId);
        int successCount = 0;
        
        for (Level level : levels) {
            try {
                generateLevelBarcode(level.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to generate barcode for level {}: {}", 
                         level.getFullLevelLocation(), e.getMessage());
            }
        }
        
        log.info("✅ Generated barcodes for {} out of {} levels in zone {}-{}", 
                successCount, levels.size(), warehouseId, zoneId);
        return successCount;
    }

    @Override
    public int generateBarcodesForWarehouseLevels(String warehouseId) {
        log.info("Generating barcodes for levels in warehouse: {}", warehouseId);
        
        List<Level> levels = levelRepository.findByRack_Aisle_Zone_Warehouse_WarehouseId(warehouseId);
        int successCount = 0;
        
        for (Level level : levels) {
            try {
                generateLevelBarcode(level.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to generate barcode for level {}: {}", 
                         level.getFullLevelLocation(), e.getMessage());
            }
        }
        
        log.info("✅ Generated barcodes for {} out of {} levels in warehouse {}", 
                successCount, levels.size(), warehouseId);
        return successCount;
    }

    @Override
    public String generateLevelBarcodeWithFormat(String data, String label, String format) {
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
            
            return barcodeGenerator.generateBarcodeBase64(data, label, barcodeFormat);
        } catch (Exception e) {
            log.error("Error generating barcode with format {}: {}", format, e.getMessage(), e);
            return null;
        }
    }
}