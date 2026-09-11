package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationSuggestion {
	
    private String binId;
    private String warehouseId;

    private String fullLocation;
    private String zone;
    private String aisle;
    private String rack;
    private String shelf;
    private String level;
    private String binBarcode;
}