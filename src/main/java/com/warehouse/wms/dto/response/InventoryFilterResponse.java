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
public class InventoryFilterResponse {

    // List of matching stock entries (paginated)
    private List<InventoryStockResponse> items;

    // Aggregated totals
    private Long totalQuantity;
    private Long totalInTransitQuantity;
    private Long totalReservedQuantity;
    private Long totalAvailableQuantity;
    
    private Long totalBinCapacity;
    private Long availableSlots;


    // ✅ SINGLE location suggestion (was a List before)
    private LocationSuggestion locationSuggestion;
}