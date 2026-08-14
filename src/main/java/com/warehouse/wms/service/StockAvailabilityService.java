// ====== FILE: src/main/java/com/warehouse/wms/service/StockAvailabilityService.java ======
package com.warehouse.wms.service;

import com.warehouse.wms.dto.response.StockAvailabilitySummary;

public interface StockAvailabilityService {

    // Get stock summary for a warehouse
    StockAvailabilitySummary getWarehouseStockSummary(String warehouseId);
    
    // Get stock summary for a zone
    StockAvailabilitySummary getZoneStockSummary(String warehouseId, String zoneId);
    
    // Get stock summary for an aisle
    StockAvailabilitySummary getAisleStockSummary(String warehouseId, String zoneId, String aisleId);
    
    // Get stock summary for a rack
    StockAvailabilitySummary getRackStockSummary(String warehouseId, String zoneId, String aisleId, String rackId);
    
    // Get stock summary for a level
    StockAvailabilitySummary getLevelStockSummary(String warehouseId, String zoneId, String aisleId, String rackId, String levelId);
    
    // Get stock summary for a bin
    StockAvailabilitySummary getBinStockSummary(String binId);
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    


    // ====== Check Stock Availability ======
    
    boolean isStockAvailable(String warehouseId, String zoneId, String aisleId, 
                            String rackId, String levelId, String binId, 
                            String itemCode, Integer requiredQuantity);
    
    Integer getAvailableQuantity(String warehouseId, String zoneId, String aisleId, 
                                String rackId, String levelId, String binId, String itemCode);
    
    
}