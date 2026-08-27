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
public class InboundStats {
    private Long totalGRN;
    private Long pendingGRN;
    private Long completedGRN;
    private Long cancelledGRN;
    private Long todayGRN;
    private Long thisWeekGRN;
    private Long thisMonthGRN;
    private Long totalItemsReceived;
    private Double totalWeightReceived;
    private Double totalVolumeReceived;
    private Integer avgProcessingTimeHours;  // Change from Integer to Double
    private Long totalPutawayTasks;
    private Long pendingPutaway;
    private Long completedPutaway;
    private Long totalSuppliers;
    private String topSupplier;
    private Long topSupplierCount;
    private List<GRNStatusCountResponse> grnStatusCounts;
    private List<SupplierPerformanceResponse> supplierPerformance;
    private List<DailyInboundResponse> dailyInbound;
}