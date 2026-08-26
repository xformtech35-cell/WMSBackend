package com.warehouse.wms.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageRequests {
    
    // SO Number per package (can be same or different)
    private String soNumber;

    @NotBlank(message = "Package number is required")
    private String packageNumber;

    private String packageBarcode;
    
    // Customer fields per package
    private String customerCode;
    private String customerName;
    private String customerAddress;
    private String customerGst;
    private String customerPhone;
    private String invoiceNumber;
    private LocalDateTime orderDate;
    private LocalDateTime dispatchDate;
    private LocalDateTime expectedDeliveryDate;
    
    // Item fields
    @NotBlank(message = "Item code is required")
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
    private String remarks;
}
