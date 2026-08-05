// ====== FILE: src/main/java/com/warehouse/wms/dto/BinResponse.java ======
package com.warehouse.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private String status;
    private String fullLocation;
    
    // ✅ Add rack information (optional)
    private Long rackId;
    private String rackName;
    
    // ✅ Or include full rack object (but this may cause circular dependency)
    // private RackResponse rack;
}