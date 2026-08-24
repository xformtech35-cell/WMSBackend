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
public class SalesOrderItemUpdateRequest {

    @NotBlank(message = "Item code is required")
    private String itemCode;

    private String itemName;
    
    private String uom;

    @NotNull(message = "Ordered quantity is required")
    @Positive(message = "Ordered quantity must be greater than 0")
    private Integer orderedQuantity;

    private Integer reservedQuantity;
    private Integer pickedQuantity;
    private Integer shippedQuantity;
    private String batchNumber;
    private String sourceLocation;
    private String createdBy;
}