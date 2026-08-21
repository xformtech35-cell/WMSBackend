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
public class PackageRequest {

    @NotBlank(message = "SO Number is required")
    private String soNumber;

    @NotBlank(message = "Pick List Number is required")
    private String pickListNumber;

    @NotBlank(message = "Item code is required")
    private String itemCode;

    @NotNull(message = "Packed quantity is required")
    @Positive(message = "Packed quantity must be greater than 0")
    private Integer packedQuantity;

    @NotBlank(message = "Package type is required")
    private String packageType; // CARTON, BOX, PALLET, BAG

    private Double weight;
    private Double length;
    private Double width;
    private Double height;

    private String packedBy;

    private String createdBy;

    private String remarks;
}