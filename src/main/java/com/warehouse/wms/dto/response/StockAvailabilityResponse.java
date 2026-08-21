// ====== FILE: src/main/java/com/warehouse/wms/dto/response/StockAvailabilityResponse.java ======
package com.warehouse.wms.dto.response;

import java.util.List;

import com.warehouse.wms.dto.response.LocationSuggestionResponse.Summary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAvailabilityResponse {
    private Integer maxCapacity;
     
    private Integer totalQuantity;
    private Integer availableSlots;
    private Double utilizationPercentage;
    
    
    
    private String itemCode;
    private String itemName;
    private String uom;
    private Integer totalAvailable;
    private Integer totalReserved;
    private Integer requestedQuantity;
    private Boolean isAvailable;
    private Integer shortageQuantity;
    private String status; // AVAILABLE, PARTIAL, OUT_OF_STOCK
    private List<LocationAvailability> locationAvailability;
    private Summary summary;
}