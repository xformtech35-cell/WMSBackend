// ====== FILE: src/main/java/com/warehouse/wms/dto/BinResponse.java ======
package com.warehouse.wms.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.warehouse.wms.dto.response.RackResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    
    // ✅ Add rack information
    private Long rackId;
    private String rackName;
    
    @JsonIgnore  // ✅ Ignore full rack object to avoid circular reference
    private RackResponse rack;
}