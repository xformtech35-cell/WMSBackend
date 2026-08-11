// ====== FILE: src/main/java/com/warehouse/wms/service/impl/RackBarcodeServiceImpl.java ======
package com.warehouse.wms.service.impl;

import com.google.zxing.BarcodeFormat;
import com.warehouse.wms.entity.Rack;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.repository.RackRepository;
import com.warehouse.wms.service.RackBarcodeService;
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
public class RackBarcodeServiceImpl implements RackBarcodeService {

    private final BarcodeGenerator barcodeGenerator;
    private final RackRepository rackRepository;

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 100;

    @Override
    public String generateRackBarcode(String warehouseId, String zoneId, String aisleId, String rackId) {
        String fullIdentifier = warehouseId + "-" + zoneId + "-" + aisleId + "-" + rackId;
        return generateRackBarcodeWithLabel(warehouseId, zoneId, aisleId, rackId, "Rack: " + fullIdentifier);
    }

    @Override
    public String generateRackBarcode(Long rackId) {
        Rack rack = rackRepository.findById(rackId)
                .orElseThrow(() -> new ResourceNotFoundException("Rack not found with ID: " + rackId));
        return generateRackBarcode(
                rack.getAisle().getZone().getWarehouse().getWarehouseId(),
                rack.getAisle().getZone().getZoneId(),
                rack.getAisle().getAisleId(),
                rack.getRackId()
        );
    }

