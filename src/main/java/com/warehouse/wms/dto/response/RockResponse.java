// ====== FILE: src/main/java/com/warehouse/wms/dto/response/RockResponse.java ======
package com.warehouse.wms.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RockResponse {
    private Long id;
    private String rockId;
    private String name;
    private String description;
    private String rockType;
    private BigDecimal weightKg;      // ✅ Changed to BigDecimal
    private BigDecimal lengthCm;      // ✅ Changed to BigDecimal
    private BigDecimal widthCm;       // ✅ Changed to BigDecimal
    private BigDecimal heightCm;      // ✅ Changed to BigDecimal
    private BigDecimal volumeCm3;     // ✅ Changed to BigDecimal
    private BigDecimal densityGcm3;   // ✅ Changed to BigDecimal
    private String unit;

    private String color;
    private Integer hardness;
    private Boolean isActive;
    private Integer quantity;
    private Integer minQuantity;
    private Integer maxQuantity;
    private BigDecimal unitPrice;     // ✅ Changed to BigDecimal
    private BigDecimal totalWeight;   // ✅ Changed to BigDecimal
    private String remarks;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @JsonIgnoreProperties({"zones"})
    private WarehouseResponse warehouse;
}