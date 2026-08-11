// ====== FILE: src/main/java/com/warehouse/wms/service/RackBarcodeService.java ======
package com.warehouse.wms.service;

public interface RackBarcodeService {
    String generateRackBarcode(String warehouseId, String zoneId, String aisleId, String rackId);
    String generateRackBarcode(Long rackId);
    String generateRackBarcodeWithLabel(String warehouseId, String zoneId, String aisleId, String rackId, String label);
    String generateRackBarcodeWithLabel(Long rackId, String label);
    byte[] getRackBarcodeBytes(Long rackId);
    String getRackBarcodeBase64(Long rackId);
    String getRackBarcodeDataURI(Long rackId);
    boolean regenerateRackBarcode(Long rackId);
    int generateBarcodesForAllRacks();
    int generateBarcodesForAisleRacks(String warehouseId, String zoneId, String aisleId);
    int generateBarcodesForZoneRacks(String warehouseId, String zoneId);
    int generateBarcodesForWarehouseRacks(String warehouseId);
    String generateRackBarcodeWithFormat(String data, String label, String format);
}