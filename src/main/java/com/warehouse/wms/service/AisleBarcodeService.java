// ====== FILE: src/main/java/com/warehouse/wms/service/AisleBarcodeService.java ======
package com.warehouse.wms.service;

public interface AisleBarcodeService {
    String generateAisleBarcode(String warehouseId, String zoneId, String aisleId);
    String generateAisleBarcode(Long aisleId);
    String generateAisleBarcodeWithLabel(String warehouseId, String zoneId, String aisleId, String label);
    String generateAisleBarcodeWithLabel(Long aisleId, String label);
    byte[] getAisleBarcodeBytes(Long aisleId);
    String getAisleBarcodeBase64(Long aisleId);
    String getAisleBarcodeDataURI(Long aisleId);
    boolean regenerateAisleBarcode(Long aisleId);
    int generateBarcodesForAllAisles();
    int generateBarcodesForZoneAisles(String warehouseId, String zoneId);
    int generateBarcodesForWarehouseAisles(String warehouseId);
    String generateAisleBarcodeWithFormat(String data, String label, String format);
}