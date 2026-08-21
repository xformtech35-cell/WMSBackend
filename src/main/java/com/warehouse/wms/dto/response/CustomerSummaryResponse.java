package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSummaryResponse {
    private Long totalCustomers;
    private Long activeCustomers;
    private Long inactiveCustomers;
    private Long blacklistedCustomers;
    private Long verifiedCustomers;
    private Double totalRevenue;
    private Integer totalOrders;
    private Double averageOrderValue;
    private List<CustomerTypeCountResponse> customerTypeCounts;
    private List<LoyaltyTierCountResponse> loyaltyTierCounts;
}


