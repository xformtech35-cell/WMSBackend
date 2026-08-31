// ====== FILE: src/main/java/com/warehouse/wms/dto/response/StockAvailabilitySummary.java ======
package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAvailabilitySummary {
    // ====== Stock Counts ======
	
    private Long totalItems;      // Total stock in quantity
    private Long occupiedItems;      // Total stock in quantity
    private Long availableItems;      // Total stock in quantity

    

    private Integer totalQuantity;      // Total stock in quantity
    private Integer stockin;             // Stock available for picking (was availableQuantity)
    private Integer reservedQuantity;    // Reserved stock
    private Integer inTransitQuantity;   // In transit stock
    
    // ====== Capacity Information ======
    private Integer maxCapacity;         // Maximum capacity
    private Integer minCapacity;         // Minimum capacity threshold
    private Double utilizationPercentage; // Percentage used
    private Integer availableSlots;      // Available slots (maxCapacity - totalQuantity)
    private Integer occupiedSlots;       // Occupied slots
    
    // ====== Stock Status ======
    private Boolean hasStock;            // True if totalQuantity > 0
    private Boolean isFull;              // True if totalQuantity >= maxCapacity
    private Boolean isAvailable;         // True if stockin > 0
    private Boolean isLowStock;          // True if utilization < 20%
    private Boolean isHighStock;         // True if utilization > 80%
    private String stockStatus;          // "EMPTY", "LOW", "NORMAL", "HIGH", "FULL"
    
    // ====== Location Information ======
    private String locationPath;         // Full location path
    private String locationLevel;        // WAREHOUSE, ZONE, AISLE, RACK, LEVEL, BIN
    
    // ====== Item Information ======
    private Integer uniqueItemsCount;    // Number of unique items
    private List<ItemStockSummary> items; // List of items with stock
    
    // ====== Timestamps ======
    private String lastPutawayDate;      // Last putaway date
    private String lastPickDate;         // Last pick date
    
    // ====== Summary ======
    private Integer totalBinsUsed;       // Number of bins with stock
    private Integer totalBinsAvailable;  // Total bins available
    private Double stockTurnoverRate;    // Stock turnover rate
}