// ====== FILE: src/main/java/com/warehouse/wms/service/LevelBarcodeService.java ======
package com.warehouse.wms.service;

public interface LevelBarcodeService {
    String generateLevelBarcode(String warehouseId, String zoneId, String aisleId, String rackId, String levelId);
    String generateLevelBarcode(Long levelId);
    String generateLevelBarcodeWithLabel(String warehouseId, String zoneId, String aisleId, String rackId, String levelId, String label);
    String generateLevelBarcodeWithLabel(Long levelId, String label);
    byte[] getLevelBarcodeBytes(Long levelId);
    String getLevelBarcodeBase64(Long levelId);
    String getLevelBarcodeDataURI(Long levelId);
    boolean regenerateLevelBarcode(Long levelId);
    int generateBarcodesForAllLevels();
    int generateBarcodesForRackLevels(String warehouseId, String zoneId, String aisleId, String rackId);
    int generateBarcodesForAisleLevels(String warehouseId, String zoneId, String aisleId);
    int generateBarcodesForZoneLevels(String warehouseId, String zoneId);
    int generateBarcodesForWarehouseLevels(String warehouseId);
    String generateLevelBarcodeWithFormat(String data, String label, String format);
}