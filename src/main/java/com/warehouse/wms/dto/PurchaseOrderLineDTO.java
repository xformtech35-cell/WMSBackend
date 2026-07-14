package com.warehouse.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderLineDTO {
    
    private Long id;
    private String itemCode;
    private String itemName;
    private String description;
    private String hsnCode;
    private String uom;
    private Integer quantity;
    private Double gstRate;
    private Double sgstRate;
    private Double cgstRate;
    private Double igstRate;
    private Double unitPrice;
    private Double discountPercentage;
    private Double discountAmount;
    private Double totalPrice;
    private Double gstAmount;
    private Double totalWithGst;
    private Integer receivedQuantity;
    private Integer pendingQuantity;
    private String lineStatus;
    private Long itemId;
}