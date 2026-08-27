package com.warehouse.wms.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceMetrics {
    private Double inboundEfficiency;
    private Double outboundEfficiency;
    private Double pickingAccuracy;
    private Double packingAccuracy;
    private Double shippingAccuracy;
    private Double onTimeDeliveryRate;
    private Double orderFulfillmentRate;
    private Double inventoryAccuracy;
    private Double warehouseUtilization;
    private Double costPerOrder;
    private Double revenuePerOrder;
    private String topPerformer;
    private String bestPerformingZone;
}
