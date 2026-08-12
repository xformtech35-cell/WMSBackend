// ====== FILE: src/main/java/com/warehouse/wms/service/impl/BinBarcodeServiceImpl.java ======
package com.warehouse.wms.service.impl;

import com.google.zxing.BarcodeFormat;
import com.warehouse.wms.entity.Bin;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.repository.BinRepository;
import com.warehouse.wms.service.BinBarcodeService;
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
public class BinBarcodeServiceImpl implements BinBarcodeService {

    private final BarcodeGenerator barcodeGenerator;
    private final BinRepository binRepository;

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 100;

    @Override
    public String generateBinBarcode(String warehouseId, String zoneId, String aisleId, String rackId, String levelId, String binBarcode) {
        String fullIdentifier = warehouseId + "-" + zoneId + "-" + aisleId + "-" + rackId + "-" + levelId + "-" + binBarcode;
        return generateBinBarcodeWithLabel(warehouseId, zoneId, aisleId, rackId, levelId, binBarcode, "Bin: " + fullIdentifier);
    }

    @Override
    public String generateBinBarcode(Long binId) {
        Bin bin = binRepository.findById(binId)
                .orElseThrow(() -> new ResourceNotFoundException("Bin not found with ID: " + binId));
        return generateBinBarcode(
                bin.getLevel().getRack().getAisle().getZone().getWarehouse().getWarehouseId(),
                bin.getLevel().getRack().getAisle().getZone().getZoneId(),
                bin.getLevel().getRack().getAisle().getAisleId(),
                bin.getLevel().getRack().getRackId(),
                bin.getLevel().getLevelId(),
                bin.getBarcode()
        );
    }

