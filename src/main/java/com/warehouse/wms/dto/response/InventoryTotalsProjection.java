package com.warehouse.wms.dto.response;

public interface InventoryTotalsProjection {
    Long getTotalQuantity();
    Long getTotalInTransitQuantity();
    Long getTotalReservedQuantity();
    Long getTotalAvailableQuantity();
}