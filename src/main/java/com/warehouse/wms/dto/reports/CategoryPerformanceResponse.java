package com.warehouse.wms.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryPerformanceResponse {
    private String category;
    private Integer inboundQuantity;
    private Integer outboundQuantity;
    private Double turnover;
}
