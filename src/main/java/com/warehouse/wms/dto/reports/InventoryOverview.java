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
public class InventoryOverview {
    private Long totalSKUs;
    private Long totalQuantity;
    private Double totalValue;
    private Long lowStockItems;
    private Long outOfStockItems;
    private Long overStockItems;
    private Long reservedQuantity;
    private Long availableQuantity;
    private Long inTransitQuantity;
    private Double inventoryTurnoverRate;
    private Integer daysOfInventory;
    private List<StockStatusResponse> stockStatus;
    private List<TopStockItemResponse> topStockItems;
    private List<CategoryStockResponse> categoryStock;
}