package com.warehouse.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceivingItemDTO {
    private Long lineId;
    private String itemCode;
    private String itemName;
    private String uom;
    private Integer requiredQuantity;
    private Integer receivedQuantity;
    private Integer pendingQuantity;
    private Integer totalQuantity;
    private String remarks;
}