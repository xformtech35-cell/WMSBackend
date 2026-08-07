// ====== FILE: src/main/java/com/warehouse/wms/dto/request/RockRequest.java ======
package com.warehouse.wms.dto.request;

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
public class RockRequest {

    @NotBlank(message = "Rock ID is required")
    private String rockId;

    @NotBlank(message = "Rock name is required")
    private String name;

    private String description;

    private String rockType;

    private BigDecimal weightKg;  // ✅ Changed to BigDecimal

    private BigDecimal lengthCm;  // ✅ Changed to BigDecimal

    private BigDecimal widthCm;   // ✅ Changed to BigDecimal

    private BigDecimal heightCm;  // ✅ Changed to BigDecimal

    private BigDecimal densityGcm3;  // ✅ Changed to BigDecimal

    private String unit;

    private String color;

    private Integer hardness;

    private Boolean isActive = true;

    private Integer quantity = 0;

    private Integer minQuantity = 0;

    private Integer maxQuantity = 0;

    private BigDecimal unitPrice;  // ✅ Changed to BigDecimal

    private String createdBy;

    private String remarks;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;
}