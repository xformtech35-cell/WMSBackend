// ====== FILE: src/main/java/com/warehouse/wms/dto/BinCreateRequest.java ======
package com.warehouse.wms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BinCreateRequest {

    @NotBlank(message = "Barcode is required")
    private String barcode;

    @NotNull(message = "Length is required")
    private BigDecimal lengthCm;

    @NotNull(message = "Width is required")
    private BigDecimal widthCm;

    @NotNull(message = "Height is required")
    private BigDecimal heightCm;

    @NotNull(message = "Max weight is required")
    private BigDecimal maxWeightG;

    @NotNull(message = "Rack ID is required")
    private Long rackId;
}