package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReturnItemRequestDto {

    @NotBlank(message = "Item code is required")
    private String itemCode;

    private String itemName;
    private String sku;
    private String uom;
    private String batchNumber;

    private Integer deliveredQty;

    @NotNull
    @Min(1)
    private Integer returnQty;

    private String returnReason;
    private String remarks;
    private Double unitPrice;
}