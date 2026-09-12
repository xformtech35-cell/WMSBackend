package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GstRequest {

    private String code;

    private String name;

    @NotNull(message = "Rate is required")
    @DecimalMin(value = "0.00", message = "Rate cannot be negative")
    @DecimalMax(value = "100.00", message = "Rate cannot exceed 100%")
    private BigDecimal rate;

    private String description;

    private Boolean active;
}