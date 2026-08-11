// ====== FILE: src/main/java/com/warehouse/wms/service/ZoneBarcodeService.java ======
package com.warehouse.wms.service;

public interface ZoneBarcodeService {
    String generateZoneBarcode(String warehouseId, String zoneId);
    String generateZoneBarcode(Long zoneId);
    String generateZoneBarcodeWithLabel(String warehouseId, String zoneId, String label);
    String generateZoneBarcodeWithLabel(Long zoneId, String label);
    byte[] getZoneBarcodeBytes(Long zoneId);
    String getZoneBarcodeBase64(Long zoneId);
    String getZoneBarcodeDataURI(Long zoneId);
    boolean regenerateZoneBarcode(Long zoneId);
    int generateBarcodesForAllZones();
    int generateBarcodesForWarehouseZones(String warehouseId);
    String generateZoneBarcodeWithFormat(String data, String label, String format);
}