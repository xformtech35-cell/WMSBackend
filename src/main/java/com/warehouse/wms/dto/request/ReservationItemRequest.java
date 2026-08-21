package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationItemRequest {

    @NotBlank(message = "Item code is required")
    private String itemCode;

    private String itemName;

    private String uom;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private Integer quantity;

    private String batchNumber;

    // Optional: Specific bin/location to reserve from
    private String warehouseId;
    private String zoneId;
    private String aisleId;
    private String rackId;
    private String levelId;
    private String binId;

    // Optional: Reserve from specific inventory stock ID
    private Long inventoryStockId;

    // Optional: Reserve by priority
    private String reservePriority; // FIFO, LIFO, SPECIFIC
}
