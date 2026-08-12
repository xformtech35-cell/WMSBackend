// ====== FILE: src/main/java/com/warehouse/wms/service/impl/BarcodeServiceImpl.java ======
package com.warehouse.wms.service.impl;

import com.google.zxing.BarcodeFormat;
import com.warehouse.wms.entity.Warehouse;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.repository.WarehouseRepository;
import com.warehouse.wms.service.BarcodeService;
import com.warehouse.wms.util.BarcodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BarcodeServiceImpl implements BarcodeService {

    private final BarcodeGenerator barcodeGenerator;
    private final WarehouseRepository warehouseRepository;

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 100;

    @Override
    public String generateWarehouseBarcode(String warehouseId) {
        return generateWarehouseBarcode(warehouseId, "Warehouse: " + warehouseId);
    }

    @Override
    public String generateWarehouseBarcode(String warehouseId, String label) {
        return generateWarehouseBarcode(warehouseId, label, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    public String generateWarehouseBarcode(String warehouseId, String label, int width, int height) {
        try {
            log.info("Generating barcode for warehouse: {}", warehouseId);
            
            // Validate warehouse exists
            Warehouse warehouse = warehouseRepository.findByWarehouseId(warehouseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + warehouseId));
            
            // Generate barcode
            String barcodeBase64 = barcodeGenerator.generateBarcodeBase64(warehouseId, width, height);
            
            if (barcodeBase64 != null) {
                // Update warehouse with barcode data
                warehouse.setBarcodeData(warehouseId);
                warehouse.setBarcodeImage(barcodeBase64);
                warehouse.setBarcodeFormat("CODE128");
                warehouseRepository.save(warehouse);
                
                log.info("✅ Barcode generated and saved for warehouse: {}", warehouseId);
                return barcodeBase64;
            } else {
                log.error("Failed to generate barcode for warehouse: {}", warehouseId);
                return null;
            }
        } catch (Exception e) {
            log.error("Error generating warehouse barcode: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate barcode: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] getWarehouseBarcodeBytes(String warehouseId) {
        Warehouse warehouse = warehouseRepository.findByWarehouseId(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + warehouseId));
        
        if (warehouse.getBarcodeImage() == null) {
            // Generate barcode if not exists
            generateWarehouseBarcode(warehouseId);
            warehouse = warehouseRepository.findByWarehouseId(warehouseId).orElseThrow();
        }
        
        // Convert base64 to bytes
        return java.util.Base64.getDecoder().decode(warehouse.getBarcodeImage());
    }

    @Override
    public String getWarehouseBarcodeBase64(String warehouseId) {
        Warehouse warehouse = warehouseRepository.findByWarehouseId(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + warehouseId));
        
        if (warehouse.getBarcodeImage() == null) {
            // Generate barcode if not exists
            generateWarehouseBarcode(warehouseId);
            warehouse = warehouseRepository.findByWarehouseId(warehouseId).orElseThrow();
        }
        
        return warehouse.getBarcodeImage();
    }

    @Override
    public String getWarehouseBarcodeDataURI(String warehouseId) {
        String base64 = getWarehouseBarcodeBase64(warehouseId);
        return "data:image/png;base64," + base64;
    }

    @Override
    public boolean regenerateWarehouseBarcode(Long warehouseId) {
        try {
            Warehouse warehouse = warehouseRepository.findById(warehouseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + warehouseId));
            
            // Generate new barcode
            String barcodeBase64 = barcodeGenerator.generateBarcodeBase64(
                    warehouse.getWarehouseId()
                    
            );
            
            if (barcodeBase64 != null) {
                warehouse.setBarcodeImage(barcodeBase64);
                warehouse.setBarcodeData(warehouse.getWarehouseId());
                warehouse.setBarcodeFormat("CODE128");
                warehouseRepository.save(warehouse);
                log.info("✅ Barcode regenerated for warehouse: {}", warehouse.getWarehouseId());
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error regenerating barcode: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String generateBarcodeWithFormat(String data, String label, String format) {
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
            
            return barcodeGenerator.generateBarcodeBase64(data,  barcodeFormat);
        } catch (Exception e) {
            log.error("Error generating barcode with format {}: {}", format, e.getMessage(), e);
            return null;
        }
    }
}