package com.warehouse.wms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RestockLineRequest {
    @NotBlank(message = "binBarcode is required")
    private String binBarcode;
}
