package com.warehouse.wms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReturnLineRequest {
    @NotBlank(message = "skuCode is required")
    private String skuCode;

    @NotNull(message = "orderedQty is required")
    @Min(value = 1, message = "orderedQty must be at least 1")
    private Integer orderedQty;

    @NotNull(message = "returnedQty is required")
    @Min(value = 1, message = "returnedQty must be at least 1")
    private Integer returnedQty;

    private String batchNumber;
}
