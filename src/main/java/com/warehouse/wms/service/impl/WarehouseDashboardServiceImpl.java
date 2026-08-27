package com.warehouse.wms.service.impl;

import java.time.LocalDate;
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
import com.warehouse.wms.entity.Inbound;
import com.warehouse.wms.entity.InboundStatus;
import com.warehouse.wms.entity.SalesOrder;
import com.warehouse.wms.repository.CustomerRepository;
import com.warehouse.wms.repository.DeliveryRepository;
import com.warehouse.wms.repository.DispatchRepository;
import com.warehouse.wms.repository.GoodsReceiptRepository;
import com.warehouse.wms.repository.InboundLineRepository;
import com.warehouse.wms.repository.InboundRepository;
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
    private final InboundRepository inboundRepository;
    private final InboundLineRepository inboundLineRepository;


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
        long totalInbound = inboundRepository.count(); // ✅ Fixed
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
    // Convert LocalDateTime to LocalDate for repository calls
    LocalDate startLocalDate = startDate.toLocalDate();
    LocalDate endLocalDate = endDate.toLocalDate();
    
    // Use date range for all counts with LocalDate
    long totalInboundInRange = inboundRepository.countByInboundDateBetween(startLocalDate, endLocalDate);
    
    // Status counts with date range
    long pendingGRN = inboundRepository.countByStatusAndInboundDateBetween(
        InboundStatus.PENDING, startLocalDate, endLocalDate);
    long completedGRN = inboundRepository.countByStatusAndInboundDateBetween(
        InboundStatus.COMPLETED, startLocalDate, endLocalDate);
    long cancelledGRN = inboundRepository.countByStatusAndInboundDateBetween(
        InboundStatus.CANCELLED, startLocalDate, endLocalDate);
    
    // Today, this week, this month
    LocalDate today = LocalDate.now();
    long todayGRN = inboundRepository.countByInboundDateBetween(today, today);
    long thisWeekGRN = inboundRepository.countByInboundDateBetween(today.minusDays(7), today);
    long thisMonthGRN = inboundRepository.countByInboundDateBetween(today.minusDays(30), today);
    
    // Get top supplier with date range
    List<Object[]> supplierData = inboundRepository.findTopSupplier(startLocalDate, endLocalDate);
    String topSupplier = "";
    long topSupplierCount = 0;
    if (!supplierData.isEmpty()) {
        topSupplier = (String) supplierData.get(0)[0];
        topSupplierCount = safeGetLong(supplierData.get(0)[1]);
    }
    
    return InboundStats.builder()
            .totalGRN(totalInboundInRange)
            .pendingGRN(pendingGRN)
            .completedGRN(completedGRN)
            .cancelledGRN(cancelledGRN)
            .todayGRN(todayGRN)
            .thisWeekGRN(thisWeekGRN)
            .thisMonthGRN(thisMonthGRN)
            .totalItemsReceived(getTotalItemsReceived(startDate, endDate))
            .totalWeightReceived(getTotalWeightReceived(startDate, endDate))
            .totalVolumeReceived(getTotalVolumeReceived(startDate, endDate))
            .avgProcessingTimeHours(calculateAvgInboundProcessingTime(startDate, endDate))
            .totalPutawayTasks(getTotalPutawayTasks(startDate, endDate))
            .pendingPutaway(getPendingPutaway(startDate, endDate))
            .completedPutaway(getCompletedPutaway(startDate, endDate))
            .totalSuppliers(supplierRepository.count())
            .topSupplier(topSupplier)
            .topSupplierCount(topSupplierCount)
            .grnStatusCounts(getGRNStatusCounts(startDate, endDate))
            .supplierPerformance(getSupplierPerformance(startDate, endDate))
            .dailyInbound(getDailyInbound(startDate, endDate))
            .build();
}
    
    
  private Long getTotalItemsReceived(LocalDateTime startDate, LocalDateTime endDate) {
	    try {
	        LocalDate startLocalDate = startDate.toLocalDate();
	        LocalDate endLocalDate = endDate.toLocalDate();
	        return inboundLineRepository.sumReceivedQuantityByDateRange(startLocalDate, endLocalDate);
	    } catch (Exception e) {
	        log.error("Error calculating total items received: {}", e.getMessage(), e);
	        return 0L;
	    }
	}

	private Double getTotalWeightReceived(LocalDateTime startDate, LocalDateTime endDate) {
	    try {
	        LocalDate startLocalDate = startDate.toLocalDate();
	        LocalDate endLocalDate = endDate.toLocalDate();
	        return inboundLineRepository.sumWeightByDateRange(startLocalDate, endLocalDate);
	    } catch (Exception e) {
	        log.error("Error calculating total weight received: {}", e.getMessage(), e);
	        return 0.0;
	    }
	}

	private Double getTotalVolumeReceived(LocalDateTime startDate, LocalDateTime endDate) {
	    try {
	        LocalDate startLocalDate = startDate.toLocalDate();
	        LocalDate endLocalDate = endDate.toLocalDate();
	        return inboundLineRepository.sumVolumeByDateRange(startLocalDate, endLocalDate);
	    } catch (Exception e) {
	        log.error("Error calculating total volume received: {}", e.getMessage(), e);
	        return 0.0;
	    }
	}

	private Integer calculateAvgInboundProcessingTime(LocalDateTime startDate, LocalDateTime endDate) {
	    try {
	        LocalDate startLocalDate = startDate.toLocalDate();
	        LocalDate endLocalDate = endDate.toLocalDate();
	        Double avgMinutes = inboundRepository.calculateAvgProcessingTime(startLocalDate, endLocalDate);
	        if (avgMinutes == null || avgMinutes == 0) {
	            return 0;
	        }
	        // Convert minutes to hours (round up)
	        return (int) Math.ceil(avgMinutes / 60.0);
	    } catch (Exception e) {
	        log.error("Error calculating average inbound processing time: {}", e.getMessage(), e);
	        return 0;
	    }
	}
    private Long getTotalPutawayTasks(LocalDateTime startDate, LocalDateTime endDate) {
        // Implement using relevant repository
        return 0L; // Placeholder - implement as needed
    }

    private Long getPendingPutaway(LocalDateTime startDate, LocalDateTime endDate) {
        // Implement using relevant repository
        return 0L; // Placeholder - implement as needed
    }

    private Long getCompletedPutaway(LocalDateTime startDate, LocalDateTime endDate) {
        // Implement using relevant repository
        return 0L; // Placeholder - implement as needed
    }

    private List<GRNStatusCountResponse> getGRNStatusCounts(LocalDateTime startDate, LocalDateTime endDate) {
        return new ArrayList<>(); // Implement as needed
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
    
    // Get top customer - FIXED
    // Query returns: [customer_code, customer_name, count(id)]
    List<Object[]> customerData = salesOrderRepository.findTopCustomer();
    String topCustomer = "";
    long topCustomerOrders = 0;
    if (!customerData.isEmpty()) {
        topCustomer = (String) customerData.get(0)[1];  // customer_name is at index 1
        topCustomerOrders = safeGetLong(customerData.get(0)[2]);  // count is at index 2
    }
    
    // Get top item - FIXED
    // Query returns: [item_code, item_name, sum(ordered_quantity), count(distinct so_number), uom]
    List<Object[]> itemData = salesOrderItemRepository.findTopItem();
    String topItem = "";
    int topItemQuantity = 0;
    if (!itemData.isEmpty()) {
        topItem = (String) itemData.get(0)[1];  // item_name is at index 1 (more user-friendly)
        topItemQuantity = safeGetInt(itemData.get(0)[2]);  // sum(ordered_quantity) is at index 2
    }
    
    // Get top transporter - FIXED
    // Query returns: [transporter_name, count(id)] (assuming this is the structure)
    List<Object[]> transporterData = dispatchRepository.findTopTransporter();
    String topTransporter = "";
    long topTransporterCount = 0;
    if (!transporterData.isEmpty()) {
        topTransporter = (String) transporterData.get(0)[0];  // transporter_name
        topTransporterCount = safeGetLong(transporterData.get(0)[1]);  // count
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
        List<Inbound> recentInbound = inboundRepository.findTop10ByOrderByInboundDateDesc();
        for (Inbound inbound : recentInbound) {
            activities.add(RecentActivityResponse.builder()
                    .activityType("INBOUND_CREATED")
                    .description("Inbound Created")
                    .soNumber(inbound.getInboundNumber())  // or getGrnNumber()
                    .status(inbound.getStatus() != null ? inbound.getStatus().name() : "UNKNOWN")
                    .user(inbound.getCreatedBy() != null ? inbound.getCreatedBy().toString() : "SYSTEM")
                    .timestamp(inbound.getCreatedAt())
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
    
    
    
    private long safeGetLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof String) {
            return Long.parseLong((String) value);
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }

    private int safeGetInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }
}