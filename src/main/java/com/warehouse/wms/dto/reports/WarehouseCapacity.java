package com.warehouse.wms.dto.reports;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseCapacity {
    private Double totalCapacity;
    private Double usedCapacity;
    private Double availableCapacity;
    private Double utilizationPercentage;
    private Long totalBins;
    private Long occupiedBins;
    private Long emptyBins;
    private Double binUtilization;
    private List<ZoneCapacityResponse> zoneCapacity;
    private List<BinStatusResponse> binStatus;
}
