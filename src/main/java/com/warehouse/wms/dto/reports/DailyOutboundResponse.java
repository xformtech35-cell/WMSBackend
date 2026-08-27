package com.warehouse.wms.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyOutboundResponse {
    private String date;
    private Integer orders;
    private Integer itemsShipped;
    private Double weight;
}