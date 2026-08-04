// ====== FILE: src/main/java/com/warehouse/wms/dto/response/BinLocationStatistics.java ======
package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BinLocationStatistics {
    private String warehouseId;
    private Long totalBins;
    private Long occupiedBins;
    private Long availableBins;
    private Long totalAvailableCapacity;
    private Double utilizationPercentage;
    
    public Double getUtilizationPercentage() {
        if (totalBins == null || totalBins == 0) {
            return 0.0;
        }
        return (occupiedBins != null ? occupiedBins.doubleValue() : 0) / totalBins * 100;
    }
}