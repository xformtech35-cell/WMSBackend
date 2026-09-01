package com.warehouse.wms.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorReturnStatisticsDTO {
    // Request statistics
    private Long totalRequests;
    private Long pendingRequests;
    private Long approvedRequests;
    private Long rejectedRequests;
    private Long cancelledRequests;
    
    // Order statistics
    private Long totalOrders;
    private Long pendingOrders;
    private Long inProgressOrders;
    private Long completedOrders;
    private Long cancelledOrders;
    
    // Status breakdowns
    private Map<String, Long> requestStatusBreakdown;
    private Map<String, Long> orderStatusBreakdown;
    private Map<String, Long> priorityBreakdown;
    private Map<String, Long> returnTypeBreakdown;
    
    // Financial statistics
    private BigDecimal totalReturnAmount;
    private BigDecimal totalSettlementAmount;
    private BigDecimal totalCreditNoteAmount;
    private BigDecimal totalRefundAmount;
    
    // Quantity statistics
    private Long totalItemsReturned;
    private Long totalItemsAccepted;
    private Long totalItemsRejected;
    private Long totalItemsDamaged;
    private Long totalItemsShort;
    
    // Time-based statistics
    private BigDecimal averageProcessingTime; // in days
    private BigDecimal averageApprovalTime;
    private BigDecimal averageDispatchTime;
    private BigDecimal averageSettlementTime;
    
    // Top suppliers
    private List<TopSupplierDTO> topSuppliers;
    
    // Recent trends
    private List<MonthlyTrendDTO> monthlyTrends;
}