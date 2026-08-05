// ====== FILE: src/main/java/com/warehouse/wms/dto/request/SkuDimensionRequest.java ======
package com.warehouse.wms.dto.request;

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
public class SkuDimensionRequest {

    @NotNull(message = "Length is required")
    private BigDecimal lengthCm;

    @NotNull(message = "Width is required")
    private BigDecimal widthCm;

    @NotNull(message = "Height is required")
    private BigDecimal heightCm;

    @NotNull(message = "Weight is required")
    private BigDecimal weightG;
}