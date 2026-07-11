package com.warehouse.wms.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FulfillmentPendingSummary {
    private long pendingPickTasks;
    private long ordersCreatedOrReserved;
    private long ordersPackedNotShipped;
    private long inventoryReserved;
    private long inventoryPacked;
}
