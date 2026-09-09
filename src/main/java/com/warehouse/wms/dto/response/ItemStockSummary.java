// ====== FILE: src/main/java/com/warehouse/wms/dto/response/ItemStockSummary.java ======
package com.warehouse.wms.dto.response;

import java.time.LocalDateTime;

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
    private Integer quantity ;
    private LocalDateTime expiryDate;
    private Double unitPrice;
    private Double totalValue;
    private LocalDateTime mfgDate;

}