    @Override
    public String generateRackBarcodeWithLabel(String warehouseId, String zoneId, String aisleId, String rackId, String label) {
        try {
            String fullIdentifier = warehouseId + "-" + zoneId + "-" + aisleId + "-" + rackId;
            log.info("Generating barcode for rack: {}", fullIdentifier);
            
            // Find rack by warehouseId, zoneId, aisleId and rackId
            Rack rack = rackRepository.findByAisle_Zone_Warehouse_WarehouseIdAndAisle_Zone_ZoneIdAndAisle_AisleIdAndRackId(
                    warehouseId, zoneId, aisleId, rackId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Rack not found: " + fullIdentifier));
            
            // Generate barcode
            String barcodeBase64 = barcodeGenerator.generateBarcodeBase64(
                    fullIdentifier, 
                    label, 
                    DEFAULT_WIDTH, 
                    DEFAULT_HEIGHT
            );
            
            if (barcodeBase64 != null) {
                // Update rack with barcode data
                rack.setBarcodeData(fullIdentifier);
                rack.setBarcodeImage(barcodeBase64);
                rack.setBarcodeFormat("CODE128");
                rackRepository.save(rack);
                
                log.info("✅ Barcode generated and saved for rack: {}", fullIdentifier);
                return barcodeBase64;
            } else {
                log.error("Failed to generate barcode for rack: {}", fullIdentifier);
                return null;
            }
        } catch (Exception e) {
            log.error("Error generating rack barcode: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate barcode: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateRackBarcodeWithLabel(Long rackId, String label) {
        Rack rack = rackRepository.findById(rackId)
                .orElseThrow(() -> new ResourceNotFoundException("Rack not found with ID: " + rackId));
        return generateRackBarcodeWithLabel(
                rack.getAisle().getZone().getWarehouse().getWarehouseId(),
                rack.getAisle().getZone().getZoneId(),
                rack.getAisle().getAisleId(),
                rack.getRackId(),
                label
        );
    }

    @Override
    public byte[] getRackBarcodeBytes(Long rackId) {
        Rack rack = rackRepository.findById(rackId)
                .orElseThrow(() -> new ResourceNotFoundException("Rack not found with ID: " + rackId));
        
        if (rack.getBarcodeImage() == null) {
            // Generate barcode if not exists
            generateRackBarcode(rackId);
            rack = rackRepository.findById(rackId).orElseThrow();
        }
        
        // Convert base64 to bytes
        return java.util.Base64.getDecoder().decode(rack.getBarcodeImage());
    }

    @Override
    public String getRackBarcodeBase64(Long rackId) {
        Rack rack = rackRepository.findById(rackId)
                .orElseThrow(() -> new ResourceNotFoundException("Rack not found with ID: " + rackId));
        
        if (rack.getBarcodeImage() == null) {
            // Generate barcode if not exists
            generateRackBarcode(rackId);
            rack = rackRepository.findById(rackId).orElseThrow();
        }
        
        return rack.getBarcodeImage();
    }

    @Override
    public String getRackBarcodeDataURI(Long rackId) {
        String base64 = getRackBarcodeBase64(rackId);
        return "data:image/png;base64," + base64;
    }

    @Override
    public boolean regenerateRackBarcode(Long rackId) {
        try {
            Rack rack = rackRepository.findById(rackId)
                    .orElseThrow(() -> new ResourceNotFoundException("Rack not found with ID: " + rackId));
            
            String fullIdentifier = rack.getFullRackIdentifier();
            
            // Generate new barcode
            String barcodeBase64 = barcodeGenerator.generateBarcodeBase64(
                    fullIdentifier,
                    "Rack: " + fullIdentifier,
                    DEFAULT_WIDTH,
                    DEFAULT_HEIGHT
            );
            
            if (barcodeBase64 != null) {
                rack.setBarcodeImage(barcodeBase64);
                rack.setBarcodeData(fullIdentifier);
                rack.setBarcodeFormat("CODE128");
                rackRepository.save(rack);
                log.info("✅ Barcode regenerated for rack: {}", fullIdentifier);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error regenerating barcode: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public int generateBarcodesForAllRacks() {
        log.info("Generating barcodes for all racks");
        
        List<Rack> racks = rackRepository.findAll();
        int successCount = 0;
        
        for (Rack rack : racks) {
            try {
                generateRackBarcode(rack.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to generate barcode for rack {}: {}", 
                         rack.getFullRackIdentifier(), e.getMessage());
            }
        }
        
        log.info("✅ Generated barcodes for {} out of {} racks", successCount, racks.size());
        return successCount;
    }

    @Override
    public int generateBarcodesForAisleRacks(String warehouseId, String zoneId, String aisleId) {
        log.info("Generating barcodes for racks in aisle: {}-{}-{}", warehouseId, zoneId, aisleId);
        
        List<Rack> racks = rackRepository.findByAisle_Zone_Warehouse_WarehouseIdAndAisle_Zone_ZoneIdAndAisle_AisleId(
                warehouseId, zoneId, aisleId);
        int successCount = 0;
        
        for (Rack rack : racks) {
            try {
                generateRackBarcode(rack.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to generate barcode for rack {}: {}", 
                         rack.getFullRackIdentifier(), e.getMessage());
            }
        }
        
        log.info("✅ Generated barcodes for {} out of {} racks in aisle {}-{}-{}", 
                successCount, racks.size(), warehouseId, zoneId, aisleId);
        return successCount;
    }

    @Override
    public int generateBarcodesForZoneRacks(String warehouseId, String zoneId) {
        log.info("Generating barcodes for racks in zone: {}-{}", warehouseId, zoneId);
        
        List<Rack> racks = rackRepository.findByAisle_Zone_Warehouse_WarehouseIdAndAisle_Zone_ZoneId(
                warehouseId, zoneId);
        int successCount = 0;
        
        for (Rack rack : racks) {
            try {
                generateRackBarcode(rack.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to generate barcode for rack {}: {}", 
                         rack.getFullRackIdentifier(), e.getMessage());
            }
        }
        
        log.info("✅ Generated barcodes for {} out of {} racks in zone {}-{}", 
                successCount, racks.size(), warehouseId, zoneId);
        return successCount;
    }

    @Override
    public int generateBarcodesForWarehouseRacks(String warehouseId) {
        log.info("Generating barcodes for racks in warehouse: {}", warehouseId);
        
        List<Rack> racks = rackRepository.findByAisle_Zone_Warehouse_WarehouseId(warehouseId);
        int successCount = 0;
        
        for (Rack rack : racks) {
            try {
                generateRackBarcode(rack.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to generate barcode for rack {}: {}", 
                         rack.getFullRackIdentifier(), e.getMessage());
            }
        }
        
        log.info("✅ Generated barcodes for {} out of {} racks in warehouse {}", 
                successCount, racks.size(), warehouseId);
        return successCount;
    }

    @Override
    public String generateRackBarcodeWithFormat(String data, String label, String format) {
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