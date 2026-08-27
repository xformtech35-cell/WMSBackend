package com.warehouse.wms.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyInboundResponse {
    private String date;
    private Integer grnCount;
    private Integer itemsReceived;
    private Double weight;
}