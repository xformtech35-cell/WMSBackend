package com.warehouse.wms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReturnOrderRequest {
    @NotNull(message = "originalOrderId is required")
    private Long originalOrderId;

    private String customerRef;

    @Valid
    @NotEmpty(message = "lines are required")
    private List<ReturnLineRequest> lines;
}
