// ====== FILE: src/main/java/com/warehouse/wms/service/BinBarcodeService.java ======
package com.warehouse.wms.service;

public interface BinBarcodeService {
    String generateBinBarcode(String warehouseId, String zoneId, String aisleId, String rackId, String levelId, String binBarcode);
    String generateBinBarcode(Long binId);
    String generateBinBarcodeWithLabel(String warehouseId, String zoneId, String aisleId, String rackId, String levelId, String binBarcode, String label);
    String generateBinBarcodeWithLabel(Long binId, String label);
    byte[] getBinBarcodeBytes(Long binId);
    String getBinBarcodeBase64(Long binId);
    String getBinBarcodeDataURI(Long binId);
    boolean regenerateBinBarcode(Long binId);
    int generateBarcodesForAllBins();
    int generateBarcodesForLevelBins(String warehouseId, String zoneId, String aisleId, String rackId, String levelId);
    int generateBarcodesForRackBins(String warehouseId, String zoneId, String aisleId, String rackId);
    int generateBarcodesForAisleBins(String warehouseId, String zoneId, String aisleId);
    int generateBarcodesForZoneBins(String warehouseId, String zoneId);
    int generateBarcodesForWarehouseBins(String warehouseId);
    String generateBinBarcodeWithFormat(String data, String label, String format);
}