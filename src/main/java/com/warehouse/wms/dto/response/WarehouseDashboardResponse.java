package com.warehouse.wms.dto.response;

import java.util.List;

import com.warehouse.wms.dto.reports.AlertResponse;
import com.warehouse.wms.dto.reports.ChartsData;
import com.warehouse.wms.dto.reports.InboundStats;
import com.warehouse.wms.dto.reports.InventoryOverview;
import com.warehouse.wms.dto.reports.OutboundStats;
import com.warehouse.wms.dto.reports.PerformanceMetrics;
import com.warehouse.wms.dto.reports.RecentActivityResponse;
import com.warehouse.wms.dto.reports.SummaryOverview;
import com.warehouse.wms.dto.reports.WarehouseCapacity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseDashboardResponse {
    
    // ====== Time & Date ======
    private String generatedAt;
    private String dateRange;
    
    // ====== Summary Overview ======
    private SummaryOverview summaryOverview;
    
    // ====== Inbound Statistics ======
    private InboundStats inboundStats;
    
    // ====== Outbound Statistics ======
    private OutboundStats outboundStats;
    
    // ====== Inventory Overview ======
    private InventoryOverview inventoryOverview;
    
    // ====== Warehouse Capacity ======
    private WarehouseCapacity warehouseCapacity;
    
    // ====== Performance Metrics ======
    private PerformanceMetrics performanceMetrics;
    
    // ====== Charts Data ======
    private ChartsData chartsData;
    
    // ====== Recent Activities ======
    private List<RecentActivityResponse> recentActivities;
    
    // ====== Alerts & Notifications ======
    private List<AlertResponse> alerts;
}

// ====== Summary Overview ======































