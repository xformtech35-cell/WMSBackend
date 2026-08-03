// ====== FILE: src/main/java/com/warehouse/wms/dto/response/LocationSuggestionResponse.java ======
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
public class LocationSuggestionResponse {

    private String itemCode;
    private Integer quantityRequired;
    private String warehouseId;
    private String preferredZone;
    private String movementType;
    private String zoneType;
    private Boolean partialAllowed;

    private List<SuggestedLocation> suggestedLocations;
    private List<AlternativeLocation> alternativeLocations;

    private Summary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuggestedLocation {
        private String binId;
        private String binBarcode;
        private String warehouseId;
        private String zone;
        private String aisle;
        private String rack;
        private String shelf;
        private Integer capacity;
        private Integer availableCapacity;
        private Integer usedCapacity;
        private Integer suggestedQuantity;
        private Integer priority;
        private Integer distanceFromDispatch;
        private String locationType;
        private String zoneType;
        private String movementType;
        private String fullLocation;
        private Boolean isAvailable;
        private String itemCode;
        private String itemName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlternativeLocation {
        private String binId;
        private String fullLocation;
        private Integer availableCapacity;
        private Integer priority;
        private String reason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Integer totalLocationsSuggested;
        private Integer totalAvailableCapacity;
        private Integer totalQuantityAllocated;
        private Integer remainingQuantity;
        private Boolean isFullyAllocated;
        private String bestLocation;
    }
}