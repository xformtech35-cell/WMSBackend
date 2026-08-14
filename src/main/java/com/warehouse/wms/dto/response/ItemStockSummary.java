// ====== FILE: src/main/java/com/warehouse/wms/dto/response/ItemStockSummary.java ======
package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemStockSummary {
    private String itemCode;
    private String itemName;
    private String uom;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private String batchNumber;
}