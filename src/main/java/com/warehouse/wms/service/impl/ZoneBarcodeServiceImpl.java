// ====== FILE: src/main/java/com/warehouse/wms/service/impl/ZoneBarcodeServiceImpl.java ======
package com.warehouse.wms.service.impl;

import com.google.zxing.BarcodeFormat;
import com.warehouse.wms.entity.Zone;
import com.warehouse.wms.entity.Warehouse;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.repository.ZoneRepository;
import com.warehouse.wms.repository.WarehouseRepository;
import com.warehouse.wms.service.ZoneBarcodeService;
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
public class ZoneBarcodeServiceImpl implements ZoneBarcodeService {

    private final BarcodeGenerator barcodeGenerator;
    private final ZoneRepository zoneRepository;
    private final WarehouseRepository warehouseRepository;

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 100;

    @Override
    public String generateZoneBarcode(String warehouseId, String zoneId) {
        String fullIdentifier = warehouseId + "-" + zoneId;
        return generateZoneBarcodeWithLabel(warehouseId, zoneId, "Zone: " + fullIdentifier);
    }

    @Override
    public String generateZoneBarcode(Long zoneId) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with ID: " + zoneId));
        return generateZoneBarcode(zone.getWarehouse().getWarehouseId(), zone.getZoneId());
    }

    @Override
    public String generateZoneBarcodeWithLabel(String warehouseId, String zoneId, String label) {
        try {
            String fullIdentifier = warehouseId + "-" + zoneId;
            log.info("Generating barcode for zone: {}", fullIdentifier);
            
            // Find zone by warehouseId and zoneId
            Zone zone = zoneRepository.findByWarehouse_WarehouseIdAndZoneId(warehouseId, zoneId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Zone not found: " + warehouseId + "-" + zoneId));
            
            // Generate barcode
            String barcodeBase64 = barcodeGenerator.generateBarcodeBase64(
                    fullIdentifier, 
                   
                    DEFAULT_WIDTH, 
                    DEFAULT_HEIGHT
            );
            
            if (barcodeBase64 != null) {
                // Update zone with barcode data
                zone.setBarcodeData(fullIdentifier);
                zone.setBarcodeImage(barcodeBase64);
                zone.setBarcodeFormat("CODE128");
                zoneRepository.save(zone);
                
                log.info("✅ Barcode generated and saved for zone: {}", fullIdentifier);
                return barcodeBase64;
            } else {
                log.error("Failed to generate barcode for zone: {}", fullIdentifier);
                return null;
            }
        } catch (Exception e) {
            log.error("Error generating zone barcode: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate barcode: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateZoneBarcodeWithLabel(Long zoneId, String label) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with ID: " + zoneId));
        return generateZoneBarcodeWithLabel(
                zone.getWarehouse().getWarehouseId(), 
                zone.getZoneId(), 
                label
        );
    }

    @Override
    public byte[] getZoneBarcodeBytes(Long zoneId) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with ID: " + zoneId));
        
        if (zone.getBarcodeImage() == null) {
            // Generate barcode if not exists
            generateZoneBarcode(zoneId);
            zone = zoneRepository.findById(zoneId).orElseThrow();
        }
        
        // Convert base64 to bytes
        return java.util.Base64.getDecoder().decode(zone.getBarcodeImage());
    }

    @Override
    public String getZoneBarcodeBase64(Long zoneId) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with ID: " + zoneId));
        
        if (zone.getBarcodeImage() == null) {
            // Generate barcode if not exists
            generateZoneBarcode(zoneId);
            zone = zoneRepository.findById(zoneId).orElseThrow();
        }
        
        return zone.getBarcodeImage();
    }

    @Override
    public String getZoneBarcodeDataURI(Long zoneId) {
        String base64 = getZoneBarcodeBase64(zoneId);
        return "data:image/png;base64," + base64;
    }

    @Override
    public boolean regenerateZoneBarcode(Long zoneId) {
        try {
            Zone zone = zoneRepository.findById(zoneId)
                    .orElseThrow(() -> new ResourceNotFoundException("Zone not found with ID: " + zoneId));
            
            String fullIdentifier = zone.getFullZoneIdentifier();
            
            // Generate new barcode
            String barcodeBase64 = barcodeGenerator.generateBarcodeBase64(
                    fullIdentifier,
                    
                    DEFAULT_WIDTH,
                    DEFAULT_HEIGHT
            );
            
            if (barcodeBase64 != null) {
                zone.setBarcodeImage(barcodeBase64);
                zone.setBarcodeData(fullIdentifier);
                zone.setBarcodeFormat("CODE128");
                zoneRepository.save(zone);
                log.info("✅ Barcode regenerated for zone: {}", fullIdentifier);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error regenerating barcode: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public int generateBarcodesForAllZones() {
        log.info("Generating barcodes for all zones");
        
        List<Zone> zones = zoneRepository.findAll();
        int successCount = 0;
        
        for (Zone zone : zones) {
            try {
                generateZoneBarcode(zone.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to generate barcode for zone {}: {}", 
                         zone.getFullZoneIdentifier(), e.getMessage());
            }
        }
        
        log.info("✅ Generated barcodes for {} out of {} zones", successCount, zones.size());
        return successCount;
    }

    @Override
    public int generateBarcodesForWarehouseZones(String warehouseId) {
        log.info("Generating barcodes for zones in warehouse: {}", warehouseId);
        
        List<Zone> zones = zoneRepository.findByWarehouse_WarehouseId(warehouseId);
        int successCount = 0;
        
        for (Zone zone : zones) {
            try {
                generateZoneBarcode(zone.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to generate barcode for zone {}: {}", 
                         zone.getFullZoneIdentifier(), e.getMessage());
            }
        }
        
        log.info("✅ Generated barcodes for {} out of {} zones in warehouse {}", 
                successCount, zones.size(), warehouseId);
        return successCount;
    }

    @Override
    public String generateZoneBarcodeWithFormat(String data, String label, String format) {
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