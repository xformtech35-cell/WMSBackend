package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickTaskItemRequest {

    @NotBlank(message = "Item code is required")
    private String itemCode;

    private String itemName;

    private String uom;

    @NotNull(message = "Required quantity is required")
    @Positive(message = "Required quantity must be greater than 0")
    private Integer requiredQuantity;

    @PositiveOrZero
    private Integer pickedQuantity;

    @PositiveOrZero
    private Integer shortQuantity;

    @PositiveOrZero
    private Integer quantityToPick;

    private String locationBarcode;

    private String itemBarcode;

    private String binId;

    private String batchNumber;

    private String sourceLocation;

    private String status;

    private String priority;

    private Boolean isScanned;

    private Long inventoryId;

    private Long salesOrderLineId;

    private String remarks;
}