// ====== FILE: src/main/java/com/warehouse/wms/service/BarcodeService.java ======
package com.warehouse.wms.service;

public interface BarcodeService {
    String generateWarehouseBarcode(String warehouseId);
    String generateWarehouseBarcode(String warehouseId, String label);
    String generateWarehouseBarcode(String warehouseId, String label, int width, int height);
    byte[] getWarehouseBarcodeBytes(String warehouseId);
    String getWarehouseBarcodeBase64(String warehouseId);
    String getWarehouseBarcodeDataURI(String warehouseId);
    boolean regenerateWarehouseBarcode(Long warehouseId);
    String generateBarcodeWithFormat(String data, String label, String format);
}