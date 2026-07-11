package com.warehouse.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestItemDTO {
    private Long id;
    private String itemCode;
    private String itemName;
    private String itemBarcode;
    
    // ✅ ADD THIS NEW FIELD
    private String batchNo;
    
    private Integer quantity;
    private String unit;
    private Double unitPrice;
    private Double total;
    private String remarks;
    private Integer receivedQuantity;
    private Integer pendingQuantity;
    private String itemStatus;
    private List<ItemReceiptDTO> receipts;
}