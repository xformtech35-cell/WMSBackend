package com.warehouse.wms.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyPerformanceResponse {
    private String month;
    private Integer inboundVolume;
    private Integer outboundVolume;
    private Double revenue;
}

