package com.warehouse.wms.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierPerformanceResponse {
    private String supplierName;
    private Long grnCount;
    private Integer itemsReceived;
    private Double onTimeRate;
    private Double qualityRate;
}