package com.warehouse.wms.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickPerformanceResponse {
    private String pickerName;
    private Integer tasksCompleted;
    private Integer itemsPicked;
    private Double accuracy;
    private Double avgTime;
}

