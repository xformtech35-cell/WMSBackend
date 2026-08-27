package com.warehouse.wms.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryOverview {
    private Long totalOrders;
    private Long totalInbound;
    private Long totalOutbound;
    private Long totalInventory;
    private Long totalCustomers;
    private Long totalSuppliers;
    private Double totalRevenue;
    private Double totalCost;
    private Double profitMargin;
}


