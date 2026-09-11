package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTotalsProjection {
    private Long totalQuantity;
    private Long totalInTransitQuantity;
    private Long totalReservedQuantity;
    private Long totalAvailableQuantity;
}