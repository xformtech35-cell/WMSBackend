package com.warehouse.wms.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusCountResponse {
    
    private String status;
    private Long count;
    private Double percentage;
    private String color;
    private String icon;
    private String description;
    private String category; // INBOUND, OUTBOUND, ALL
}