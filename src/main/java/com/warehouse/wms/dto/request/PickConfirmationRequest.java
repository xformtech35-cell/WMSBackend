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
public class PickConfirmationRequest {

    @NotBlank(message = "Pick Task Number is required")
    private String pickTaskNumber;

    @NotBlank(message = "Item code is required")
    private String itemCode;

    @NotNull(message = "Picked quantity is required")
    @Positive(message = "Picked quantity must be greater than 0")
    private Integer pickedQuantity;

    private Integer shortQuantity;

    @NotBlank(message = "Barcode is required")
    private String barcode;

    @NotBlank(message = "Confirmed by is required")
    private String confirmedBy;

    private String remarks;
}