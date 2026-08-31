// ====== FILE: src/main/java/com/warehouse/wms/dto/response/BinResponse.java ======
package com.warehouse.wms.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.warehouse.wms.entity.Bin.BinStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BinResponse {
    private Long id;
    private String barcode;
    private BigDecimal lengthCm;
    private BigDecimal widthCm;
    private BigDecimal heightCm;
    private BigDecimal volumeCm3;
    private BigDecimal maxWeightG;
    private BigDecimal occupiedVolumeCm3;
    private BigDecimal occupiedWeightG;
    private BigDecimal utilizationPercentage;
    private BinStatus status;
    private String fullLocation;
    
    
    private Integer maxCapacity;
    private Integer minCapacity;

    private String capacityUnit;
    
    // ✅ Available space (calculated fields)

    
    // Barcode fields
    private String barcodeData;
    private String barcodeImage;
    private String barcodeFormat;

    // Stock availability
    private StockAvailabilitySummary stockSummary;
    
    private String unit;
    private Boolean isActive;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Rack information (only ID and Name, not full object)
    private Long rackId;
    private String rackName;
    
    // Level information
    private Long levelId;
    private String levelName;
    
    // ✅ Level with full hierarchy (break circular reference)
    @JsonIgnoreProperties({"bins"})
    private LevelResponse level;
}