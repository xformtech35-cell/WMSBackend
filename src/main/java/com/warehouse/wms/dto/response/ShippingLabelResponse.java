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
public class ShippingLabelResponse {
    private String labelNumber;
    private String packageNumber;
    private String packageBarcode;
    private String soNumber;
    private String customerCode;
    private String customerName;
    private String customerAddress;
    private String itemCode;
    private String itemName;
    private Integer quantity;
    private Double weight;
    private String shippingMethod;
    private String trackingNumber;
    private String labelStatus;
    private String printedBy;
    private LocalDateTime printedDate;
    private String labelUrl;
    private String remarks;
    private LocalDateTime createdAt;
    
    
    private String labelImage;
    
    private String qrImage;
}