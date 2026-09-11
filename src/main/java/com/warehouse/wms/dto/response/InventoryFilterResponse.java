package com.warehouse.wms.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryFilterResponse {

    // List of matching stock entries (your existing InventoryStockResponse)
    private List<InventoryStockResponse> items;

    // Aggregated totals
    private Long totalQuantity;
    private Long totalInTransitQuantity;
    private Long totalReservedQuantity;
    private Long totalAvailableQuantity;

    // Location suggestions (distinct bin/location values matching filters)
    private List<LocationSuggestion> locationSuggestions;
}