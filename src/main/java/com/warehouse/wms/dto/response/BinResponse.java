// ====== FILE: src/main/java/com/warehouse/wms/dto/response/BinResponse.java ======
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
    
    // Rack information (only ID and Name, not full object)
    private Long rackId;
    private String rackName;
    
    // Level information
    private Long levelId;
    private String levelName;
    
    // ❌ REMOVE this - duplicate rack object
    // private RackResponse rack;
    
    // ✅ Keep only level with full hierarchy
    @JsonIgnoreProperties({"bins"})
    private LevelResponse level;
}