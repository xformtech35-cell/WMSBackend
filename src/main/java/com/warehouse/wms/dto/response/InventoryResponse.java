package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private Long id;
    private Long purchaseRequestItemId;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private Long binId;
    private String binCode;
    private Long goodsReceiptLineId;
    private String batchNo;
    private String serialNo;
    private String itemCode;
    private String itemName;
    private Integer quantity;
    private String state;
    private LocalDateTime manufactureDate;
    private LocalDateTime expiryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}