    @Override
    public String generateBinBarcodeWithLabel(String warehouseId, String zoneId, String aisleId, String rackId, String levelId, String binBarcode, String label) {
        try {
            String fullIdentifier = warehouseId + "-" + zoneId + "-" + aisleId + "-" + rackId + "-" + levelId + "-" + binBarcode;
            log.info("Generating barcode for bin: {}", fullIdentifier);
            
            // Find bin by full path
            Bin bin = binRepository.findByLevel_Rack_Aisle_Zone_Warehouse_WarehouseIdAndLevel_Rack_Aisle_Zone_ZoneIdAndLevel_Rack_Aisle_AisleIdAndLevel_Rack_RackIdAndLevel_LevelIdAndBarcode(
                    warehouseId, zoneId, aisleId, rackId, levelId, binBarcode)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Bin not found: " + fullIdentifier));
            
            // Generate barcode
            String barcodeBase64 = barcodeGenerator.generateBarcodeBase64(
                    fullIdentifier, 
                    
                    DEFAULT_WIDTH, 
                    DEFAULT_HEIGHT
            );
            
            if (barcodeBase64 != null) {
                // Update bin with barcode data
                bin.setBarcodeData(fullIdentifier);
                bin.setBarcodeImage(barcodeBase64);
                bin.setBarcodeFormat("CODE128");
                binRepository.save(bin);
                
                log.info("✅ Barcode generated and saved for bin: {}", fullIdentifier);
                return barcodeBase64;
            } else {
                log.error("Failed to generate barcode for bin: {}", fullIdentifier);
                return null;
            }
        } catch (Exception e) {
            log.error("Error generating bin barcode: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate barcode: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateBinBarcodeWithLabel(Long binId, String label) {
        Bin bin = binRepository.findById(binId)
                .orElseThrow(() -> new ResourceNotFoundException("Bin not found with ID: " + binId));
        return generateBinBarcodeWithLabel(
                bin.getLevel().getRack().getAisle().getZone().getWarehouse().getWarehouseId(),
                bin.getLevel().getRack().getAisle().getZone().getZoneId(),
                bin.getLevel().getRack().getAisle().getAisleId(),
                bin.getLevel().getRack().getRackId(),
                bin.getLevel().getLevelId(),
                bin.getBarcode(),
                label
        );
    }

    @Override
    public byte[] getBinBarcodeBytes(Long binId) {
        Bin bin = binRepository.findById(binId)
                .orElseThrow(() -> new ResourceNotFoundException("Bin not found with ID: " + binId));
        
        if (bin.getBarcodeImage() == null) {
            // Generate barcode if not exists
            generateBinBarcode(binId);
            bin = binRepository.findById(binId).orElseThrow();
        }
        
        // Convert base64 to bytes
        return java.util.Base64.getDecoder().decode(bin.getBarcodeImage());
    }

    @Override
    public String getBinBarcodeBase64(Long binId) {
        Bin bin = binRepository.findById(binId)
                .orElseThrow(() -> new ResourceNotFoundException("Bin not found with ID: " + binId));
        
        if (bin.getBarcodeImage() == null) {
            // Generate barcode if not exists
            generateBinBarcode(binId);
            bin = binRepository.findById(binId).orElseThrow();
        }
        
        return bin.getBarcodeImage();
    }

    @Override
    public String getBinBarcodeDataURI(Long binId) {
        String base64 = getBinBarcodeBase64(binId);
        return "data:image/png;base64," + base64;
    }

    @Override
    public boolean regenerateBinBarcode(Long binId) {
        try {
            Bin bin = binRepository.findById(binId)
                    .orElseThrow(() -> new ResourceNotFoundException("Bin not found with ID: " + binId));
            
            String fullIdentifier = bin.getFullLocation();
            
            // Generate new barcode
            String barcodeBase64 = barcodeGenerator.generateBarcodeBase64(
                    fullIdentifier,
                   
                    DEFAULT_WIDTH,
                    DEFAULT_HEIGHT
            );
            
            if (barcodeBase64 != null) {
                bin.setBarcodeImage(barcodeBase64);
                bin.setBarcodeData(fullIdentifier);
                bin.setBarcodeFormat("CODE128");
                binRepository.save(bin);
                log.info("✅ Barcode regenerated for bin: {}", fullIdentifier);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error regenerating barcode: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public int generateBarcodesForAllBins() {
        log.info("Generating barcodes for all bins");
        
        List<Bin> bins = binRepository.findAll();
        int successCount = 0;
        
        for (Bin bin : bins) {
            try {
                generateBinBarcode(bin.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to generate barcode for bin {}: {}", 
                         bin.getFullLocation(), e.getMessage());
            }
        }
        
        log.info("✅ Generated barcodes for {} out of {} bins", successCount, bins.size());
        return successCount;
    }

    @Override
    public int generateBarcodesForLevelBins(String warehouseId, String zoneId, String aisleId, String rackId, String levelId) {
        log.info("Generating barcodes for bins in level: {}-{}-{}-{}-{}", 
                warehouseId, zoneId, aisleId, rackId, levelId);
        
        List<Bin> bins = binRepository.findByLevel_Rack_Aisle_Zone_Warehouse_WarehouseIdAndLevel_Rack_Aisle_Zone_ZoneIdAndLevel_Rack_Aisle_AisleIdAndLevel_Rack_RackIdAndLevel_LevelId(
                warehouseId, zoneId, aisleId, rackId, levelId);
        int successCount = 0;
        
        for (Bin bin : bins) {
            try {
                generateBinBarcode(bin.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to generate barcode for bin {}: {}", 
                         bin.getFullLocation(), e.getMessage());
            }
        }
        
        log.info("✅ Generated barcodes for {} out of {} bins in level {}-{}-{}-{}-{}", 
                successCount, bins.size(), warehouseId, zoneId, aisleId, rackId, levelId);
        return successCount;
    }

    @Override
    public int generateBarcodesForRackBins(String warehouseId, String zoneId, String aisleId, String rackId) {
        log.info("Generating barcodes for bins in rack: {}-{}-{}-{}", 
                warehouseId, zoneId, aisleId, rackId);
        
        List<Bin> bins = binRepository.findByLevel_Rack_Aisle_Zone_Warehouse_WarehouseIdAndLevel_Rack_Aisle_Zone_ZoneIdAndLevel_Rack_Aisle_AisleIdAndLevel_Rack_RackId(
                warehouseId, zoneId, aisleId, rackId);
        int successCount = 0;
        
        for (Bin bin : bins) {
            try {
                generateBinBarcode(bin.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to generate barcode for bin {}: {}", 
                         bin.getFullLocation(), e.getMessage());
            }
        }
        
        log.info("✅ Generated barcodes for {} out of {} bins in rack {}-{}-{}-{}", 
                successCount, bins.size(), warehouseId, zoneId, aisleId, rackId);
        return successCount;
    }

    @Override
    public int generateBarcodesForAisleBins(String warehouseId, String zoneId, String aisleId) {
        log.info("Generating barcodes for bins in aisle: {}-{}-{}", 
                warehouseId, zoneId, aisleId);
        
        List<Bin> bins = binRepository.findByLevel_Rack_Aisle_Zone_Warehouse_WarehouseIdAndLevel_Rack_Aisle_Zone_ZoneIdAndLevel_Rack_Aisle_AisleId(
                warehouseId, zoneId, aisleId);
        int successCount = 0;
        
        for (Bin bin : bins) {
            try {
                generateBinBarcode(bin.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to generate barcode for bin {}: {}", 
                         bin.getFullLocation(), e.getMessage());
            }
        }
        
        log.info("✅ Generated barcodes for {} out of {} bins in aisle {}-{}-{}", 
                successCount, bins.size(), warehouseId, zoneId, aisleId);
        return successCount;
    }

    @Override
    public int generateBarcodesForZoneBins(String warehouseId, String zoneId) {
        log.info("Generating barcodes for bins in zone: {}-{}", warehouseId, zoneId);
        
        List<Bin> bins = binRepository.findByLevel_Rack_Aisle_Zone_Warehouse_WarehouseIdAndLevel_Rack_Aisle_Zone_ZoneId(
                warehouseId, zoneId);
        int successCount = 0;
        
        for (Bin bin : bins) {
            try {
                generateBinBarcode(bin.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to generate barcode for bin {}: {}", 
                         bin.getFullLocation(), e.getMessage());
            }
        }
        
        log.info("✅ Generated barcodes for {} out of {} bins in zone {}-{}", 
                successCount, bins.size(), warehouseId, zoneId);
        return successCount;
    }

    @Override
    public int generateBarcodesForWarehouseBins(String warehouseId) {
        log.info("Generating barcodes for bins in warehouse: {}", warehouseId);
        
        List<Bin> bins = binRepository.findByLevel_Rack_Aisle_Zone_Warehouse_WarehouseId(warehouseId);
        int successCount = 0;
        
        for (Bin bin : bins) {
            try {
                generateBinBarcode(bin.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to generate barcode for bin {}: {}", 
                         bin.getFullLocation(), e.getMessage());
            }
        }
        
        log.info("✅ Generated barcodes for {} out of {} bins in warehouse {}", 
                successCount, bins.size(), warehouseId);
        return successCount;
    }

    @Override
    public String generateBinBarcodeWithFormat(String data, String label, String format) {
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