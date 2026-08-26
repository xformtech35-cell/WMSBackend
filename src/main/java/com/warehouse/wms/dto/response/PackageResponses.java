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
public class PackageResponses {
    private Long id;
    private String soNumber;
    private String packageNumber;
    private String packageBarcode;
    private String customerCode;
    private String customerName;
    private String customerAddress;
    private String customerGst;
    private String customerPhone;
    private String invoiceNumber;
    private LocalDateTime orderDate;
    private LocalDateTime dispatchDate;
    private LocalDateTime expectedDeliveryDate;
    private String itemCode;
    private String itemName;
    private String uom;
    private Integer orderedQuantity;
    private Integer dispatchedQuantity;
    private Integer deliveredQuantity;
    private Integer shortQuantity;
    private String batchNumber;
    private String serialNumbers;
    private Double unitPrice;
    private Double totalPrice;
    private Double weight;
    private Double volume;
    private String status;
    private String remarks;
}
