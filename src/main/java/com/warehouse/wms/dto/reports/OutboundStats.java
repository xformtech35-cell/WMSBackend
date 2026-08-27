package com.warehouse.wms.dto.reports;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboundStats {
    private Long totalOrders;
    private Long todayOrders;
    private Long thisWeekOrders;
    private Long thisMonthOrders;
    private Long pendingOrders;
    private Long processingOrders;
    private Long completedOrders;
    private Long cancelledOrders;
    private Long totalItemsShipped;
    private Double totalWeightShipped;
    private Double totalVolumeShipped;
    private Integer avgProcessingTimeHours;
    private Long totalPickLists;
    private Long pendingPickLists;
    private Long completedPickLists;
    private Long totalPickTasks;
    private Long pendingPickTasks;
    private Long completedPickTasks;
    private Long totalPackages;
    private Long totalShipments;
    private Long totalDeliveries;
    private String topCustomer;
    private Long topCustomerOrders;
    private String topItem;
    private Integer topItemQuantity;
    private String topTransporter;
    private Long topTransporterCount;
    private List<OrderStatusCountResponse> orderStatusCounts;
    private List<DailyOutboundResponse> dailyOutbound;
    private List<PickPerformanceResponse> pickPerformance;
}