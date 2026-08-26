package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryChallanItemRequest {
    @NotBlank(message = "Item code is required")
    private String itemCode;

    private String itemName;
    private String uom;
    private Integer orderedQuantity;
    private Integer dispatchedQuantity;
    private Integer deliveredQuantity;
    private Integer shortQuantity;
    private String batchNumber;
    private String serialNumbers;
    private Double unitPrice;
    private Double totalPrice;
    private Double weight;
    private Double volume;
    private String remarks;
}