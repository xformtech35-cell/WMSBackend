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
public class ChartsData {
    private List<InboundOutboundTrendResponse> inboundOutboundTrend;
    private List<OrderStatusDistributionResponse> orderStatusDistribution;
    private List<DailyPerformanceResponse> dailyPerformance;
    private List<WeeklyPerformanceResponse> weeklyPerformance;
    private List<MonthlyPerformanceResponse> monthlyPerformance;
    private List<ZoneUtilizationResponse> zoneUtilization;
    private List<CategoryPerformanceResponse> categoryPerformance;
    private List<HourlyActivityResponse> hourlyActivity;
}
