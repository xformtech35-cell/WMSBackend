package com.warehouse.wms.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyPerformanceResponse {
    private String date;
    private Integer inboundTasks;
    private Integer outboundTasks;
    private Double efficiency;
}

