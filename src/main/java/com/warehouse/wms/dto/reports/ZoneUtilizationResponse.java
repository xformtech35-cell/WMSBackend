package com.warehouse.wms.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneUtilizationResponse {
    private String zone;
    private Integer inboundUsage;
    private Integer outboundUsage;
    private Double utilization;
}