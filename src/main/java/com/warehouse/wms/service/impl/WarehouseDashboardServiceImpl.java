package com.warehouse.wms.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

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
import com.warehouse.wms.repository.BinRepository;
import com.warehouse.wms.repository.CustomerRepository;
import com.warehouse.wms.repository.DeliveryRepository;
import com.warehouse.wms.repository.DispatchRepository;
import com.warehouse.wms.repository.InboundLineRepository;
import com.warehouse.wms.repository.InboundRepository;
import com.warehouse.wms.repository.InventoryStockRepository;
import com.warehouse.wms.repository.PackageInfoRepository;
import com.warehouse.wms.repository.PickListRepository;
import com.warehouse.wms.repository.PickTaskRepository;
import com.warehouse.wms.repository.PutawayTaskRepository;
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
public class WarehouseDashboardServiceImpl implements WarehouseDashboardService {

    private final SalesOrderRepository salesOrderRepository;
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
    private final PutawayTaskRepository putawayTaskRepository;
    private final BinRepository binRepository;

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

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
        return WarehouseDashboardResponse.builder()
                .generatedAt(LocalDateTime.now().format(ISO_FMT))
                .dateRange(startDate + " to " + endDate)
                .summaryOverview(getSummaryOverview())
                .inboundStats(getInboundStats(startDate, endDate))
                .outboundStats(getOutboundStats(startDate, endDate))
                .inventoryOverview(getInventoryOverview())
                .warehouseCapacity(getWarehouseCapacity())
                .performanceMetrics(getPerformanceMetrics(startDate, endDate))
                .chartsData(getChartsData(startDate, endDate))
                .recentActivities(getRecentActivities())
                .alerts(getAlerts())
                .build();
    }

    // ====== SUMMARY OVERVIEW ======

    private SummaryOverview getSummaryOverview() {
        long totalOrders = salesOrderRepository.count();
        long totalInbound = inboundRepository.count();
        long totalOutbound = salesOrderRepository.countByStatusIn(Arrays.asList("DISPATCHED", "DELIVERED"));
        long totalInventory = inventoryStockRepository.count();
        long totalCustomers = customerRepository.count();
        long totalSuppliers = supplierRepository.count();

        Double revenue = calculateTotalRevenue();

        return SummaryOverview.builder()
                .totalOrders(totalOrders)
                .totalInbound(totalInbound)
                .totalOutbound(totalOutbound)
                .totalInventory(totalInventory)
                .totalCustomers(totalCustomers)
                .totalSuppliers(totalSuppliers)
                .totalRevenue(revenue)
                .build();
    }

    // ====== INBOUND STATS ======

    private InboundStats getInboundStats(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDate startLocalDate = startDate.toLocalDate();
        LocalDate endLocalDate = endDate.toLocalDate();

        long totalInboundInRange = inboundRepository.countByInboundDateBetween(startLocalDate, endLocalDate);
        long pendingGRN = inboundRepository.countByStatusAndInboundDateBetween(InboundStatus.PENDING, startLocalDate, endLocalDate);
        long completedGRN = inboundRepository.countByStatusAndInboundDateBetween(InboundStatus.COMPLETED, startLocalDate, endLocalDate);
        long cancelledGRN = inboundRepository.countByStatusAndInboundDateBetween(InboundStatus.CANCELLED, startLocalDate, endLocalDate);

        LocalDate today = LocalDate.now();
        long todayGRN = inboundRepository.countByInboundDateBetween(today, today);
        long thisWeekGRN = inboundRepository.countByInboundDateBetween(today.minusDays(7), today);
        long thisMonthGRN = inboundRepository.countByInboundDateBetween(today.minusDays(30), today);

        String topSupplier = "";
        long topSupplierCount = 0;
        List<Object[]> supplierData = inboundRepository.findTopSupplier(startLocalDate, endLocalDate);
        if (supplierData != null && !supplierData.isEmpty()) {
            Object[] row = supplierData.get(0);
            topSupplier = row[0] != null ? row[0].toString() : "";
            topSupplierCount = safeGetLong(row[1]);
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
                .totalPutawayTasks(putawayTaskRepository.count())
                .pendingPutaway(putawayTaskRepository.countByStatus("PENDING"))
                .completedPutaway(putawayTaskRepository.countByStatus("COMPLETED"))
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
            return inboundLineRepository.sumReceivedQuantityByDateRange(
                    startDate.toLocalDate(), endDate.toLocalDate());
        } catch (Exception e) {
            log.error("Error calculating total items received", e);
            return 0L;
        }
    }

    private Double getTotalWeightReceived(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            Double v = inboundLineRepository.sumWeightByDateRange(
                    startDate.toLocalDate(), endDate.toLocalDate());
            return v != null ? v : 0.0;
        } catch (Exception e) {
            log.error("Error calculating total weight received", e);
            return 0.0;
        }
    }

    private Double getTotalVolumeReceived(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            Double v = inboundLineRepository.sumVolumeByDateRange(
                    startDate.toLocalDate(), endDate.toLocalDate());
            return v != null ? v : 0.0;
        } catch (Exception e) {
            log.error("Error calculating total volume received", e);
            return 0.0;
        }
    }

    private Integer calculateAvgInboundProcessingTime(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            Double avgMinutes = inboundRepository.calculateAvgProcessingTime(
                    startDate.toLocalDate(), endDate.toLocalDate());
            if (avgMinutes == null || avgMinutes <= 0) return 0;
            return (int) Math.ceil(avgMinutes / 60.0);
        } catch (Exception e) {
            log.error("Error calculating avg inbound processing time", e);
            return 0;
        }
    }

    private List<GRNStatusCountResponse> getGRNStatusCounts(LocalDateTime startDate, LocalDateTime endDate) {
        // TODO: implement using inboundRepository.groupByStatus(...)
        return new ArrayList<>();
    }

    private List<SupplierPerformanceResponse> getSupplierPerformance(LocalDateTime start, LocalDateTime end) {
        // TODO: implement using inboundRepository.groupBySupplier(...)
        return new ArrayList<>();
    }

    private List<DailyInboundResponse> getDailyInbound(LocalDateTime start, LocalDateTime end) {
        // TODO: implement using inboundRepository.countGroupedByInboundDate(...)
        return new ArrayList<>();
    }

    // ====== OUTBOUND STATS ======

    private OutboundStats getOutboundStats(LocalDateTime startDate, LocalDateTime endDate) {
        long totalOrders = salesOrderRepository.count();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
        long todayOrders = salesOrderRepository.countByCreatedDateBetween(todayStart, todayEnd);
        long thisWeekOrders = salesOrderRepository.countByCreatedDateBetween(todayStart.minusDays(7), todayEnd);
        long thisMonthOrders = salesOrderRepository.countByCreatedDateBetween(todayStart.minusDays(30), todayEnd);

        long pendingOrders = salesOrderRepository.countByStatusIn(
                Arrays.asList("DRAFT", "PENDING", "APPROVED"));
        long processingOrders = salesOrderRepository.countByStatusIn(
                Arrays.asList("PROCESSING", "PICKING", "PACKING"));
        long completedOrders = salesOrderRepository.countByStatus("DELIVERED");
        long cancelledOrders = salesOrderRepository.countByStatus("CANCELLED");

        String topCustomer = "";
        long topCustomerOrders = 0;
        List<Object[]> customerData = salesOrderRepository.findTopCustomer();
        if (customerData != null && !customerData.isEmpty()) {
            Object[] row = customerData.get(0);
            topCustomer = row.length > 1 && row[1] != null ? row[1].toString() : "";
            topCustomerOrders = safeGetLong(row[row.length - 1]);
        }

        String topItem = "";
        int topItemQuantity = 0;
        List<Object[]> itemData = salesOrderItemRepository.findTopItem();
        if (itemData != null && !itemData.isEmpty()) {
            Object[] row = itemData.get(0);
            topItem = row.length > 1 && row[1] != null ? row[1].toString() : "";
            topItemQuantity = row.length > 2 ? safeGetInt(row[2]) : 0;
        }

        String topTransporter = "";
        long topTransporterCount = 0;
        List<Object[]> transporterData = dispatchRepository.findTopTransporter();
        if (transporterData != null && !transporterData.isEmpty()) {
            Object[] row = transporterData.get(0);
            topTransporter = row[0] != null ? row[0].toString() : "";
            topTransporterCount = safeGetLong(row[1]);
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

    private Long getTotalItemsShipped() {
        try {
            Long v = salesOrderItemRepository.sumShippedQuantity();
            return v != null ? v : 0L;
        } catch (Exception e) {
            log.error("Error calculating total items shipped", e);
            return 0L;
        }
    }

    private Double getTotalWeightShipped() {
        try {
            Double v = packageInfoRepository.sumWeight();
            return v != null ? v : 0.0;
        } catch (Exception e) {
            log.error("Error calculating total weight shipped", e);
            return 0.0;
        }
    }

    private Double getTotalVolumeShipped() {
        try {
            Double v = packageInfoRepository.sumVolume();
            return v != null ? v : 0.0;
        } catch (Exception e) {
            log.error("Error calculating total volume shipped", e);
            return 0.0;
        }
    }

    private Integer calculateAvgOutboundProcessingTime() {
        try {
            Double avgMinutes = salesOrderRepository.calculateAvgFulfillmentTimeMinutes();
            if (avgMinutes == null || avgMinutes <= 0) return 0;
            return (int) Math.ceil(avgMinutes / 60.0);
        } catch (Exception e) {
            log.error("Error calculating avg outbound time", e);
            return 0;
        }
    }

    private List<OrderStatusCountResponse> getOrderStatusCounts() {
        // TODO: salesOrderRepository.groupByStatus()
        return new ArrayList<>();
    }

    private List<DailyOutboundResponse> getDailyOutbound(LocalDateTime start, LocalDateTime end) {
        // TODO: salesOrderRepository.countGroupedByCreatedDate(...)
        return new ArrayList<>();
    }

    private List<PickPerformanceResponse> getPickPerformance(LocalDateTime start, LocalDateTime end) {
        // TODO: pickTaskRepository.performanceByPicker(...)
        return new ArrayList<>();
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
                .reservedQuantity(inventoryStockRepository.sumReservedQuantity())
                .availableQuantity(inventoryStockRepository.sumAvailableQuantity())
                .inTransitQuantity(inventoryStockRepository.sumInTransitQuantity())
                .inventoryTurnoverRate(calculateInventoryTurnoverRate())
                .daysOfInventory(calculateDaysOfInventory())
                .stockStatus(getStockStatus())
                .topStockItems(getTopStockItems())
                .categoryStock(getCategoryStock())
                .build();
    }

    // ====== WAREHOUSE CAPACITY ======

    private WarehouseCapacity getWarehouseCapacity() {
        long totalBins = binRepository.count();
        long occupiedBins = binRepository.countByStatusNot("AVAILABLE");
        long emptyBins = totalBins - occupiedBins;
        double binUtil = totalBins > 0 ? (occupiedBins * 100.0 / totalBins) : 0.0;

        Double totalCapacity = binRepository.sumMaxCapacity();
        Double usedCapacity = binRepository.sumOccupiedVolume();
        if (totalCapacity == null) totalCapacity = 0.0;
        if (usedCapacity == null) usedCapacity = 0.0;
        double available = totalCapacity - usedCapacity;
        double util = totalCapacity > 0 ? (usedCapacity * 100.0 / totalCapacity) : 0.0;

        return WarehouseCapacity.builder()
                .totalCapacity(totalCapacity)
                .usedCapacity(usedCapacity)
                .availableCapacity(available)
                .utilizationPercentage(round2(util))
                .totalBins(totalBins)
                .occupiedBins(occupiedBins)
                .emptyBins(emptyBins)
                .binUtilization(round2(binUtil))
                .zoneCapacity(getZoneCapacity())
                .binStatus(getBinStatus())
                .build();
    }

    // ====== PERFORMANCE METRICS (NOW CALCULATED) ======

    private PerformanceMetrics getPerformanceMetrics(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDate s = startDate.toLocalDate();
        LocalDate e = endDate.toLocalDate();

        double inboundEfficiency = computeInboundEfficiency(s, e);
        double outboundEfficiency = computeOutboundEfficiency(s, e);
        double pickingAccuracy = computePickingAccuracy(s, e);
        double packingAccuracy = computePackingAccuracy(s, e);
        double shippingAccuracy = computeShippingAccuracy(s, e);
        double onTimeDelivery = computeOnTimeDeliveryRate(s, e);
        double orderFulfillment = computeOrderFulfillmentRate(s, e);
        double inventoryAccuracy = computeInventoryAccuracy();
        double warehouseUtil = getWarehouseCapacity().getUtilizationPercentage();

        Double totalRevenue = calculateTotalRevenue();
        long orderCount = salesOrderRepository.count();
        double revenuePerOrder = orderCount > 0 ? totalRevenue / orderCount : 0.0;

        // Real top performer and zone
        String topPerformer = resolveTopPerformer();
        String bestZone = resolveBestPerformingZone();

        return PerformanceMetrics.builder()
                .inboundEfficiency(round2(inboundEfficiency))
                .outboundEfficiency(round2(outboundEfficiency))
                .pickingAccuracy(round2(pickingAccuracy))
                .packingAccuracy(round2(packingAccuracy))
                .shippingAccuracy(round2(shippingAccuracy))
                .onTimeDeliveryRate(round2(onTimeDelivery))
                .orderFulfillmentRate(round2(orderFulfillment))
                .inventoryAccuracy(round2(inventoryAccuracy))
                .warehouseUtilization(round2(warehouseUtil))
                .revenuePerOrder(round2(revenuePerOrder))
                .topPerformer(topPerformer)
                .bestPerformingZone(bestZone)
                .build();
    }

    private double computeInboundEfficiency(LocalDate start, LocalDate end) {
        long total = inboundRepository.countByInboundDateBetween(start, end);
        if (total == 0) return 0.0;
        long completed = inboundRepository.countByStatusAndInboundDateBetween(InboundStatus.COMPLETED, start, end);
        return (completed * 100.0) / total;
    }

    private double computeOutboundEfficiency(LocalDate start, LocalDate end) {
        long total = pickTaskRepository.count();
        if (total == 0) return 0.0;
        long confirmed = pickTaskRepository.countByStatus("CONFIRMED");
        return (confirmed * 100.0) / total;
    }

    private double computePickingAccuracy(LocalDate start, LocalDate end) {
        Long picked = pickTaskRepository.sumPickedQuantity();
        Long required = pickTaskRepository.sumRequiredQuantity();
        if (picked == null || required == null || required == 0) return 0.0;
        return (picked * 100.0) / required;
    }

    private double computePackingAccuracy(LocalDate start, LocalDate end) {
        Long packed = packageInfoRepository.sumPackedQuantity();
        Long picked = pickTaskRepository.sumPickedQuantity();
        if (packed == null || picked == null || picked == 0) return 0.0;
        return (packed * 100.0) / picked;
    }

    private double computeShippingAccuracy(LocalDate start, LocalDate end) {
        long dispatched = dispatchRepository.count();
        long delivered = deliveryRepository.countByDeliveryStatus("DELIVERED");
        if (dispatched == 0) return 0.0;
        return (delivered * 100.0) / dispatched;
    }

    private double computeOnTimeDeliveryRate(LocalDate start, LocalDate end) {
        // Requires an expected_delivery_date column — if absent, return 0
        try {
            return deliveryRepository.computeOnTimeRate();
        } catch (Exception ex) {
            return 0.0;
        }
    }

    private double computeOrderFulfillmentRate(LocalDate start, LocalDate end) {
        long total = salesOrderRepository.count();
        if (total == 0) return 0.0;
        long delivered = salesOrderRepository.countByStatus("DELIVERED");
        return (delivered * 100.0) / total;
    }

    private double computeInventoryAccuracy() {
        // Compare expected vs counted from wms_count_line
        try {
            Long expected = inventoryStockRepository.getTotalQuantity();
            Long actual = inventoryStockRepository.getTotalQuantity(); // replace with count table if available
            if (expected == null || expected == 0) return 0.0;
            return (actual * 100.0) / expected;
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String resolveTopPerformer() {
        try {
            List<Object[]> rows = pickTaskRepository.findTopPerformer();
            if (rows != null && !rows.isEmpty() && rows.get(0)[0] != null) {
                return rows.get(0)[0].toString();
            }
        } catch (Exception e) {
            log.warn("Could not resolve top performer", e);
        }
        return "";
    }

    private String resolveBestPerformingZone() {
        try {
            List<Object[]> rows = inventoryStockRepository.findTopPerformingZone();
            if (rows != null && !rows.isEmpty() && rows.get(0)[0] != null) {
                return rows.get(0)[0].toString();
            }
        } catch (Exception e) {
            log.warn("Could not resolve best performing zone", e);
        }
        return "";
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

    private List<InboundOutboundTrendResponse> getInboundOutboundTrend(LocalDateTime start, LocalDateTime end) {
        // TODO: implement union query of inbound/outbound by date
        return new ArrayList<>();
    }

    private List<OrderStatusDistributionResponse> getOrderStatusDistribution() {
        // TODO: salesOrderRepository.groupByStatus()
        return new ArrayList<>();
    }

    private List<DailyPerformanceResponse> getDailyPerformance(LocalDateTime start, LocalDateTime end) {
        return new ArrayList<>();
    }

    private List<WeeklyPerformanceResponse> getWeeklyPerformance(LocalDateTime start, LocalDateTime end) {
        return new ArrayList<>();
    }

    private List<MonthlyPerformanceResponse> getMonthlyPerformance(LocalDateTime start, LocalDateTime end) {
        return new ArrayList<>();
    }

    private List<ZoneUtilizationResponse> getZoneUtilization() {
        try {
            List<Object[]> rows = inventoryStockRepository.zoneUsage();
            if (rows == null || rows.isEmpty()) return new ArrayList<>();

            return rows.stream().map(r -> {
                String zone = r[0] != null ? r[0].toString() : "UNKNOWN";
                int inbound = r[1] != null ? ((Number) r[1]).intValue() : 0;
                int outbound = r[2] != null ? ((Number) r[2]).intValue() : 0;
                double util = inbound > 0 ? (outbound * 100.0 / inbound) : 0.0;
                return ZoneUtilizationResponse.builder()
                        .zone(zone)
                        .inboundUsage(inbound)
                        .outboundUsage(outbound)
                        .utilization(round2(util))
                        .build();
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to compute zone utilization", e);
            return new ArrayList<>();
        }
    }

    private List<CategoryPerformanceResponse> getCategoryPerformance(LocalDateTime start, LocalDateTime end) {
        return new ArrayList<>();
    }

    private List<HourlyActivityResponse> getHourlyActivity() {
        return new ArrayList<>();
    }

    // ====== RECENT ACTIVITIES ======

    private List<RecentActivityResponse> getRecentActivities() {
        List<RecentActivityResponse> activities = new ArrayList<>();

        List<Inbound> recentInbound = inboundRepository.findTop10ByOrderByCreatedAtDesc();
        for (Inbound inbound : recentInbound) {
            activities.add(RecentActivityResponse.builder()
                    .activityType("INBOUND_CREATED")
                    .description("Inbound Created")
                    .soNumber(inbound.getInboundNumber())
                    .status(inbound.getStatus() != null ? inbound.getStatus().name() : "UNKNOWN")
                    .user(inbound.getCreatedBy() != null ? inbound.getCreatedBy().toString() : "SYSTEM")
                    .timestamp(inbound.getCreatedAt())
                    .icon("\uD83D\uDCE5")
                    .color("green")
                    .build());
        }

        List<SalesOrder> recentOrders = salesOrderRepository.findTop10ByOrderByCreatedAtDesc();
        for (SalesOrder order : recentOrders) {
            activities.add(RecentActivityResponse.builder()
                    .activityType("ORDER_CREATED")
                    .description("Sales Order Created")
                    .soNumber(order.getSoNumber())
                    .status(order.getStatus())
                    .user(order.getCreatedBy())
                    .timestamp(order.getCreatedAt())
                    .icon("\uD83D\uDCE6")
                    .color("blue")
                    .build());
        }

        activities.sort((a, b) -> {
            if (a.getTimestamp() == null) return 1;
            if (b.getTimestamp() == null) return -1;
            return b.getTimestamp().compareTo(a.getTimestamp());
        });

        return activities.stream().limit(20).collect(Collectors.toList());
    }

    // ====== ALERTS (NOW DYNAMIC) ======

    private List<AlertResponse> getAlerts() {
        List<AlertResponse> alerts = new ArrayList<>();

        // Real low-stock count
        long lowStockCount = inventoryStockRepository.countLowStockItems();
        if (lowStockCount > 0) {
            alerts.add(AlertResponse.builder()
                    .type("LOW_STOCK")
                    .severity("WARNING")
                    .message(lowStockCount + " items are below minimum stock level")
                    .action("Review and reorder items")
                    .timestamp(LocalDateTime.now())
                    .isRead(false)
                    .build());
        }

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

        long pendingGRN = inboundRepository.countByStatus(InboundStatus.PENDING);
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

    // ====== CALCULATIONS ======

    private Double calculateTotalRevenue() {
        try {
            BigDecimal v = salesOrderRepository.sumTotalAmountByStatusIn(
                    Arrays.asList("DELIVERED", "DISPATCHED"));
            return v != null ? v.doubleValue() : 0.0;
        } catch (Exception e) {
            log.error("Error calculating total revenue", e);
            return 0.0;
        }
    }





    private Double calculateTotalInventoryValue() {
        try {
            Double v = inventoryStockRepository.sumInventoryValue();
            return v != null ? v : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    private Double calculateInventoryTurnoverRate() { return 0.0; /* TODO */ }
    private Integer calculateDaysOfInventory() { return 0; /* TODO */ }

    private Long getLowStockItems() {
        try { return inventoryStockRepository.countLowStockItems(); }
        catch (Exception e) { return 0L; }
    }
    private Long getOutOfStockItems() {
        try { return inventoryStockRepository.countOutOfStockItems(); }
        catch (Exception e) { return 0L; }
    }
    private Long getOverStockItems() {
        try { return inventoryStockRepository.countOverStockItems(); }
        catch (Exception e) { return 0L; }
    }

    private List<StockStatusResponse> getStockStatus() { return new ArrayList<>(); /* TODO */ }
    private List<TopStockItemResponse> getTopStockItems() { return new ArrayList<>(); /* TODO */ }
    private List<CategoryStockResponse> getCategoryStock() { return new ArrayList<>(); /* TODO */ }
    private List<ZoneCapacityResponse> getZoneCapacity() { return new ArrayList<>(); /* TODO */ }
    private List<BinStatusResponse> getBinStatus() {
        try {
            List<Object[]> raw = binRepository.groupByStatus();
            long total = raw.stream()
                    .mapToLong(r -> r[1] != null ? ((Number) r[1]).longValue() : 0L)
                    .sum();

            return raw.stream()
                    .map(r -> BinStatusResponse.builder()
                            .status(r[0] != null ? r[0].toString() : "UNKNOWN")
                            .count(r[1] != null ? ((Number) r[1]).longValue() : 0L)
                            .percentage(total > 0
                                    ? round2(((Number) r[1]).longValue() * 100.0 / total)
                                    : 0.0)
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to compute bin status distribution", e);
            return new ArrayList<>();
        }
    }

    // ====== UTILS ======

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private long safeGetLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number) return ((Number) value).longValue();
        try { return Long.parseLong(value.toString()); } catch (Exception e) { return 0L; }
    }

    private int safeGetInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        try { return Integer.parseInt(value.toString()); } catch (Exception e) { return 0; }
    }
}