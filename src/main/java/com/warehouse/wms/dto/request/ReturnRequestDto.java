package com.warehouse.wms.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReturnRequestDto {

    @NotNull
    private Long deliveryId;

    @NotBlank
    private String returnReason;

    private String remarks;
    private String requestedBy;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<ReturnItemRequestDto> items;
}