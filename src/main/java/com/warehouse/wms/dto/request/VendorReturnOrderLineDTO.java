package com.warehouse.wms.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorReturnOrderLineDTO {
    
    private Long id;
    private Long returnRequestLineId;
    
    @NotBlank(message = "Item code is required")
    private String itemCode;
    
    @NotBlank(message = "Item name is required")
    private String itemName;
    
    private String uom;
    
    @NotNull(message = "Order quantity is required")
    private Integer orderQuantity;
    
    private Integer pickedQuantity;
    private Integer qcQuantity;
    private Integer packedQuantity;
    private Integer dispatchedQuantity;
    
    private String batchNumber;
    private String serialNumbers;
    private LocalDate expiryDate;
    
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    
    private String pickLocation;
    private Integer pickSequence;
    private String packBarcode;
}

// DispatchDTO.java
