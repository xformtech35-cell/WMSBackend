// ====== FILE: src/main/java/com/warehouse/wms/dto/response/StockAvailabilityResponse.java ======
package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAvailabilityResponse {
    private Integer maxCapacity;
    private Integer totalQuantity;
    private Integer availableSlots;
    private Double utilizationPercentage;
}