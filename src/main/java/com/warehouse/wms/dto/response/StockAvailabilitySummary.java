// ====== FILE: src/main/java/com/warehouse/wms/dto/response/StockAvailabilitySummary.java ======
package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAvailabilitySummary {
    // Total stock counts
    private Integer totalQuantity;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer inTransitQuantity;
    
    // Unique items count
    private Integer uniqueItemsCount;
    
    // Capacity information
//    private Integer maxCapacity;
    private Double utilizationPercentage;
    
    // Location path
    private String locationPath;
    private String locationLevel;
    
    // Status flags
    private Boolean hasStock;
    private Boolean isFull;
    private Boolean isAvailable;
    
    // Timestamps
    private String lastPutawayDate;
    private String lastPickDate;
    
    // Items in this location
    private List<ItemStockSummary> items;
}