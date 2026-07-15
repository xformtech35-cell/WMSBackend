package com.warehouse.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorQuotationItemDTO {
    
    private Long id;
    private String itemCode;
    private String itemName;
    private String description;
    private String uom;
    private Double quantity;
    private Double unitPrice;
    private Double gstRate;
    private Double cgstRate;
    private Double sgstRate;
    private Double igstRate;
    private Double totalAmount;
    private Double gstAmount;
    private Double totalWithGst;
    private Double discountPercentage;
    private Double discountAmount;
}