// ====== FILE: src/main/java/com/warehouse/wms/dto/request/LocationSuggestionRequest.java ======
package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationSuggestionRequest {

    @NotBlank(message = "Item code is required")
    private String itemCode;

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    @NotBlank(message = "Warehouse ID is required")
    private String warehouseId;

    private String preferredZone;

    private String preferredAisle;

    private String preferredRack;

    private String preferredShelf;

    private String movementType; // FIFO, FEFO, LIFO

    private String zoneType; // PICKING, BULK, OVERFLOW, DANGEROUS

    private String itemCategory;

    private Boolean allowPartial = true;

    private Integer maxLocations = 5;
}