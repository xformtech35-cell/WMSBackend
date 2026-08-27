package com.warehouse.wms.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyPerformanceResponse {
    private String week;
    private Integer inboundOrders;
    private Integer outboundOrders;
    private Double productivity;
}