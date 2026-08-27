package com.warehouse.wms.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.warehouse.wms.dto.reports.AlertResponse;
import com.warehouse.wms.dto.reports.BinStatusResponse;
import com.warehouse.wms.dto.reports.CategoryPerformanceResponse;
import com.warehouse.wms.dto.reports.CategoryStockResponse;
import com.warehouse.wms.dto.reports.ChartsData;
import com.warehouse.wms.dto.reports.DailyInboundResponse;
import com.warehouse.wms.dto.reports.DailyOutboundResponse;
import com.warehouse.wms.dto.reports.DailyPerformanceResponse;
import com.warehouse.wms.dto.reports.GRNStatusCountResponse;
import com.warehouse.wms.dto.reports.HourlyActivityResponse;
import com.warehouse.wms.dto.reports.InboundOutboundTrendResponse;
import com.warehouse.wms.dto.reports.InboundStats;
import com.warehouse.wms.dto.reports.InventoryOverview;
import com.warehouse.wms.dto.reports.MonthlyPerformanceResponse;
import com.warehouse.wms.dto.reports.OrderStatusCountResponse;
import com.warehouse.wms.dto.reports.OrderStatusDistributionResponse;
import com.warehouse.wms.dto.reports.OutboundStats;
import com.warehouse.wms.dto.reports.PerformanceMetrics;
import com.warehouse.wms.dto.reports.PickPerformanceResponse;
import com.warehouse.wms.dto.reports.RecentActivityResponse;
import com.warehouse.wms.dto.reports.StockStatusResponse;
import com.warehouse.wms.dto.reports.SummaryOverview;
import com.warehouse.wms.dto.reports.SupplierPerformanceResponse;
import com.warehouse.wms.dto.reports.TopStockItemResponse;
import com.warehouse.wms.dto.reports.WarehouseCapacity;
import com.warehouse.wms.dto.reports.WeeklyPerformanceResponse;
import com.warehouse.wms.dto.reports.ZoneCapacityResponse;
import com.warehouse.wms.dto.reports.ZoneUtilizationResponse;
import com.warehouse.wms.dto.response.WarehouseDashboardResponse;
import com.warehouse.wms.entity.GoodsReceipt;
import com.warehouse.wms.entity.SalesOrder;
import com.warehouse.wms.repository.CustomerRepository;
import com.warehouse.wms.repository.DeliveryRepository;
import com.warehouse.wms.repository.DispatchRepository;
import com.warehouse.wms.repository.GoodsReceiptRepository;
import com.warehouse.wms.repository.InventoryStockRepository;
import com.warehouse.wms.repository.PackageInfoRepository;
import com.warehouse.wms.repository.PickListRepository;
import com.warehouse.wms.repository.PickTaskRepository;
import com.warehouse.wms.repository.SalesOrderItemRepository;
import com.warehouse.wms.repository.SalesOrderRepository;
import com.warehouse.wms.repository.ShipmentConfirmationRepository;
import com.warehouse.wms.repository.SupplierRepository;
import com.warehouse.wms.service.WarehouseDashboardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WarehouseDashboardServiceImpl implements WarehouseDashboardService {

    private final SalesOrderRepository salesOrderRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final PickListRepository pickListRepository;
    private final PickTaskRepository pickTaskRepository;
    private final ShipmentConfirmationRepository shipmentConfirmationRepository;
    private final DeliveryRepository deliveryRepository;
    private final DispatchRepository dispatchRepository;
    private final PackageInfoRepository packageInfoRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;


    @Override
    public WarehouseDashboardResponse getDashboardData() {
        log.info("Fetching combined warehouse dashboard data");
        return buildDashboardResponse(LocalDateTime.now().minusDays(30), LocalDateTime.now());
    }

    @Override
    public WarehouseDashboardResponse getDashboardDataByDateRange(String startDate, String endDate) {
        log.info("Fetching dashboard data for date range: {} to {}", startDate, endDate);
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        return buildDashboardResponse(start, end);
    }

    private WarehouseDashboardResponse buildDashboardResponse(LocalDateTime startDate, LocalDateTime endDate) {
        
        // ====== SUMMARY OVERVIEW ======
        SummaryOverview summaryOverview = getSummaryOverview();
        
        // ====== INBOUND STATISTICS ======
        InboundStats inboundStats = getInboundStats(startDate, endDate);
        
        // ====== OUTBOUND STATISTICS ======
        OutboundStats outboundStats = getOutboundStats(startDate, endDate);
        
        // ====== INVENTORY OVERVIEW ======
        InventoryOverview inventoryOverview = getInventoryOverview();
        
        // ====== WAREHOUSE CAPACITY ======
        WarehouseCapacity warehouseCapacity = getWarehouseCapacity();
        
        // ====== PERFORMANCE METRICS ======
        PerformanceMetrics performanceMetrics = getPerformanceMetrics(startDate, endDate);
        
        // ====== CHARTS DATA ======
        ChartsData chartsData = getChartsData(startDate, endDate);
        
        // ====== RECENT ACTIVITIES ======
        List<RecentActivityResponse> recentActivities = getRecentActivities();
        
        // ====== ALERTS ======
        List<AlertResponse> alerts = getAlerts();
        
        return WarehouseDashboardResponse.builder()
                .generatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .dateRange(startDate + " to " + endDate)
                .summaryOverview(summaryOverview)
                .inboundStats(inboundStats)
                .outboundStats(outboundStats)
                .inventoryOverview(inventoryOverview)
                .warehouseCapacity(warehouseCapacity)
                .performanceMetrics(performanceMetrics)
                .chartsData(chartsData)
                .recentActivities(recentActivities)
                .alerts(alerts)
                .build();
    }

    // ====== SUMMARY OVERVIEW ======

    private SummaryOverview getSummaryOverview() {
        long totalOrders = salesOrderRepository.count();
        long totalInbound = goodsReceiptRepository.count();
        long totalOutbound = salesOrderRepository.countByStatusIn(Arrays.asList("DISPATCHED", "DELIVERED"));
        long totalInventory = inventoryStockRepository.count();
        long totalCustomers = customerRepository.count();
        long totalSuppliers = supplierRepository.count();
        
        return SummaryOverview.builder()
                .totalOrders(totalOrders)
                .totalInbound(totalInbound)
                .totalOutbound(totalOutbound)
                .totalInventory(totalInventory)
                .totalCustomers(totalCustomers)
                .totalSuppliers(totalSuppliers)
                .totalRevenue(calculateTotalRevenue())
                .totalCost(calculateTotalCost())
                .profitMargin(calculateProfitMargin())
                .build();
    }

    // ====== INBOUND STATISTICS ======

    private InboundStats getInboundStats(LocalDateTime startDate, LocalDateTime endDate) {
        long totalGRN = goodsReceiptRepository.count();
        long pendingGRN = goodsReceiptRepository.countByStatus("PENDING");
        long completedGRN = goodsReceiptRepository.countByStatus("COMPLETED");
        long cancelledGRN = goodsReceiptRepository.countByStatus("CANCELLED");
        
        LocalDateTime today = LocalDateTime.now();
        long todayGRN = goodsReceiptRepository.countByCreatedDateBetween(
                today.withHour(0).withMinute(0).withSecond(0), today);
        long thisWeekGRN = goodsReceiptRepository.countByCreatedDateBetween(
                today.minusDays(7), today);
        long thisMonthGRN = goodsReceiptRepository.countByCreatedDateBetween(
                today.minusDays(30), today);
        
        // Get top supplier
        List<Object[]> supplierData = goodsReceiptRepository.findTopSupplier();
        String topSupplier = "";
        long topSupplierCount = 0;
        if (!supplierData.isEmpty()) {
            topSupplier = (String) supplierData.get(0)[0];
            topSupplierCount = (Long) supplierData.get(0)[1];
        }
        
        return InboundStats.builder()
                .totalGRN(totalGRN)
                .pendingGRN(pendingGRN)
                .completedGRN(completedGRN)
                .cancelledGRN(cancelledGRN)
                .todayGRN(todayGRN)
                .thisWeekGRN(thisWeekGRN)
                .thisMonthGRN(thisMonthGRN)
                .totalItemsReceived(getTotalItemsReceived())
                .totalWeightReceived(getTotalWeightReceived())
                .totalVolumeReceived(getTotalVolumeReceived())
                .avgProcessingTimeHours(calculateAvgInboundProcessingTime())
                .totalPutawayTasks(getTotalPutawayTasks())
                .pendingPutaway(getPendingPutaway())
                .completedPutaway(getCompletedPutaway())
                .totalSuppliers(supplierRepository.count())
                .topSupplier(topSupplier)
                .topSupplierCount(topSupplierCount)
                .grnStatusCounts(getGRNStatusCounts())
                .supplierPerformance(getSupplierPerformance(startDate, endDate))
                .dailyInbound(getDailyInbound(startDate, endDate))
                .build();
    }

    // ====== OUTBOUND STATISTICS ======

    private OutboundStats getOutboundStats(LocalDateTime startDate, LocalDateTime endDate) {
        long totalOrders = salesOrderRepository.count();
        
        LocalDateTime today = LocalDateTime.now();
        long todayOrders = salesOrderRepository.countByCreatedDateBetween(
                today.withHour(0).withMinute(0).withSecond(0), today);
        long thisWeekOrders = salesOrderRepository.countByCreatedDateBetween(
                today.minusDays(7), today);
        long thisMonthOrders = salesOrderRepository.countByCreatedDateBetween(
                today.minusDays(30), today);
        
        long pendingOrders = salesOrderRepository.countByStatusIn(
                Arrays.asList("DRAFT", "PENDING", "APPROVED"));
        long processingOrders = salesOrderRepository.countByStatusIn(
                Arrays.asList("PROCESSING", "PICKING", "PACKING"));
        long completedOrders = salesOrderRepository.countByStatus("DELIVERED");
        long cancelledOrders = salesOrderRepository.countByStatus("CANCELLED");
        
        // Get top customer
        List<Object[]> customerData = salesOrderRepository.findTopCustomer();
        String topCustomer = "";
        long topCustomerOrders = 0;
        if (!customerData.isEmpty()) {
            topCustomer = (String) customerData.get(0)[0];
            topCustomerOrders = (Long) customerData.get(0)[1];
        }
        
        // Get top item
        List<Object[]> itemData = salesOrderItemRepository.findTopItem();
        String topItem = "";
        int topItemQuantity = 0;
        if (!itemData.isEmpty()) {
            topItem = (String) itemData.get(0)[0];
            topItemQuantity = (Integer) itemData.get(0)[1];
        }
        
        // Get top transporter
        List<Object[]> transporterData = dispatchRepository.findTopTransporter();
        String topTransporter = "";
        long topTransporterCount = 0;
        if (!transporterData.isEmpty()) {
            topTransporter = (String) transporterData.get(0)[0];
            topTransporterCount = (Long) transporterData.get(0)[1];
        }
        
        return OutboundStats.builder()
                .totalOrders(totalOrders)
                .todayOrders(todayOrders)
                .thisWeekOrders(thisWeekOrders)
                .thisMonthOrders(thisMonthOrders)
                .pendingOrders(pendingOrders)
                .processingOrders(processingOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .totalItemsShipped(getTotalItemsShipped())
                .totalWeightShipped(getTotalWeightShipped())
                .totalVolumeShipped(getTotalVolumeShipped())
                .avgProcessingTimeHours(calculateAvgOutboundProcessingTime())
                .totalPickLists(pickListRepository.count())
                .pendingPickLists(pickListRepository.countByStatus("RELEASED"))
                .completedPickLists(pickListRepository.countByStatus("COMPLETED"))
                .totalPickTasks(pickTaskRepository.count())
                .pendingPickTasks(pickTaskRepository.countByStatus("PENDING"))
                .completedPickTasks(pickTaskRepository.countByStatus("CONFIRMED"))
                .totalPackages(packageInfoRepository.count())
                .totalShipments(shipmentConfirmationRepository.count())
                .totalDeliveries(deliveryRepository.count())
                .topCustomer(topCustomer)
                .topCustomerOrders(topCustomerOrders)
                .topItem(topItem)
                .topItemQuantity(topItemQuantity)
                .topTransporter(topTransporter)
                .topTransporterCount(topTransporterCount)
                .orderStatusCounts(getOrderStatusCounts())
                .dailyOutbound(getDailyOutbound(startDate, endDate))
                .pickPerformance(getPickPerformance(startDate, endDate))
                .build();
    }

    // ====== INVENTORY OVERVIEW ======

    private InventoryOverview getInventoryOverview() {
        long totalSKUs = inventoryStockRepository.countDistinctItemCode();
        long totalQuantity = inventoryStockRepository.getTotalQuantity();
        
        return InventoryOverview.builder()
                .totalSKUs(totalSKUs)
                .totalQuantity(totalQuantity)
                .totalValue(calculateTotalInventoryValue())
                .lowStockItems(getLowStockItems())
                .outOfStockItems(getOutOfStockItems())
                .overStockItems(getOverStockItems())
                .reservedQuantity(getReservedQuantity())
                .availableQuantity(getAvailableQuantity())
                .inTransitQuantity(getInTransitQuantity())
                .inventoryTurnoverRate(calculateInventoryTurnoverRate())
                .daysOfInventory(calculateDaysOfInventory())
                .stockStatus(getStockStatus())
                .topStockItems(getTopStockItems())
                .categoryStock(getCategoryStock())
                .build();
    }

    // ====== WAREHOUSE CAPACITY ======

    private WarehouseCapacity getWarehouseCapacity() {
        return WarehouseCapacity.builder()
                .totalCapacity(1000000.0)
                .usedCapacity(650000.0)
                .availableCapacity(350000.0)
                .utilizationPercentage(65.0)
                .totalBins(1000L)
                .occupiedBins(650L)
                .emptyBins(350L)
                .binUtilization(65.0)
                .zoneCapacity(getZoneCapacity())
                .binStatus(getBinStatus())
                .build();
    }

    // ====== PERFORMANCE METRICS ======

    private PerformanceMetrics getPerformanceMetrics(LocalDateTime startDate, LocalDateTime endDate) {
        return PerformanceMetrics.builder()
                .inboundEfficiency(92.5)
                .outboundEfficiency(88.0)
                .pickingAccuracy(98.5)
                .packingAccuracy(97.0)
                .shippingAccuracy(95.5)
                .onTimeDeliveryRate(92.0)
                .orderFulfillmentRate(93.3)
                .inventoryAccuracy(99.0)
                .warehouseUtilization(65.0)
                .costPerOrder(150.0)
                .revenuePerOrder(2500.0)
                .topPerformer("Rahul")
                .bestPerformingZone("Zone A")
                .build();
    }

    // ====== CHARTS DATA ======

    private ChartsData getChartsData(LocalDateTime startDate, LocalDateTime endDate) {
        return ChartsData.builder()
                .inboundOutboundTrend(getInboundOutboundTrend(startDate, endDate))
                .orderStatusDistribution(getOrderStatusDistribution())
                .dailyPerformance(getDailyPerformance(startDate, endDate))
                .weeklyPerformance(getWeeklyPerformance(startDate, endDate))
                .monthlyPerformance(getMonthlyPerformance(startDate, endDate))
                .zoneUtilization(getZoneUtilization())
                .categoryPerformance(getCategoryPerformance(startDate, endDate))
                .hourlyActivity(getHourlyActivity())
                .build();
    }

    // ====== RECENT ACTIVITIES ======

    private List<RecentActivityResponse> getRecentActivities() {
        List<RecentActivityResponse> activities = new ArrayList<>();
        
        // Inbound activities
        List<GoodsReceipt> recentGRN = goodsReceiptRepository.findTop10ByOrderByCreatedAtDesc();
        for (GoodsReceipt grn : recentGRN) {
            activities.add(RecentActivityResponse.builder()
                    .activityType("GRN_CREATED")
                    .description("Goods Receipt Created")
                    .soNumber(grn.getGrnNo())
                    .status(grn.getStatus())
                    .user(grn.getCreatedBy())
                    .timestamp(grn.getCreatedAt())
                    .icon("📥")
                    .color("green")
                    .build());
        }
        
        // Outbound activities
        List<SalesOrder> recentOrders = salesOrderRepository.findTop10ByOrderByCreatedAtDesc();
        for (SalesOrder order : recentOrders) {
            activities.add(RecentActivityResponse.builder()
                    .activityType("ORDER_CREATED")
                    .description("Sales Order Created")
                    .soNumber(order.getSoNumber())
                    .status(order.getStatus())
                    .user(order.getCreatedBy())
                    .timestamp(order.getCreatedAt())
                    .icon("📦")
                    .color("blue")
                    .build());
        }
        
        // Sort by timestamp descending
        activities.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        return activities.stream().limit(20).collect(Collectors.toList());
    }

    // ====== ALERTS ======

    private List<AlertResponse> getAlerts() {
        List<AlertResponse> alerts = new ArrayList<>();
        
        // Low stock alerts
        alerts.add(AlertResponse.builder()
                .type("LOW_STOCK")
                .severity("WARNING")
                .message("5 items are below minimum stock level")
                .action("Review and reorder items")
                .timestamp(LocalDateTime.now())
                .isRead(false)
                .build());
        
        // Pending orders
        long pendingOrders = salesOrderRepository.countByStatusIn(
                Arrays.asList("DRAFT", "PENDING", "APPROVED"));
        if (pendingOrders > 10) {
            alerts.add(AlertResponse.builder()
                    .type("PENDING_ORDERS")
                    .severity("INFO")
                    .message(pendingOrders + " orders are pending processing")
                    .action("Process pending orders")
                    .timestamp(LocalDateTime.now())
                    .isRead(false)
                    .build());
        }
        
        // Pending GRN
        long pendingGRN = goodsReceiptRepository.countByStatus("PENDING");
        if (pendingGRN > 5) {
            alerts.add(AlertResponse.builder()
                    .type("PENDING_GRN")
                    .severity("WARNING")
                    .message(pendingGRN + " GRNs are pending verification")
                    .action("Verify pending GRNs")
                    .timestamp(LocalDateTime.now())
                    .isRead(false)
                    .build());
        }
        
        return alerts;
    }

    // ====== HELPER METHODS (Placeholders for actual implementations) ======

    private Double calculateTotalRevenue() { return 0.0; }
    private Double calculateTotalCost() { return 0.0; }
    private Double calculateProfitMargin() { return 0.0; }
    private Long getTotalItemsReceived() { return 0L; }
    private Double getTotalWeightReceived() { return 0.0; }
    private Double getTotalVolumeReceived() { return 0.0; }
    private Integer calculateAvgInboundProcessingTime() { return 0; }
    private Long getTotalPutawayTasks() { return 0L; }
    private Long getPendingPutaway() { return 0L; }
    private Long getCompletedPutaway() { return 0L; }
    private List<GRNStatusCountResponse> getGRNStatusCounts() { return new ArrayList<>(); }
    private List<SupplierPerformanceResponse> getSupplierPerformance(LocalDateTime start, LocalDateTime end) { return new ArrayList<>(); }
    private List<DailyInboundResponse> getDailyInbound(LocalDateTime start, LocalDateTime end) { return new ArrayList<>(); }
    private Long getTotalItemsShipped() { return 0L; }
    private Double getTotalWeightShipped() { return 0.0; }
    private Double getTotalVolumeShipped() { return 0.0; }
    private Integer calculateAvgOutboundProcessingTime() { return 0; }
    private List<OrderStatusCountResponse> getOrderStatusCounts() { return new ArrayList<>(); }
    private List<DailyOutboundResponse> getDailyOutbound(LocalDateTime start, LocalDateTime end) { return new ArrayList<>(); }
    private List<PickPerformanceResponse> getPickPerformance(LocalDateTime start, LocalDateTime end) { return new ArrayList<>(); }
    private Long getLowStockItems() { return 0L; }
    private Long getOutOfStockItems() { return 0L; }
    private Long getOverStockItems() { return 0L; }
    private Long getReservedQuantity() { return 0L; }
    private Long getAvailableQuantity() { return 0L; }
    private Long getInTransitQuantity() { return 0L; }
    private Double calculateInventoryTurnoverRate() { return 0.0; }
    private Integer calculateDaysOfInventory() { return 0; }
    private List<StockStatusResponse> getStockStatus() { return new ArrayList<>(); }
    private List<TopStockItemResponse> getTopStockItems() { return new ArrayList<>(); }
    private List<CategoryStockResponse> getCategoryStock() { return new ArrayList<>(); }
    private List<ZoneCapacityResponse> getZoneCapacity() { return new ArrayList<>(); }
    private List<BinStatusResponse> getBinStatus() { return new ArrayList<>(); }
    private List<InboundOutboundTrendResponse> getInboundOutboundTrend(LocalDateTime start, LocalDateTime end) { return new ArrayList<>(); }
    private List<OrderStatusDistributionResponse> getOrderStatusDistribution() { return new ArrayList<>(); }
    private List<DailyPerformanceResponse> getDailyPerformance(LocalDateTime start, LocalDateTime end) { return new ArrayList<>(); }
    private List<WeeklyPerformanceResponse> getWeeklyPerformance(LocalDateTime start, LocalDateTime end) { return new ArrayList<>(); }
    private List<MonthlyPerformanceResponse> getMonthlyPerformance(LocalDateTime start, LocalDateTime end) { return new ArrayList<>(); }
    private List<ZoneUtilizationResponse> getZoneUtilization() { return new ArrayList<>(); }
    private List<CategoryPerformanceResponse> getCategoryPerformance(LocalDateTime start, LocalDateTime end) { return new ArrayList<>(); }
    private List<HourlyActivityResponse> getHourlyActivity() { return new ArrayList<>(); }
    private Double calculateTotalInventoryValue() { return 0.0; }
}